package bank.ecl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

import referenceobjects.Currency;
import referenceobjects.DateFormat;
import referenceobjects.FxRate;
import referenceobjects.stores.FxRateStore;
import utilities.InputHandlers;
import utilities.Logger;
import utilities.PreferencesStore;

public class FXRateLoader {
	
	private static Logger l = Logger.getInstance();
	private static PreferencesStore ps = PreferencesStore.getInstance();

	public static void loadFXRates() {
		String line;
		String[] lineArray;
		boolean first = true;
		Currency ccy;
		String ccyName;
		Date rateDate;
		Double rate;
				
		FileReader fr = null;
		BufferedReader sc = null;
				
		FxRateStore fxStore = FxRateStore.getInstance();
		FxRate fxRate;
		
		int count = 1;
		
		try {
			
			fr = new FileReader(ps.getPreference(PreferencesStore.DIRECTORY) + ps.getPreference(PreferencesStore.FX_FILE));
			sc = new BufferedReader(fr);
			
			while ((line = sc.readLine()) != null) {
								
				if (first) {
					first = false;
					line = sc.readLine();
				}
				
				lineArray = line.split(",");
				
				ccyName = lineArray[0];
				rateDate = InputHandlers.dateMe(lineArray[1], DateFormat.ISO_FORMAT);
				rate = InputHandlers.doubleMe(lineArray[2]);
				
				fxRate = new FxRate(rateDate, rate);
				
				ccy = fxStore.getCurrency(ccyName);
				ccy.addFxRate(fxRate);
				count++;
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
		
		l.info("Loaded " + count + " FX Rates");
	}
}
