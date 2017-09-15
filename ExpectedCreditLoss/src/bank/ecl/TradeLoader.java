package bank.ecl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import financialobjects.Trade;
import referenceobjects.BusinessDate;
import referenceobjects.DateFormat;
import referenceobjects.LossGivenDefault;
import referenceobjects.stores.CountryStore;
import referenceobjects.stores.RatingStore;
import referenceobjects.stores.TradeStore;
import utilities.InputHandlers;
import utilities.Logger;
import utilities.PreferencesStore;

public class TradeLoader {
	private static Logger l = Logger.getInstance();
	private static PreferencesStore ps = PreferencesStore.getInstance();

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
	public static void loadTrades() {
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
				balanceSheetDate = InputHandlers.dateMe(lineArray[2], DateFormat.DMY_FORMAT);
				isImpaired = lineArray[3];
				daysPastDue = InputHandlers.intMe(lineArray[4]);
				overallPDRating = lineArray[5];
				countryOfRisk = lineArray[6];
				overallLGDRating = InputHandlers.doubleMe(lineArray[7]);
				watchList = InputHandlers.intMe(lineArray[8]);
				initialRiskRating = lineArray[9];
				currentRiskRating = lineArray[10];
				lastDayInStage2 = InputHandlers.intMe(lineArray[11]);
				lastDayInStage3 = InputHandlers.intMe(lineArray[12]);
				sovereignRiskType = lineArray[13];
				activitySector = lineArray[14];
				facilityCommitmentAmount = InputHandlers.doubleMe(lineArray[15]);
				lastFixingInterestRate = InputHandlers.doubleMe(lineArray[16]);
				drlFlag = lineArray[17];
				DPDCategory = lineArray[18];
				overdueCashFlowType = lineArray[19];
				accountType = lineArray[20];
				accountNature = lineArray[21];
				contractStructure = lineArray[22];
				holdingPartyReference = lineArray[23];
				currency = lineArray[24];
				maturityDate = InputHandlers.dateMe(lineArray[25], DateFormat.DMY_FORMAT);
				originDate = InputHandlers.dateMe(lineArray[26], DateFormat.DMY_FORMAT);
				principal = InputHandlers.doubleMe(lineArray[27]);
				specificProductClass = lineArray[28];
				amortizationType = lineArray[29];
				interestMethod = lineArray[30];
				basis = lineArray[31];
				businessRollDayConvention = lineArray[32];
				compoundedPeriod = lineArray[33];
				compoundingBreakingDate = InputHandlers.dateMe(lineArray[34], DateFormat.DMY_FORMAT);
				interestBreakingDate = InputHandlers.dateMe(lineArray[35], DateFormat.DMY_FORMAT);
				interestCapitalized = InputHandlers.doubleMe(lineArray[36]);
				interestPaymentDetermination = lineArray[37];
				interestPaymentTiming = lineArray[38];
				interestPeriodicity = lineArray[39];
				rateCap = lineArray[40];
				rateFloor = lineArray[41];
				rollConvention = lineArray[42];
				lastAvailabilityDate = InputHandlers.dateMe(lineArray[43], DateFormat.DMY_FORMAT);
				effectiveYield = InputHandlers.doubleMe(lineArray[44]);
				
				String[] split = contractReference.split("/");
				String dealId = split[0];
				String facilityId = split[1];
				
				t = new Trade(dealId, facilityId, bookID, contractReference, principal.intValue(), currency);
				
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
}
