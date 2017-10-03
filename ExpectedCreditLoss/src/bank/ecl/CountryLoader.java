package bank.ecl;

import java.io.File;
import java.util.List;
import java.util.Scanner;

import referenceobjects.GrossDomesticProduct;
import referenceobjects.stores.CountryStore;
import utilities.FileUtils;
import utilities.InputHandlers;
import utilities.Logger;
import utilities.PreferencesStore;

public class CountryLoader {

	private static Logger l = Logger.getInstance();
	private static PreferencesStore ps = PreferencesStore.getInstance();
	
	/*
	 * GDP file structure is tab separated:
	 * Country
	 * Year
	 * Value
	 */
	public static void loadCountries() {
		String line = "";
		List<String> lineArray = null;
		boolean first = true;
		GrossDomesticProduct gdp;
		String country;
		Integer year;
		Double value;
		Double oneYearGrowth;
		
		CountryStore ctryStore = CountryStore.getInstance();
				
		String delimiter = "\t";
		Scanner scanner = null;
		if (null != ps.getPreference(PreferencesStore.GDP_FILE_DELIMITER)) { delimiter =  ps.getPreference(PreferencesStore.GDP_FILE_DELIMITER); }
		
		try {
			
			scanner = new Scanner(new File(ps.getPreference(PreferencesStore.DIRECTORY) + ps.getPreference(PreferencesStore.GDP_FILE)));
		     while (scanner.hasNext()) {
								
		    	line = scanner.nextLine();
				if (first) {
					first = false;
					line = scanner.nextLine();
				}
				lineArray = FileUtils.parseLine(line, delimiter);
				
				country = lineArray.get(0);
				year = InputHandlers.intMe(lineArray.get(1));
				
				if ((null == lineArray.get(2)) || (lineArray.get(2).isEmpty()) || (lineArray.get(2).equals(""))) { 
					value = null;
				}
				else {
					value = InputHandlers.doubleMe(lineArray.get(2)) / 100d;
				}
				
				if ((null == lineArray.get(3)) || (lineArray.get(3).isEmpty()) || (lineArray.get(3).equals(""))) {
					oneYearGrowth = null;
				}
				else {
					oneYearGrowth = InputHandlers.doubleMe(lineArray.get(3)) / 100d;
				}
				
				
				gdp = new GrossDomesticProduct(year);
				
				gdp.setActualGrowth(value);
				gdp.setOneYearPredictedGrowth(oneYearGrowth);
				
				ctryStore.getCountry(country).addGdp(gdp);
			}
		}
		catch (Exception fe) {
			fe.printStackTrace();
			l.error(line);
		}
		finally {
			if (null != scanner) { scanner.close(); }
		}
		l.info("Loaded " + CountryStore.getInstance().getSize() + " countries");
	}
	
	public static void printAllCountries() {
		CountryStore ctryStore = CountryStore.getInstance();
		ctryStore.printAll();
	}
}
