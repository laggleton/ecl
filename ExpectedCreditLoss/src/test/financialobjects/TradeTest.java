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
import referenceobjects.DateFormat;
import referenceobjects.GrossDomesticProduct;
import referenceobjects.LossGivenDefault;
import referenceobjects.ProbabilityOfDefault;
import referenceobjects.Rating;
import referenceobjects.Scenario;
import utilities.Logger;

public class TradeTest {
	
	Trade t;
	Logger l = Logger.getInstance();
		
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
	public void evaluatesGenerateEIR() {
		setUpTestTrade();
		Date startTime = new Date();
		//t.generateDailyInterest();
		Double eir = t.calculateEIR();
		Date endTime = new Date();
		long diff = endTime.getTime() - startTime.getTime();
		l.info("Start Time for generateEIR " + startTime.getTime() + ", endTime for generateEIR " + endTime.getTime() + ", diff " + diff + ", value = " + eir);
		
		Double expectedResult = new Double(0.9945659d);
		//printAllCashFlows();
		assertEquals(expectedResult, eir, 0.001);
	}
	
	@Test
	public void evaluatesCalculateDiscountFactor() {
		setUpTestTrade();
		Date startDate = new Date(); Date endDate = new Date();
		try {
			startDate = DateFormat.ISO_FORMAT.parse("2017-05-31");
			endDate = DateFormat.ISO_FORMAT.parse("2017-08-31");
		}
		catch(ParseException pe) {
			l.info("Couldn't parse date string" + pe.toString());
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
			eadDate = DateFormat.ISO_FORMAT.parse("2017-05-31");
		}
		catch(ParseException pe) {
			l.info("Couldn't parse date string" + pe.toString());
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
		l.info("Start Time for quarterly ECL " + startTime.getTime() + ", endTime for ECL " + endTime.getTime() + ", diff " + diff + ", value = " + t.getECLResult().getTwelveMonthECL());
		Double expectedResult = new Double(742119d);
		assertEquals(expectedResult, t.getECLResult().getTwelveMonthECL(), 2.0d);
	}
	
	@Test
	public void evaluatesMonthlyECL() {
		
		setUpTestTrade();
		Scenario s = new Scenario(1.0d);
		Date startTime = new Date();
		t.calculateECL(s, 30.4375d); //monthly
		Date endTime = new Date();
		long diff = endTime.getTime() - startTime.getTime();
		l.info("Start Time for monthly ECL " + startTime.getTime() + ", endTime for ECL " + endTime.getTime() + ", diff " + diff + ", value = " + t.getECLResult().getTwelveMonthECL());
		
		Double expectedResult = new Double(620005d);
		assertEquals(expectedResult, t.getECLResult().getTwelveMonthECL(), 2.0d);
	}
	
	@Test
	public void evaluatesDailyECL() {
		
		setUpTestTrade();
		Scenario s = new Scenario(1.0d);
		Date startTime = new Date();
		t.calculateECL(s, 1.0d); //monthly
		Date endTime = new Date();
		long diff = endTime.getTime() - startTime.getTime();
		l.info("Start Time for daily ECL " + startTime.getTime() + ", endTime for ECL " + endTime.getTime() + ", diff " + diff + ", value = " + t.getECLResult().getTwelveMonthECL());
		
		Double expectedResult = new Double(562184d);
		assertEquals(expectedResult, t.getECLResult().getTwelveMonthECL(), 2.0d);
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
		t.setInitialCreditRating("5.3");
		t.setCountry(new Country("BG"));
		t.setCreditRating("6.3");
						
		try {
			BusinessDate.getInstance().initialise(DateFormat.ISO_FORMAT.parse("2017-05-31"));
			t.setAsOfDate(DateFormat.ISO_FORMAT.parse("2017-05-31"));
			t.setMaturityDate(DateFormat.ISO_FORMAT.parse("2023-02-28"));
		}
		catch (ParseException pe) {
			System.out.println("Failed to parse date: " + pe.toString());
		}
		
		List<CashFlow> cfs = setUpCashFlows();
		t.setCfs(cfs);
		setUpPDs();
		setUpLGDs();
		setUpGrowthData();
		t.setFirstDisbursementCurrency();
	}
	
	@Test
	public void evaluateStaging() {
		setUpTestTrade();
		t.setCreditRating("6.3");
		t.setInitialCreditRating("5.3");
		t.setRating(new Rating("6.3"));
		t.assessIFRS9Staging();
		
		assertEquals(2, t.assessIFRS9Staging());
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
		try {
			cf = new CashFlow(ccy,-7018681.75d,DateFormat.DMY_FORMAT.parse("22/08/2011"),"Principal"); cfs.add(cf);
			cf = new CashFlow(ccy,334222.95d,DateFormat.DMY_FORMAT.parse("21/02/2012"),"Principal"); cfs.add(cf);
			cf = new CashFlow(ccy,334222.95d,DateFormat.DMY_FORMAT.parse("21/08/2012"),"Principal"); cfs.add(cf);
			cf = new CashFlow(ccy,334222.95d,DateFormat.DMY_FORMAT.parse("21/02/2013"),"Principal"); cfs.add(cf);
			cf = new CashFlow(ccy,334222.95d,DateFormat.DMY_FORMAT.parse("21/08/2013"),"Principal"); cfs.add(cf);
			cf = new CashFlow(ccy,334222.95d,DateFormat.DMY_FORMAT.parse("21/02/2014"),"Principal"); cfs.add(cf);
			cf = new CashFlow(ccy,334222.95d,DateFormat.DMY_FORMAT.parse("21/08/2014"),"Principal"); cfs.add(cf);
			cf = new CashFlow(ccy,334222.95d,DateFormat.DMY_FORMAT.parse("23/02/2015"),"Principal"); cfs.add(cf);
			cf = new CashFlow(ccy,334222.95d,DateFormat.DMY_FORMAT.parse("21/08/2015"),"Principal"); cfs.add(cf);
			cf = new CashFlow(ccy,334222.95d,DateFormat.DMY_FORMAT.parse("22/02/2016"),"Principal"); cfs.add(cf);
			cf = new CashFlow(ccy,334222.95d,DateFormat.DMY_FORMAT.parse("22/08/2016"),"Principal"); cfs.add(cf);
			cf = new CashFlow(ccy,334222.95d,DateFormat.DMY_FORMAT.parse("21/02/2017"),"Principal"); cfs.add(cf);
			cf = new CashFlow(ccy,370066.17d,DateFormat.DMY_FORMAT.parse("21/08/2017"),"Principal"); cfs.add(cf);
			cf = new CashFlow(ccy,405269.61d,DateFormat.DMY_FORMAT.parse("21/02/2018"),"Principal"); cfs.add(cf);
			cf = new CashFlow(ccy,394593.47d,DateFormat.DMY_FORMAT.parse("21/08/2018"),"Principal"); cfs.add(cf);
			cf = new CashFlow(ccy,386149.66d,DateFormat.DMY_FORMAT.parse("21/02/2019"),"Principal"); cfs.add(cf);
			cf = new CashFlow(ccy,376231.24d,DateFormat.DMY_FORMAT.parse("21/08/2019"),"Principal"); cfs.add(cf);
			cf = new CashFlow(ccy,367925.68d,DateFormat.DMY_FORMAT.parse("21/02/2020"),"Principal"); cfs.add(cf);
			cf = new CashFlow(ccy,358863.26d,DateFormat.DMY_FORMAT.parse("21/08/2020"),"Principal"); cfs.add(cf);
			cf = new CashFlow(ccy,350640.16d,DateFormat.DMY_FORMAT.parse("22/02/2021"),"Principal"); cfs.add(cf);
			cf = new CashFlow(ccy,332490.05d,DateFormat.DMY_FORMAT.parse("23/08/2021"),"Principal"); cfs.add(cf);
			cf = new CashFlow(ccy,5000.00d,DateFormat.DMY_FORMAT.parse("22/08/2011"),"Fee"); cfs.add(cf);
			cf = new CashFlow(ccy,142249.38d,DateFormat.DMY_FORMAT.parse("21/02/2012"),"Interest"); cfs.add(cf);
			cf = new CashFlow(ccy,104219.63d,DateFormat.DMY_FORMAT.parse("21/08/2012"),"Interest"); cfs.add(cf);
			cf = new CashFlow(ccy,76500.59d,DateFormat.DMY_FORMAT.parse("21/02/2013"),"Interest"); cfs.add(cf);
			cf = new CashFlow(ccy,63730.80d,DateFormat.DMY_FORMAT.parse("21/08/2013"),"Interest"); cfs.add(cf);
			cf = new CashFlow(ccy,60752.22d,DateFormat.DMY_FORMAT.parse("21/02/2014"),"Interest"); cfs.add(cf);
			cf = new CashFlow(ccy,57429.30d,DateFormat.DMY_FORMAT.parse("21/08/2014"),"Interest"); cfs.add(cf);
			cf = new CashFlow(ccy,52892.45d,DateFormat.DMY_FORMAT.parse("23/02/2015"),"Interest"); cfs.add(cf);
			cf = new CashFlow(ccy,43623.06d,DateFormat.DMY_FORMAT.parse("21/08/2015"),"Interest"); cfs.add(cf);
			cf = new CashFlow(ccy,40011.68d,DateFormat.DMY_FORMAT.parse("22/02/2016"),"Interest"); cfs.add(cf);
			cf = new CashFlow(ccy,32969.09d,DateFormat.DMY_FORMAT.parse("22/08/2016"),"Interest"); cfs.add(cf);
			cf = new CashFlow(ccy,29135.58d,DateFormat.DMY_FORMAT.parse("21/02/2017"),"Interest"); cfs.add(cf);
			cf = new CashFlow(ccy,16280.41d,DateFormat.DMY_FORMAT.parse("07/10/2017"),"Interest"); cfs.add(cf);
			cf = new CashFlow(ccy,14320.45d,DateFormat.DMY_FORMAT.parse("07/04/2018"),"Interest"); cfs.add(cf);
			cf = new CashFlow(ccy,12361.52d,DateFormat.DMY_FORMAT.parse("08/10/2018"),"Interest"); cfs.add(cf);
			cf = new CashFlow(ccy,10355.67d,DateFormat.DMY_FORMAT.parse("08/04/2019"),"Interest"); cfs.add(cf);
			cf = new CashFlow(ccy,8470.76d,DateFormat.DMY_FORMAT.parse("07/10/2019"),"Interest"); cfs.add(cf);
			cf = new CashFlow(ccy,6605.22d,DateFormat.DMY_FORMAT.parse("07/04/2020"),"Interest"); cfs.add(cf);
			cf = new CashFlow(ccy,4762.14d,DateFormat.DMY_FORMAT.parse("07/10/2020"),"Interest"); cfs.add(cf);
			cf = new CashFlow(ccy,2983.60d,DateFormat.DMY_FORMAT.parse("07/04/2021"),"Interest"); cfs.add(cf);
			cf = new CashFlow(ccy,1257.09d,DateFormat.DMY_FORMAT.parse("07/10/2021"),"Interest"); cfs.add(cf);
		}
		catch (ParseException pe) {
			l.error(pe);
			l.error("Failed to parse date in DMY_FORMAT");
		}
		Collections.sort(cfs);
		
		return cfs;
	}
	
	private List<CashFlow> setUpCashFlows2() {
		String ccy = "EUR";
		List<CashFlow> cfs = new ArrayList<>();
		CashFlow cf;
		cf = new CashFlow(ccy,-15047655.06d,"2006-05-09","DISBURSEMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,70139.15d,"2006-06-09","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,49113.3d,"2006-06-30","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,74408.49d,"2006-07-31","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,-5945684.4d,"2006-08-02","DISBURSEMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,104228.24d,"2006-08-31","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,100907.98d,"2006-09-29","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,-4279709.8d,"2006-10-06","DISBURSEMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,133238.67d,"2006-10-31","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,-4416127.46d,"2006-11-20","DISBURSEMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,140661.72d,"2006-11-30","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,-3549382.09d,"2006-12-27","DISBURSEMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,155582.35d,"2006-12-29","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,-1774691.46d,"2007-01-29","DISBURSEMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,203057.99d,"2007-01-31","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,-1802674.8d,"2007-02-26","DISBURSEMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,180890.6d,"2007-02-28","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,-2312372.17d,"2007-03-27","DISBURSEMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,208015.29d,"2007-03-30","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,-2026356.77d,"2007-04-26","DISBURSEMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,210297.18d,"2007-04-27","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,-2259365.97d,"2007-05-29","DISBURSEMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,268627.43d,"2007-05-31","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,-2987500.46d,"2007-06-27","DISBURSEMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,247777.23d,"2007-06-29","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,-2569377.63d,"2007-07-26","DISBURSEMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,299694.18d,"2007-07-31","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,-2067523.51d,"2007-08-29","DISBURSEMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,303904.92d,"2007-08-31","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,300441.29d,"2007-09-28","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,-4319498.63d,"2007-10-30","DISBURSEMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,354221.56d,"2007-10-31","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,334501.82d,"2007-11-30","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,-2492447.26d,"2007-12-21","DISBURSEMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,316209.68d,"2007-12-28","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,416018.95d,"2008-01-31","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,339653.97d,"2008-02-29","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,362837.89d,"2008-03-31","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,-4857565.46d,"2008-04-03","DISBURSEMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,389712.69d,"2008-04-30","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,-1774691.46d,"2008-05-07","DISBURSEMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,401768.66d,"2008-05-30","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,417101.29d,"2008-06-30","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,-1774691.46d,"2008-07-04","DISBURSEMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,433449.19d,"2008-07-31","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,407887.4d,"2008-08-29","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,-2084677.43d,"2008-09-03","DISBURSEMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,462302.82d,"2008-09-30","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,-1804156.29d,"2008-10-14","DISBURSEMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,493679.38d,"2008-10-31","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,418385.5d,"2008-11-28","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,-1774691.46d,"2008-12-03","DISBURSEMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,-1774691.46d,"2009-01-09","DISBURSEMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,346198.46d,"2009-01-30","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,250492.81d,"2009-02-27","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,-1774691.46d,"2009-03-09","DISBURSEMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,273357.85d,"2009-03-31","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,-1774691.46d,"2009-04-03","DISBURSEMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,232306.99d,"2009-04-30","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,-1774691.46d,"2009-05-12","DISBURSEMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,214762.29d,"2009-05-29","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,236598.63d,"2009-06-30","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,216229.51d,"2009-07-31","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,-2226954.74d,"2009-08-04","DISBURSEMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,181695.85d,"2009-08-28","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,-1774691.46d,"2009-09-03","DISBURSEMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,214075.06d,"2009-09-30","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,-1774691.46d,"2009-10-05","DISBURSEMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,194448.99d,"2009-10-30","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,200574.9d,"2009-11-30","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,195128.14d,"2009-12-30","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,-1774691.46d,"2010-01-06","DISBURSEMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,201465.63d,"2010-01-29","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,-429364.06d,"2010-02-04","DISBURSEMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,185595.18d,"2010-02-26","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,218295d,"2010-03-31","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,196787.5d,"2010-04-30","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,184403.33d,"2010-05-28","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,208303.01d,"2010-06-30","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,10761.99d,"2010-06-30","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,203087.5d,"2010-07-30","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,232400d,"2010-08-31","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,216825d,"2010-09-30","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,209005.42d,"2010-10-29","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,251720d,"2010-11-30","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,231525d,"2010-12-30","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,247613.33d,"2011-01-31","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,71977.5d,"2011-02-09","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,2262000d,"2011-02-09","REPAYMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,1910434.85d,"2011-08-31","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,2044500d,"2011-08-31","REPAYMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,1889061.72d,"2012-02-29","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,1957500d,"2012-02-29","REPAYMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,1639503.79d,"2012-08-31","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,1870500d,"2012-08-31","REPAYMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,1219840.41d,"2013-02-28","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,2479500d,"2013-02-28","REPAYMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,1095662.79d,"2013-08-30","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,2262000d,"2013-08-30","REPAYMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,1060120.88d,"2014-02-28","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,2566500d,"2014-02-28","REPAYMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,1042189.06d,"2014-08-29","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,3001500d,"2014-08-29","REPAYMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,949115.14d,"2015-02-27","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,3306000d,"2015-02-27","REPAYMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,843228.75d,"2015-08-28","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,3393000d,"2015-08-28","REPAYMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,782249.61d,"2016-02-29","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,3871500d,"2016-02-29","REPAYMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,7280164.54d,"2016-05-03","REPAYMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,614765.39d,"2016-08-31","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,2610000d,"2016-08-31","REPAYMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,527943.74d,"2017-02-28","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,479635.6747d,"2017-06-07","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,1664116.314d,"2017-08-31","REPAYMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,473334.9589d,"2017-12-07","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,3684066.913d,"2018-02-28","REPAYMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,443055.3024d,"2018-06-07","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,4608928.112d,"2018-08-31","REPAYMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,403893.3651d,"2018-12-07","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,4683007.509d,"2019-02-28","REPAYMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,354933.6469d,"2019-06-07","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,5112183.477d,"2019-08-30","REPAYMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,307736.9033d,"2019-12-07","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,5258495.158d,"2020-02-28","REPAYMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,255393.1812d,"2020-06-08","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,4807372.103d,"2020-08-28","REPAYMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,204872.6508d,"2020-12-07","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,5128482.119d,"2021-02-26","REPAYMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,153888.6304d,"2021-06-07","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,4502766.332d,"2021-08-31","REPAYMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,107667.8376d,"2021-12-07","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,4696562.144d,"2022-02-28","REPAYMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,60745.05907d,"2022-06-07","INTEREST"); cfs.add(cf);
		cf = new CashFlow(ccy,3949355.33d,"2022-08-31","REPAYMENT"); cfs.add(cf);
		cf = new CashFlow(ccy,18394.2577d,"2022-12-07","INTEREST"); cfs.add(cf);
		Collections.sort(cfs);
		
		return cfs;
	}
	
	private void setUpPDs() {
		List<ProbabilityOfDefault> pds = new ArrayList<>();
		
		try {
			ProbabilityOfDefault pd = new ProbabilityOfDefault("6.3",DateFormat.ISO_FORMAT.parse("2017-05-31"),0.0d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",DateFormat.ISO_FORMAT.parse("2018-05-31"),0.0276d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",DateFormat.ISO_FORMAT.parse("2019-05-31"),0.0647d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",DateFormat.ISO_FORMAT.parse("2020-05-31"),0.0979d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",DateFormat.ISO_FORMAT.parse("2021-05-31"),0.1206d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",DateFormat.ISO_FORMAT.parse("2022-05-31"),0.1461d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",DateFormat.ISO_FORMAT.parse("2023-05-31"),0.1777d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",DateFormat.ISO_FORMAT.parse("2024-05-31"),0.1979d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",DateFormat.ISO_FORMAT.parse("2025-05-31"),0.219d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",DateFormat.ISO_FORMAT.parse("2026-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",DateFormat.ISO_FORMAT.parse("2027-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",DateFormat.ISO_FORMAT.parse("2028-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",DateFormat.ISO_FORMAT.parse("2029-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",DateFormat.ISO_FORMAT.parse("2030-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",DateFormat.ISO_FORMAT.parse("2031-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",DateFormat.ISO_FORMAT.parse("2032-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",DateFormat.ISO_FORMAT.parse("2033-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",DateFormat.ISO_FORMAT.parse("2034-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",DateFormat.ISO_FORMAT.parse("2035-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",DateFormat.ISO_FORMAT.parse("2036-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",DateFormat.ISO_FORMAT.parse("2037-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",DateFormat.ISO_FORMAT.parse("2038-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",DateFormat.ISO_FORMAT.parse("2039-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",DateFormat.ISO_FORMAT.parse("2040-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",DateFormat.ISO_FORMAT.parse("2041-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",DateFormat.ISO_FORMAT.parse("2042-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",DateFormat.ISO_FORMAT.parse("2043-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",DateFormat.ISO_FORMAT.parse("2044-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",DateFormat.ISO_FORMAT.parse("2045-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",DateFormat.ISO_FORMAT.parse("2046-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",DateFormat.ISO_FORMAT.parse("2047-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",DateFormat.ISO_FORMAT.parse("2048-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",DateFormat.ISO_FORMAT.parse("2049-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",DateFormat.ISO_FORMAT.parse("2050-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",DateFormat.ISO_FORMAT.parse("2051-05-31"),0.2577d); pds.add(pd);
			pd = new ProbabilityOfDefault("6.3",DateFormat.ISO_FORMAT.parse("2052-05-31"),0.2577d); pds.add(pd);
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
	
	private void printAllCashFlows() {
		String delimiter = ",";
		String decorator = t.getPrimaryKeyDecorator(delimiter);
		System.out.print(Trade.getPrimaryKeyHeader(delimiter) + CashFlow.getHeader(delimiter));
		
		for (CashFlow cf : t.getCfs()) {
			System.out.print(decorator + cf.toString(delimiter));
		}
	}
}
