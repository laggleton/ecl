package bank.ecl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import referenceobjects.BusinessDate;
import referenceobjects.DateFormat;
import utilities.InputHandlers;
import utilities.Logger;
import utilities.PreferencesStore;

public class BusinessDateLoader {
	
	private static Logger l = Logger.getInstance();
	private static PreferencesStore ps = PreferencesStore.getInstance();
	
	public void loadAsOfDateFromFile(String fileName) {
		String line;
		String[] lineArray;
		
		FileReader fr = null;
		BufferedReader sc = null;
		String d= "";
		
		try {
			fr = new FileReader(fileName);
			sc = new BufferedReader(fr);
			
			while ((line = sc.readLine()) != null) {
				
				lineArray = line.split("=");
				d = InputHandlers.cleanMe(lineArray[1]);
			
				loadAsOfDate(d);
			}
		}
		catch (Exception fe) {
			fe.printStackTrace();
		}
		finally {
			try {
				if (sc != null) { sc.close(); }
				if (fr != null) { fr.close(); }
			} 
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
	}
	
	public static void loadAsOfDate(Date d) {
		BusinessDate.getInstance().initialise(d);
	}
	
	public static void loadAsOfDate(String s) {
		try {
				BusinessDate.getInstance().initialise(DateFormat.DMY_FORMAT.parse(s));
		}
		catch (ParseException pe) {
			l.error("Failed to parse business date: " + s + " with format " + DateFormat.DMY_FORMAT.toString());
			pe.printStackTrace();
		}
	}
	
	public static void loadAsOfDateFromPreferences() {
		BusinessDate.getInstance().initialise(ps.getPreference(PreferencesStore.BUSINESS_DATE));
	}

}
