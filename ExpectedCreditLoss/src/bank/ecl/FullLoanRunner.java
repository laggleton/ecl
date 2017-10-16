package bank.ecl;

import utilities.Logger;
import utilities.PreferencesLoader;

public class FullLoanRunner {
	
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
		dl.loadDataForFullRun();
				
		//Cash Flow steps
		dl.generateDisbursements();
		dl.applyCancellations();
		dl.calculatePrepayments();
		//dl.calculateInterest();
		
		//EIR
		//dl.calculateEIR();
		
		//ECL
		//dl.calculateECL();
		
		//Output
		//dl.printResults(true);
		dl.printCashFlows();
		//dl.printEURResults();

	}

}
