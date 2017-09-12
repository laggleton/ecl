package referenceobjects;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import utilities.Logger;

public class Country {
	private String countryName;
	private String iso3AlphaCode;
	private String iso2AlphaCode;
	private String currency;
	private Double probRecession;
	private Double probGrowth;
	private List<GrossDomesticProduct> gdps = new ArrayList<>();
	
	private Logger l = Logger.getInstance();
	
	/*
	 * Deprecated in favour of RMSE
	 */
	@Deprecated
	public void setProbabilityOfRecessionAndGrowthFromStdDev() {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(BusinessDate.getInstance().getDate());
		int currentYear = calendar.get(Calendar.YEAR);
		int nextYear = currentYear + 1;
		
		Double stdDev = getStandardDeviationForGDPs(currentYear); 
		
		Double lastGrowth = getGrowthForYear(currentYear);
		Double nextGrowth = getGrowthForYear(nextYear);	
		
		double month = (double)calendar.get(Calendar.MONTH) + 1.0d;
		
		Double currentGrowth = ((month/12) * nextGrowth) + ((12-month)/12) * lastGrowth;
		
		NormalDistribution nd = new NormalDistribution(currentGrowth, stdDev);
		probRecession = nd.cumulativeProbability(0d);
		probGrowth = 1 - probRecession;
	}
	
	public void setProbabilityOfRecessionAndGrowth() {
		setProbabilityOfRecessionAndGrowthFromRMSE();
		//setProbabilityOfRecessionAndGrowthFromStdDev();
	}
	
	public void setProbabilityOfRecessionAndGrowthFromRMSE() {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(BusinessDate.getInstance().getDate());
		int currentYear = calendar.get(Calendar.YEAR);
		int nextYear = currentYear + 1;
		
		Double rmse = getRootMeanSquaredErrorForGDPs();
		
		Double lastGrowth = getGrowthForYear(currentYear);
		Double nextGrowth = getGrowthForYear(nextYear);	
		
		double month = (double)calendar.get(Calendar.MONTH) + 1.0d;
		Double currentGrowth = 0d;
		try {
			 currentGrowth = ((month/12) * nextGrowth) + ((12-month)/12) * lastGrowth;
		}
		catch (Exception e) {
			l.error(e);
			l.error(this.countryName);
			l.error("currentYear = " + currentYear);
			l.error("nextYear = " + nextYear);
		}
				
		NormalDistribution nd = new NormalDistribution(currentGrowth, rmse);
		probRecession = nd.cumulativeProbability(0d);
		probGrowth = 1 - probRecession;
	}
	
	public Double getProbabilityOfGrowth() {
		if (probGrowth == null) {
			setProbabilityOfRecessionAndGrowth();
		}
		return probGrowth;
	}
	
	public Double getProbabilityOfRecession() {
		if (probRecession == null) {
			setProbabilityOfRecessionAndGrowth();
		}
		return probRecession;
	}
	
	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	public String getIso3AlphaCode() {
		return iso3AlphaCode;
	}

	public void setIso3AlphaCode(String iso3AlphaCode) {
		this.iso3AlphaCode = iso3AlphaCode;
	}

	public String getIso2AlphaCode() {
		return iso2AlphaCode;
	}

	public void setIso2AlphaCode(String iso2AlphaCode) {
		this.iso2AlphaCode = iso2AlphaCode;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public List<GrossDomesticProduct> getGdps() {
		return gdps;
	}

	public void setGdps(List<GrossDomesticProduct> gdps, Date asOfDate) {
		this.gdps = gdps;
		setProbabilityOfRecessionAndGrowth();
	}
	
	public void addGdp(GrossDomesticProduct gdp) {
		gdps.add(gdp);
	}

	public Country(String countryName) {
		this.countryName = countryName;
	}
	
	public Double getRootMeanSquaredErrorForGDPs() {
		Double error;
		Double square;
		Double runningSum = 0d;
		int count = 0;
		for (GrossDomesticProduct gdp : gdps) {
			if (!(gdp.getActualGrowth() == null) && (!(gdp.getOneYearPredictedGrowth() == null))) {
				error = gdp.getActualGrowth() - gdp.getOneYearPredictedGrowth();
				square = Math.pow(error, 2);
				runningSum += square;
			}
			count++;
		}
		
		Double mse = runningSum / (double) count;
		
		return Math.sqrt(mse);
	}
	
	public Double getStandardDeviationForGDPs(int currentYear) {
		
		SummaryStatistics ss = new SummaryStatistics();
		
		for (GrossDomesticProduct gdp : gdps) {
			if (gdp.getReferenceYear() < currentYear) {
				ss.addValue(gdp.getActualGrowth());
			}
		}
		
		return ss.getStandardDeviation();
	}
	
	public Double getGrowthForYear(int year) {
		for (GrossDomesticProduct gdp : gdps) {
			if (gdp.getReferenceYear() == year) {
				if (gdp.getActualGrowth() != null) { return gdp.getActualGrowth(); }
				else if (gdp.getOneYearPredictedGrowth() != null) { return gdp.getOneYearPredictedGrowth(); }
				else if (gdp.getTwoYearPredictedGrowth() != null) { return gdp.getTwoYearPredictedGrowth(); }
			}
		}
		//TODO error no data
		return null;
	}
}
