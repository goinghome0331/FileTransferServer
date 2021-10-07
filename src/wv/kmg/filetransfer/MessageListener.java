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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class MessageListener extends Thread {
	static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	Logger logger = LogFactory.getInstance().getLogger(MessageListener.class.getName());
	
	Socket s;

	public MessageListener(Socket s) {
		this.s = s;
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
			pw = new PrintWriter(output, true );
			
			JsonObject jo = new JsonObject();
			jo.addProperty("path", Server.DEFAULT_PATH);
			pw.println(jo.toString());
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
				logger.log(Level.INFO,"request : "+ jo.toString());
				Class<?> cls = Class.forName(this.getClass().getName());
					
				Method m = cls.getMethod(jo.get("request").getAsString(), JsonObject.class);

				JsonObject ret = (JsonObject) m.invoke(this, jo);
				logger.log(Level.INFO,"answer : "+ ret.toString());
				pw.println(ret.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.log(Level.INFO,e.getMessage());
		}

	}
	public JsonObject cd(JsonObject jo) {
		
		String add = jo.get("add").getAsString();
		JsonObject ret = new JsonObject();
		String fullPath;
		if(add.startsWith("/")) {
			fullPath = add;
		}else if(add.equals(".")) {
			fullPath = jo.get("path").getAsString();
		}else if(add.equals("..")) {
			fullPath = jo.get("path").getAsString().substring(0,jo.get("path").getAsString().lastIndexOf("\\"));
		}else {
			fullPath = jo.get("path").getAsString() + "\\" + add;
		}
		File c = new File(fullPath);
		if(!c.exists()) {
			ret.addProperty("error", "not exist");
		}else if(!c.isDirectory()) {
			ret.addProperty("error", "not directory");
		}else {
			ret.addProperty("path", fullPath);	
		}
		
		return ret;
	}
	
	private void addFile(File f, JsonArray ja) {
		JsonObject sub = new JsonObject();
		sub.addProperty("name", f.getName());
		Date d = new Date(f.lastModified());
		sub.addProperty("lastModified", df.format(d));
		String fileName = f.getName();
		String ext = f.isDirectory() ? "파일 폴더"
				: fileName.substring(fileName.lastIndexOf(".") + 1).toUpperCase() + " 파일";
		sub.addProperty("ext", ext);
		sub.addProperty("size", f.length());
		ja.add(sub);
	}
	public JsonObject fileList(JsonObject jo) throws IOException {

		JsonObject ret = new JsonObject();
		ret.addProperty("type", "fileList");
		
		File folder = null;
		String path = null;
		if (jo.get("path").getAsString().equals("")) {
			folder = new File(Server.DEFAULT_PATH);
			path = Server.DEFAULT_PATH;
		}else {
			folder = new File(jo.get("path").getAsString());
			path = jo.get("path").getAsString();
		}
		
		JsonArray ja = new JsonArray();
		File[] files = folder.listFiles();
		if (files != null) {
			for (File f : files) {
				addFile(f,ja);
			}
		}else {
			File f = folder;
			addFile(f,ja);
		}
		ret.addProperty("count", ja.size());
		ret.addProperty("path", path);
		ret.add("result", ja);

		return ret;
	}
}