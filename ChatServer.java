//https://github.com/1nhee/SimpleChat.git

import java.net.*;
import java.util.HashMap;
import java.util.Iterator;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class ChatServer {

	public static void main(String[] args) {
		try{
			//client의 요청을 받기 위해 포트 번호와 함께 ServerSocket 인스턴스를 생성하여 서버를 생성한다.
			ServerSocket server = new ServerSocket(10001);
			//ServerSocket을 통해 server를 생성해주었다.
			System.out.println("Waiting connection...");
			//데이터의 저장을 위해 HashMap인스턴스를 생성한다.
			//***HashMap은 while 밖, Socket은 While안에서 만드는 이유?
			//서버의 역할을 하는 소켓은 각 client가 전용 소켓을 갖고 있어야 한다.(마치 각자 방이 있는 것처럼 client별로 server를 생성해준다.) 하지만, HashMap은 여러 데이터를 저장하는 곳으로 공통으로 사용될 수 있기 때문에 밖에서 한번만 생성되어도 된다. 
            HashMap<String, PrintWriter> hm = new HashMap<String, PrintWriter>();
	
               while(true){
			//while문이 도는 동안 계속하여 client의 요청을 받아들인다. (.accept)
			Socket sock = server.accept();
			//sock인스턴스를 통해 받은 client의 요청을 chatthread에 데이터가 저장된 HashMap을 Chatthread에 넘긴다.(주소 값으로 넘김)
			ChatThread chatthread = new ChatThread(sock, hm);
			chatthread.start();//run을 실행시킨다.
		} // end of while
		//에러가 나는 예외 상황 시, e가 프린트 되게 한다.
		}catch(Exception e){
			System.out.println(e);
		}
	} // end of main
}//end of ChatServer


class ChatThread extends Thread{// thread를 가져와서 start가 가능
	private Socket sock;
	private String id;
	private String id_myself;
	private BufferedReader br;
	private HashMap<String, PrintWriter> hm;
	private boolean initFlag = false;
	
	public ChatThread(Socket sock, HashMap<String, PrintWriter> hm){
		//reference 즉, 주소 값을 copy하는 방식으로 인스턴스 생성
		this.sock = sock;
		this.hm = hm;

		try{
			//client한테 아웃풋 값 내보내기
			//PrintWriter 클래스는 주어진 데이터를 문자 출력으로 바꾸어 주는 것이다. 그러므로, getOutputStream으로부터 return된 byte 단위의 데이터를 OutputStream으로 내보낸다.
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			//client한테서 입력 값 받기
			//getInputStream은 return 값이 byte이다. 그러므로, 이를 문자 입력 스트림인InputstreamReader에 넣어주어byte를 인풋 값으로 변환시켜준다.(구글링으로는 int로 바꿔준다고 한다.) 그리고, buffered reader로 감싸서 하나씩 읽어내는 것이 아닌 일정 데이터가 쌓이면 읽어내어 효율적으로 데이터를 읽어들인다. 
			//Id, 대화 내용 등 앞으로 사용자로의 입력 값을 읽어 들일 때 사용한다.
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			//br로 읽어온 데이터를 id에 넣어준다. (사용자의 id를 읽어오는 것임)
			id = br.readLine();
			id_myself = id;
			broadcast(id + " entered.");
			System.out.println("[Server] User (" + id + ") entered.");
			
			//synchronized는 둘 이상의 쓰레드가 공동의 자원을 공유하는 경우, 여러 개의 쓰레드가 하나의 자원에 접근하려고 할 때 주어진 순간에는 오직 하나의 쓰레드만이 접근 가능하도록 한다.
			synchronized(hm){
				//HashMap에 저장 시, (Key, Value) 형태로 저장한다. 
				hm.put(this.id, pw);
			}
			initFlag = true;
			//에러 발생 시 실행
			}catch(Exception ex){
				System.out.println(ex);
			}
		} // end of constructor

		public void run(){
			try{
				String line = null;
				//사용자로부터 계속하여 문장을 읽어온다.
				while((line = br.readLine()) != null){
					//만약 사용자가 /quit을 입력하면 사용자로부터 읽어오는 것을 멈춘다.
					if(line.equals("/quit"))
						break;
					//만약 사용자가 /to를 입력하면 귓속말 메소드가 실행되어 개인적으로 메세지를 보낼 수 있다.
					if(line.indexOf("/to ") == 0){ 
						sendmsg(line);
					}else if(line.indexOf("/userlist") == 0) {
						send_userlist();
					}else
					//위의 두가지 경우 이외에 모든 대화 내용은 다음과 같은 형식으로 broadcast메소드로 보내진다.
					//Broadcast 메소드는 모든 서버에 동일한 대화 내용이 입력되는 것이다.(전체 채팅을 위해)
						broadcast(id + " : " + line);
					}
			//이외에 에러가 발생하면 ex가 출력된다.
			}catch(Exception ex){
				System.out.println(ex);
			//채팅의 마지막 즉, 채팅이 종료될 때 
			}finally{
			//하나의 자원에 접근하려고 할 때 주어진 순간에는 오직 하나의 쓰레드만이 접근 가능하도록 한 후에 HashMap에서 id를 지운다.
				synchronized(hm){
				hm.remove(id);
			}
			//각 채팅방에 client 즉, id가 채팅방을 나갔음을 알린다.
			broadcast(id + " exited.");
			try{
			//client가 채팅방을 나갔으므로 소켓 즉, 서버를 닫는다.
				if(sock != null)
				sock.close();
			//예외가 생긴 경우 ex를 실행시킨다.
			}catch(Exception ex){}
		}
	} // end of run

		//귓속말 기능을 실행하는 함수
		public void sendmsg(String msg){
			//start에는 to이후 즉, id의 첫번째 인덱스가 담긴다.
			int start = msg.indexOf(" ") +1;
			//end에는 id 뒤의 빈칸의 인데스 번호가 담긴다.
			//indexof("char", num);은 num번째의 char를 의미한다. 즉, 동일한 char의 num번째 char의 인덱스를 찾아준다.
			int end = msg.indexOf(" ", start);
			
			//id가 존재한다면 end는 -1이 아니므로
			if(end != -1){
				//to에는 id가 들어간다. 
				//substring은 start부터 end까지의 범위로 string을 잘라서 저장한다.
				String to = msg.substring(start, end);
				//msg2에는 end이후 즉, 귓속말하고자 하는 대화 내용이 저장된다.
				String msg2 = msg.substring(end+1);
				//to 즉, id에 해당하는 value를 HashMap으로부터 불러온다. (HashMap에는 id의 sock 즉, 소켓이 저장되어 있다.)
				Object obj = hm.get(to);
				//obj이 null이 아니면, 즉 id에 알맞은 value값 서버(소켓)이 있으면
				if(obj != null){
					PrintWriter pw = (PrintWriter)obj;
					//id가 msg2를 속삭였다고 화면에 출력한다.
					pw.println(id + " whisphered. : " + msg2);
					//print후 남는 버퍼가 없도록 flush를 해준다.
					pw.flush();
				} // end of if
			}
		} // end of sendmsg
		
		public void send_userlist() {
			Iterator<Entry<String, PrintWriter>> iterator = hm.entrySet().iterator();
			 int numOfUsers = 0;
			
			while(iterator.hasNext()) {
				Entry entry = (Entry)iterator.next();
				Entry check = (Entry)iterator.next();
				
				System.out.println("In this Chat room, users are");
				System.out.println(); 
				if(check.getKey() != null) {
					numOfUsers++;
				}
				System.out.println("userID: " + entry.getKey());
			}//end of while
			System.out.println("Total number of users is" + numOfUsers);
		}//end of send_userlist
		
		//모든 채팅방에 msg를 broadcast하는 메소드
		public void broadcast(String msg){
			//synchronized는 둘 이상의 쓰레드가 공동의 자원을 공유하는 경우, 여러 개의 쓰레드가 하나의 자원에 접근하려고 할 때 주어진 순간에는 오직 하나의 쓰레드만이 접근 가능하도록 한다.
			synchronized(hm){
				Collection<PrintWriter> collection = hm.values();
				//iterator는 컬렉션의 있는 데이타를 읽어 알맞는 정보를 찾아주는 인터페이스이다. iterator는 처음부터 끝까지 하나씩 순차적으로 정보를 읽을 수 밖에 없다.
				Iterator<PrintWriter> iter = collection.iterator();
				//iterator가 다음에 읽어 올 요소가 있으면 true를 반환한다. 만약 반환할 요소가 없다면 즉, 데이터의 끝을 넘어가면 false를 반환한다.
				while(iter.hasNext()){
					//iterator의 다음 값을 pw에 저장한다.
					PrintWriter pw = (PrintWriter)iter.next();
					
					//if(pw != id_myself) {
						//3. 금지어 경고 기능 If user input bad word,
						if(msg == "씨발" || msg == "ㅅㅂ" || msg == "존나" || msg == "엠창" || msg == "좆같다"){
							System.out.println("You can't send bad words to others. Use good words in this chat room");
						}else {
							//msg를 모든 방에 출력한다.
							pw.println(msg);
							//print후 남는 버퍼가 없도록 flush를 해준다.
							pw.flush();
						}
					//}
				}
			}
		} //end of broadcast
	}//end of class