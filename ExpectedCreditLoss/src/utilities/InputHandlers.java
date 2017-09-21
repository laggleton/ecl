package utilities;

import java.text.SimpleDateFormat;
import java.util.Date;

import referenceobjects.BusinessDate;

public class InputHandlers {
	public static Integer intMe(String s) {
		Integer i = new Integer(0);
		if (s != "") {
			try {
				i = new Integer(s);
			}
			catch (NumberFormatException nfe) {
				
			}
		}
		return i;
	}
	
	public static Double doubleMe(String s) {
		Double i = new Double(0d);
		if (s != "") {
			try {
				i = new Double(s);
			}	
			catch (NumberFormatException nfe) {
			
			}
		}
		return i;
	}
	
	public static Date dateMe(String s, SimpleDateFormat df) {
		Date d = BusinessDate.getInstance().getDate();
		if ((null != s) || (s != "")) {
			try {
				d = df.parse(s);
			}	
			catch (Exception pe) {
			
			}
		}
		return d;
	}
	
	public static String cleanMe(String s) {
		String p = "^\\s+";
		String q = "\\s+$";
		
		s = s.replaceAll(p, "");
		s = s.replaceAll(q, "");
		
		return s;
	}
}
