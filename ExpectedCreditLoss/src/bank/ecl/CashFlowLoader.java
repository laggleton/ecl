package ebrd.ecl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import financialobjects.CashFlow;
import financialobjects.CashFlowType;
import referenceobjects.DateFormat;
import referenceobjects.stores.TradeStore;
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
		String paymentDate;
		Date balanceSheetDate;
		String cashFlowType;
		String cashFlowSubType;
		String currency;
		Double amount;
		String line = "";
		String[] lineArray;
		boolean first = true;
		
		List<String> missingTrades = new ArrayList<>();
		
		CashFlow cf;
				
		FileReader fr = null;
		BufferedReader sc = null;
		TradeStore tradeStore = TradeStore.getInstance();
		
		try {
			
			fr = new FileReader(ps.getPreference(PreferencesStore.DIRECTORY) + ps.getPreference(PreferencesStore.CASHFLOW_FILE));
			sc = new BufferedReader(fr);
			
			while ((line = sc.readLine()) != null) {
								
				if (first) {
					first = false;
					line = sc.readLine();
				}
				
				lineArray = line.split("\t");
				
				dealID = lineArray[0];
				facID = lineArray[1];
				bookID = lineArray[2];
				contractReference = lineArray[3];
				paymentDate = lineArray[4];
				balanceSheetDate = InputHandlers.dateMe(lineArray[5], DateFormat.ISO_FORMAT);
				cashFlowType = lineArray[6];
				cashFlowSubType = lineArray[7];
				currency = lineArray[8];
				amount = InputHandlers.doubleMe(lineArray[9]);
				
				CashFlowType cft = CashFlowType.DISBURSEMENT;
				
				if (cashFlowType.equals("INT")) { cft = CashFlowType.INTEREST; }
				else if (amount > 0) { cft = CashFlowType.REPAYMENT; }
				
				cf = new CashFlow(currency, amount, paymentDate, cft);
				
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
			try {
				if (sc != null) { sc.close(); }
				if (fr != null) { fr.close(); }
			} 
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		for (String t : missingTrades) {
			l.warn("No trade data for cash flows, contractref " + t);
		}
		l.warn("Missing trade data for " + missingTrades.size());
		
		l.info("Loaded " + count + " cash flows");
	}
}
