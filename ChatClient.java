//https://github.com/1nhee/SimpleChat.git
	
import java.net.*;
import java.io.*;

public class ChatClient {

	public static void main(String[] args) {
		if(args.length != 2){
			System.out.println("Usage : java ChatClient <username> <server-ip>");
			System.exit(1);
		}
		Socket sock = null;
		BufferedReader br = null;
		PrintWriter pw = null;
		boolean endflag = false;
		try{
			sock = new Socket(args[1], 10001);
			pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
			pw.println(args[0]);
			pw.flush();
			InputThread it = new InputThread(sock, br);
			it.start();
			String line = null;
			while((line = keyboard.readLine()) != null){
				//3. 금지어 경고 기능 If user input bad word,
				if(line.equals("씨발") || line.equals("ㅆㅂ") || line.equals("존나") || line.equals("fuck") || line.equals("좆같다")){
					System.out.println("You can't send bad words to others. Use good words in this chat room");
				}else
				{
				pw.println(line);
				pw.flush();
					if(line.equals("/quit")){
						endflag = true;
						break;
					}
				}
			}
			System.out.println("Connection closed.");
		}catch(Exception ex){
			if(!endflag)
				System.out.println(ex);
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
	public InputThread(Socket sock, BufferedReader br){
		this.sock = sock;
		this.br = br;
	}
	
	public void run(){
		try{
			String line = null;
			while((line = br.readLine()) != null){
				System.out.println(line);
			}
		}catch(Exception ex){
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
