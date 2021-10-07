package wv.kmg.filetransfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileProcessor extends Thread{
	public static final int BUFFER_SIZE = 1024*8;
	File file;
	byte[] buffer;
	boolean isRequest;
	boolean isEnd;
	int bytes;
	Thread parent;
	public FileProcessor(File file) {
		this.file = file;
		buffer = new byte[BUFFER_SIZE];
		isRequest = false;
		isEnd = false;
		bytes = 0;
	}
	
	@Override
	public void run() {
		
		FileInputStream fis = null;
		
		char[] buffer = new char[BUFFER_SIZE];
		try {
			fis = new FileInputStream(this.file);
			
			while(true) {

//				while(!isRequest) {}
				synchronized (this) {
					try {

						wait();	
						
						bytes = fis.read(this.buffer);

						if (bytes == -1) {
							isEnd = true;
							break;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

					isRequest = false;
					notify();
				}
				
			}
			fis.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
		
	}

	public byte[] getData() {
		return this.buffer;
	}
	public int getDataLength() {
		return this.bytes;
	}
}
