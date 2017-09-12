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
	
	private static final SimpleDateFormat DMY_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
	private static final SimpleDateFormat ISO_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private static final SimpleDateFormat OUTPUT_FORMAT = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
	
	private PreferencesStore ps = PreferencesStore.getInstance();
	
	private Logger l = Logger.getInstance();
	
	public void loadData() {
		setUpPDs();
		setUpGDPs();
		
		loadTrades();
		loadCFs();
	}
	
	/*
	 * PD file structure is tab separated:
	 * Observation date
	 * Maturity
	 * Value
	 * Rating reference
	 */
	public void setUpPDs() {
		String line;
		String[] lineArray;
		boolean first = true;
		ProbabilityOfDefault pd;
		Integer year;
		Double value;
		String rating;
		
		Calendar gc = GregorianCalendar.getInstance();
		FileReader fr = null;
		BufferedReader sc = null;

		Date asOfDate = BusinessDate.getInstance().getDate();
		
		RatingStore ratStore = RatingStore.getInstance();
				
		try {
			
			fr = new FileReader(ps.getPreference(PreferencesStore.DIRECTORY) + ps.getPreference(PreferencesStore.PD_FILE));
			sc = new BufferedReader(fr);
			
			while ((line = sc.readLine()) != null) {
				if (first) {
					first = false;
					line = sc.readLine();
				}
				
				lineArray = line.split("\t");
				year = intMe(lineArray[1].replaceAll("y",  ""));
				value = doubleMe(lineArray[2]);
				rating = lineArray[3];
				
				gc.setTime(asOfDate);
				gc.add(Calendar.YEAR, year);
					
				pd = new ProbabilityOfDefault(rating, gc.getTime(), value);
				ratStore.getRating(rating).addPD(pd);
			}
		}
		catch (Exception fe) {
			fe.printStackTrace();
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
		
		l.info("Loaded " + ratStore.getSize() + " ratings");
	}
	
	private void printPDs() {
		for (Rating r : RatingStore.getInstance().getAllRatings()) {
			List<ProbabilityOfDefault> pds = r.getPDs();
			for (ProbabilityOfDefault pd : pds) {
				l.info("PD Rating " + pd.getRating() + ", Date " + pd.getDate() + ", pd  " + pd.getPD());
			}
		}
	}
	
	/*
	 * GDP file structure is tab separated:
	 * Country
	 * Year
	 * Value
	 */
	public void setUpGDPs() {
		String line = "";
		String[] lineArray;
		boolean first = true;
		GrossDomesticProduct gdp;
		String country;
		Integer year;
		Double value;
		Double oneYearGrowth;
		
		CountryStore ctryStore = CountryStore.getInstance();
				
		FileReader fr = null;
		BufferedReader sc = null;
		
		try {
			
			fr = new FileReader(ps.getPreference(PreferencesStore.DIRECTORY) + ps.getPreference(PreferencesStore.GDP_FILE));
			sc = new BufferedReader(fr);
			
			while ((line = sc.readLine()) != null) {
								
				if (first) {
					first = false;
					line = sc.readLine();
				}
				
				lineArray = line.split("\t");
				
				country = lineArray[0];
				year = intMe(lineArray[1]);
				value = doubleMe(lineArray[2]);
				
				try {
					oneYearGrowth = doubleMe(lineArray[3]);
				}
				catch (ArrayIndexOutOfBoundsException e) {
					oneYearGrowth = null;
				}
					
				gdp = new GrossDomesticProduct(year);
				gdp.setActualGrowth(value);
				gdp.setOneYearPredictedGrowth(oneYearGrowth);
				
				ctryStore.getCountry(country).addGdp(gdp);
			}
		}
		catch (Exception fe) {
			fe.printStackTrace();
			ps.printAll();
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
		l.info("Loaded " + CountryStore.getInstance().getSize() + " countries");
	}
	
	
	/*
	 * Trade file structure is tab separated:
	 * ContractReference
	 * BookID
	 * BalanceSheetDate
	 * IsImpaired
	 * DatesPastDue
	 * OverallPDRating
	 * CountryOfRisk
	 * OverallLGDRating
	 * WatchList
	 * InitialRiskRating
	 * CurrentRiskRating
	 * LastDayInStage2
	 * LastDayInStage3
	 * SovereignRiskType
	 * ActivitySector
	 * FacilityCommitmentAmount
	 * LastFixingInterestRate
	 * DrlFlag
	 * DPDCategory
	 * OverdueCashFlowType
	 * AccountType
	 * AccountNature
	 * ContractStructure
	 * HoldingPartyReference
	 * Currency
	 * MaturityDate
	 * OriginDate
	 * Principal
	 * SpecificProductClass
	 * AmortizationType
	 * InterestMethod
	 * Basis
	 * BusinessRollDayConvention
	 * CompoundedPeriod
	 * CompoundingBreakingDate
	 * InterestBreakingDate
	 * InterestCapitalized
	 * InterestPaymentDetermination
	 * InterestPaymentTiming
	 * InterestPeriodicity
	 * RateCap
	 * RateFloor
	 * RollConvention
	 * LastAvailabilityDate
	 */
	public void loadTrades() {
		String line = "";
		String[] lineArray;
		boolean first = true;
		Trade t;
		
		String	contractReference;
		String	bookID;
		Date	balanceSheetDate;
		String	isImpaired;
		Integer	daysPastDue;
		String	overallPDRating;
		String	countryOfRisk;
		Double	overallLGDRating;
		Integer	watchList;
		String	initialRiskRating;
		String	currentRiskRating;
		Integer	lastDayInStage2;
		Integer	lastDayInStage3;
		String	sovereignRiskType;
		String	activitySector;
		Double	facilityCommitmentAmount;
		Double	lastFixingInterestRate;
		String	drlFlag;
		String	DPDCategory;
		String	overdueCashFlowType;
		String	accountType;
		String	accountNature;
		String	contractStructure;
		String	holdingPartyReference;
		String	currency;
		Date	maturityDate;
		Date	originDate;
		Double	principal;
		String	specificProductClass;
		String	amortizationType;
		String	interestMethod;
		String	basis;
		String	businessRollDayConvention;
		String	compoundedPeriod;
		Date	compoundingBreakingDate;
		Date	interestBreakingDate;
		Double	interestCapitalized;
		String	interestPaymentDetermination;
		String	interestPaymentTiming;
		String	interestPeriodicity;
		String	rateCap;
		String	rateFloor;
		String	rollConvention;
		Date	lastAvailabilityDate;
		Double	effectiveYield;

		FileReader fr = null;
		BufferedReader sc = null;
		
		Date asOfDate = BusinessDate.getInstance().getDate();
		RatingStore ratStore = RatingStore.getInstance();
		TradeStore tradeStore = TradeStore.getInstance();
		
		try {
			
			fr = new FileReader(ps.getPreference(PreferencesStore.DIRECTORY) + ps.getPreference(PreferencesStore.CONTRACT_FILE));
			sc = new BufferedReader(fr);
			
			while ((line = sc.readLine()) != null) {
								
				if (first) {
					first = false;
					line = sc.readLine();
				}
				
				lineArray = line.split("\t");
				
				contractReference = lineArray[0];
				bookID = lineArray[1];
				balanceSheetDate = dateMe(lineArray[2], DMY_FORMAT);
				isImpaired = lineArray[3];
				daysPastDue = intMe(lineArray[4]);
				overallPDRating = lineArray[5];
				countryOfRisk = lineArray[6];
				overallLGDRating = doubleMe(lineArray[7]);
				watchList = intMe(lineArray[8]);
				initialRiskRating = lineArray[9];
				currentRiskRating = lineArray[10];
				lastDayInStage2 = intMe(lineArray[11]);
				lastDayInStage3 = intMe(lineArray[12]);
				sovereignRiskType = lineArray[13];
				activitySector = lineArray[14];
				facilityCommitmentAmount = doubleMe(lineArray[15]);
				lastFixingInterestRate = doubleMe(lineArray[16]);
				drlFlag = lineArray[17];
				DPDCategory = lineArray[18];
				overdueCashFlowType = lineArray[19];
				accountType = lineArray[20];
				accountNature = lineArray[21];
				contractStructure = lineArray[22];
				holdingPartyReference = lineArray[23];
				currency = lineArray[24];
				maturityDate = dateMe(lineArray[25], DMY_FORMAT);
				originDate = dateMe(lineArray[26], DMY_FORMAT);
				principal = doubleMe(lineArray[27]);
				specificProductClass = lineArray[28];
				amortizationType = lineArray[29];
				interestMethod = lineArray[30];
				basis = lineArray[31];
				businessRollDayConvention = lineArray[32];
				compoundedPeriod = lineArray[33];
				compoundingBreakingDate = dateMe(lineArray[34], DMY_FORMAT);
				interestBreakingDate = dateMe(lineArray[35], DMY_FORMAT);
				interestCapitalized = doubleMe(lineArray[36]);
				interestPaymentDetermination = lineArray[37];
				interestPaymentTiming = lineArray[38];
				interestPeriodicity = lineArray[39];
				rateCap = lineArray[40];
				rateFloor = lineArray[41];
				rollConvention = lineArray[42];
				lastAvailabilityDate = dateMe(lineArray[43], DMY_FORMAT);
				effectiveYield = doubleMe(lineArray[44]);
			
				t = new Trade(contractReference, contractReference, bookID, contractReference, principal.intValue(), currency);
				
				t.setEIR(effectiveYield);
				t.setCountry(CountryStore.getInstance().getCountry(countryOfRisk));
				t.setCreditRating(overallPDRating);
				
				t.setInitialCreditRating(initialRiskRating);
				t.setRating(ratStore.getRating(overallPDRating));
				t.setAsOfDate(asOfDate);
				t.setMaturityDate(maturityDate);
				
				LossGivenDefault lgd = new LossGivenDefault(asOfDate, overallLGDRating);
				List<LossGivenDefault> lgds = new ArrayList<>();
				lgds.add(lgd);
				t.setLgds(lgds);
				
				tradeStore.addTrade(t);
			}
		}
		catch (Exception fe) {
			fe.printStackTrace();
			System.out.println(line);
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
		
		l.info("Loaded " + tradeStore.getSize() + " trades");
		
	}
	
	/*
	 * CF file structure is tab separated:
	 * Observation date
	 * Maturity
	 * Value
	 * Rating reference
	 */
	public void loadCFs() {
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
				balanceSheetDate = dateMe(lineArray[5], ISO_FORMAT);
				cashFlowType = lineArray[6];
				cashFlowSubType = lineArray[7];
				currency = lineArray[8];
				amount = doubleMe(lineArray[9]);
				
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
			
			File file = new File(ps.getPreference(PreferencesStore.DIRECTORY) + "ecl_ebrd-" + OUTPUT_FORMAT.format(d) + ".csv");
	      
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
	
	private Integer intMe(String s) {
		Integer i = new Integer(0);
		if (s != "") {
			try {
				i = new Integer(s);
			}
			catch (NumberFormatException nfe) {
				
			}
		}
		return i;
	}
	
	private Double doubleMe(String s) {
		Double i = new Double(0d);
		if (s != "") {
			try {
				i = new Double(s);
			}	
			catch (NumberFormatException nfe) {
			
			}
		}
		return i;
	}
	
	private Date dateMe(String s, SimpleDateFormat df) {
		Date d = BusinessDate.getInstance().getDate();
		if (s != "") {
			try {
				d = df.parse(s);
			}	
			catch (Exception pe) {
			
			}
		}
		return d;
	}

	public void loadFXRates() {
		String line;
		String[] lineArray;
		boolean first = true;
		Currency ccy;
		String ccyName;
		Date rateDate;
		Double rate;
				
		FileReader fr = null;
		BufferedReader sc = null;
				
		FxRateStore fxStore = FxRateStore.getInstance();
		FxRate fxRate;
		
		int count = 1;
		
		try {
			
			fr = new FileReader(ps.getPreference(PreferencesStore.DIRECTORY) + ps.getPreference(PreferencesStore.FX_FILE));
			sc = new BufferedReader(fr);
			
			while ((line = sc.readLine()) != null) {
								
				if (first) {
					first = false;
					line = sc.readLine();
				}
				
				lineArray = line.split(",");
				
				ccyName = lineArray[0];
				rateDate = dateMe(lineArray[1], ISO_FORMAT);
				rate = doubleMe(lineArray[2]);
				
				fxRate = new FxRate(rateDate, rate);
				
				ccy = fxStore.getCurrency(ccyName);
				ccy.addFxRate(fxRate);
				count++;
			}
		}
		catch (Exception fe) {
			fe.printStackTrace();
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
		
		l.info("Loaded " + count + " FX Rates");
	}

	public void loadAsOfDate() {
		//TODO load from file
		try {
			BusinessDate.getInstance().initialise(DMY_FORMAT.parse("31/05/2017"));
		}
		catch (ParseException pe) {
			l.error("Failed to parse business date");
			pe.printStackTrace();
		}
		
	}
	
}
