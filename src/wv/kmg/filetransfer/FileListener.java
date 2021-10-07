package wv.kmg.filetransfer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class FileListener extends Thread {

	Map<String, FileProcessor> m;
	Socket s;
	Logger logger = LogFactory.getInstance().getLogger(MessageListener.class.getName());
	public FileListener(Socket s) {
		this.s = s;
		m = new HashMap<String, FileProcessor>();
	}
	
	@Override
	public void run() {
		logger.log(Level.INFO,this.s.getInetAddress().getHostAddress() + " connected");
		BufferedReader br = null;
		PrintWriter pw = null;
		OutputStreamWriter output = null;
		try {
			br = new BufferedReader(new InputStreamReader(s.getInputStream(),"UTF-8"));
			output = new OutputStreamWriter(this.s.getOutputStream(),"UTF-8");
			pw = new PrintWriter(output, true);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			logger.log(Level.INFO,e1.getMessage());
			return;
		}

		try {
			while (true) {
				String input = br.readLine();

				Gson gson = new Gson();
				JsonObject jo = gson.fromJson(input, JsonObject.class);

				Class<?> cls = Class.forName(this.getClass().getName());

				Method m = cls.getMethod(jo.get("request").getAsString(), JsonObject.class);

				JsonObject ret = (JsonObject) m.invoke(this, jo);
				pw.println(ret.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.log(Level.INFO,e.getMessage());
		}

	}
	
	public JsonObject data(JsonObject jo) throws Exception {

		JsonObject ret = new JsonObject();
		FileProcessor fp = null;
		String path = jo.get("path").getAsString();
		
		
		String key = (this.s.getInetAddress().getHostAddress()+path+jo.get("down-path").getAsString());
		File f = new File(path);
		if (!f.exists()) {
			ret.addProperty("path", path);
			ret.addProperty("count", "0");
			JsonObject result = new JsonObject();
			result.addProperty("error", "file not found");
			ret.add("result", result);
		} else {
			if ((fp = m.get(key)) == null) {

				fp = new FileProcessor(f);
				m.put(key, fp);
				fp.start();
			} 
			while (fp.getState() != Thread.State.WAITING) {

			}


			synchronized (fp) {
				try {

					fp.notify();

					fp.wait();

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				JsonObject result = new JsonObject();

				result.addProperty("data", Base64.getEncoder().encodeToString(fp.getData()));
				result.addProperty("total", f.length());
				result.addProperty("length", fp.getDataLength());
				result.addProperty("isEnd", fp.isEnd);
				ret.addProperty("name", f.getName());
				ret.addProperty("path", path);
				ret.addProperty("count", "1");
				ret.add("result", result);
			}

		}

		return ret;
	}
}
