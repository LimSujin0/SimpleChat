import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer {

	public static void main(String[] args) {
		try{
			ServerSocket server = new ServerSocket(10001);
			System.out.println("Waiting connection...");
			HashMap hm = new HashMap();
			while(true){
				Socket sock = server.accept();
				ChatThread chatthread = new ChatThread(sock, hm);
				chatthread.start();
			} // while
		}catch(Exception e){
			System.out.println(e);
		}
	} // main
}

class ChatThread extends Thread{
	private int swear_flag = 0;
	private String swear[] = {"바보", "바보2", "바보3", "바보4", "바보5"}; 
	private Socket sock;
	private String id;
	private BufferedReader br;
	private HashMap hm;
	private boolean initFlag = false;
	public ChatThread(Socket sock, HashMap hm){
		this.sock = sock;
		this.hm = hm;
		try{
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			id = br.readLine();
			broadcast(id + " entered.");
			System.out.println("[Server] User (" + id + ") entered.");
			synchronized(hm){
				hm.put(this.id, pw);
			}
			initFlag = true;
		}catch(Exception ex){
			System.out.println(ex);
		}
	} // constructor
	
	public void donot_swear(String msg){
		swear_flag=0;
		for(String s: swear) {
			if(msg.indexOf(s)>-1){
				swear_flag=1;
				break;
			}
		}
		if(swear_flag==1) {
			PrintWriter pw = (PrintWriter)hm.get(id);
			pw.println("do not wear. your message is not sent");
			pw.flush();
		}
	}//do not swear
	
	public void send_userlist() {
		int total = 0;
		PrintWriter pw = (PrintWriter)hm.get(id);
		String header = "\n=====show user list======";
		pw.println(header);
		synchronized(hm){
			Collection<String> collection = hm.keySet();
			Iterator<String> iter = collection.iterator();
			while(iter.hasNext()) {
				total++;
				String userlist = " user : " + (String)iter.next();
				pw.println(userlist);
				pw.flush();
			}
			pw.println("total user number : " + total );
			pw.println();
			pw.flush();
		}
	}//send user list
	
	public void run(){
		try{
			String line = null;
			while((line = br.readLine()) != null){
				donot_swear(line);
				if(swear_flag==0) {
					if(line.equals("/quit"))
						break;
					if(line.indexOf("/to ") == 0)
						sendmsg(line);
					if(line.equals("/userlist"))
						send_userlist();
					else
						broadcast(id + " : " + line);
				}
			}
		}catch(Exception ex){
			System.out.println(ex);
		}finally{
			synchronized(hm){
				hm.remove(id);
			}
			broadcast(id + " exited.");
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		}
	} // run
	public void sendmsg(String msg){
		int start = msg.indexOf(" ") +1;
		int end = msg.indexOf(" ", start);
		if(end != -1){
			String to = msg.substring(start, end);
			String msg2 = msg.substring(end+1);
			Object obj = hm.get(to);
			if(obj != null){
				PrintWriter pw = (PrintWriter)obj;
				pw.println(id + " whisphered. : " + msg2);
				pw.flush();
			} // if
		}
	} // sendmsg
	public void broadcast(String msg){
		synchronized(hm){
			Collection<String> collection = hm.keySet();
			Iterator<String> iter = collection.iterator();
			while(iter.hasNext()){
				String userid = (String)iter.next();
				if(userid==id) ;
				else {
					PrintWriter pw = (PrintWriter) hm.get(userid);
					pw.println(msg);
					pw.flush();
				}
			}
		}
	} // broadcast
}
