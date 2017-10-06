package bank.ecl;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import financialobjects.CashFlow;
import financialobjects.stores.TradeStore;
import referenceobjects.DateFormat;
import utilities.FileUtils;
import utilities.InputHandlers;
import utilities.Logger;
import utilities.PreferencesStore;

public class CashFlowLoader {
	private static Logger l = Logger.getInstance();
	private static PreferencesStore ps = PreferencesStore.getInstance();
	
	/*
	 * CF file structure is tab separated:
	 * Observation date
	 * Maturity
	 * Value
	 * Rating reference
	 */
	public static void loadCFs() {
		int count = 0;
		String dealID;
		String facID;
		String bookID;
		String contractReference;
		Date paymentDate;
		Date balanceSheetDate;
		String cashFlowType;
		String cashFlowSubType;
		String currency;
		Double amount;
		String line = "";
		List<String> lineArray;
		boolean first = true;
		
		List<String> missingTrades = new ArrayList<>();
		
		CashFlow cf;
		String delimiter = "\t";
		Scanner scanner = null;
		if (null != ps.getPreference(PreferencesStore.CASHFLOW_FILE_DELIMITER)) { delimiter =  ps.getPreference(PreferencesStore.CASHFLOW_FILE_DELIMITER); }
		
		TradeStore tradeStore = TradeStore.getInstance();
		
		try {
			
			scanner = new Scanner(new File(ps.getPreference(PreferencesStore.DIRECTORY) + ps.getPreference(PreferencesStore.CASHFLOW_FILE)));
		     while (scanner.hasNext()) {
		    	 line = scanner.nextLine();
								
				if (first) {
					first = false;
					line = scanner.nextLine();
				}
				
				lineArray = FileUtils.parseLine(line, delimiter);
				
				dealID = lineArray.get(0);
				facID = lineArray.get(1);
				bookID = lineArray.get(2);
				contractReference = lineArray.get(3);
				paymentDate = InputHandlers.dateMe(lineArray.get(4), DateFormat.ISO_FORMAT);
				balanceSheetDate = InputHandlers.dateMe(lineArray.get(5), DateFormat.ISO_FORMAT);
				cashFlowType = lineArray.get(6);
				cashFlowSubType = lineArray.get(7);
				currency = lineArray.get(8);
				amount = InputHandlers.doubleMe(lineArray.get(9));
				
				if (amount == 0d) { continue; }
				
				if ((null == cashFlowSubType) || (cashFlowSubType.isEmpty())) { cashFlowSubType = cashFlowType; }
				
				cf = new CashFlow(currency, amount, paymentDate, cashFlowSubType);
				
				//Adding book level granularity
				contractReference += "/" + bookID;
				
				if (tradeStore.getTrade(contractReference) != null) { 
					tradeStore.getTrade(contractReference).addCF(cf);
				}
				else {
					if (!missingTrades.contains(contractReference)) {
						missingTrades.add(contractReference);
						
					}
				}
				
				count++;
			}
		}
		catch (Exception fe) {
			fe.printStackTrace();
			l.error(line);
		}
		finally {
			if (null != scanner) { scanner.close(); }
		}
		
		for (String t : missingTrades) {
			l.info("No trade data for cash flows, contractref " + t);
		}
		l.warn("Missing trade data for " + missingTrades.size());
		
		l.info("Loaded " + count + " cash flows");
	}
}
