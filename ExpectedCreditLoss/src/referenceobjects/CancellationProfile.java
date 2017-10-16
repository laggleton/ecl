package referenceobjects;

import java.util.HashMap;
import java.util.Map;

import utilities.Logger;

public class CancellationProfile {
	
	
	private static Map<String, Double> nonSovCancellationRate = setNonSovCancellations();
	private static Map<String, Double> sovereignCancellationRate = setSovCancellations();
	
	private static Map<String, Double> setNonSovCancellations() {
		Map<String, Double> map = new HashMap<String, Double>();
		map.put(IndustryList.ENERGY, 0.085d);
		map.put(IndustryList.FINANCE, 0.113d);
		map.put(IndustryList.INDUSTRY, 0.149d);
		map.put(IndustryList.INFRA, 0.097d);
		return map;
	}
	
	private static Map<String, Double> setSovCancellations() {
		Map<String, Double> map = new HashMap<String, Double>();
		map.put(IndustryList.ENERGY, 0.042d);
		map.put(IndustryList.FINANCE, 0.080d);
		map.put(IndustryList.INDUSTRY, 0.074d);
		map.put(IndustryList.INFRA, 0.020d);
		return map;
	}
	
	public static Double getCancellationRate(String sovereignity, String industrySector) {
		if (!IndustryList.getIndustryList().contains(industrySector)) {
			Logger.getInstance().error("No cancellation rate for sector " + industrySector);
			return 0d;
		}
		if (!sovereignity.equals("N")) {
			return sovereignCancellationRate.get(industrySector);
		}
		return nonSovCancellationRate.get(industrySector);
	}
}
