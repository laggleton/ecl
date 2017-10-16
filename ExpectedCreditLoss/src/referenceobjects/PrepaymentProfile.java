package referenceobjects;

import java.util.HashMap;
import java.util.Map;

public class PrepaymentProfile {
	private static Map<String, Double> nonSovRecessionPrepaymentRate = setNonSovRecessionPrepayments();
	private static Map<String, Double> nonSovGrowthPrepaymentRate = setNonSovGrowthPrepayments();
	private static Map<String, Double> sovereignPrepaymentRate = setSovPrepayments();
	
	private static Map<String, Double> setNonSovRecessionPrepayments() {
		Map<String, Double> map = new HashMap<String, Double>();
		map.put("Other", 0.031d);
		map.put(IndustryList.INDUSTRY, 0.049d);
		return map;
	}
	
	private static Map<String, Double> setNonSovGrowthPrepayments() {
		Map<String, Double> map = new HashMap<String, Double>();
		map.put("Other", 0.051d);
		map.put(IndustryList.INDUSTRY, 0.084d);
		return map;
	}
	
	private static Map<String, Double> setSovPrepayments() {
		Map<String, Double> map = new HashMap<String, Double>();
		map.put("Growth", 0.015d);
		map.put("Recession", 0.004d);
		return map;
	}
	
	public static Double getPrepaymentRate(Country ctry, String sovereignity, String industrySector) {
		Double probGrowth = ctry.getProbabilityOfGrowth();
		Double probRecession = ctry.getProbabilityOfRecession();
		Double rate = 0d;
		if (!sovereignity.equals("N")) {
			rate = (sovereignPrepaymentRate.get("Growth") * probGrowth) + (sovereignPrepaymentRate.get("Recession") * probRecession); 
		}
		else if (industrySector.equals(IndustryList.INDUSTRY)) {
			rate = (nonSovGrowthPrepaymentRate.get(IndustryList.INDUSTRY) * probGrowth) + (nonSovRecessionPrepaymentRate.get(IndustryList.INDUSTRY) * probRecession);
		}
		else {
			rate = (nonSovGrowthPrepaymentRate.get("Other") * probGrowth) + (nonSovRecessionPrepaymentRate.get("Other") * probRecession);
		}
		return rate;	
	}
}
