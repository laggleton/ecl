package bank.ecl;

import java.io.FileWriter;
import java.io.IOException;

import java.io.File;
import java.text.NumberFormat;
import java.util.Date;
import financialobjects.ECLIntermediateResult;
import financialobjects.ECLResult;
import financialobjects.Trade;
import referenceobjects.DateFormat;
import referenceobjects.Scenario;
import referenceobjects.stores.TradeStore;
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
			t.calculateECL(s, Trade.MONTHLY);
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
			intermediateWriter.write(ECLIntermediateResult.getHeader(delimiter));
			
			// Loops through trades and intermediate results 
			for (Trade t : TradeStore.getInstance().getAllTrades()) {
				for (ECLIntermediateResult e : t.getIntermediateECLResults()) {
					intermediateWriter.write(e.toString(delimiter));
				}
			}
		
			intermediateWriter.flush();
			intermediateWriter.close();
		}
		catch (IOException e) {
			l.error(e);
		}
	}
}
