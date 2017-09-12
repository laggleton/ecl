package referenceobjects;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class Country {
	private String countryName;
	private String iso3AlphaCode;
	private String iso2AlphaCode;
	private String currency;
	private Double probRecession;
	private Double probGrowth;
	private List<GrossDomesticProduct> gdps = new ArrayList<>();
	
	public void setProbabilityOfRecessionAndGrowth() {
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
	
	public Double getStandardDeviationForGDPs(int currentYear) {
		
		SummaryStatistics ss = new SummaryStatistics();
		
		for (GrossDomesticProduct gdp : gdps) {
			if (gdp.getReferenceYear() < currentYear) {
				ss.addValue(gdp.getActualGrowth());
			}
		}
		
		return ss.getStandardDeviation();
	}
	
	public Double getRMSEForGDPs(int currentYear) {
		//TODO implement
		return 0d;
	}

	public Double getGrowthForYear(int year) {
		for (GrossDomesticProduct gdp : gdps) {
			if (gdp.getReferenceYear() == year) {
				if (gdp.getActualGrowth() != -99999999d) { return gdp.getActualGrowth(); }
				else if (gdp.getOneYearPredictedGrowth() != -99999999d) { return gdp.getOneYearPredictedGrowth(); }
				else if (gdp.getTwoYearPredictedGrowth() != -99999999d) { return gdp.getTwoYearPredictedGrowth(); }
			}
		}
		//TODO error no data
		return null;
	}
}
