package referenceobjects;

import java.text.SimpleDateFormat;
import java.util.Date;

import utilities.Logger;

public class BusinessDate {
	private static BusinessDate instance = new BusinessDate();
	private boolean initialised = false;
		
	private Date d;
	
	private BusinessDate() {};
	
	public static synchronized BusinessDate getInstance() {
		return instance;
	}
	
	public void initialise(Date asOfDate) {
		d = asOfDate;
		initialised = true;
	}
	
	public void initialise(String asOfDate) {
		try {
			d = DateFormat.ISO_FORMAT.parse(asOfDate);
		}
		catch (Exception e) {
			Logger.getInstance().error(e);
			Logger.getInstance().error("Couldn't parse string " + asOfDate + " should be in ISO format yyyy-MM-dd");
		}
		initialised = true;
	}
	
	public Date getDate() {
		if (!initialised) {
			System.out.println("BusinessDate not initialised!!");
		}
		return d;
	}
	
	

}
