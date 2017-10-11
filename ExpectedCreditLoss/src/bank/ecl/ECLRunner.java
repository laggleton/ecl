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
		
		FullDataLoader dl = new FullDataLoader();
		dl.loadDataForECL();
		//dl.generateExpenses();
		dl.calculateECL();
		dl.printResults(true);
		dl.printCashFlows();
		dl.printEURResults();
		//dl.printStageReasons();
		//dl.printRatings();
		//dl.printCountries();

	}

}
