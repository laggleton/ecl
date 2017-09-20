package bank.ecl;

import utilities.Logger;
import utilities.PreferencesLoader;

public class ECLRunner {
	
	/* 
	 * Usage:
	 * arg[0] = preferences file
	 */
	public static void main(String[] args) {
		
		Logger l = Logger.getInstance();
		String preferencesFile = "";
		try { 
			preferencesFile = args[0];
		}
		catch (Exception fe) {
			l.error(fe);
			l.error("Preferences file not specified! Exiting");
			System.exit(1);
		}
		
		PreferencesLoader pl = new PreferencesLoader(preferencesFile);
		pl.load();
		
		ECLDataLoader dl = new ECLDataLoader();
		dl.loadData();
		dl.calculateECL();
		dl.printResults(true);
		dl.printCashFlows();

	}

}
