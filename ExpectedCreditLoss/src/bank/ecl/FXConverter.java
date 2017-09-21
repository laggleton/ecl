package bank.ecl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import financialobjects.CashFlow;
import financialobjects.CashFlowType;
import financialobjects.Trade;
import financialobjects.stores.TradeStore;
import referenceobjects.DateFormat;
import utilities.Logger;
import utilities.PreferencesLoader;
import utilities.PreferencesStore;

public class FXConverter {

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
		l.setLogLevel(1);
		
		BusinessDateLoader.loadAsOfDateFromPreferences();
		FXRateLoader.loadFXRates();
		TradeLoader.loadTrades();
		CashFlowLoader.loadCFs();
		FeeLoader.loadFees();
		TradeLoader.setFirstDisbursementCurrency();
		
		printCashFlows();
		

	}
	
	public static void printCashFlows() {
		printCashFlows(null);
	}
	
	public static void printCashFlows(CashFlowType type) {
		String delimiter = PreferencesStore.getInstance().getPreference(PreferencesStore.OUTPUT_DELIMITER);
		try {
			Date d = new Date();
			
			File cashFlowFile = new File(PreferencesStore.getInstance().getPreference(PreferencesStore.DIRECTORY) + "ecl_bank-cashflows-" + DateFormat.OUTPUT_FORMAT.format(d) + ".csv");
		      
			// creates the file
			cashFlowFile.createNewFile();
      
			// creates a FileWriter Object
			FileWriter cashFlowWriter = new FileWriter(cashFlowFile); 
      
			// Writes the header to the file
			cashFlowWriter.write(Trade.getAbbreviatedPrimaryKeyHeader(delimiter));
			cashFlowWriter.write(CashFlow.getDisbursementHeader(delimiter));
			
			String tradeDecorator = "";
			
			// Loops through trades and cash flows 
			for (Trade t : TradeStore.getInstance().getAllTrades()) {
				tradeDecorator = t.getAbbreviatedPrimaryKeyDecorator(delimiter);
				for (CashFlow cf : t.getCashFlows()) {
					if (null != type) { if (!cf.getCashFlowSubType().equals(type)) { continue; }}
					cashFlowWriter.write(tradeDecorator + cf.toDisbursementCcyFormat(delimiter));
				}
			}
		
			cashFlowWriter.flush();
			cashFlowWriter.close();
		}
		catch (IOException e) {
			Logger.getInstance().error(e);
		}
	}

}
