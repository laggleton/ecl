package bank.ecl;

import java.io.FileWriter;
import java.io.IOException;

import java.io.File;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import financialobjects.CashFlow;
import financialobjects.ECLIntermediateResult;
import financialobjects.ECLResult;
import financialobjects.Trade;
import financialobjects.stores.TradeStore;
import referenceobjects.DateFormat;
import referenceobjects.Scenario;
import utilities.DateTimeUtils;
import utilities.Logger;
import utilities.PreferencesStore;

public class EIRDataLoader {
	
	private PreferencesStore ps = PreferencesStore.getInstance();
	
	private Logger l = Logger.getInstance();
	
	public void loadData() {
		BusinessDateLoader.loadAsOfDateFromPreferences();
		FXRateLoader.loadFXRates();
		RatingLoader.loadRatings();
		CountryLoader.loadCountries();		
		EIRCashFlowLoader.loadCFs();
	}
	
	
	public void calculateEIR() {
		Double eir = 0d;
		
		//Trade t = TradeStore.getInstance().getTrade("11865_MARITZAIZT/16324");
		for (Trade t : TradeStore.getInstance().getAllTrades()) {
		
			t.setFirstDisbursementCurrency();
			//t.generateExpensesCashFlow();
			eir = t.calculateEIR();
			
			l.info(t.getPrimaryKeyDecorator(",") + eir.toString());
		}
	}
	
	public void printCashFlows() {
		String delimiter = ps.getPreference(PreferencesStore.OUTPUT_DELIMITER);
		try {
			Date d = new Date();
			
			File cashFlowFile = new File(ps.getPreference(PreferencesStore.DIRECTORY) + "eir_bank-cashflows-" + DateFormat.OUTPUT_FORMAT.format(d) + ".csv");
		      
			// creates the file
			cashFlowFile.createNewFile();
      
			// creates a FileWriter Object
			FileWriter cashFlowWriter = new FileWriter(cashFlowFile); 
      
			// Writes the header to the file
			cashFlowWriter.write(Trade.getPrimaryKeyHeader(delimiter));
			cashFlowWriter.write(CashFlow.getHeader(delimiter));
			
			String tradeDecorator = "";
			
			// Loops through trades and cash flows 
			for (Trade t : TradeStore.getInstance().getAllTrades()) {
				tradeDecorator = t.getPrimaryKeyDecorator(delimiter);
				for (CashFlow cf : t.getCashFlows()) {
					cashFlowWriter.write(tradeDecorator + cf.toString(delimiter));
				}
			}
		
			cashFlowWriter.flush();
			cashFlowWriter.close();
		}
		catch (IOException e) {
			l.error(e);
		}
	}
	
	

	public void printResults(boolean printIntermediateResults) {
		String delimiter = ps.getPreference(PreferencesStore.OUTPUT_DELIMITER);
		try {
			
			Date d = new Date();
					
			File file = new File(ps.getPreference(PreferencesStore.DIRECTORY) + "ecl_bank-" + DateFormat.OUTPUT_FORMAT.format(d) + ".csv");
	      
			// creates the file
			file.createNewFile();
      
			// creates a FileWriter Object
			FileWriter writer = new FileWriter(file); 
      
			// Writes the header to the file
			writer.write(ECLResult.getFullReportHeader(delimiter)); 
			
			// Writes for each trade to the file
			for (Trade t : TradeStore.getInstance().getAllTrades()) {
				writer.write(t.getECLResult().toFullReport(delimiter));
			}
		
			writer.flush();
			writer.close();

		}
		catch (IOException e) {
			l.error(e);
		}
		
	}
	
}
