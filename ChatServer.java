//https://github.com/1nhee/SimpleChat.git

import java.net.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.Date;
  
public class ChatServer {

	public static void main(String[] args) {
		try{
			//client�� ��û�� �ޱ� ���� ��Ʈ ��ȣ�� �Բ� ServerSocket �ν��Ͻ��� �����Ͽ� ������ �����Ѵ�.
			ServerSocket server = new ServerSocket(10001);
			//ServerSocket�� ���� server�� �������־���.
			Calendar calendar = Calendar.getInstance();
			Date date = calendar.getTime();
			String today = (new SimpleDateFormat("H:mm:ss").format(date));
			String time = "["+ today + "] ";
			
			System.out.println(time + "Waiting connection...");
			//�������� ������ ���� HashMap�ν��Ͻ��� �����Ѵ�.
			//***HashMap�� while ��, Socket�� While�ȿ��� ����� ����?
			//������ ������ �ϴ� ������ �� client�� ���� ������ ���� �־�� �Ѵ�.(��ġ ���� ���� �ִ� ��ó�� client���� server�� �������ش�.) ������, HashMap�� ���� �����͸� �����ϴ� ������ �������� ���� �� �ֱ� ������ �ۿ��� �ѹ��� �����Ǿ �ȴ�. 
            HashMap<String, PrintWriter> hm = new HashMap<String, PrintWriter>();
	
               while(true){
			//while���� ���� ���� ����Ͽ� client�� ��û�� �޾Ƶ��δ�. (.accept)
			Socket sock = server.accept();
			//sock�ν��Ͻ��� ���� ���� client�� ��û�� chatthread�� �����Ͱ� ����� HashMap�� Chatthread�� �ѱ��.(�ּ� ������ �ѱ�)
			ChatThread chatthread = new ChatThread(sock, hm);
			chatthread.start();//run�� �����Ų��.
		} // end of while
		//������ ���� ���� ��Ȳ ��, e�� ����Ʈ �ǰ� �Ѵ�.
		}catch(Exception e){
			System.out.println(e);
		}
	} // end of main
}//end of ChatServer


class ChatThread extends Thread{// thread�� �����ͼ� start�� ����
	private Socket sock;
	private String id;
	private String id_myself;
	private BufferedReader br;
	private HashMap<String, PrintWriter> hm;
	private boolean initFlag = false;
	ArrayList<String> badWords = new ArrayList<String>();
	
	public ChatThread(Socket sock, HashMap<String, PrintWriter> hm){
		//reference ��, �ּ� ���� copy�ϴ� ������� �ν��Ͻ� ����
		this.sock = sock;
		this.hm = hm;

		try{
			//client���� �ƿ�ǲ �� ��������
			//PrintWriter Ŭ������ �־��� �����͸� ���� ������� �ٲپ� �ִ� ���̴�. �׷��Ƿ�, getOutputStream���κ��� return�� byte ������ �����͸� OutputStream���� ��������.
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			//client���׼� �Է� �� �ޱ�
			//getInputStream�� return ���� byte�̴�. �׷��Ƿ�, �̸� ���� �Է� ��Ʈ����InputstreamReader�� �־��־�byte�� ��ǲ ������ ��ȯ�����ش�.(���۸����δ� int�� �ٲ��شٰ� �Ѵ�.) �׸���, buffered reader�� ���μ� �ϳ��� �о�� ���� �ƴ� ���� �����Ͱ� ���̸� �о�� ȿ�������� �����͸� �о���δ�. 
			//Id, ��ȭ ���� �� ������ ����ڷ��� �Է� ���� �о� ���� �� ����Ѵ�.
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			//br�� �о�� �����͸� id�� �־��ش�. (������� id�� �о���� ����)
			id = br.readLine();
			//���� id�� �� �� �ְ� �����صд�.
			id_myself = id;
			Calendar calendar = Calendar.getInstance();
			Date date = calendar.getTime();
			String today = (new SimpleDateFormat("H:mm:ss").format(date));
			String time = "["+ today + "] ";
			broadcast(id + " entered.");
			
			System.out.println(time + "[Server] User (" + id + ") entered.");
			
			//add bad words to arrayList
			badWords.add("fuck");
			badWords.add("����");
			badWords.add("����");
			badWords.add("����");
			badWords.add("������");
			
			//synchronized�� �� �̻��� �����尡 ������ �ڿ��� �����ϴ� ���, ���� ���� �����尡 �ϳ��� �ڿ��� �����Ϸ��� �� �� �־��� �������� ���� �ϳ��� �����常�� ���� �����ϵ��� �Ѵ�.
			synchronized(hm){
				//HashMap�� ���� ��, (Key, Value) ���·� �����Ѵ�. 
				hm.put(this.id, pw);
			}
			initFlag = true;
			//���� �߻� �� ����
			}catch(Exception ex){
				System.out.println(ex);
			}
		} // end of constructor

		public void run(){
			
			//to check that user's input line has bad words or not
			boolean toCheck;
			
			try{
				String line = null;
				//����ڷκ��� ����Ͽ� ������ �о�´�.
				while((line = br.readLine()) != null){
					
					//check user's input line
					toCheck = toCheckLine(line);
					
					//if user's input line doesn't have bad words, print it out
					if (toCheck == false) {
						//���� ����ڰ� /quit�� �Է��ϸ� ����ڷκ��� �о���� ���� �����.
						if(line.equals("/quit"))
							break;
						//���� ����ڰ� /to�� �Է��ϸ� �ӼӸ� �޼ҵ尡 ����Ǿ� ���������� �޼����� ���� �� �ִ�.
						if(line.indexOf("/to ") == 0){ 
							sendmsg(line);
						}else if(line.indexOf("/userlist") == 0) {
							send_userlist();
						}else if(line.indexOf("/spamlist") == 0) {
							spam_list();
						}else if(line.indexOf("/addspam") == 0) {
							
							add_spam(line);
						}else{
						//���� �ΰ��� ��� �̿ܿ� ��� ��ȭ ������ ������ ���� �������� broadcast�޼ҵ�� ��������.
						//Broadcast �޼ҵ�� ��� ������ ������ ��ȭ ������ �ԷµǴ� ���̴�.(��ü ä���� ����)
							broadcast(id + " : " + line);
						}
					//if user's input line has bad words, send a warning message to user and send nothing to other users
					}else 
						send_warning_msg();
						
				}
			//�̿ܿ� ������ �߻��ϸ� ex�� ��µȴ�.
			}catch(Exception ex){
				System.out.println(ex);
			//ä���� ������ ��, ä���� ����� �� 
			}finally{
			//�ϳ��� �ڿ��� �����Ϸ��� �� �� �־��� �������� ���� �ϳ��� �����常�� ���� �����ϵ��� �� �Ŀ� HashMap���� id�� �����.
				synchronized(hm){
				hm.remove(id);
				}
				//�� ä�ù濡 client ��, id�� ä�ù��� �������� �˸���.
				broadcast(id + " exited.");
				try{
				//client�� ä�ù��� �������Ƿ� ���� ��, ������ �ݴ´�.
					if(sock != null)
					sock.close();
				//���ܰ� ���� ��� ex�� �����Ų��.
				}catch(Exception ex){}
			}//end of final
	} // end of run

		//�ӼӸ� ����� �����ϴ� �Լ�
		public void sendmsg(String msg){
			//start���� to���� ��, id�� ù��° �ε����� ����.
			int start = msg.indexOf(" ") +1;
			//end���� id ���� ��ĭ�� �ε��� ��ȣ�� ����.
			//indexof("char", num);�� num��°�� char�� �ǹ��Ѵ�. ��, ������ char�� num��° char�� �ε����� ã���ش�.
			int end = msg.indexOf(" ", start);
			Calendar calendar = Calendar.getInstance();
			Date date = calendar.getTime();
			String today = (new SimpleDateFormat("H:mm:ss").format(date));
			String time = "["+ today + "] ";
			
			//id�� �����Ѵٸ� end�� -1�� �ƴϹǷ�
			if(end != -1){
				//to���� id�� ����. 
				//substring�� start���� end������ ������ string�� �߶� �����Ѵ�.
				String to = msg.substring(start, end);
				//msg2���� end���� ��, �ӼӸ��ϰ��� �ϴ� ��ȭ ������ ����ȴ�.
				String msg2 = msg.substring(end+1);
				//to ��, id�� �ش��ϴ� value�� HashMap���κ��� �ҷ��´�. (HashMap���� id�� sock ��, ������ ����Ǿ� �ִ�.)
					Object obj = hm.get(to);
					//obj�� null�� �ƴϸ�, �� id�� �˸��� value�� ����(����)�� ������
					if(obj != null){
						PrintWriter pw = (PrintWriter)obj;
						//id�� msg2�� �ӻ迴�ٰ� ȭ�鿡 ����Ѵ�.
						pw.println(time + id + " whisphered. : " + msg2);
						//print�� ���� ���۰� ������ flush�� ���ش�.
						pw.flush();
					} // end of if
			}//end of second if
		} // end of sendmsg
		
		public void add_spam(String line) {	
			int start = line.indexOf(" ") +1;
			//end���� id ���� ��ĭ�� �ε��� ��ȣ�� ����.
			//indexof("char", num);�� num��°�� char�� �ǹ��Ѵ�. ��, ������ char�� num��° char�� �ε����� ã���ش�.
			int end = line.indexOf(" ", 2);
			String msg = line.substring(end+1);

			badWords.add(msg);
		}
		
		public void send_userlist() {
			Iterator<Entry<String, PrintWriter>> iterator = hm.entrySet().iterator();
			
			//���� ����� ���� counting�� ���� ����
			int numOfUsers = 0;
			
			//id_myself�� ���� �����´�.
			Object obj = hm.get(id_myself);
			//���� id�� ���� obj ����
			PrintWriter pw = (PrintWriter)obj;
			
			Calendar calendar = Calendar.getInstance();
			Date date = calendar.getTime();
			String today = (new SimpleDateFormat("H:mm:ss").format(date));
			String time = "["+ today + "] ";
			
			//userlist�� ����ϱ� ���� ���� ����
			pw.println(" ");
			pw.println(time);
			
			while(iterator.hasNext()) {
				Entry entry = (Entry)iterator.next();
				
				//hm�� ���� iterator�� ���� �� ����� ����ڰ� hashmap�ȿ� ����ִ��� Ȯ���Ѵ�.
				if(entry.getKey() != null) {
					numOfUsers++;
				}
				
				//hashmap�ȿ� �ִ� id�� �ҷ�����.
				String curr_id = "userID: " + entry.getKey();
				
				//�׸��� �̸� ä�ù濡 ����Ѵ�.
				pw.println(curr_id);
				//print�� ���� ���۰� ������ flush�� ���ش�.
				//pw.flush();
			}//end of while
			
			//���������� ���� ä�ù濡 �ִ� �ο��� ���� ����Ѵ�. 
			String final_number = "In this chat room, Total chat client is " + numOfUsers;
			pw.println(final_number);
			//print�� ���� ���۰� ������ flush�� ���ش�.
			pw.flush();
		}//end of send_userlist
		
		public void spam_list() {
			//id_myself�� ���� �����´�.
			Object obj = hm.get(id_myself);
			//���� id�� ���� obj ����
			PrintWriter pw = (PrintWriter)obj;
			Calendar calendar = Calendar.getInstance();
			Date date = calendar.getTime();
			String today = (new SimpleDateFormat("H:mm:ss").format(date));
			String time = "["+ today + "] ";
			
			//userlist�� ����ϱ� ���� ���� ����
			pw.println(" ");
			pw.println(time + "This is spam list");
			
			for (String word : badWords) {
				pw.print(word + " ");
			}
			pw.println(" ");
			pw.println(" ");
			pw.flush();
			
		}//end of send_userlist
		
		//��� ä�ù濡 msg�� broadcast�ϴ� �޼ҵ�
		public void broadcast(String msg){
			Calendar calendar = Calendar.getInstance();
			//synchronized�� �� �̻��� �����尡 ������ �ڿ��� �����ϴ� ���, ���� ���� �����尡 �ϳ��� �ڿ��� �����Ϸ��� �� �� �־��� �������� ���� �ϳ��� �����常�� ���� �����ϵ��� �Ѵ�.
			synchronized(hm){
				//���� �ڽ��� id�� pw�� �����Ѵ�.
				PrintWriter pw_Myself = (PrintWriter)hm.get(id);
				Collection<PrintWriter> collection = hm.values();
				//iterator�� �÷����� �ִ� ����Ÿ�� �о� �˸´� ������ ã���ִ� �������̽��̴�. iterator�� ó������ ������ �ϳ��� ���������� ������ ���� �� �ۿ� ����.
				Iterator<PrintWriter> iter = collection.iterator();
				//iterator�� ������ �о� �� ��Ұ� ������ true�� ��ȯ�Ѵ�. ���� ��ȯ�� ��Ұ� ���ٸ� ��, �������� ���� �Ѿ�� false�� ��ȯ�Ѵ�.
				while(iter.hasNext()){
					//iterator�� ���� ���� pw�� �����Ѵ�.
					PrintWriter pw = (PrintWriter)iter.next();
					//���� ���� id�� pw�� iter�Ǵ� ������ pw�� ������ ��µ��� �ʰ��Ѵ�. (broadcast�� �ǰ� �Ѵ�.)
					if(!pw.equals(pw_Myself)) {
							//msg�� ��� �濡 ����Ѵ�.
						Date date = calendar.getTime();
						String today = (new SimpleDateFormat("H:mm:ss").format(date));
						String time = "["+ today + "] ";
							pw.println(time + msg);
							//print�� ���� ���۰� ������ flush�� ���ش�.
							pw.flush();
					}//end of if
					}//end of while
				}//end of sync
			}//end of broadcast
		
		public boolean toCheckLine(String line) {
			//make arrayList to contain bad words
			boolean toCheck = false;
			
			//if line has a bad word, change toCheck to true and break because it doesn't need to check till the end of arrayList
			for (String word : badWords) {
				if (line.indexOf(word) == 0) {
					toCheck = true;
					break;
				}
			}
			
			//return true/false to check that line has bad words or not
			return toCheck;
		}
		
		//send warning message to user. logic is similar with send_msg
		public void send_warning_msg() {
			//get user's id
			Object obj = hm.get(id);
			//make user's print writer
			PrintWriter pw_Myself = (PrintWriter) obj;
			//send a warning message
			
			Calendar calendar = Calendar.getInstance();
			Date date = calendar.getTime();
			String today = (new SimpleDateFormat("H:mm:ss").format(date));
			String time = "["+ today + "] ";
			
			pw_Myself.println(time + "You can't use bad words in this chat room!");
			pw_Myself.flush();

		} 

	}//end of class