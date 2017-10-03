package test.referenceobjects;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import referenceobjects.BusinessDate;
import referenceobjects.Country;
import referenceobjects.DateFormat;
import referenceobjects.GrossDomesticProduct;
import utilities.Logger;

public class CountryTest {
	
	private Logger l = Logger.getInstance();

	@Test
	public void evaluateStandardDeviation() {
		Country ctry = setUpCountry();
		Double stdDev = ctry.getStandardDeviationForGDPs(2017);
		Double expectedResult = 0.040264d;
		
		assertEquals(expectedResult, stdDev, 0.01);
	}
	
	@Test
	public void evaluateRMSE() {
		Country ctry = setUpCountry();
		Double rmse = ctry.getRootMeanSquaredErrorForGDPs();
		Double expectedResult = 0.0308609988d;
		
		assertEquals(expectedResult, rmse, 0.01);
	}
	
	@Test
	public void evaluateCurrentGrowth() {
		Country ctry = setUpCountry();
		Double growth = ctry.getCurrentGrowth();
		Double expectedResult = 0.02675d;
		
		assertEquals(expectedResult, growth, 0.01d);
	}
	
	@Test
	public void evaluateProbRecessionFromRMSE() {
		Country ctry = setUpCountry();
		Double growth = ctry.getCurrentGrowth();
		
		Double rmse = ctry.getRootMeanSquaredErrorForGDPs();
		ctry.setProbabilityOfRecessionAndGrowthFromVariables(growth, rmse);
		Double rmseRec = ctry.getProbabilityOfRecession();
				
		Double expectedResult = 0.19303d;
		assertEquals(expectedResult, rmseRec, 0.01d);
	}
	
	@Test
	public void evaluateProbRecessionFromVariables() {
		Country ctry = setUpCountry();
		Double growth = ctry.getCurrentGrowth();
		
		Double shape = new Double(0.033333161d);
		ctry.setProbabilityOfRecessionAndGrowthFromVariables(growth, shape);
		Double rec = ctry.getProbabilityOfRecession();
				
		Double expectedResult = 0.211d;
		assertEquals(expectedResult, rec, 0.01);
	}
	
	@Test
	public void evaluateProbRecessionFromStdDev() {
		Country ctry = setUpCountry();
		Double growth = ctry.getCurrentGrowth();
		Double stdDev = ctry.getStandardDeviationForGDPs(2017);
		ctry.setProbabilityOfRecessionAndGrowthFromVariables(growth, stdDev);
		Double stdRec = ctry.getProbabilityOfRecession();
		
		Double expectedResult = 0.25322d;
		assertEquals(expectedResult, stdRec, 0.01d);
	}
	
	private void setBusinessDate() {
		BusinessDate.getInstance().initialise("2017-05-31");
	}
	
	
	private Country setUpCountry() {
		setBusinessDate();
		Country ctry = new Country("BG");
		
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
		try {
			ctry.setGdps(gdps, DateFormat.ISO_FORMAT.parse("2017-05-31"));
		}
		catch (ParseException pe) {
			l.error(pe);
			l.error("Failed to parse date");
		}
		
		return ctry;
	}

}
