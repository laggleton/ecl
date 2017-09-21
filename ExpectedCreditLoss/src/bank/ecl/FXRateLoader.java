package bank.ecl;

import java.util.List;
import java.io.File;
import java.util.Date;
import java.util.Scanner;

import referenceobjects.Currency;
import referenceobjects.DateFormat;
import referenceobjects.FxRate;
import referenceobjects.stores.FxRateStore;
import utilities.FileUtils;
import utilities.InputHandlers;
import utilities.Logger;
import utilities.PreferencesStore;

public class FXRateLoader {
	
	private static Logger l = Logger.getInstance();
	private static PreferencesStore ps = PreferencesStore.getInstance();

	public static void loadFXRates() {
		String line = "";
		List<String> lineArray = null;
		boolean first = true;
		Currency ccy;
		String ccyName;
		Date rateDate;
		Double rate;
				
		FxRateStore fxStore = FxRateStore.getInstance();
		FxRate fxRate;
		
		int count = 1;
		
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
				
				ccyName = lineArray.get(0);
				rateDate = InputHandlers.dateMe(lineArray.get(1), DateFormat.ISO_FORMAT);
				rate = InputHandlers.doubleMe(lineArray.get(2));
				
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
			if (null != scanner) { scanner.close(); }
		}
		
		l.info("Loaded " + count + " FX Rates");
	}
}
