//git hub URL : https://github.com/LimSujin0/SimpleChat
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
	//swear flag(int type) is used to represent whether a sentence is including swear word.
	//if the sentence is including swear word, the flag will be changed to 1
	private String swear[] = {"바보", "바보2", "바보3", "바보4", "바보5"};
	//string array swear is including swear word prohibited.
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
	
	public void run(){
		try{
			String line = null;
			while((line = br.readLine()) != null){
				donot_swear(line);//check whether there is swear word in the sentence.
				if(swear_flag==0) {//if there is not swear word in the sentence. send message
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
			//get key instea of value of hashmap for comparison of user id
			//change printwriter type to string type. for comparison of user id
			Collection<String> collection = hm.keySet();
			Iterator<String> iter = collection.iterator();
			while(iter.hasNext()){
				String userid = (String)iter.next();
				if(userid==id) ;//if id of user sending message is same as key(id) from hash map, do not anything
				else {//else send message
					PrintWriter pw = (PrintWriter) hm.get(userid);
					pw.println(msg);
					pw.flush();
				}
			}
		}
	} // broadcast
	

	//send_userlist is send user list and total number connecting this chat server
	//by using iteration class, bring all key of this hashmap and count user number
	//print user list and total user number
	public void send_userlist() {
		int total = 0;
		PrintWriter pw = (PrintWriter)hm.get(id);
		String header = "\n=====show user list======"; //send header
		pw.println(header);
		synchronized(hm){//to prohibit multi-threads' simultaneous access to hm
			Collection<String> collection = hm.keySet();//from key of hashmap to collection
			Iterator<String> iter = collection.iterator();//from collectoin to iteration
			while(iter.hasNext()) {//by using iteration class. bring all key of hashmap
				total++; //add total number of user
				String userlist = " user : " + (String)iter.next();
				pw.println(userlist); //print user list
				pw.flush();
			}
			pw.println(" total user number : " + total );//print total user number
			pw.println();
			pw.flush();
		}
	}//send user list
	
	//do not swear is to set swear_flag
	//if user is enter a sentence including swear word, 
	//set sear_flag and send that caution message to the user
	//if not, flag is clear
	//this flag will be use in run function. if flag set, cannot send message.
	public void donot_swear(String msg){
		swear_flag=0;
		for(String s: swear) {//bring a swear word in swear array by using for loop
			if(msg.indexOf(s)>-1){//check whether the sentence is including swear word
				swear_flag=1;//if there is swear word, set swear_flag
				break;
			}
		}
		if(swear_flag==1) {//if there is a swear word
			PrintWriter pw = (PrintWriter)hm.get(id);
			pw.println("do not wear. your message is not sent"); //send user caution message
			pw.flush();
		}
	}//do not swear
	
}
