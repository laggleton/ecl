package bank.ecl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import referenceobjects.GrossDomesticProduct;
import referenceobjects.stores.CountryStore;
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
		String[] lineArray;
		boolean first = true;
		GrossDomesticProduct gdp;
		String country;
		Integer year;
		Double value;
		Double oneYearGrowth;
		
		CountryStore ctryStore = CountryStore.getInstance();
				
		FileReader fr = null;
		BufferedReader sc = null;
		
		try {
			
			fr = new FileReader(ps.getPreference(PreferencesStore.DIRECTORY) + ps.getPreference(PreferencesStore.GDP_FILE));
			sc = new BufferedReader(fr);
			
			while ((line = sc.readLine()) != null) {
								
				if (first) {
					first = false;
					line = sc.readLine();
				}
				
				lineArray = line.split("\t");
				
				country = lineArray[0];
				year = InputHandlers.intMe(lineArray[1]);
				value = InputHandlers.doubleMe(lineArray[2]);
				
				try {
					oneYearGrowth = InputHandlers.doubleMe(lineArray[3]);
				}
				catch (ArrayIndexOutOfBoundsException e) {
					oneYearGrowth = null;
				}
					
				gdp = new GrossDomesticProduct(year);
				gdp.setActualGrowth(value);
				gdp.setOneYearPredictedGrowth(oneYearGrowth);
				
				ctryStore.getCountry(country).addGdp(gdp);
			}
		}
		catch (Exception fe) {
			fe.printStackTrace();
			ps.printAll();
			l.error(line);
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
		l.info("Loaded " + CountryStore.getInstance().getSize() + " countries");
	}
}
