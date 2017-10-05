package referenceobjects;

import java.util.HashMap;
import java.util.Map;

public class CancellationProfile {
	private static Map<String, Double> nonSovCancellationRate = setNonSovCancellations();
	private static Map<String, Double> sovereignCancellationRate = setSovCancellations();
	
	private static Map<String, Double> setNonSovCancellations() {
		Map<String, Double> map = new HashMap<String, Double>();
		map.put("Energy", 0.085d);
		map.put("Financial Institutions", 0.113d);
		map.put("Industry, Commerce & Agribusiness", 0.149d);
		map.put("Infrastructure", 0.097d);
		return map;
	}
	
	private static Map<String, Double> setSovCancellations() {
		Map<String, Double> map = new HashMap<String, Double>();
		map.put("Energy", 0.042d);
		map.put("Financial Institutions", 0.080d);
		map.put("Industry, Commerce & Agribusiness", 0.074d);
		map.put("Infrastructure", 0.020d);
		return map;
	}
	
	public static Double getCancellationRate(String sovereignity, String industrySector) {
		if (!sovereignity.equals("N")) {
			return sovereignCancellationRate.get(industrySector);
		}
		return nonSovCancellationRate.get(industrySector);
	}
}
