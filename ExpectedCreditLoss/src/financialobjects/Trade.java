package financialobjects;
import java.util.*;
import java.util.concurrent.TimeUnit;


import referenceobjects.*;
import referenceobjects.Currency;
import referenceobjects.stores.FxRateStore;
import utilities.DateTimeUtils;
import utilities.Logger;

public class Trade {
	public Double getEIR() {
		return EIR;
	}

	private String tradeIdentifier;
	private String dealId;
	private String facilityId;
	private String bookId;
	
	private Integer notional;
	private Currency currency;
	

	private List<CashFlow> cfs = new ArrayList<>();
	private List<CashFlow> reversedCFs = new ArrayList<>();
	private List<LossGivenDefault> lgds = new ArrayList<>();
	private Double EIR = 0d; //used if EIR imported not calculated
	
	private Rating rating;
	private String creditRating; // should come from cpty or guarantor?
	private String initialCreditRating;
	private int watchlist = 0;
	private int daysPastDue = 0;
	private int daysInStage2 = 0;
	private int daysInStage3 = 0;
	private Country country;
	private Institution counterparty;
	private Institution guarantor;
	private Date maturityDate;
	private Date asOfDate;
	private boolean impaired = false;
	private String drlFlag;
	private String firstDisbursementCurrency;
	
	private Integer initialDirection = null;
	
	private ECLResult eclResult;
	private List<ECLIntermediateResult> eclIntermediateList = new ArrayList<>();
	private String sovereignRiskType;
	private Double facilityCommitmentAmount;
	private String stagingReason;
	private int ifrs9Stage;
	
	public Logger l = Logger.getInstance();
	private Date firstDisbursementDate = null;
	private Date lastCashFlowDate = null;
	private Date firstCashFlowDate = null;
	private Date signingDate;
	private Date lastAvailabilityDate;
	private String industry;
	
	public Double getFacilityCommitmentAmount() {
		return facilityCommitmentAmount;
	}

	public void setFacilityCommitmentAmount(Double facilityCommitmentAmount) {
		this.facilityCommitmentAmount = facilityCommitmentAmount;
	}
	
	public String getDrlFlag() {
		return drlFlag;
	}

	public void setDrlFlag(String drlFlag) {
		this.drlFlag = drlFlag;
	}

	public Trade(String instrumentId, String posId, String bookId, String tradeId, Integer tradeSize, String currency) {
		this.currency = new Currency(currency);
		this.notional = tradeSize;
		this.tradeIdentifier = tradeId;
		this.dealId = instrumentId;
		this.facilityId = posId;
		this.bookId = bookId;
		Position pos = new Position(instrumentId, posId, bookId);
		//TODO Position lookup
		pos.addTrade(this);
	}
	
	public List<CashFlow> getCashFlows() { return cfs; }
	
	public List<CashFlow> getFutureCashFlows() {
		List<CashFlow> futCFs = new ArrayList<>();
		for (CashFlow cf : cfs) {
			if (asOfDate.before(cf.getCashFlowDate())) {
				futCFs.add(cf);
			}
		}
		return futCFs;
	}
	
	public Double getTradeBalance(Date d) {
		Double balance = 0d;
		for (CashFlow cf : cfs) {
			if (cf.getCashFlowDate().before(d)) {
				if (cf.getCashFlowType().equals(CashFlowType.XNL)) {
					balance += cf.getTradeDisbursementAmount();
				}
			}
			else {
				break;
			}
		}
		return balance;
	}
	
	public List<CashFlow> getInterestCashFlows() {
		List<CashFlow> intCFs = new ArrayList<>();
		for (CashFlow cf : cfs) {
			if (cf.getCashFlowType().equals(CashFlowType.INT)) {
				intCFs.add(cf);
			}
		}
		return intCFs;
	}
	
	public int assessIFRS9Staging() {
		
		if (impaired) {
			ifrs9Stage = 3;
			stagingReason = "Impaired";
		}
		else if (creditRating == "8.0") {
			ifrs9Stage = 3;
			stagingReason = "8.0";
		}
		else if (daysPastDue > 90) {
			ifrs9Stage = 3;
			stagingReason = "90DPD";
		}
		else if ((daysInStage3 < 60) && (daysInStage3 > 0)) {
			ifrs9Stage = 3;
			stagingReason = "Probation";
		}
		else if (threeNotchDownGrade()) {
			ifrs9Stage = 2;
			stagingReason = "Three Notch Downgrade";
		}
		else if (watchlist >= 3) {
			ifrs9Stage = 2;
			stagingReason = "Watchlist";
		}
		else if (daysPastDue > 30) {
			ifrs9Stage = 2;
			stagingReason = "30DPD";
		}
		else if ((daysInStage2 < 60) && (daysInStage2 > 0)) {
			ifrs9Stage = 2;
			stagingReason = "Probation";
		}
		else if (getDrlFlag().equals("Y")) {
			ifrs9Stage = 2;
			stagingReason = "DRL";
		}
		else {
			ifrs9Stage = 1;
			stagingReason = "N/A";
		}
		return ifrs9Stage;
		
	}
	
	private boolean threeNotchDownGrade() {
		Double creditRating = new Double(this.creditRating);
		Double initialCreditRating = new Double(this.initialCreditRating);
		
		if ((initialCreditRating == 1.0d && creditRating > 2.0d)
				|| ((initialCreditRating != 0.0d) && (creditRating - initialCreditRating) >= 1.0d)) {
			return true;
		}
		return false;
	}
	
	public Double sumCashFlowTypes(CashFlowType type) {
		Double amount = new Double(0d);
		for (CashFlow cf : cfs) {
			if ((cf.getCashFlowType() == type) || (cf.getCashFlowSubType() == type)) { amount += cf.getTradeDisbursementAmount(); }
		}
		return amount;
	}
	
	public Double sumAllRepaymentsAndPrepayments() {
		Double reeps = new Double(0d);
		reeps += sumCashFlowTypes(CashFlowType.PREPAYMENT);
		reeps += sumCashFlowTypes(CashFlowType.REPAYMENT);
		return reeps;
	}
	
	public Double sumAllDisbursements() {
		Double disbs = new Double(0d);
		disbs += sumCashFlowTypes(CashFlowType.DISBURSEMENT);
		return disbs;
	}

	public Double sumAllRepaymentsAndPrepaymentsBefore(Date d) {
		Double amount = new Double(0d);
		for (CashFlow cf : cfs) {
			if (!cf.getCashFlowDate().after(d)) { 
				if ((cf.getCashFlowSubType().equals(CashFlowType.REPAYMENT))
						|| (cf.getCashFlowSubType().equals(CashFlowType.PREPAYMENT))) {
					amount += cf.getAmount(); 
				}
			}
		}
		return amount;
	}
	
	public Double sumAllRepaymentsAndPrepaymentsAfter(Date d) {
		Double amount = new Double(0d);
		for (CashFlow cf : cfs) {
			if (cf.getCashFlowDate().after(d)) { 
				if ((cf.getCashFlowSubType().equals(CashFlowType.REPAYMENT))
						|| (cf.getCashFlowSubType().equals(CashFlowType.PREPAYMENT))) {
					amount += cf.getAmount(); 
				}
			}
		}
		return amount;
	}
	
	public Double sumAllCashFlowsAfter(Date date) {
		Double amount = new Double(0d);
		for (CashFlow cf : cfs) {
			if (cf.getCashFlowDate().after(date)) { 
				amount += cf.getTradeDisbursementAmount(); 
			}
		}
		return amount;
	}
	
	public Double sumAllCashFlowsBefore(Date date) {
		Double amount = new Double(0d);
		for (CashFlow cf : cfs) {
			if (cf.getCashFlowDate().before(date)) { 
				amount += cf.getTradeDisbursementAmount();
			}
		}
		return amount;
	}
	
	
	public void calculateECL(List<Scenario> scenList) {
		Double totalWeight = new Double(0d);
		Double ecl = new Double(0d);
		Double totalECL= new Double(0d);
		
		for (Scenario s : scenList) {
			totalWeight += s.getWeight();
		}
		
		for (Scenario s : scenList) {
			calculateECL(s);
			totalECL += ((s.getWeight()/totalWeight) * eclResult.getTwelveMonthECL());
		}
	}
	
	/*
	 * Default period is quarterly
	 */
	public void calculateECL(Scenario s) {
		double periodLength = 91.3125d; //quarterly
		calculateECL(s, periodLength);
	}
	
	public boolean allCashFlowsBefore(Date d) {
		List<CashFlow> revCFs = new ArrayList<>(cfs);
		Collections.reverse(revCFs);
		
		if (revCFs.get(0).getCashFlowDate().before(d)) { return true; }
		return false;
	}
	
	public void calculateImpairedECL() {
		
		Double ead = calculateEAD(asOfDate, cfs, 0d);
		Double lgd = calculateLGD(lgds);
		Double ecl = ead * lgd;
		
		ECLIntermediateResult e = new ECLIntermediateResult(dealId, facilityId, bookId, tradeIdentifier, getFirstDisbursementCurrency());
		e.setPeriodDate(asOfDate);
		e.setDiscountFactor(1d);
		e.setProbabilityOfDefault(1d);
		e.setIncrementalPD(1d);
		e.setEad(ead);
		e.setPeriodECL(ecl);
		e.setLgd(lgd);
		eclIntermediateList.add(e);
		
		eclResult = new ECLResult(dealId, facilityId, bookId, tradeIdentifier, getFirstDisbursementCurrency());
		eclResult.setLifetimeECL(ecl);
		eclResult.setTwelveMonthECL(ecl);
		eclResult.setEir(0d);
		eclResult.setStagingReason(stagingReason);
		eclResult.setImpairmentStageIFRS9(3);
	}
	
	public void calculateECL(Scenario s, double periodLength) {
		
		if (maturityDate.before(asOfDate)) { 
			l.info("Maturity Date " + maturityDate.toString() + " before asOfDate " + asOfDate.toString() + " for trade " + getAbbreviatedPrimaryKeyDecorator(","));
		} //don't care about matured trades
		
		if (assessIFRS9Staging() == 3) {
			calculateImpairedECL();
			return;
		}
		if (cfs.isEmpty()) {
			l.info("No Cash Flows therefore no ECL calculated for " + getAbbreviatedPrimaryKeyDecorator(","));
			ECLIntermediateResult e = new ECLIntermediateResult(dealId, facilityId, bookId, tradeIdentifier, getFirstDisbursementCurrency());
			e.setPeriodDate(asOfDate);
			e.setDiscountFactor(1d);
			e.setProbabilityOfDefault(0d);
			e.setIncrementalPD(0d);
			e.setPeriodECL(0d);
			e.setEad(0d);
			e.setLgd(0d);
			eclIntermediateList.add(e);
			eclResult = new ECLResult(dealId, facilityId, bookId, tradeIdentifier, getFirstDisbursementCurrency());
			eclResult.setLifetimeECL(0d);
			eclResult.setTwelveMonthECL(0d);
			eclResult.setEir(0d);
			eclResult.setImpairmentStageIFRS9(assessIFRS9Staging());
			eclResult.setStagingReason(stagingReason);
			return;
		}
		
		if (allCashFlowsBefore(asOfDate)) { 
			l.info("All cash flows in past for " + getPrimaryKeyDecorator(","));
			ECLIntermediateResult e = new ECLIntermediateResult(dealId, facilityId, bookId, tradeIdentifier, getFirstDisbursementCurrency());
			e.setPeriodDate(asOfDate);
			e.setDiscountFactor(1d);
			e.setProbabilityOfDefault(0d);
			e.setIncrementalPD(0d);
			e.setPeriodECL(0d);
			e.setEad(0d);
			e.setLgd(0d);
			eclIntermediateList.add(e);
			eclResult = new ECLResult(dealId, facilityId, bookId, tradeIdentifier, getFirstDisbursementCurrency());
			eclResult.setLifetimeECL(0d);
			eclResult.setTwelveMonthECL(0d);
			eclResult.setEir(0d);
			eclResult.setImpairmentStageIFRS9(assessIFRS9Staging());
			eclResult.setStagingReason(stagingReason);
			return;
		}
		
		List<CashFlow> cfs = getCFs(s); 
		List<ProbabilityOfDefault> pds = getPDs(s);
		List<LossGivenDefault> lgds = getLGDs(s);
		Double eir = EIR;
		
		eclResult = new ECLResult(dealId, facilityId, bookId, tradeIdentifier, getFirstDisbursementCurrency());
				
		long diff = maturityDate.getTime() - asOfDate.getTime();
		long days =  TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
		int periods = (int) (days/periodLength);
		periods++;
		
		int daysFromAsOf;
		Double ead = new Double(0d);
		Double pd = new Double (0d);
		Double lastPD = new Double (0d);
		Double incrementalPD = new Double (0d);
		Double lgd = new Double (0d);
		Date periodEndDate;
		Date periodStartDate = asOfDate;
		Calendar gc;
		
		int periodsPerYear = 12; // Default to MONTHLY
		if (periodLength == DateTimeUtils.DAILY) { periodsPerYear = 365; }
		else if (periodLength == DateTimeUtils.QUARTERLY) { periodsPerYear = 4; }
	
		Double discountFactor = new Double(0d);
		Double periodECL = new Double(0d);
		Double lifetimeECL = new Double(0d);
		Double twelveMonthECL = new Double(0d);
		boolean twelveMonthECLCheck = true;
		gc = GregorianCalendar.getInstance();
		gc.setTime(asOfDate);
		gc.add(Calendar.YEAR, 1);
		
		for (int i = 1; i <= periods; i++) {
			
			gc.setTime(asOfDate);
			if (periodLength == DateTimeUtils.DAILY) {
				gc.add(Calendar.DATE, i);
			}
			else if (periodLength == DateTimeUtils.QUARTERLY) {
				gc.add(Calendar.MONTH, 3 * i);
			}
			else {
				gc.add(Calendar.MONTH, i);
			}
			
			periodEndDate = gc.getTime();
			
			if (periodEndDate.after(maturityDate)) { periodEndDate = maturityDate; }
			
			ECLIntermediateResult e = new ECLIntermediateResult(dealId, facilityId, bookId, tradeIdentifier, getFirstDisbursementCurrency());
			
			discountFactor = getDiscountFactor(asOfDate, periodStartDate, eir);
			
			if (periodStartDate.before(getFirstDisbursementDate())) {
				ead = 0d;
			}
			else if (getTradeBalance(periodStartDate) > -1d) {
				ead = 0d;
			}
			else {
				ead = calculateEAD(periodStartDate, cfs, eir);
			}
			
			
			pd = calculateAbsolutePD(asOfDate, periodEndDate, pds);
			incrementalPD = calculateIncrementalPD(lastPD, pd);
			lgd = calculateLGD(lgds);
			
			periodECL = discountFactor * ead * incrementalPD * lgd;
			
			e.setPeriodDate(periodEndDate);
			e.setEad(ead);
			e.setDiscountFactor(discountFactor);
			e.setProbabilityOfDefault(pd);
			e.setIncrementalPD(incrementalPD);
			e.setPeriodECL(periodECL);
			e.setLgd(lgd);
			
			eclIntermediateList.add(e);

			lifetimeECL += periodECL;
			
			if (twelveMonthECLCheck)  {
				
				if (i == periodsPerYear) { 
					twelveMonthECL = lifetimeECL;
					twelveMonthECLCheck = false;
				}
			}
			
			lastPD = pd;
			gc.add(Calendar.DATE, 1);
			periodStartDate = gc.getTime();
		}
		
		if (twelveMonthECL == 0d) { twelveMonthECL = lifetimeECL; }
		
		eclResult.setLifetimeECL(lifetimeECL);
		eclResult.setTwelveMonthECL(twelveMonthECL);
		eclResult.setEir(eir);
		eclResult.setImpairmentStageIFRS9(assessIFRS9Staging());
		eclResult.setStagingReason(stagingReason);
	}
	
	/*
	 * Following method is for a potential LGD curve
	 */
	private Double calculateLGD(Date lgdDate, List<LossGivenDefault> lgds) {
		Double loss = new Double(0d);
		Date thisDate;
		Double lastLoss = new Double(0d);
		Date lastDate = asOfDate;
		
		for (LossGivenDefault lgd: lgds) {
			if (lgd.getDate().after(lgdDate)) {
				loss = lgd.getLoss(); 
				thisDate = lgd.getDate();
				long periodDiff = thisDate.getTime() - lastDate.getTime();
				long lastDiff = thisDate.getTime() - lgdDate.getTime();
				
				return (((lastDiff/periodDiff) * loss) + (((periodDiff - lastDiff)/periodDiff) * lastLoss));
				
			}
			lastLoss = lgd.getLoss();
			lastDate = lgd.getDate();
		}
		
		// TODO Warn lgdDate beyond end of lgd curve
		return 0d;
	}
	
	/*
	 * This version of the method assumes a constant lgd
	 */
	private Double calculateLGD(List<LossGivenDefault> lgds) {
		
		for (LossGivenDefault lgd: lgds) {
			return lgd.getLoss(); 
		}
		
		// TODO Warn lgdDate beyond end of lgd curve
		return 0d;
	}

	public Double getDiscountFactor(Date startDate, Date endDate, double rate) {
		if (endDate.before(startDate)) {
			l.error("endDate " + endDate.toString() + " before startDate " + startDate.toString());
			return 1d;
		}
		long periodDiff = endDate.getTime() - startDate.getTime();
		int dayDiff = (int) (TimeUnit.DAYS.convert(periodDiff, TimeUnit.MILLISECONDS));
			
		return 1/(Math.pow((1d + rate),((double)dayDiff/365d)));
	}
	
	public Double calculateIncrementalPD(Double startPD, Double endPD) {
		return ((endPD - startPD)/(1-startPD));
	}
	
	/*
	private Double calculateEIR(List<CashFlow> cfs) {
		
		if (EIR != 0d) { return EIR; }
		
		//TODO this is a hack approximation - implement correctly
		//TODO revolvers???
		Double disbs = new Double(0d); //disbs
		Double iAndF = new Double(0d); //interest and fees etc.
		
		for (CashFlow cf : cfs) {
			if (cf.getCashFlowType() == CashFlowType.DISBURSEMENT) {
				disbs += cf.getAmount();
			}
			else if ((cf.getCashFlowType() == CashFlowType.EXPENSE)
					|| (cf.getCashFlowType() == CashFlowType.FEE)
					|| (cf.getCashFlowType() == CashFlowType.INTEREST)) {
				iAndF += cf.getAmount();
			}
				
		}
		
		try {
			return iAndF / disbs;
		}
		catch (Exception e) {
			System.out.println("Error in calculation " + e.toString());
		}
		return EIR;
	}*/
	
	public Double calculateEAD(Date eadDate, List<CashFlow> cfs, double eir) {
		
		Double ead = new Double(0d);
		Double cashFlowAmount = new Double(0d);
		Double discountedCashFlowAmount = new Double(0d);
				
		for (CashFlow cf : cfs) {
			if (cf.getCashFlowDate().after(eadDate)
					&& (cf.getCashFlowType().equals(CashFlowType.XNL)
							|| cf.getCashFlowType().equals(CashFlowType.INT))) { 
				cashFlowAmount = cf.getAmount();
				discountedCashFlowAmount = cashFlowAmount * getDiscountFactor(eadDate, cf.getCashFlowDate(), eir);
				ead += discountedCashFlowAmount;
				//System.out.println("EAD for " + eadDate.toString() + ", cashflow "+ cf.getCashFlowDate().toString() + ", amount " + cf.getAmount() + ", discountedCashFlowAmount " + discountedCashFlowAmount + ", ead " + ead);
			}
		}
		
		return ead;
	}
	
	private Double calculateAbsolutePD(Date asOfDate, Date pdDate, List<ProbabilityOfDefault> pds) {
		Double prob = new Double(0d);
		Date thisDate;
		Double lastProb = new Double(0d);
		Date lastDate = asOfDate;
		
		Collections.sort(pds);
		
		for (ProbabilityOfDefault pd : pds) {
			if (pd.getDate().after(pdDate)) {
				prob = pd.getPD(); 
				thisDate = pd.getDate(); // date of PD point
							
				long periodDiff  = thisDate.getTime() - lastDate.getTime(); //date diff between last period and this
				long lastDiff = thisDate.getTime() - pdDate.getTime(); //date diff between pd point and requested pd
				int periodDays = (int) (TimeUnit.DAYS.convert(periodDiff, TimeUnit.MILLISECONDS));
				int lastDiffDays = (int) (TimeUnit.DAYS.convert(lastDiff, TimeUnit.MILLISECONDS));
				
				double firstBit =  ((double)(periodDays - lastDiffDays)/(double)periodDays) * prob;
				double secondBit = ((double)lastDiffDays/(double)periodDays) * lastProb;
				
				
				return (((double)(periodDays - lastDiffDays)/(double)periodDays) * prob) + (((double)lastDiffDays/(double)periodDays) * lastProb);
				
			}
			lastProb = pd.getPD();
			lastDate = pd.getDate();
		}
		
		// TODO Warn pdDate beyond end of pd curve
		return 0d;
	}
	
	private List<CashFlow> getCFs(Scenario s) {
		//TODO for multiple scenarios
		Collections.sort(cfs);
		return cfs;
	}
	
	private List<CashFlow> getCFs() {
		Collections.sort(cfs);
		return cfs;
	}
	
	public String getFirstDisbursementCurrency() {
		if (firstDisbursementCurrency == null) { setFirstDisbursementCurrency(); } 
		return firstDisbursementCurrency;
	}
	
	public Date getFirstDisbursementDate() {
		if (firstDisbursementDate == null) {
			List<CashFlow> cfs = getCFs();
			
			for (CashFlow cf : cfs) {
				if (cf.getCashFlowType().equals(CashFlowType.DISBURSEMENT)) {
					firstDisbursementDate = cf.getCashFlowDate();
					return firstDisbursementDate;
				}
			}
			
			for (CashFlow cf : cfs) {
				firstDisbursementDate = cf.getCashFlowDate();
				return firstDisbursementDate;
			}
			firstDisbursementDate = getAsOfDate();
		}
		return firstDisbursementDate;
	}
	
	public Date getLastCashFlowDate() {
		if (lastCashFlowDate == null) {
			List<CashFlow> cfs = getCFs();
			CashFlow cf = cfs.get(cfs.size() - 1);
			lastCashFlowDate = cf.getCashFlowDate();
		}
		return lastCashFlowDate;
	}
	
	public Date getFirstCashFlowDate() {
		if (firstCashFlowDate == null) {
			List<CashFlow> cfs = getCFs();
			CashFlow cf = cfs.get(0);
			firstCashFlowDate = cf.getCashFlowDate();
		}
		return firstCashFlowDate;
	}
	
	public Integer getInitialDirection() {
		if (initialDirection == null) {
			List<CashFlow> cfs = getCFs();
			
			for (CashFlow cf : cfs) {
				if (null == cf.getCashFlowSubType()) { continue; }
				if (cf.getCashFlowSubType().equals(CashFlowType.DISBURSEMENT)) {
					initialDirection = new Integer(1);
					return initialDirection;
				}
				else if (cf.getCashFlowSubType().equals(CashFlowType.REPAYMENT)) {
					initialDirection = new Integer(-1);
					return initialDirection;
				}
			}
			if (initialDirection == null) {
				l.error("No Principal CashFlows for " + getAbbreviatedPrimaryKeyDecorator(","));
				initialDirection = 1;
			}
		}
		return initialDirection;
	}
	
	public void setFirstDisbursementCurrency() {
		List<CashFlow> cfs = getCFs();
		for (CashFlow cf : cfs) {
			if (null == cf.getCashFlowSubType()) { continue; }
			if (cf.getCashFlowSubType().equals(CashFlowType.DISBURSEMENT)) {
				firstDisbursementCurrency = cf.getCurrency();
				break;
			}
			else if (cf.getCashFlowSubType().equals(CashFlowType.PRINCIPAL) && (cf.getAmount() < 0)) {
				firstDisbursementCurrency = cf.getCurrency();
				break;
			}
		}
		if (null == firstDisbursementCurrency) {
			for (CashFlow cf : cfs) {
				firstDisbursementCurrency = cf.getCurrency();
				break;
			}
		}
		
		if (null == firstDisbursementCurrency) {
			l.info("No first disbursement currency, using trade currency for trade " + getAbbreviatedPrimaryKeyDecorator(","));
			firstDisbursementCurrency = getCurrency().getCurrencyName();
		}
		
		for (CashFlow cf : cfs) {
			cf.setTradeDisbursementCurrency(firstDisbursementCurrency);
		}
		
	}
	
	public String getTradeIdentifier() {
		return tradeIdentifier;
	}

	public void setTradeIdentifier(String tradeIdentifier) {
		this.tradeIdentifier = tradeIdentifier;
	}

	private List<ProbabilityOfDefault> getPDs(Scenario s) {
		//TODO for multiple scenarios
		
		Country ctry = getCountry();
		
		Double probGrowth = ctry.getProbabilityOfGrowth();
		Double probRecession = ctry.getProbabilityOfRecession();
		
		List<ProbabilityOfDefault> adjustedPDs = new ArrayList<>();
		
		for (ProbabilityOfDefault pd : rating.getPDs()) {
			ProbabilityOfDefault newPd = pd;
			newPd.setPD((probGrowth * pd.getGrowthPD()) + (probRecession * pd.getRecessionPD()));
			adjustedPDs.add(newPd);
		}
		
		Collections.sort(adjustedPDs);
		return adjustedPDs;
	}
	
	private List<LossGivenDefault> getLGDs(Scenario s) {
		//TODO for multiple scenarios
		return lgds;
	}
	
	public List<CashFlow> getCfs() {
		return cfs;
	}

	public void setCfs(List<CashFlow> cfs) {
		this.cfs = cfs;
	}
	
	public void addCF(CashFlow cf) {
		cfs.add(cf);
	}

	public List<LossGivenDefault> getLgds() {
		return lgds;
	}

	public void setLgds(List<LossGivenDefault> lgds) {
		this.lgds = lgds;
	}

	public void setEIR(Double eir) {
		this.EIR = eir;
	}

	public String getCreditRating() {
		return creditRating;
	}

	public String getInitialCreditRating() {
		return initialCreditRating;
	}

	public void setInitialCreditRating(String initialCreditRating) {
		this.initialCreditRating = initialCreditRating;
	}

	public int getWatchlist() {
		return watchlist;
	}

	public void setWatchlist(int watchlist) {
		this.watchlist = watchlist;
	}

	public int getDaysPastDue() {
		return daysPastDue;
	}

	public void setDaysPastDue(int daysPastDue) {
		this.daysPastDue = daysPastDue;
	}

	public int getDaysInStage2() {
		return daysInStage2;
	}

	public void setDaysInStage2(int daysInStage2) {
		this.daysInStage2 = daysInStage2;
	}

	public int getDaysInStage3() {
		return daysInStage3;
	}

	public void setDaysInStage3(int daysInStage3) {
		this.daysInStage3 = daysInStage3;
	}

	public boolean isImpaired() {
		return impaired;
	}

	public void setImpaired(boolean impaired) {
		this.impaired = impaired;
	}

	public void setCreditRating(String creditRating) {
		this.creditRating = creditRating;
	}

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	public Date getMaturityDate() {
		return maturityDate;
	}

	public void setMaturityDate(Date maturityDate) {
		this.maturityDate = maturityDate;
	}

	public Date getAsOfDate() {
		return asOfDate;
	}

	public void setAsOfDate(Date asOfDate) {
		this.asOfDate = asOfDate;
	}
	
	public Rating getRating() {
		return rating;
	}

	public void setRating(Rating rating) {
		this.rating = rating;
	}
	
	public void print() {
		System.out.println("Id: " + this.getTradeIdentifier() + ", country: " + this.getCountry() + ", rating: " + this.getCreditRating() + ", cfs:" + cfs.size());
		
	}
	
	public String toString() {
		return "Id: " + this.getTradeIdentifier() + ", country: " + this.getCountry() + ", rating: " + this.getCreditRating() + ", cfs:" + cfs.size();
		
	}
	
	public ECLResult getECLResult() {
		return eclResult;
	}

	public List<ECLIntermediateResult> getIntermediateECLResults() {
		Collections.sort(eclIntermediateList);
		return eclIntermediateList;
	}
	
	public String getPrimaryKeyDecorator(String delimiter) {
		return getDealId() 
			+ delimiter + getFacilityId()
			+ delimiter + getTradeIdentifier()
			+ delimiter + getBookId()
			+ delimiter + DateFormat.ISO_FORMAT.format(BusinessDate.getInstance().getDate())
			+ delimiter + DateFormat.ISO_FORMAT.format(new Date())
			+ delimiter;
	}
	
	public static String getPrimaryKeyHeader(String delimiter) {
		return "DealId" 
			+ delimiter + "FacilityId"
			+ delimiter + "ContractReference"
			+ delimiter + "BookId"
			+ delimiter + "BalanceSheetDate"
			+ delimiter + "RunDate"
			+ delimiter;
	}
	
	public String getAbbreviatedPrimaryKeyDecorator(String delimiter) {
		return getDealId() 
			+ delimiter + getFacilityId()
			+ delimiter + getTradeIdentifier()
			+ delimiter + getBookId()
			+ delimiter;
	}
	
	public static String getAbbreviatedPrimaryKeyHeader(String delimiter) {
		return "DealId" 
			+ delimiter + "FacilityId"
			+ delimiter + "ContractReference"
			+ delimiter + "BookId"
			+ delimiter;
	}

	public String getDealId() {
		return dealId;
	}

	public void setDealId(String dealId) {
		this.dealId = dealId;
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public String getBookId() {
		return bookId;
	}

	public void setBookId(String bookId) {
		this.bookId = bookId;
	}
	
	public void generateExpensesCashFlow() {
		String currency = getFirstDisbursementCurrency();
		Double facCommitmentAmount = getFacilityCommitmentAmount();
		String tradeCurrency = getCurrency().getCurrencyName();
		Date expensesDate = getFirstDisbursementDate();
		
		facCommitmentAmount /= FxRateStore.getInstance().getCurrency(tradeCurrency).getFxRate(asOfDate).getRate();
		facCommitmentAmount *= FxRateStore.getInstance().getCurrency("EUR").getFxRate(asOfDate).getRate();
		
		Double amount = 0d;
		if (sovereignRiskType.equals("N")) {
			amount = -5000d;
		}
		else {
			amount = -22000d;
			amount += (facCommitmentAmount / 1000000d) * -450d;
		}
		
		
		amount = amount * FxRateStore.getInstance().getCurrency("EUR").getFxRate(expensesDate).getRate() / FxRateStore.getInstance().getCurrency(currency).getFxRate(expensesDate).getRate();
		CashFlow cf = new CashFlow(currency, amount, expensesDate, "Expense", currency);
		cfs.add(cf);		
		Collections.sort(cfs);
	}

	public void setSovereignRiskType(String sovereignRiskType) {
		this.sovereignRiskType = sovereignRiskType;
		
	}
	
	public void generateDailyInterestAccrualCashFlows() {
		if (cfs.isEmpty()) { l.error("No cash flows loaded, cannot generate Daily Interest for Trade " + toString()); return; }
		
		String currency = getFirstDisbursementCurrency();
		Double couponFrequency = DateTimeUtils.QUARTERLY; // TODO hardcoded
		Double couponAmount = 100000d; // TODO hardcoded
		Double dailyInterest = 0d;
		Calendar gc = new GregorianCalendar();
		gc.setTime(getFirstDisbursementDate()); //Start date
		
		while (gc.getTime().before(maturityDate)) {
			gc.add(Calendar.DATE, 1);
			Date flowDate = gc.getTime();
			dailyInterest = -1d * couponAmount / couponFrequency;
			CashFlow cf = new CashFlow(currency, dailyInterest, flowDate, "INTEREST", currency);
			cfs.add(cf);
		}
		
		Collections.sort(cfs);
 	}
	
	public void generateDailyInterestAccruals() {
		if (cfs.isEmpty()) { l.error("No cash flows loaded, cannot generate Daily Interest for Trade " + toString()); return; }
		
		String currency = getFirstDisbursementCurrency();
		Date startDate = getFirstDisbursementDate();
		Date endDate = getLastCashFlowDate();
		Date flowDate = endDate;

		Double dailyInterest = 0d;
		Calendar gc = new GregorianCalendar();
		gc.setTime(endDate);
		String tradeDisbursementCurrency = getFirstDisbursementCurrency();

		List<CashFlow> intCFs = getInterestCashFlows();
		Collections.reverse(intCFs);
		CashFlow intCF = intCFs.remove(0);
		CashFlow newIntCF;
		Date intFlowDate = intCF.getCashFlowDate();
		Date newFlowDate;
		long dayDiff;
		int count = 0;
		while (gc.getTime().after(startDate)) {
			if (flowDate.equals(intFlowDate)) {
				if (!intCFs.isEmpty()) {
					newIntCF = intCFs.remove(0);
					newFlowDate = newIntCF.getCashFlowDate();
				}
				else {
					newFlowDate = startDate;
					newIntCF = intCF;
				}
				
				dayDiff = DateTimeUtils.getDateDiff(newFlowDate,intFlowDate, TimeUnit.DAYS);
				dailyInterest = -1d * intCF.getTradeDisbursementAmount() / dayDiff;
				//System.out.println("Flow date" + DateFormat.ISO_FORMAT.format(flowDate) + ", New CF date " + DateFormat.ISO_FORMAT.format(newFlowDate) + ", previous CF date " + DateFormat.ISO_FORMAT.format(intFlowDate) + ", dayDiff " + dayDiff + "dailyInterest " + dailyInterest);
				intCF = newIntCF;
				intFlowDate = newIntCF.getCashFlowDate();
					
			}
			gc.add(Calendar.DATE, -1);
			flowDate = gc.getTime();
			CashFlow cf = new CashFlow(currency, dailyInterest, flowDate, "INTEREST", currency);
			cfs.add(cf);
			count++;
		}
		
		Collections.sort(cfs);
		
 	}
	
	public void calculateRepayments() {
		Double allDisbs = sumAllDisbursements();
		Double reepsToDate = sumAllRepaymentsAndPrepaymentsBefore(asOfDate);
		Double toBeRepayed = (-1d * (allDisbs)) - reepsToDate;
		Double futureReeps = sumAllRepaymentsAndPrepaymentsAfter(asOfDate);
		Double scalingRatio = toBeRepayed / futureReeps;
		String currency = getFirstDisbursementCurrency();
		
		// If no future repayment schedule, create a bullet repayment at maturity
		if (futureReeps.equals(0d)) {
			CashFlow cf = new CashFlow(currency, toBeRepayed, getMaturityDate(), "Repayment", currency);
			cfs.add(cf);
			return;
		}
		
		// Scale existing repayment schedule
		for (CashFlow cf : cfs) {
			if (cf.getCashFlowDate().after(asOfDate)) {
				if (cf.getCashFlowSubType().equals(CashFlowType.REPAYMENT)
						|| cf.getCashFlowSubType().equals(CashFlowType.PREPAYMENT)) {
					cf.setAmount(cf.getAmount() * scalingRatio);
					cf.setTradeDisbursementAmount(cf.getTradeDisbursementAmount() * scalingRatio);
				}
			}
		}
		
	}
	
	public void applyCancellations() {
		Double cancellationRate = CancellationProfile.getCancellationRate(sovereignRiskType, industry); 
		Double scalingRatio = 1 - cancellationRate;
		Date lastCFdate = asOfDate;
		
		// Scale future disbursement schedule
		for (CashFlow cf : cfs) {
			if (cf.getCashFlowDate().after(asOfDate)) {
				if (cf.getCashFlowSubType().equals(CashFlowType.DISBURSEMENT)) {
					long dayDiff = DateTimeUtils.getDateDiff(lastCFdate, cf.getCashFlowDate(), TimeUnit.DAYS);
					double cfPrePaymentRate = (1d-(Math.pow(1d-scalingRatio,dayDiff/365d)));
					
					cf.setAmount(cf.getAmount() * cfPrePaymentRate);
					cf.setTradeDisbursementAmount(cf.getTradeDisbursementAmount() * cfPrePaymentRate);
					
					lastCFdate = cf.getCashFlowDate();
				}
			}
		}
		
		// Adjust repayments accordingly
		calculateRepayments();
	}
	
	public void calculatePrepayments() {
		Double prepaymentRate = PrepaymentProfile.getPrepaymentRate(country, sovereignRiskType, industry); 
		Double scalingRatio = prepaymentRate;
		List<CashFlow> preepCFs = new ArrayList<>();
		
		Double balance = getTradeBalance(asOfDate);
		Date lastCFdate = asOfDate;
		// Scale future disbursement schedule
		for (CashFlow cf : getCFs()) {
			if ((cf.getCashFlowDate().after(asOfDate)) && (cf.getCashFlowType().equals(CashFlowType.XNL))) {
				
				// If cash flow amount more than balance - set cash flow to balance value and flatten
				if (Math.abs(balance) < cf.getTradeDisbursementAmount()) {
					cf.setAmount(Math.abs(balance));
					cf.setTradeDisbursementAmount(Math.abs(balance));
					balance = 0d; continue;
				}
				
				long dayDiff = DateTimeUtils.getDateDiff(lastCFdate, cf.getCashFlowDate(), TimeUnit.DAYS);
				double cfPrePaymentRate = (1d-(Math.pow(1d-scalingRatio,dayDiff/365d)));
				
				//Create new cash flow
				CashFlow newCf = new CashFlow(cf.getCurrency(), Math.abs(balance * cfPrePaymentRate), cf.getCashFlowDate(), "Prepayment", cf.getTradeDisbursementCurrency());

				// Update balance
				balance += cf.getTradeDisbursementAmount();
				
				// If cash flow amount more than balance - set cash flow to balance value and flatten
				if (Math.abs(balance) < newCf.getTradeDisbursementAmount()) {
					newCf.setAmount(balance);
					newCf.setTradeDisbursementAmount(balance);
					balance = 0d;
				}
				else {
					balance += newCf.getTradeDisbursementAmount();
				}
				preepCFs.add(newCf);
				
				lastCFdate = cf.getCashFlowDate();
			}
		}
		
		for (CashFlow cf : preepCFs) {
			cfs.add(cf);
		}
		
		Collections.sort(cfs);

		// Remove any potential zero value cfs
		cleanZeroCashFlows();
	}
	
	public void cleanZeroCashFlows() {
		List<CashFlow> cleanCfs = new ArrayList<>(cfs);
		for (CashFlow cf : cleanCfs) {
			if (cf.getCashFlowDate().after(asOfDate)) {
				if (cf.getAmount().equals(0d) || cf.getTradeDisbursementAmount().equals(0d)) {
					cfs.remove(cf);
				}
			}
		}
	}
	
	
	public Double getAccruedInterestForDate(Date d) {
		Double accruedInterest = 0d;
		for (CashFlow cf : cfs) {
			if (cf.getCashFlowDate().after(d)) {
				break;
			}
			if (cf.getCashFlowType().equals(CashFlowType.INT)) {
				accruedInterest += cf.getTradeDisbursementAmount();
			}
		}
		return accruedInterest;
	}
	
	public Double calculateEIR() {
		if (cfs.isEmpty()) {
			l.error("Can't calculate EIR - no cash flows for " + getAbbreviatedPrimaryKeyDecorator(","));
			return 0d;
		}
		
		Double sumAllPrin = sumCashFlowTypes(CashFlowType.XNL);
		if ((-2d > sumAllPrin) || (2d < sumAllPrin)) {
			l.warn("Principals do not sum to zero for " +  getAbbreviatedPrimaryKeyDecorator(",") + sumAllPrin);
			return 0d;
		}
		
		Double amortisedCost = 1000d;
		Double eir = 0.03d;
		int count = 0;
		boolean first = true;
			
		Double step = 1.0d;
		int up = 1;
		int lastUp = 1;
		List<CashFlow> dailyCFList = aggregateDailyCashFlows();
		
		Date lastCashFlowDate = getLastCashFlowDate();
		Date firstCashFlowDate = getFirstCashFlowDate();
		Calendar gc = new GregorianCalendar();
		
		Integer initialDirection = getInitialDirection();
		
		long dayDiff = DateTimeUtils.getDateDiff(firstCashFlowDate, lastCashFlowDate, TimeUnit.DAYS);
		
		while ((amortisedCost > 0.1) || (amortisedCost < -0.1)) {
			
			List<CashFlow> thisCFList = new ArrayList<>(dailyCFList);
			CashFlow cf = thisCFList.remove(0);
			Date cfDate = cf.getCashFlowDate();
			gc.setTime(firstCashFlowDate);
			amortisedCost = 0d; 
			
			for (long i = 0; i <= dayDiff; i++) {
				if (gc.getTime().equals(cfDate)) {
					amortisedCost = cf.getTradeDisbursementAmount() + (amortisedCost * getDailyEIR(eir));
					if (!thisCFList.isEmpty()) {
						cf = thisCFList.remove(0);
					}
					cfDate = cf.getCashFlowDate();
				}
				else {
					amortisedCost = amortisedCost * getDailyEIR(eir);
				}
				gc.add(Calendar.DATE, 1);
			}
			if (count % 10 == 0) {
				if (count == 1000) {
					l.error(count + " EIR runs for " + getPrimaryKeyDecorator(",") + " amortisedCost = " + amortisedCost + ", eir = " + eir + ", not completing!");
					if (initialDirection.equals(getInitialDirection())) {
						l.error("Trying alternative direction");
						initialDirection *= -1;
						eir = 0.03d;
						count = 0;
					}
					else {
						eir = 0d;
						return eir;
					}
				}
				//l.info(count + " eir runs for " + getPrimaryKeyDecorator(",") + " amortisedCost = " + amortisedCost);
			}
			
			//l.info("Amortised cost = " + amortisedCost + ", eir = " + eir);
			lastUp = up;
			up = (amortisedCost > 0) ? 1 : -1;
			up *= initialDirection;
			if (first) { first = false; }
			else if (up != lastUp) { step /= 10d; }
			eir += (step * (double) up );
			count++;
		}
		
		if (eir < -1) { 
			l.error("Calculated eir is less that -1 - not possible : " + eir);
			eir = 0d;
		}
		
		this.EIR = eir;
		//l.info("Count to calculate eir = " + count);
		return eir;
	}
	
	private Double getDailyEIR(Double rate) {
		return  1d + (rate/365d); 
	}
	
	private List<CashFlow> aggregateDailyCashFlows() {
		List<CashFlow> returnCfs = new ArrayList<>();
		CashFlow previousCf = null;
		
		for (CashFlow cf : cfs) {
			if (null == previousCf) {
				previousCf = cf.clone();
				returnCfs.add(previousCf);
			}
			else {
				if (cf.getCashFlowDate().equals(previousCf.getCashFlowDate())) {
					Double previousCfAmount = previousCf.getTradeDisbursementAmount();
					Double currentAmt = cf.getTradeDisbursementAmount();
					previousCf.setTradeDisbursementAmount(previousCfAmount + currentAmt);
				}
				else {
					previousCf = cf.clone();
					returnCfs.add(previousCf);
				}
			}
			
		}
		Collections.sort(returnCfs);
		
		return returnCfs;
	}
	
	public Double calculateAmortisedCost(Double eir) {
		if (reversedCFs.isEmpty()) { return 0d; }
		CashFlow cf = reversedCFs.remove(0);
		if (cf.getCashFlowDate().equals(getFirstDisbursementDate())) {
			return cf.getAmount();
		}
		return cf.getAmount() + (calculateAmortisedCost(eir) * (getDailyEIR(eir)));
	}
	
	public Double sumAllCashFlows(List<CashFlow> cfs) {
		Double amount = 0d;
		for (CashFlow cf : cfs) {
			amount += cf.getTradeDisbursementAmount();
		}
		return amount;
	}
	
	public void generateDisbursementCashFlows() {
		
		if (sumAllDisbursements().equals(-1d * getFacilityCommitmentAmount())) {
			l.info("Loan " + getAbbreviatedPrimaryKeyDecorator(", ") + "is fully disbursed, no disbursements generated");
			return;
		}
		
		Date lastAvailabilityDate = getLastAvailabilityDate();
		if (!lastAvailabilityDate.after(asOfDate)) {
			l.info("Last Availability Date in past for " + getAbbreviatedPrimaryKeyDecorator(", ") + "no disbursements generated, asOfDate, lastAvailabilityDate " +
					DateFormat.ISO_FORMAT.format(asOfDate) + "," + DateFormat.ISO_FORMAT.format(lastAvailabilityDate));
			return;
		}
		
		Map<Integer, Double> probabilityProfile = DisbursementProbabilities.getProfile(getSovereignRiskType(), getIndustry());
		
		if (null == probabilityProfile) {
			l.error("No disbursements generated");
			return;
		}
		
		
		long dayDiff = DateTimeUtils.getDateDiff(getSigningDate(), lastAvailabilityDate, TimeUnit.DAYS);
		int bucketLength = (int) dayDiff/10;
		
		Calendar gc = new GregorianCalendar();
		gc.setTime(getSigningDate());
		Map<Integer, Date> dateMap = new HashMap<>();
		Date bucketDate = gc.getTime();
		if (bucketDate.after(asOfDate)) {
			dateMap.put(new Integer(1), bucketDate);
		}
		
		for (int i = 1; i <= 8; i++) {
			gc.add(Calendar.DATE, bucketLength);
			bucketDate = gc.getTime();
			if (bucketDate.after(asOfDate)) {
				dateMap.put(i + 1, bucketDate);
			}
		}
		
		dateMap.put(10, lastAvailabilityDate);
		
		double sumProbabilities = 0d;
		for (Integer i : dateMap.keySet()) {
			sumProbabilities += probabilityProfile.get(i);
		}
		
		String currency = getFirstDisbursementCurrency();
		Double outstandingDisbursements = getFacilityCommitmentAmount() + sumAllDisbursements();
		
		for (Integer i : dateMap.keySet()) {
			Double amount = -1d * outstandingDisbursements * probabilityProfile.get(i) / sumProbabilities;
			CashFlow cf = new CashFlow(currency, amount, dateMap.get(i), "Disbursement", currency);
			cfs.add(cf);
		}
		
		// Adjust repayments accordingly
		calculateRepayments();
		
		Collections.sort(cfs);
	}
	
	public void calculateInterest() {
		//TODO
	}
	
	public String getStagingReason() {
		return stagingReason;
	}

	public void setStagingReason(String stagingReason) {
		this.stagingReason = stagingReason;
	}
	
	public String getStagingCriteria(String delimiter) {
		return getRating().getRating()
				+ delimiter + getInitialCreditRating()
				+ delimiter + getCreditRating()
				+ delimiter + getWatchlist()
				+ delimiter + getDrlFlag()
				+ delimiter + assessIFRS9Staging()
				+ delimiter + getStagingReason() 
				;
	}
	
	public static String getStagingHeader(String delimiter) {
		return "OverallPDRating"
				+ delimiter + "InitialCreditRating"
				+ delimiter + "CurrentCreditRating"
				+ delimiter + "Watchlist"
				+ delimiter + "DrlFlag"
				+ delimiter + "Stage"
				+ delimiter + "StageReason"
				;
	}

	public void setSigningDate(Date d) {
		this.signingDate = d;
	}
	
	public Date getSigningDate() {
		return this.signingDate;
	}

	public void setLastAvailabilityDate(Date d) {
		this.lastAvailabilityDate = d;
	}
	
	public Date getLastAvailabilityDate() {
		return this.lastAvailabilityDate;
	}

	public void setIndustry(String activitySector) {
		this.industry = activitySector;
	}
	
	public String getIndustry() {
		return this.industry;
	}
	
	public String getSovereignRiskType() {
		return sovereignRiskType;
	}
	
	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}
	
}
