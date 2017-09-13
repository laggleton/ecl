package ebrd.ecl;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.File;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import financialobjects.CashFlow;
import financialobjects.CashFlowType;
import financialobjects.Trade;
import referenceobjects.BusinessDate;
import referenceobjects.Currency;
import referenceobjects.DateFormat;
import referenceobjects.FxRate;
import referenceobjects.GrossDomesticProduct;
import referenceobjects.LossGivenDefault;
import referenceobjects.ProbabilityOfDefault;
import referenceobjects.Rating;
import referenceobjects.Scenario;
import referenceobjects.stores.CountryStore;
import referenceobjects.stores.FxRateStore;
import referenceobjects.stores.RatingStore;
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
			
			eurProvision += t.getProvisionEUR();
		}
		
		l.info("For " + TradeStore.getInstance().getSize() + " trades - total EUR provision is: " + NumberFormat.getInstance().format(eurProvision));
	}

	public void printResults() {
		try {
			
			Date d = new Date();
			
			File file = new File(ps.getPreference(PreferencesStore.DIRECTORY) + "ecl_ebrd-" + DateFormat.OUTPUT_FORMAT.format(d) + ".csv");
	      
			// creates the file
			file.createNewFile();
      
			// creates a FileWriter Object
			FileWriter writer = new FileWriter(file); 
      
			// Writes the content to the file
			writer.write("ContractRef, CCY, 12MECL, LifetimeECL, Stage, 12MECLEUR, LifetimeECLEUR, ProvisionEUR\n"); 
	
			for (Trade t : TradeStore.getInstance().getAllTrades()) {
				writer.write(t.getECLResults() + "\n");
			}
		
			writer.flush();
			writer.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
