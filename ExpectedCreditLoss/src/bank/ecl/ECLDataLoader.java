package bank.ecl;

import java.io.FileWriter;
import java.io.IOException;

import java.io.File;
import java.text.NumberFormat;
import java.util.Date;

import financialobjects.CashFlow;
import financialobjects.ECLIntermediateResult;
import financialobjects.ECLResult;
import financialobjects.Trade;
import referenceobjects.DateFormat;
import referenceobjects.Scenario;
import referenceobjects.stores.TradeStore;
import utilities.DateTimeUtils;
import utilities.Logger;
import utilities.PreferencesStore;

public class ECLDataLoader {
	
	private PreferencesStore ps = PreferencesStore.getInstance();
	
	private Logger l = Logger.getInstance();
	
	public void loadData() {
		BusinessDateLoader.loadAsOfDateFromPreferences();
		FXRateLoader.loadFXRates();
		RatingLoader.loadRatings();
		CountryLoader.loadCountries();		
		TradeLoader.loadTrades();
		CashFlowLoader.loadCFs();
	}
	
	
	public void calculateECL() {
		Scenario s = new Scenario(1.0d);
		Double eurProvision = 0d;
		
		for (Trade t : TradeStore.getInstance().getAllTrades()) {
			t.calculateECL(s, DateTimeUtils.MONTHLY);
			t.assessIFRS9Staging();
			
			eurProvision += t.getECLResult().getProvisionEUR();
		}
		
		l.info("For " + TradeStore.getInstance().getSize() + " trades - total EUR provision is: " + NumberFormat.getInstance().format(eurProvision));
	}
	
	public void printEURResults() {
		try {
			
			Date d = new Date();
			String delimiter = ",";
			
			File file = new File(ps.getPreference(PreferencesStore.DIRECTORY) + "ecl_bank-" + DateFormat.OUTPUT_FORMAT.format(d) + ".ECLResult");
	      
			// creates the file
			file.createNewFile();
      
			// creates a FileWriter Object
			FileWriter writer = new FileWriter(file); 
			writer.write("ContractRef, CCY, 12MECL, LifetimeECL, Stage, 12MECLEUR, LifetimeECLEUR, ProvisionEUR\n");
			
			for (Trade t : TradeStore.getInstance().getAllTrades()) {
				writer.write(t.getECLResult().toString(delimiter) + "\n");
			}
		
			writer.flush();
			writer.close();
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
			
			if (printIntermediateResults) {
				printIntermediateResults();				
			}
		}
		catch (IOException e) {
			l.error(e);
		}
		
	}
	
	public void printIntermediateResults() {
		String delimiter = ps.getPreference(PreferencesStore.OUTPUT_DELIMITER);
		try {
			Date d = new Date();
			
			File intermediateFile = new File(ps.getPreference(PreferencesStore.DIRECTORY) + "ecl_bank-intermediate-" + DateFormat.OUTPUT_FORMAT.format(d) + ".csv");
		      
			// creates the file
			intermediateFile.createNewFile();
      
			// creates a FileWriter Object
			FileWriter intermediateWriter = new FileWriter(intermediateFile); 
      
			// Writes the header to the file
			intermediateWriter.write(Trade.getPrimaryKeyHeader(delimiter));
			intermediateWriter.write(ECLIntermediateResult.getHeader(delimiter));
			
			String tradeDecorator = "";
			
			// Loops through trades and intermediate results 
			for (Trade t : TradeStore.getInstance().getAllTrades()) {
				tradeDecorator = t.getPrimaryKeyDecorator(delimiter);
				for (ECLIntermediateResult e : t.getIntermediateECLResults()) {
					intermediateWriter.write(tradeDecorator + e.toString(delimiter));
				}
			}
		
			intermediateWriter.flush();
			intermediateWriter.close();
		}
		catch (IOException e) {
			l.error(e);
		}
	}
	
	public void printCashFlows() {
		String delimiter = ps.getPreference(PreferencesStore.OUTPUT_DELIMITER);
		try {
			Date d = new Date();
			
			File cashFlowFile = new File(ps.getPreference(PreferencesStore.DIRECTORY) + "ecl_bank-cashflows-" + DateFormat.OUTPUT_FORMAT.format(d) + ".csv");
		      
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
}
