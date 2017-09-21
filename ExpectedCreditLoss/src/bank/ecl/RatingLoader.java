package bank.ecl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Scanner;

import referenceobjects.BusinessDate;
import referenceobjects.ProbabilityOfDefault;
import referenceobjects.Rating;
import referenceobjects.stores.RatingStore;
import utilities.FileUtils;
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
		String line = "";
		List<String> lineArray = null;
		boolean first = true;
		ProbabilityOfDefault pd;
		Integer year;
		Double value;
		String rating;
		
		Calendar gc = GregorianCalendar.getInstance();

		Date asOfDate = BusinessDate.getInstance().getDate();
		
		RatingStore ratStore = RatingStore.getInstance();
		
		String delimiter = "\t";
		Scanner scanner = null;
		if (null != ps.getPreference(PreferencesStore.FX_FILE_DELIMITER)) { delimiter =  ps.getPreference(PreferencesStore.FX_FILE_DELIMITER); }
	
				
		try {
			
			scanner = new Scanner(new File(ps.getPreference(PreferencesStore.DIRECTORY) + ps.getPreference(PreferencesStore.FX_FILE)));
		    while (scanner.hasNext()) {
								
		    	line = scanner.nextLine();
				if (first) {
					first = false;
					line = scanner.nextLine();
				}
				lineArray = FileUtils.parseLine(line, delimiter);
				
				year = InputHandlers.intMe(lineArray.get(1).replaceAll("y",  ""));
				value = InputHandlers.doubleMe(lineArray.get(2));
				rating = lineArray.get(3);
				
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
			if (null != scanner) { scanner.close(); }
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
