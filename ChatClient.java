import java.net.*;
import java.io.*;

public class ChatClient {

	public static void main(String[] args) {
		//client의 input값이 2개가 아니면 에러 메세지 출력
		//client의 input 값이 user name과 server-ip주소가 아니면 소켓 즉, 서버를 생성할 수 없으므로 에러 메세지를 출력한다.
		if(args.length != 2){
			System.out.println("Usage : java ChatClient <username> <server-ip>");
			//에러 메세지 출력 후 나가기
			System.exit(1);
		}
		//client가 올바르게 user name과 server-ip를 입력하였다면,
		Socket sock = null;
		BufferedReader br = null;
		PrintWriter pw = null;
		boolean endflag = false;
		try{
			//client의 server-ip주소와 port 넘버를 갖고 소켓 즉, 서버를 만든다. 
			//각 client마다 하나의 소켓 즉, 서버가 필요하므로 각 user name가 server-ip 주소가 입력될 때마다 socket을 만들어준다.
			sock = new Socket(args[1], 10001);
			//server로 아웃풋 값 내보내기
			//PrintWriter 클래스는 주어진 데이터를 문자 출력으로 바꾸어 주는 것이다. 그러므로, getOutputStream으로부터 return된 byte 단위의 데이터를 OutputStream으로 내보낸다.
			pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			//server로부터 입력 값 받기
			//getInputStream은 return 값이 byte이다. 그러므로, 이를 문자 입력 스트림인InputstreamReader에 넣어주어byte를 인풋 값으로 변환시켜준다.(구글링으로는 int로 바꿔준다고 한다.) 그리고, buffered reader로 감싸서 하나씩 읽어내는 것이 아닌 일정 데이터가 쌓이면 읽어내어 효율적으로 데이터를 읽어들인다. id, 대화 내용 등 앞으로 사용자로의 입력 값을 읽어 들일 때에는 
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
			// send username.
			pw.println(args[0]);
			//flush를 해주어 buffer를 비워준다.
			pw.flush();
			//InputThread 인스턴스 생성(pass sock과 br의 reference)
			InputThread it = new InputThread(sock, br);
			//InputThread 동작 시킴
			it.start();
			String line = null;
			//client가 입력을 하는 동안 계속 실행된다.
			while((line = keyboard.readLine()) != null){
				//사용자의 입력 값을 server로 보낸다.
				pw.println(line);
				//flush를 해줘야 buffer가 비워진다.
				pw.flush();
				//사용자가 /quit을 입력하면 endflag가 true 바뀌며 while문이 정지된다. 즉, client로부터 입력 값을 읽어오는 것을 정지한다.
				if(line.equals("/quit")){
					endflag = true;
					break;
				}
			}
			//client가 종료하였으므로 서버도 닫혔음을 나타낸다.
			System.out.println("Connection closed.");
		//예외적인 에러가 있을 때에는 ex를 출력한다.
		}catch(Exception ex){
			if(!endflag)
				System.out.println(ex);
		//마지막으로 아웃풋과 인풋, 서버와 관련된 모든 instance들을 닫아준다. 그리고, 예외 상황시 ex를 출력한다.
		}finally{
			try{
				if(pw != null)
					pw.close();
			}catch(Exception ex){}
			try{
				if(br != null)
					br.close();
			}catch(Exception ex){}
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		} // end of finally
	} // end of main
} // end of class

class InputThread extends Thread{
	private Socket sock = null;
	private BufferedReader br = null;
	//받아온 sock과 br의 reference를 copy하여 인스턴스를 생성시킨다.
	public InputThread(Socket sock, BufferedReader br){
		this.sock = sock;
		this.br = br;
	}
	
	public void run(){
		try{
			String line = null;
			//server에서 보내는 데이터가 null일 때까지 읽는다.
			while((line = br.readLine()) != null){
				//server로부터 읽어들인 데이터를 출력한다.
				System.out.println(line);
			}
		//exception한 상황일 경우 ex를 출력한다.
		}catch(Exception ex){
		//sock, br 모두 끝낸다. 그리고 예외 상황일 경우 ex를 출력한다.
		}finally{
			try{
				if(br != null)
					br.close();
			}catch(Exception ex){}
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		}
	} 
}//end of InputThread
