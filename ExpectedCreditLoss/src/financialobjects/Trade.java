package financialobjects;
import java.util.*;
import java.util.concurrent.TimeUnit;


import referenceobjects.*;
import referenceobjects.Currency;
import referenceobjects.stores.FxRateStore;

public class Trade {
	private String tradeIdentifier;
	private Integer notional;
	private Currency currency;
	private List<CashFlow> cfs = new ArrayList<>();
	private List<LossGivenDefault> lgds = new ArrayList<>();
	private double EIR = 0d; //used if EIR imported not calculated
	
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
	private Double twelveMonthECL;
	private Double lifetimeECL;
	private Double twelveMonthECLEUR;
	private Double lifetimeECLEUR;
	private int impairmentStageIFRS9 = 1;
	private boolean impaired = false;
	
	public static final double DAILY = 1.0d;
	public static final double MONTHLY = 30.4375d;
	public static final double QUARTERLY = 91.3125d;
	
	private String firstDisbursementCurrency;
	
	//public Logger log = new Logger();
	
	public Trade(String instrumentId, String posId, String bookId, String tradeId, Integer tradeSize, String currency) {
		this.currency = new Currency(currency);
		this.notional = tradeSize;
		this.tradeIdentifier = tradeId;
		Position pos = new Position(instrumentId, posId, bookId);
		//TODO Position lookup
		pos.addTrade(this);
	}
	
	private void convertToEUR() {
		double eurFx = FxRateStore.getInstance().getCurrency("EUR").getAsOfDateRate();
		double disbFx = FxRateStore.getInstance().getCurrency(getFirstDisbursementCurrency()).getAsOfDateRate();
		
		twelveMonthECLEUR = twelveMonthECL * disbFx / eurFx;
		lifetimeECLEUR = lifetimeECL * disbFx / eurFx;
	}
	
	public List<CashFlow> getCashFlows() { return cfs; }
	
	public void assessIFRS9Staging() {
				
		if ((impaired)|| (creditRating == "8.0") || (daysPastDue > 90) || ((daysInStage3 < 60) && (daysInStage3 > 0))) {
			impairmentStageIFRS9 = 3;
		}
		else if (threeNotchDownGrade() || (watchlist > 3) || (daysPastDue > 30) || ((daysInStage2 < 60) && (daysInStage2 > 0)) ) {
			impairmentStageIFRS9 = 2;
		}
		
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
			if (cf.getCashFlowType() == type) { amount += cf.getAmount(); }
		}
		return amount;
	}
	
	public Double sumAllRepaymentsAndPrepayments() {
		Double reeps = new Double(0d);
		reeps += sumCashFlowTypes(CashFlowType.PREPAYMENT);
		reeps += sumCashFlowTypes(CashFlowType.REPAYMENT);
		return reeps;
	}
	
	public Double sumAllCashFlowsAfter(Date date) {
		Double amount = new Double(0d);
		for (CashFlow cf : cfs) {
			if (cf.getCashFlowDate().after(date)) { 
				amount += cf.getAmount(); 
			}
		}
		return amount;
	}
	
	public Double sumAllCashFlowsBefore(Date date) {
		Double amount = new Double(0d);
		for (CashFlow cf : cfs) {
			if (cf.getCashFlowDate().before(date)) { 
				amount += cf.getAmount(); 
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
			totalECL += ((s.getWeight()/totalWeight) * twelveMonthECL);
		}
	}
	
	/*
	 * Default period is quarterly
	 */
	public void calculateECL(Scenario s) {
		double periodLength = 91.3125d; //quarterly
		calculateECL(s, periodLength);
	}
	
	
	public void calculateECL(Scenario s, double periodLength) {
		
		if (maturityDate.before(asOfDate)) { 
			System.out.println("Maturity Date " + maturityDate.toString() + " before asOfDate " + asOfDate.toString());
		} //don't care about matured trades
		
		List<CashFlow> cfs = getCFs(s); 
		List<ProbabilityOfDefault> pds = getPDs(s);
		List<LossGivenDefault> lgds = getLGDs(s);
		Double eir = calculateEIR(cfs);
		
		long diff = maturityDate.getTime() - asOfDate.getTime();
		long days =  TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
		int periods = (int) (days/periodLength);
		
		int daysFromAsOf;
		Double ead = new Double(0d);
		Double pd = new Double (0d);
		Double lastPD = new Double (0d);
		Double incrementalPD = new Double (0d);
		Double lgd = new Double (0d);
		Date periodDate;
		Calendar gc;
	
		Double discountFactor = new Double(0d);
		Double periodECL = new Double(0d);
		lifetimeECL = new Double(0d);
		twelveMonthECL = new Double(0d);
		boolean twelveMonthECLCheck = true;
		gc = GregorianCalendar.getInstance();
		gc.setTime(asOfDate);
		gc.add(Calendar.YEAR, 1);
		Date oneYearDate = gc.getTime();
		
		for (int i = 1; i <= periods; i++) {
			daysFromAsOf = (int) (i * periodLength);
			
			gc.setTime(asOfDate);
			gc.add(Calendar.DATE, daysFromAsOf);
			periodDate = gc.getTime();
			
			if (periodDate.after(maturityDate)) { periodDate = maturityDate; }
					
			discountFactor = getDiscountFactor(asOfDate, periodDate, eir);
			ead = calculateEAD(periodDate, cfs, eir);
			pd = calculateAbsolutePD(asOfDate, periodDate, pds);
			incrementalPD = calculateIncrementalPD(lastPD, pd);
			lgd = calculateLGD(lgds);
			
			periodECL = discountFactor * ead * incrementalPD * lgd;
			
			//System.out.println("Period " + i + ", Date " + gc.get(Calendar.YEAR) + gc.get(Calendar.MONTH) + gc.get(Calendar.DATE) + ", df " + discountFactor + ", ead " + ead + ", incrementalPD " + incrementalPD + ", lgd " + lgd + ", periodECL " + periodECL);
			lifetimeECL += periodECL;
			
			if (twelveMonthECLCheck)  {
				
				if (periodDate.equals(oneYearDate) || periodDate.after(oneYearDate)) { 
					twelveMonthECL = lifetimeECL;
					twelveMonthECLCheck = false;
				}
			}
			
			lastPD = pd;
		}
		
				
		if (twelveMonthECL == 0d) { twelveMonthECL = lifetimeECL; }
		
		convertToEUR();
	}
	
	public Double getTwelveMonthECLEUR() {
		return twelveMonthECLEUR;
	}

	public void setTwelveMonthECLEUR(Double twelveMonthECLEUR) {
		this.twelveMonthECLEUR = twelveMonthECLEUR;
	}

	public Double getLifetimeECLEUR() {
		return lifetimeECLEUR;
	}

	public void setLifetimeECLEUR(Double lifetimeECLEUR) {
		this.lifetimeECLEUR = lifetimeECLEUR;
	}

	/*
	 * Alternative methodology for calculating ECL
	 */
	public void calculateAlternativeECL(Scenario s, double periodLength) {
		
		if (maturityDate.before(asOfDate)) { 
			System.out.println("Maturity Date " + maturityDate.toString() + " before asOfDate " + asOfDate.toString());
		} //don't care about matured trades
		
		List<CashFlow> cfs = getCFs(s); 
		List<ProbabilityOfDefault> pds = getPDs(s);
		List<LossGivenDefault> lgds = getLGDs(s);
		Double eir = calculateEIR(cfs);
		
		long diff = maturityDate.getTime() - asOfDate.getTime();
		long days =  TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
		int periods = (int) (days/periodLength);
		
		int daysFromAsOf;
		Double ead = new Double(0d);
		Double pd = new Double (0d);
		Double lastPD = new Double (0d);
		Double incrementalPD = new Double (0d);
		Double lgd = new Double (0d);
		Date periodDate;
		Date startPeriodDate = asOfDate;
		Calendar gc;
	
		Double discountFactor = new Double(0d);
		Double periodECL = new Double(0d);
		lifetimeECL = new Double(0d);
		twelveMonthECL = new Double(0d);
		boolean twelveMonthECLCheck = true;
		gc = GregorianCalendar.getInstance();
		gc.setTime(asOfDate);
		gc.add(Calendar.YEAR, 1);
		Date oneYearDate = gc.getTime();
		
		for (int i = 1; i <= periods; i++) {
			daysFromAsOf = (int) (i * periodLength);
			
			gc.setTime(asOfDate);
			gc.add(Calendar.DATE, daysFromAsOf);
			periodDate = gc.getTime();
						
			if (periodDate.after(maturityDate)) { periodDate = maturityDate; }
					
			discountFactor = getDiscountFactor(asOfDate, startPeriodDate, eir);
			ead = calculateEAD(startPeriodDate, cfs, eir);
			pd = calculateAbsolutePD(asOfDate, periodDate, pds);
			incrementalPD = calculateIncrementalPD(lastPD, pd);
			lgd = calculateLGD(lgds);
			
			periodECL = discountFactor * ead * incrementalPD * lgd;
			
			//System.out.println("Period " + i + ", Date " + gc.get(Calendar.YEAR) + gc.get(Calendar.MONTH) + gc.get(Calendar.DATE) + ", df " + discountFactor + ", ead " + ead + ", incrementalPD " + incrementalPD + ", lgd " + lgd + ", periodECL " + periodECL);
			lifetimeECL += periodECL;
			
			if (twelveMonthECLCheck)  {
				
				if (periodDate.equals(oneYearDate) || periodDate.after(oneYearDate)) { 
					twelveMonthECL = lifetimeECL;
					twelveMonthECLCheck = false;
				}
			}
			
			lastPD = pd;
			gc.add(Calendar.DATE, 1);
			startPeriodDate = gc.getTime();
		}
		
		if (twelveMonthECL == 0d) { twelveMonthECL = lifetimeECL; }
		convertToEUR();
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
			System.out.println("endDate " + endDate.toString() + " before startDate " + startDate.toString());
			return 1d;
		}
		long periodDiff = endDate.getTime() - startDate.getTime();
		int dayDiff = (int) (TimeUnit.DAYS.convert(periodDiff, TimeUnit.MILLISECONDS));
			
		return 1/(Math.pow((1d + rate),((double)dayDiff/365d)));
	}
	
	public Double calculateIncrementalPD(Double startPD, Double endPD) {
		return ((endPD - startPD)/(1-startPD));
	}
	
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
	}
	
	public Double calculateEAD(Date eadDate, List<CashFlow> cfs, double eir) {
		
		Double ead = new Double(0d);
		Double cashFlowAmount = new Double(0d);
		Double discountedCashFlowAmount = new Double(0d);
				
		for (CashFlow cf : cfs) {
			if (cf.getCashFlowDate().after(eadDate)) { 
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
	
	private void setFirstDisbursementCurrency() {
		List<CashFlow> cfs = getCFs();
		for (CashFlow cf : cfs) {
			if (cf.getCashFlowType().equals(CashFlowType.DISBURSEMENT)) {
				firstDisbursementCurrency = cf.getCurrency();
				return;
			}
		}
	}
	
	public String getTradeIdentifier() {
		return tradeIdentifier;
	}

	public void setTradeIdentifier(String tradeIdentifier) {
		this.tradeIdentifier = tradeIdentifier;
	}

	public Double getTwelveMonthECL() {
		return twelveMonthECL;
	}

	public void setTwelveMonthECL(Double twelveMonthECL) {
		this.twelveMonthECL = twelveMonthECL;
	}

	public Double getLifetimeECL() {
		return lifetimeECL;
	}

	public void setLifetimeECL(Double lifetimeECL) {
		this.lifetimeECL = lifetimeECL;
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

	public int getImpairmentStageIFRS9() {
		return impairmentStageIFRS9;
	}

	public void setImpairmentStageIFRS9(int impairmentStageIFRS9) {
		this.impairmentStageIFRS9 = impairmentStageIFRS9;
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
	
	public void printECLResults() {
		System.out.println(getTradeIdentifier() + "," + getFirstDisbursementCurrency() + "," + getTwelveMonthECL() + "," + getLifetimeECL() + "," + getImpairmentStageIFRS9() + "," + getTwelveMonthECLEUR() + "," + getLifetimeECLEUR() + "," + getProvisionEUR());
	}
	
	public String getECLResults() {
		return getTradeIdentifier() + "," + getFirstDisbursementCurrency() + "," + getTwelveMonthECL() + "," + getLifetimeECL() + "," + getImpairmentStageIFRS9() + "," + getTwelveMonthECLEUR() + "," + getLifetimeECLEUR() + "," + getProvisionEUR();
	}

	public double getProvisionEUR() {
		if (impairmentStageIFRS9 == 1) {
			return getTwelveMonthECLEUR();
		}
		else if (impairmentStageIFRS9 == 2) {
			return getLifetimeECLEUR();
		}
		return 0d;
	}
}