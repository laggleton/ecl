package test.financialobjects;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import financialobjects.CashFlow;
import financialobjects.CashFlowType;
import financialobjects.Trade;
import referenceobjects.BusinessDate;
import referenceobjects.Country;
import referenceobjects.GrossDomesticProduct;
import referenceobjects.LossGivenDefault;
import referenceobjects.ProbabilityOfDefault;
import referenceobjects.Rating;
import referenceobjects.Scenario;

public class TradeTest {
	
	Trade t;
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		
	@Test
	public void evaluatesCalculateIncrementalPD() {
		setUpTestTrade();
		Double startPD = new Double(0.013);
		Double endPD = new Double(0.023);
		
		Double incrementalPD = t.calculateIncrementalPD(startPD, endPD);
		Double expectedResult = ((endPD - startPD)/(1-startPD));
		
		assertEquals(expectedResult, incrementalPD);
	}
	
	@Test
	public void evaluatesCalculateDiscountFactor() {
		setUpTestTrade();
		Date startDate = new Date(); Date endDate = new Date();
		try {
			startDate = format.parse("2017-05-31");
			endDate = format.parse("2017-08-31");
		}
		catch(ParseException pe) {
			System.out.println("Couldn't parse date string" + pe.toString());
			pe.printStackTrace();
		}
		double rate = 0.03895495d;
		Double df = t.getDiscountFactor(startDate, endDate, rate);
		Double expectedResult = new Double (0.9904138798709152d);
		assertEquals(expectedResult, df); 
		
	}
	
	@Test
	public void evaluatesSetProbabilityOfRecessionAndGrowthFromRMSE() {
		setUpTestTrade();
		t.getCountry().setProbabilityOfRecessionAndGrowthFromRMSE();
		
		Double probGrowth = t.getCountry().getProbabilityOfGrowth();
		Double expectedResult = new Double (0.80697141d);
		
		assertEquals(expectedResult, probGrowth, 0.01);
	}
	
	@Test
	public void evaluatesSetProbabilityOfRecessionAndGrowthFromStdDev() {
		setUpTestTrade();
		t.getCountry().setProbabilityOfRecessionAndGrowthFromStdDev();
		
		Double probGrowth = t.getCountry().getProbabilityOfGrowth();
		Double expectedResult = new Double (0.7467700d);
		
		assertEquals(expectedResult, probGrowth, 0.01);
	}
	
	@Test
	public void evaluatesCalculateEAD() {
		Date eadDate = new Date();
		setUpTestTrade();
		double rate = 0.03895495d;
		
		try {
			eadDate = format.parse("2017-05-31");
		}
		catch(ParseException pe) {
			System.out.println("Couldn't parse date string" + pe.toString());
			pe.printStackTrace();
		}
		
		Double ead = t.calculateEAD(eadDate, t.getCashFlows(), rate);
		Double expectedResult = new Double(4.6146264226674266E7d);
		
		assertEquals(expectedResult, ead, 2.0d);
	}
	
	@Test
	public void evaluatesCalculateQuarterlyECL() {
		
		setUpTestTrade();
		Scenario s = new Scenario(1.0d);
		Date startTime = new Date();
		t.calculateECL(s); //quarterly
		Date endTime = new Date();
		long diff = endTime.getTime() - startTime.getTime();
		System.out.println("Start Time for quarterly ECL " + startTime.getTime() + ", endTime for ECL " + endTime.getTime() + ", diff " + diff + ", value = " + t.getTwelveMonthECL());
		Double expectedResult = new Double(546418d);
		assertEquals(expectedResult, t.getTwelveMonthECL(), 2.0d);
	}
	
	@Test
	public void evaluatesMonthlyECL() {
		
		setUpTestTrade();
		Scenario s = new Scenario(1.0d);
		Date startTime = new Date();
		t.calculateECL(s, 30.4375d); //monthly
		Date endTime = new Date();
		long diff = endTime.getTime() - startTime.getTime();
		System.out.println("Start Time for monthly ECL " + startTime.getTime() + ", endTime for ECL " + endTime.getTime() + ", diff " + diff + ", value = " + t.getTwelveMonthECL());
		
		Double expectedResult = new Double(555473d);
		assertEquals(expectedResult, t.getTwelveMonthECL(), 2.0d);
	}
	
	@Test
	public void evaluatesDailyECL() {
		
		setUpTestTrade();
		Scenario s = new Scenario(1.0d);
		Date startTime = new Date();
		t.calculateECL(s, 1.0d); //monthly
		Date endTime = new Date();
		long diff = endTime.getTime() - startTime.getTime();
		System.out.println("Start Time for daily ECL " + startTime.getTime() + ", endTime for ECL " + endTime.getTime() + ", diff " + diff + ", value = " + t.getTwelveMonthECL());
		
		Double expectedResult = new Double(560033d);
		assertEquals(expectedResult, t.getTwelveMonthECL(), 2.0d);
	}
	
	@Test
	public void evaluatesCalculateQuarterlyAlternativeECL() {
		
		setUpTestTrade();
		Scenario s = new Scenario(1.0d);
		Date startTime = new Date();
		t.calculateAlternativeECL(s, 91.3125d); //quarterly
		Date endTime = new Date();
		long diff = endTime.getTime() - startTime.getTime();
		System.out.println("Start Time for quarterly alternative ECL " + startTime.getTime() + ", endTime for ECL " + endTime.getTime() + ", diff " + diff + ", value = " + t.getTwelveMonthECL());
		Double expectedResult = new Double(560984d);
		assertEquals(expectedResult, t.getTwelveMonthECL(), 2.0d);
	}
	
	@Test
	public void evaluatesMonthlyAlternativeECL() {
		
		setUpTestTrade();
		Scenario s = new Scenario(1.0d);
		Date startTime = new Date();
		t.calculateAlternativeECL(s, 30.4375d); //monthly
		Date endTime = new Date();
		long diff = endTime.getTime() - startTime.getTime();
		System.out.println("Start Time for alternative monthly ECL " + startTime.getTime() + ", endTime for ECL " + endTime.getTime() + ", diff " + diff + ", value = " + t.getTwelveMonthECL());
		
		Double expectedResult = new Double(560310d);
		assertEquals(expectedResult, t.getTwelveMonthECL(), 2.0d);
	}
	
	@Test
	public void evaluatesDailyAlternativeECL() {
		
		setUpTestTrade();
		Scenario s = new Scenario(1.0d);
		Date startTime = new Date();
		t.calculateAlternativeECL(s, 1.0d); //daily
		Date endTime = new Date();
		long diff = endTime.getTime() - startTime.getTime();
		System.out.println("Start Time for daily alternative ECL " + startTime.getTime() + ", endTime for ECL " + endTime.getTime() + ", diff " + diff + ", value = " + t.getTwelveMonthECL());
		
		Double expectedResult = new Double(560033);
		assertEquals(expectedResult, t.getTwelveMonthECL(), 2.0d);
	}
	
	@Test
	public void evaluateCalculateEIR() {
		//TODO
	}
	
	@Test
	public void evaluateGetDiscountFactor() {
		//TODO
	}
	
	@Test
	public void evaluateCalculateAbsolutePD() {
		//TODO
	}
	
	@Test
	public void evaluateCalculateIncrementalPD() {
		//TODO
	}
	
	private void setUpTestTrade() {
		t = new Trade("11865_MARITZAIZT", "16324", "BK100", "16324", 100000, "EUR");
		t.setEIR(0.03895495d);
		t.setRating(new Rating("6.3"));
		t.setCountry(new Country("BG"));
		t.setCreditRating("6.3");
						
		try {
			BusinessDate.getInstance().initialise(format.parse("2017-05-31"));
			t.setAsOfDate(format.parse("2017-05-31"));
			t.setMaturityDate(format.parse("2023-02-28"));
		}
		catch (ParseException pe) {
			System.out.println("Failed to parse date: " + pe.toString());
		}
		
		List<CashFlow> cfs = setUpCashFlows();
		t.setCfs(cfs);
		setUpPDs();
		setUpLGDs();
		setUpGrowthData();
	}
	
	@Test
	public void evaluateStaging() {
		setUpTestTrade();
		t.setCreditRating("6.3");
		t.setInitialCreditRating("5.3");
		t.setRating(new Rating("6.3"));
		t.assessIFRS9Staging();
		
		assertEquals(2, t.getImpairmentStageIFRS9());
	}
	
	private void setUpGrowthData() {
		Country ctry = t.getCountry();
		
		List<GrossDomesticProduct> gdps = new ArrayList<>();
		
		GrossDomesticProduct gdp = new GrossDomesticProduct(1995); gdp.setActualGrowth(-0.016); gdps.add(gdp);
		gdp = new GrossDomesticProduct(1996); gdp.setActualGrowth(-0.08); gdp.setOneYearPredictedGrowth(0d); gdps.add(gdp);
		gdp = new GrossDomesticProduct(1997); gdp.setActualGrowth(-0.016); gdp.setOneYearPredictedGrowth(0.025); gdps.add(gdp);
		gdp = new GrossDomesticProduct(1998); gdp.setActualGrowth(0.049); gdp.setOneYearPredictedGrowth(0.025); gdps.add(gdp);
		gdp = new GrossDomesticProduct(1999); gdp.setActualGrowth(-0.005); gdp.setOneYearPredictedGrowth(0.0597); gdps.add(gdp);
		gdp = new GrossDomesticProduct(2000); gdp.setActualGrowth(0.05); gdp.setOneYearPredictedGrowth(0.041); gdps.add(gdp);
		gdp = new GrossDomesticProduct(2001); gdp.setActualGrowth(0.042); gdp.setOneYearPredictedGrowth(0.04); gdps.add(gdp);
		gdp = new GrossDomesticProduct(2002); gdp.setActualGrowth(0.06); gdp.setOneYearPredictedGrowth(0.05d); gdps.add(gdp);
		gdp = new GrossDomesticProduct(2003); gdp.setActualGrowth(0.051); gdp.setOneYearPredictedGrowth(0.05d); gdps.add(gdp);
		gdp = new GrossDomesticProduct(2004); gdp.setActualGrowth(0.066); gdp.setOneYearPredictedGrowth(0.055); gdps.add(gdp);
		gdp = new GrossDomesticProduct(2005); gdp.setActualGrowth(0.072); gdp.setOneYearPredictedGrowth(0.052); gdps.add(gdp);
		gdp = new GrossDomesticProduct(2006); gdp.setActualGrowth(0.068); gdp.setOneYearPredictedGrowth(0.055); gdps.add(gdp);
		gdp = new GrossDomesticProduct(2007); gdp.setActualGrowth(0.077); gdp.setOneYearPredictedGrowth(0.06d); gdps.add(gdp);
		gdp = new GrossDomesticProduct(2008); gdp.setActualGrowth(0.056); gdp.setOneYearPredictedGrowth(0.0587); gdps.add(gdp);
		gdp = new GrossDomesticProduct(2009); gdp.setActualGrowth(-0.042); gdp.setOneYearPredictedGrowth(0.0425); gdps.add(gdp);
		gdp = new GrossDomesticProduct(2010); gdp.setActualGrowth(0.001); gdp.setOneYearPredictedGrowth(-0.025); gdps.add(gdp);
		gdp = new GrossDomesticProduct(2011); gdp.setActualGrowth(0.016); gdp.setOneYearPredictedGrowth(0.02d); gdps.add(gdp);
		gdp = new GrossDomesticProduct(2012); gdp.setActualGrowth(0.002); gdp.setOneYearPredictedGrowth(0.03d); gdps.add(gdp);
		gdp = new GrossDomesticProduct(2013); gdp.setActualGrowth(0.013); gdp.setOneYearPredictedGrowth(0.015); gdps.add(gdp);
		gdp = new GrossDomesticProduct(2014); gdp.setActualGrowth(0.015); gdp.setOneYearPredictedGrowth(0.016); gdps.add(gdp);
		gdp = new GrossDomesticProduct(2015); gdp.setActualGrowth(0.03); gdp.setOneYearPredictedGrowth(0.02d); gdps.add(gdp);
		gdp = new GrossDomesticProduct(2016); gdp.setActualGrowth(0.03); gdp.setOneYearPredictedGrowth(0.019); gdps.add(gdp);
		gdp = new GrossDomesticProduct(2017); gdp.setActualGrowth(0.028); gdp.setOneYearPredictedGrowth(0.028); gdps.add(gdp);
		gdp = new GrossDomesticProduct(2018); gdp.setOneYearPredictedGrowth(0.025); gdps.add(gdp);

		Collections.sort(gdps);
		ctry.setGdps(gdps, t.getAsOfDate());
		
	}

	private List<CashFlow> setUpCashFlows() {
		String ccy = "EUR";
		List<CashFlow> cfs = new ArrayList<>();
		CashFlow cf;
		cf = new CashFlow(ccy,-15047655.06d,"2006-05-09",CashFlowType.DISBURSEMENT); cfs.add(cf);
		cf = new CashFlow(ccy,70139.15d,"2006-06-09",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,49113.3d,"2006-06-30",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,74408.49d,"2006-07-31",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,-5945684.4d,"2006-08-02",CashFlowType.DISBURSEMENT); cfs.add(cf);
		cf = new CashFlow(ccy,104228.24d,"2006-08-31",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,100907.98d,"2006-09-29",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,-4279709.8d,"2006-10-06",CashFlowType.DISBURSEMENT); cfs.add(cf);
		cf = new CashFlow(ccy,133238.67d,"2006-10-31",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,-4416127.46d,"2006-11-20",CashFlowType.DISBURSEMENT); cfs.add(cf);
		cf = new CashFlow(ccy,140661.72d,"2006-11-30",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,-3549382.09d,"2006-12-27",CashFlowType.DISBURSEMENT); cfs.add(cf);
		cf = new CashFlow(ccy,155582.35d,"2006-12-29",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,-1774691.46d,"2007-01-29",CashFlowType.DISBURSEMENT); cfs.add(cf);
		cf = new CashFlow(ccy,203057.99d,"2007-01-31",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,-1802674.8d,"2007-02-26",CashFlowType.DISBURSEMENT); cfs.add(cf);
		cf = new CashFlow(ccy,180890.6d,"2007-02-28",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,-2312372.17d,"2007-03-27",CashFlowType.DISBURSEMENT); cfs.add(cf);
		cf = new CashFlow(ccy,208015.29d,"2007-03-30",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,-2026356.77d,"2007-04-26",CashFlowType.DISBURSEMENT); cfs.add(cf);
		cf = new CashFlow(ccy,210297.18d,"2007-04-27",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,-2259365.97d,"2007-05-29",CashFlowType.DISBURSEMENT); cfs.add(cf);
		cf = new CashFlow(ccy,268627.43d,"2007-05-31",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,-2987500.46d,"2007-06-27",CashFlowType.DISBURSEMENT); cfs.add(cf);
		cf = new CashFlow(ccy,247777.23d,"2007-06-29",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,-2569377.63d,"2007-07-26",CashFlowType.DISBURSEMENT); cfs.add(cf);
		cf = new CashFlow(ccy,299694.18d,"2007-07-31",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,-2067523.51d,"2007-08-29",CashFlowType.DISBURSEMENT); cfs.add(cf);
		cf = new CashFlow(ccy,303904.92d,"2007-08-31",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,300441.29d,"2007-09-28",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,-4319498.63d,"2007-10-30",CashFlowType.DISBURSEMENT); cfs.add(cf);
		cf = new CashFlow(ccy,354221.56d,"2007-10-31",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,334501.82d,"2007-11-30",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,-2492447.26d,"2007-12-21",CashFlowType.DISBURSEMENT); cfs.add(cf);
		cf = new CashFlow(ccy,316209.68d,"2007-12-28",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,416018.95d,"2008-01-31",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,339653.97d,"2008-02-29",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,362837.89d,"2008-03-31",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,-4857565.46d,"2008-04-03",CashFlowType.DISBURSEMENT); cfs.add(cf);
		cf = new CashFlow(ccy,389712.69d,"2008-04-30",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,-1774691.46d,"2008-05-07",CashFlowType.DISBURSEMENT); cfs.add(cf);
		cf = new CashFlow(ccy,401768.66d,"2008-05-30",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,417101.29d,"2008-06-30",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,-1774691.46d,"2008-07-04",CashFlowType.DISBURSEMENT); cfs.add(cf);
		cf = new CashFlow(ccy,433449.19d,"2008-07-31",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,407887.4d,"2008-08-29",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,-2084677.43d,"2008-09-03",CashFlowType.DISBURSEMENT); cfs.add(cf);
		cf = new CashFlow(ccy,462302.82d,"2008-09-30",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,-1804156.29d,"2008-10-14",CashFlowType.DISBURSEMENT); cfs.add(cf);
		cf = new CashFlow(ccy,493679.38d,"2008-10-31",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,418385.5d,"2008-11-28",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,-1774691.46d,"2008-12-03",CashFlowType.DISBURSEMENT); cfs.add(cf);
		cf = new CashFlow(ccy,-1774691.46d,"2009-01-09",CashFlowType.DISBURSEMENT); cfs.add(cf);
		cf = new CashFlow(ccy,346198.46d,"2009-01-30",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,250492.81d,"2009-02-27",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,-1774691.46d,"2009-03-09",CashFlowType.DISBURSEMENT); cfs.add(cf);
		cf = new CashFlow(ccy,273357.85d,"2009-03-31",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,-1774691.46d,"2009-04-03",CashFlowType.DISBURSEMENT); cfs.add(cf);
		cf = new CashFlow(ccy,232306.99d,"2009-04-30",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,-1774691.46d,"2009-05-12",CashFlowType.DISBURSEMENT); cfs.add(cf);
		cf = new CashFlow(ccy,214762.29d,"2009-05-29",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,236598.63d,"2009-06-30",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,216229.51d,"2009-07-31",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,-2226954.74d,"2009-08-04",CashFlowType.DISBURSEMENT); cfs.add(cf);
		cf = new CashFlow(ccy,181695.85d,"2009-08-28",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,-1774691.46d,"2009-09-03",CashFlowType.DISBURSEMENT); cfs.add(cf);
		cf = new CashFlow(ccy,214075.06d,"2009-09-30",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,-1774691.46d,"2009-10-05",CashFlowType.DISBURSEMENT); cfs.add(cf);
		cf = new CashFlow(ccy,194448.99d,"2009-10-30",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,200574.9d,"2009-11-30",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,195128.14d,"2009-12-30",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,-1774691.46d,"2010-01-06",CashFlowType.DISBURSEMENT); cfs.add(cf);
		cf = new CashFlow(ccy,201465.63d,"2010-01-29",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,-429364.06d,"2010-02-04",CashFlowType.DISBURSEMENT); cfs.add(cf);
		cf = new CashFlow(ccy,185595.18d,"2010-02-26",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,218295d,"2010-03-31",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,196787.5d,"2010-04-30",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,184403.33d,"2010-05-28",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,208303.01d,"2010-06-30",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,10761.99d,"2010-06-30",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,203087.5d,"2010-07-30",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,232400d,"2010-08-31",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,216825d,"2010-09-30",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,209005.42d,"2010-10-29",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,251720d,"2010-11-30",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,231525d,"2010-12-30",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,247613.33d,"2011-01-31",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,71977.5d,"2011-02-09",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,2262000d,"2011-02-09",CashFlowType.REPAYMENT); cfs.add(cf);
		cf = new CashFlow(ccy,1910434.85d,"2011-08-31",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,2044500d,"2011-08-31",CashFlowType.REPAYMENT); cfs.add(cf);
		cf = new CashFlow(ccy,1889061.72d,"2012-02-29",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,1957500d,"2012-02-29",CashFlowType.REPAYMENT); cfs.add(cf);
		cf = new CashFlow(ccy,1639503.79d,"2012-08-31",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,1870500d,"2012-08-31",CashFlowType.REPAYMENT); cfs.add(cf);
		cf = new CashFlow(ccy,1219840.41d,"2013-02-28",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,2479500d,"2013-02-28",CashFlowType.REPAYMENT); cfs.add(cf);
		cf = new CashFlow(ccy,1095662.79d,"2013-08-30",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,2262000d,"2013-08-30",CashFlowType.REPAYMENT); cfs.add(cf);
		cf = new CashFlow(ccy,1060120.88d,"2014-02-28",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,2566500d,"2014-02-28",CashFlowType.REPAYMENT); cfs.add(cf);
		cf = new CashFlow(ccy,1042189.06d,"2014-08-29",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,3001500d,"2014-08-29",CashFlowType.REPAYMENT); cfs.add(cf);
		cf = new CashFlow(ccy,949115.14d,"2015-02-27",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,3306000d,"2015-02-27",CashFlowType.REPAYMENT); cfs.add(cf);
		cf = new CashFlow(ccy,843228.75d,"2015-08-28",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,3393000d,"2015-08-28",CashFlowType.REPAYMENT); cfs.add(cf);
		cf = new CashFlow(ccy,782249.61d,"2016-02-29",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,3871500d,"2016-02-29",CashFlowType.REPAYMENT); cfs.add(cf);
		cf = new CashFlow(ccy,7280164.54d,"2016-05-03",CashFlowType.REPAYMENT); cfs.add(cf);
		cf = new CashFlow(ccy,614765.39d,"2016-08-31",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,2610000d,"2016-08-31",CashFlowType.REPAYMENT); cfs.add(cf);
		cf = new CashFlow(ccy,527943.74d,"2017-02-28",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,479635.6747d,"2017-06-07",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,1664116.314d,"2017-08-31",CashFlowType.REPAYMENT); cfs.add(cf);
		cf = new CashFlow(ccy,473334.9589d,"2017-12-07",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,3684066.913d,"2018-02-28",CashFlowType.REPAYMENT); cfs.add(cf);
		cf = new CashFlow(ccy,443055.3024d,"2018-06-07",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,4608928.112d,"2018-08-31",CashFlowType.REPAYMENT); cfs.add(cf);
		cf = new CashFlow(ccy,403893.3651d,"2018-12-07",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,4683007.509d,"2019-02-28",CashFlowType.REPAYMENT); cfs.add(cf);
		cf = new CashFlow(ccy,354933.6469d,"2019-06-07",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,5112183.477d,"2019-08-30",CashFlowType.REPAYMENT); cfs.add(cf);
		cf = new CashFlow(ccy,307736.9033d,"2019-12-07",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,5258495.158d,"2020-02-28",CashFlowType.REPAYMENT); cfs.add(cf);
		cf = new CashFlow(ccy,255393.1812d,"2020-06-08",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,4807372.103d,"2020-08-28",CashFlowType.REPAYMENT); cfs.add(cf);
		cf = new CashFlow(ccy,204872.6508d,"2020-12-07",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,5128482.119d,"2021-02-26",CashFlowType.REPAYMENT); cfs.add(cf);
		cf = new CashFlow(ccy,153888.6304d,"2021-06-07",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,4502766.332d,"2021-08-31",CashFlowType.REPAYMENT); cfs.add(cf);
		cf = new CashFlow(ccy,107667.8376d,"2021-12-07",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,4696562.144d,"2022-02-28",CashFlowType.REPAYMENT); cfs.add(cf);
		cf = new CashFlow(ccy,60745.05907d,"2022-06-07",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,3949355.33d,"2022-08-31",CashFlowType.REPAYMENT); cfs.add(cf);
		cf = new CashFlow(ccy,18394.2577d,"2022-12-07",CashFlowType.INTEREST); cfs.add(cf);
		cf = new CashFlow(ccy,0d,"2023-02-28",CashFlowType.INTEREST); cfs.add(cf);
		Collections.sort(cfs);
		
		return cfs;
	}
	
	private void setUpPDs() {
		List<ProbabilityOfDefault> pds = new ArrayList<>();
		
		try {
			ProbabilityOfDefault pd = new ProbabilityOfDefault("6.3",format.parse("2017-05-31"),0.0d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",format.parse("2018-05-31"),0.0276d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",format.parse("2019-05-31"),0.0647d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",format.parse("2020-05-31"),0.0979d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",format.parse("2021-05-31"),0.1206d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",format.parse("2022-05-31"),0.1461d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",format.parse("2023-05-31"),0.1777d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",format.parse("2024-05-31"),0.1979d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",format.parse("2025-05-31"),0.219d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",format.parse("2026-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",format.parse("2027-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",format.parse("2028-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",format.parse("2029-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",format.parse("2030-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",format.parse("2031-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",format.parse("2032-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",format.parse("2033-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",format.parse("2034-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",format.parse("2035-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",format.parse("2036-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",format.parse("2037-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",format.parse("2038-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",format.parse("2039-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",format.parse("2040-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",format.parse("2041-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",format.parse("2042-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",format.parse("2043-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",format.parse("2044-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",format.parse("2045-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",format.parse("2046-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",format.parse("2047-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",format.parse("2048-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",format.parse("2049-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",format.parse("2050-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",format.parse("2051-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",format.parse("2052-05-31"),0.2577d); pds.add(pd);
		}
		catch (ParseException pe) {
			System.out.println("Failed to parse date: " + pe.toString());
		}
		
		Collections.sort(pds);
		t.getRating().addPDs(pds);
		
	}

	private void setUpLGDs() {
		LossGivenDefault lgd = new LossGivenDefault(t.getAsOfDate(), 0.45d);
		List<LossGivenDefault> lgds = new ArrayList<>();
		lgds.add(lgd);
		t.setLgds(lgds);
	}
}
