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
				
		Double stdDev = getStandardDeviationForGDPs(currentYear); 
		
		Double currentGrowth = getCurrentGrowth();
		//Double currentGrowth = 0.814d; //TODO HAACKHACK
		
		NormalDistribution nd = new NormalDistribution(currentGrowth, stdDev);
		probRecession = nd.cumulativeProbability(0d);
		probGrowth = 1 - probRecession;
	}
	
	public Double getCurrentGrowth() {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(BusinessDate.getInstance().getDate());
		int currentYear = calendar.get(Calendar.YEAR);
		int nextYear = currentYear + 1;
		Double lastGrowth = getProjectedGrowthForYear(currentYear);
		Double nextGrowth = getGrowthForYear(nextYear);	
		
		double month = (double)calendar.get(Calendar.MONTH) + 1.0d;
		Double currentGrowth = 0d;
		try {
			 currentGrowth = ((month/12) * nextGrowth) + ((12-month)/12) * lastGrowth;
		}
		catch (Exception e) {
			l.error(e);
			l.error(this.countryName + ", currentYear = " + currentYear + "nextYear = " + nextYear);
		}
		return currentGrowth;
	}
	
	public void setProbabilityOfRecessionAndGrowth() {
		//setProbabilityOfRecessionAndGrowthFromHack();
		
		if (!countryName.equals("RG")) {
			setProbabilityOfRecessionAndGrowthFromRMSE();
		}
		setProbabilityOfRecessionAndGrowthFromStdDev();
		 
	}
		
	public void setProbabilityOfRecessionAndGrowthFromRMSE() {
		
		Double rmse = getRootMeanSquaredErrorForGDPs();
		Double currentGrowth = getCurrentGrowth();
		
		/*
		 * Tweak below for different Economic scenarios
		 */
		//currentGrowth -= 0.05d; 
		
		NormalDistribution nd = new NormalDistribution(currentGrowth, rmse);
		probRecession = nd.cumulativeProbability(0d);
		probGrowth = 1 - probRecession;
	}
	
	public void setProbabilityOfRecessionAndGrowthFromVariables(Double offset, Double shape) {
		NormalDistribution nd = new NormalDistribution(offset, shape);
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
				count++;
			}
		}
		
		Double mse = runningSum / (double) count;
		Double rmse = Math.sqrt(mse);
		//l.warn(getCountryName() + ", rmse = " + rmse);
		return rmse;
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
		l.error("No growth value for " + countryName + " for " + year);
		return null;
	}
	
	public Double getProjectedGrowthForYear(int year) {
		for (GrossDomesticProduct gdp : gdps) {
			if (gdp.getReferenceYear() == year) {
				if (gdp.getOneYearPredictedGrowth() != null) { return gdp.getOneYearPredictedGrowth(); }
				else if (gdp.getTwoYearPredictedGrowth() != null) { return gdp.getTwoYearPredictedGrowth(); }
			}
		}
		l.error("No growth value for " + countryName + " for " + year);
		return 0d;
	}
	
	public void setProbabilityOfRecessionAndGrowthFromHack() {
		if (countryName.equals("AE")) { probRecession = 0.291; }
		if (countryName.equals("AL")) { probRecession = 0.193; }
		if (countryName.equals("AM")) { probRecession = 0.286; }
		if (countryName.equals("AT")) { probRecession = 0.191; }
		if (countryName.equals("AZ")) { probRecession = 0.395; }
		if (countryName.equals("BA")) { probRecession = 0.227; }
		if (countryName.equals("BG")) { probRecession = 0.211; }
		if (countryName.equals("BH")) { probRecession = 0.196; }
		if (countryName.equals("BY")) { probRecession = 0.507; }
		if (countryName.equals("CH")) { probRecession = 0.163; }
		if (countryName.equals("CY")) { probRecession = 0.184; }
		if (countryName.equals("CZ")) { probRecession = 0.172; }
		if (countryName.equals("DE")) { probRecession = 0.228; }
		if (countryName.equals("DK")) { probRecession = 0.214; }
		if (countryName.equals("EE")) { probRecession = 0.300; }
		if (countryName.equals("EG")) { probRecession = 0.000; }
		if (countryName.equals("FI")) { probRecession = 0.326; }
		if (countryName.equals("FR")) { probRecession = 0.096; }
		if (countryName.equals("GB")) { probRecession = 0.152; }
		if (countryName.equals("GE")) { probRecession = 0.093; }
		if (countryName.equals("GR")) { probRecession = 0.157; }
		if (countryName.equals("HR")) { probRecession = 0.250; }
		if (countryName.equals("HU")) { probRecession = 0.146; }
		if (countryName.equals("IE")) { probRecession = 0.296; }
		if (countryName.equals("IL")) { probRecession = 0.109; }
		if (countryName.equals("IT")) { probRecession = 0.275; }
		if (countryName.equals("JO")) { probRecession = 0.063; }
		if (countryName.equals("JP")) { probRecession = 0.377; }
		if (countryName.equals("KG")) { probRecession = 0.165; }
		if (countryName.equals("KR")) { probRecession = 0.218; }
		if (countryName.equals("KV")) { probRecession = 0.000; }
		if (countryName.equals("KW")) { probRecession = 0.321; }
		if (countryName.equals("KZ")) { probRecession = 0.406; }
		if (countryName.equals("LT")) { probRecession = 0.241; }
		if (countryName.equals("LV")) { probRecession = 0.207; }
		if (countryName.equals("MA")) { probRecession = 0.053; }
		if (countryName.equals("MD")) { probRecession = 0.276; }
		if (countryName.equals("ME")) { probRecession = 0.174; }
		if (countryName.equals("MK")) { probRecession = 0.109; }
		if (countryName.equals("MN")) { probRecession = 0.306; }
		if (countryName.equals("NL")) { probRecession = 0.144; }
		if (countryName.equals("PL")) { probRecession = 0.018; }
		if (countryName.equals("PT")) { probRecession = 0.201; }
		if (countryName.equals("QA")) { probRecession = 0.345; }
		if (countryName.equals("RG")) { probRecession = 0.104; }
		if (countryName.equals("RO")) { probRecession = 0.189; }
		if (countryName.equals("RS")) { probRecession = 0.109; }
		if (countryName.equals("RU")) { probRecession = 0.412; }
		if (countryName.equals("SA")) { probRecession = 0.269; }
		if (countryName.equals("SE")) { probRecession = 0.145; }
		if (countryName.equals("SI")) { probRecession = 0.280; }
		if (countryName.equals("SK")) { probRecession = 0.140; }
		if (countryName.equals("TJ")) { probRecession = 0.076; }
		if (countryName.equals("TM")) { probRecession = 0.256; }
		if (countryName.equals("TN")) { probRecession = 0.042; }
		if (countryName.equals("TR")) { probRecession = 0.250; }
		if (countryName.equals("UA")) { probRecession = 0.350; }
		if (countryName.equals("US")) { probRecession = 0.059; }
		if (countryName.equals("UZ")) { probRecession = 0.012; }
		probGrowth = 1 - probRecession;
	}

	
}
