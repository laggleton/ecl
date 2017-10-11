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

public class FullDataLoader {
	
	private PreferencesStore ps = PreferencesStore.getInstance();
	
	private Logger l = Logger.getInstance();
	
	public void loadDataForECL() {
		BusinessDateLoader.loadAsOfDateFromPreferences();
		FXRateLoader.loadFXRates();
		RatingLoader.loadRatings();
		CountryLoader.loadCountries();		
		TradeLoader.loadTrades(true); // Expects and loads EIR from file
		CashFlowLoader.loadCFs();
	}
	
	public void loadDataForFullRun() {
		BusinessDateLoader.loadAsOfDateFromPreferences();
		FXRateLoader.loadFXRates();
		RatingLoader.loadRatings();
		CountryLoader.loadCountries();		
		TradeLoader.loadTrades(false); // Does not expect or load EIR from trade file
		CashFlowLoader.loadCFs();
		FeeLoader.loadFees();
	}
	
	public void generateExpenses() {
		for (Trade t : TradeStore.getInstance().getAllTrades()) {
			t.generateExpensesCashFlow();
		}
	}
	
	public void generateDisbursements() {
		for (Trade t : TradeStore.getInstance().getAllTrades()) {
			t.generateDisbursementCashFlows();
		}
	}
	
	public void applyCancellations() {
		for (Trade t : TradeStore.getInstance().getAllTrades()) {
			t.applyCancellations();
		}
	}
	
	public void calculatePrepayments() {
		for (Trade t : TradeStore.getInstance().getAllTrades()) {
			t.calculatePrepayments();
		}
	}
	
	public void calculateInterest() {
		for (Trade t : TradeStore.getInstance().getAllTrades()) {
			t.calculateInterest();
		}
	}
	
	public void calculateEIR() {
		for (Trade t : TradeStore.getInstance().getAllTrades()) {
			t.calculateEIR();
		}
	}
	
	public void calculateECL() {
		Scenario s = new Scenario(1.0d);
		Double eurProvision = 0d;
		
		//Trade t = TradeStore.getInstance().getTrade("11865_MARITZAIZT/16324");
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
				writer.write(t.getECLResult().toString(delimiter));
			}
		
			writer.flush();
			writer.close();
		}
		catch (IOException e) {
			l.error(e);
		}
	}
	
	public void printRatings() {
		RatingLoader rl = new RatingLoader();
		rl.printPDs();
	}
	
	public void printCountries() {
		CountryLoader.printAllCountries();
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
			intermediateWriter.write(ECLIntermediateResult.getFullHeader(delimiter));
			
			String tradeDecorator = "";
			
			// Loops through trades and intermediate results 
			for (Trade t : TradeStore.getInstance().getAllTrades()) {
				tradeDecorator = t.getPrimaryKeyDecorator(delimiter);
				List<ECLIntermediateResult> eList =  t.getIntermediateECLResults();
				Collections.sort(eList);
				for (int i = 0; i < eList.size(); i++) {
					ECLIntermediateResult e = eList.get(i);
					intermediateWriter.write(tradeDecorator + e.toFullString(delimiter, i+1));
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
	
	public void printStageReasons() {
		String delimiter = ps.getPreference(PreferencesStore.OUTPUT_DELIMITER);
		try {
			Date d = new Date();
			
			File stagingFile = new File(ps.getPreference(PreferencesStore.DIRECTORY) + "ecl_bank-stage-reasons-" + DateFormat.OUTPUT_FORMAT.format(d) + ".csv");
		      
			// creates the file
			stagingFile.createNewFile();
      
			// creates a FileWriter Object
			FileWriter stagingWriter = new FileWriter(stagingFile); 
      
			// Writes the header to the file
			stagingWriter.write(Trade.getPrimaryKeyHeader(delimiter));
			stagingWriter.write(Trade.getStagingHeader(delimiter) + "\n");
			
			// Loops through trades and cash flows 
			for (Trade t : TradeStore.getInstance().getAllTrades()) {
				stagingWriter.write(t.getPrimaryKeyDecorator(delimiter) + t.getStagingCriteria(delimiter) + "\n");
			}
		
			stagingWriter.flush();
			stagingWriter.close();
		}
		catch (IOException e) {
			l.error(e);
		}
	}
}
