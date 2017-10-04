package referenceobjects;

import java.util.HashMap;
import java.util.Map;

public class DisbursementProbabilities {
	private static Map<Integer, Double> sovList = createSovereignMap();
	private static Map<Integer, Double> nonSovList = new HashMap<>();
	
	private static Map<Integer, Double> createSovereignMap() {
        Map<Integer, Double> myMap = new HashMap<Integer, Double>();
        myMap.put(1, 0.23d);
        myMap.put(2, 0.33d);
        return myMap;
    }
	
	public static  Map<Integer, Double> getProfile(String sovereignRiskType, String industrySector) {
		if (sovereignRiskType.equals("Y")) {
			return sovList;
		}
		return nonSovList;
	}
}
