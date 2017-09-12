package utilities;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
	private static Logger instance;
	private Logger() {};
	
	private int level = 0;
	public final static int WARN = 1;
	public final static int ERROR = 2;
	public final static int INFO = 0;
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static synchronized Logger getInstance() {
		if (instance == null) {
			instance = new Logger();
		}
		return instance;
	}
	
	public void warn(String s) {
		if (level > WARN) { return; }
		System.out.println("WARN: " + getTimeStamp() + " : " + s);
	}

	public void error(String s) {
		if (level > ERROR) { return; }
		System.out.println("ERROR: " + getTimeStamp() + " : " + s);
	}
	
	public void info(String s) {
		if (level > INFO) { return; }
		System.out.println("INFO: " + getTimeStamp() + " : " + s);
	}
	
	private String getTimeStamp() {
		Date d = new Date();
		return sdf.format(d);
	}
	
	public void setLogLevel(int i) {
		level = i;
	}
	
	public void warn(Exception e) {
		if (level > WARN) { return; }
		warn(e.getMessage());
		e.printStackTrace();
	}
	
	public void error(Exception e) {
		if (level > ERROR) { return; }
		error(e.getMessage());
		e.printStackTrace();
	}
	
	public void info(Exception e) {
		if (level > INFO) { return; }
		info(e.getMessage());
		e.printStackTrace();
	}
}
