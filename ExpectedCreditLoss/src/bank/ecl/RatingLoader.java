package bank.ecl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import referenceobjects.BusinessDate;
import referenceobjects.ProbabilityOfDefault;
import referenceobjects.Rating;
import referenceobjects.stores.RatingStore;
import utilities.InputHandlers;
import utilities.Logger;
import utilities.PreferencesStore;

public class RatingLoader {

	private static Logger l = Logger.getInstance();
	private static PreferencesStore ps = PreferencesStore.getInstance();
	
	/*
	 * PD file structure is tab separated:
	 * Observation date
	 * Maturity
	 * Value
	 * Rating reference
	 */
	public static void loadRatings() {
		String line;
		String[] lineArray;
		boolean first = true;
		ProbabilityOfDefault pd;
		Integer year;
		Double value;
		String rating;
		
		Calendar gc = GregorianCalendar.getInstance();
		FileReader fr = null;
		BufferedReader sc = null;

		Date asOfDate = BusinessDate.getInstance().getDate();
		
		RatingStore ratStore = RatingStore.getInstance();
				
		try {
			
			fr = new FileReader(ps.getPreference(PreferencesStore.DIRECTORY) + ps.getPreference(PreferencesStore.PD_FILE));
			sc = new BufferedReader(fr);
			
			while ((line = sc.readLine()) != null) {
				if (first) {
					first = false;
					line = sc.readLine();
				}
				
				lineArray = line.split("\t");
				year = InputHandlers.intMe(lineArray[1].replaceAll("y",  ""));
				value = InputHandlers.doubleMe(lineArray[2]);
				rating = lineArray[3];
				
				gc.setTime(asOfDate);
				gc.add(Calendar.YEAR, year);
					
				pd = new ProbabilityOfDefault(rating, gc.getTime(), value);
				ratStore.getRating(rating).addPD(pd);
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
		
		l.info("Loaded " + ratStore.getSize() + " ratings");
	}
	
	private void printPDs() {
		for (Rating r : RatingStore.getInstance().getAllRatings()) {
			List<ProbabilityOfDefault> pds = r.getPDs();
			for (ProbabilityOfDefault pd : pds) {
				l.info("PD Rating " + pd.getRating() + ", Date " + pd.getDate() + ", pd  " + pd.getPD());
			}
		}
	}
}
