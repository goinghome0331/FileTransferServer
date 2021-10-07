package wv.kmg.filetransfer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogFactory {
	private static LogFactory logFactory;
	static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	private LogFactory() {}
	
	public static synchronized LogFactory getInstance() {
		if(logFactory == null) {
			logFactory = new LogFactory();
		}
		return logFactory;
	}
	public Logger getLogger(String className) {
		Logger logger = Logger.getLogger(className);
		logger.setLevel(Level.ALL);
        ConsoleHandler ch = new ConsoleHandler();
        try {
        	String timeStamp = df.format(new Date());
//        FileHandler fileHandler = new FileHandler("fileTransfer_%u.%g_"+timeStamp+".log", 1024*1024, 10, true);
//        fileHandler.setLevel(Level.ALL);
         
        SimpleFormatter sformatter = new SimpleFormatter();
//        fileHandler.setFormatter(sformatter);
        ch.setFormatter(sformatter);
//        logger.addHandler(fileHandler);
        logger.addHandler(ch);
        return logger;
        }catch(Exception e) {
        	e.printStackTrace();
        	return null;
        }
		
	}
}
