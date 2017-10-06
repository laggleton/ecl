package bank.ecl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import financialobjects.Trade;
import financialobjects.stores.TradeStore;
import referenceobjects.BusinessDate;
import referenceobjects.DateFormat;
import referenceobjects.LossGivenDefault;
import referenceobjects.Rating;
import referenceobjects.stores.CountryStore;
import referenceobjects.stores.RatingStore;
import utilities.FileUtils;
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
		List<String> lineArray;
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

		Scanner scanner = null;
		
		String delimiter = "\t";
		
		Date asOfDate = BusinessDate.getInstance().getDate();
		RatingStore ratStore = RatingStore.getInstance();
		TradeStore tradeStore = TradeStore.getInstance();
		
		List<String> ctrctList = getInScopeContractRefs();
		
		if (null != ps.getPreference(PreferencesStore.CONTRACT_FILE_DELIMITER)) { delimiter =  ps.getPreference(PreferencesStore.CONTRACT_FILE_DELIMITER); }
		
		try {
			
			 scanner = new Scanner(new File(ps.getPreference(PreferencesStore.DIRECTORY) + ps.getPreference(PreferencesStore.CONTRACT_FILE)));
		     while (scanner.hasNext()) {
								
		    	line = scanner.nextLine();
				if (first) {
					first = false;
					line = scanner.nextLine();
				}
				lineArray = FileUtils.parseLine(line, delimiter);
					
				contractReference = lineArray.get(0);
				
				//if (!ctrctList.contains(contractReference)) { continue; }
				
				bookID = lineArray.get(1);
				balanceSheetDate = InputHandlers.dateMe(lineArray.get(2), DateFormat.ISO_FORMAT);
				isImpaired = lineArray.get(3);
				daysPastDue = InputHandlers.intMe(lineArray.get(4));
				overallPDRating = InputHandlers.cleanMe(lineArray.get(5));
				countryOfRisk = lineArray.get(6);
				overallLGDRating = InputHandlers.doubleMe(lineArray.get(7));
				watchList = InputHandlers.intMe(lineArray.get(8));
				initialRiskRating = InputHandlers.cleanMe(lineArray.get(9));
				currentRiskRating = InputHandlers.cleanMe(lineArray.get(10));
				lastDayInStage2 = InputHandlers.intMe(lineArray.get(11));
				lastDayInStage3 = InputHandlers.intMe(lineArray.get(12));
				sovereignRiskType = lineArray.get(13);
				activitySector = lineArray.get(14);
				facilityCommitmentAmount = InputHandlers.doubleMe(lineArray.get(15));
				lastFixingInterestRate = InputHandlers.doubleMe(lineArray.get(16));
				drlFlag = lineArray.get(17);
				DPDCategory = lineArray.get(18);
				overdueCashFlowType = lineArray.get(19);
				accountType = lineArray.get(20);
				accountNature = lineArray.get(21);
				contractStructure = lineArray.get(22);
				holdingPartyReference = lineArray.get(23);
				currency = lineArray.get(24);
				maturityDate = InputHandlers.dateMe(lineArray.get(25), DateFormat.ISO_FORMAT);
				originDate = InputHandlers.dateMe(lineArray.get(26), DateFormat.ISO_FORMAT);
				principal = InputHandlers.doubleMe(lineArray.get(27));
				specificProductClass = lineArray.get(28);
				amortizationType = lineArray.get(29);
				interestMethod = lineArray.get(30);
				basis = lineArray.get(31);
				businessRollDayConvention = lineArray.get(32);
				compoundedPeriod = lineArray.get(33);
				compoundingBreakingDate = InputHandlers.dateMe(lineArray.get(34), DateFormat.ISO_FORMAT);
				interestBreakingDate = InputHandlers.dateMe(lineArray.get(35), DateFormat.ISO_FORMAT);
				interestCapitalized = InputHandlers.doubleMe(lineArray.get(36));
				interestPaymentDetermination = lineArray.get(37);
				interestPaymentTiming = lineArray.get(38);
				interestPeriodicity = lineArray.get(39);
				rateCap = lineArray.get(40);
				rateFloor = lineArray.get(41);
				rollConvention = lineArray.get(42);
				lastAvailabilityDate = InputHandlers.dateMe(lineArray.get(43), DateFormat.ISO_FORMAT);
				effectiveYield = InputHandlers.doubleMe(lineArray.get(44));
				
				String[] split = contractReference.split("/");
				String dealId = split[0];
				String facilityId = split[1];
				
				//Adding book level granularity
				contractReference += "/" + bookID;
				
				t = new Trade(dealId, facilityId, bookID, contractReference, principal.intValue(), currency);
				
				t.setEIR(effectiveYield);
				t.setCountry(CountryStore.getInstance().getCountry(countryOfRisk));
				t.setFacilityCommitmentAmount(facilityCommitmentAmount);
				t.setWatchlist(watchList);
				t.setDrlFlag(drlFlag);
				
				/*
				 * Tweak the below for rating upgrade/downgrade
				 */
				//currentRiskRating = Rating.upgradeRating(currentRiskRating);
				//currentRiskRating = Rating.downgradeRating(currentRiskRating);
				
				//overallPDRating = Rating.upgradeRating(overallPDRating);
				//overallPDRating = Rating.downgradeRating(overallPDRating);
				//*/
				
				t.setCreditRating(currentRiskRating);
				t.setSovereignRiskType(sovereignRiskType);
				t.setInitialCreditRating(initialRiskRating);
				t.setRating(ratStore.getRating(overallPDRating));
				t.setAsOfDate(asOfDate);
				t.setMaturityDate(maturityDate);
				t.setSigningDate(originDate);
				t.setLastAvailabilityDate(lastAvailabilityDate);
				t.setIndustry(activitySector);
				
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
			if (null != scanner) {
				scanner.close();
			}
		}
		
		l.info("Loaded " + tradeStore.getSize() + " trades");
		
	}
	
	public static List<String> getInScopeContractRefs(){
		List<String> list = new ArrayList<>();
		list.add("1863_AZERBAREP/2090");
		list.add("34739_ANELIKBKYV/59198");
		list.add("35091_PROMSVYAZB/47410");
		list.add("35862/49203");
		list.add("37599_KYIVSKMETU/48255");
		list.add("39353_BKREPUBTBL/51493");
		list.add("41840_GRANDPLAZA/70632");
		list.add("42564_CAZINWASTE/57470");
		list.add("42944_DENIZBANK/58185");
		list.add("43375_KRERURKOS/60087");
		list.add("43375_KRERURKOS/60087_SF");
		list.add("43560_YUSTAT/60197");
		list.add("43973_SOCGENMARO/61857");
		list.add("44014_GRADSKOSRB/69219");
		list.add("44080_DUNDEE/60354");
		list.add("44166_ETILIK/60397");
		list.add("47200_STEREOLTD/66132");
		list.add("47229_KAZAVTOZHO/66193");
		list.add("47279_ALTERMODUS/66561");
		list.add("47296_BMILLCAZE/66482");
		list.add("47383_MVCARGOLCU/66604");
		list.add("47598_ZOHLY/70685");
		list.add("47704_NETBORSANT/67324");
		list.add("47968_FRUTAROMGB/67941");
		list.add("EBRD/EQUITIES/BK120/M6_45954");
		return list;
	}
	
	/*
	 * Assumes cash flows have been loaded
	 */
	public static void setFirstDisbursementCurrency() {
		Collection<Trade> tList = TradeStore.getInstance().getAllTrades();
		
		for (Trade t: tList) {
			t.setFirstDisbursementCurrency();
		}
	}
}
