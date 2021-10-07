package wv.kmg.filetransfer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.JsonObject;

public class Server {
	public static String DEFAULT_PATH;
	public static final int DEFAULT_PORT = 11111;
	public static void main(String args[]) {
		if(args.length == 0) {
			DEFAULT_PATH = System.getProperty("user.home");
		}else {
			DEFAULT_PATH = args[0];
		}
		try {
			
			ServerSocket ss = new ServerSocket(DEFAULT_PORT);
			ServerSocket sss = new ServerSocket(DEFAULT_PORT+1);
			ExecutorService es = Executors.newFixedThreadPool(100);
			
			while(true) {
				try {
					Socket s = ss.accept();
//					new MessageListener(s).start();
					es.execute(new MessageListener(s));
					
					Socket s1 = sss.accept();
//					new FileListener(s1).start();
					es.execute(new FileListener(s1));
					
				}catch(IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
