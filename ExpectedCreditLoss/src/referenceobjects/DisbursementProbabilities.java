package referenceobjects;

import java.util.HashMap;
import java.util.Map;

import utilities.Logger;

public class DisbursementProbabilities {
	private static Map<Integer, Double> sovMap = createSovereignMap();
	private static Map<Integer, Double> nonSovIndustryMap = createNonSovIndustryMap();
	private static Map<Integer, Double> nonSovFinanceMap = createNonSovFinanceMap();
	private static Map<Integer, Double> nonSovInfraMap = createNonSovInfraMap();
	private static Map<Integer, Double> nonSovEnergyMap = createNonSovEnergyMap();
	
	private static Map<Integer, Double> createSovereignMap() {
        Map<Integer, Double> myMap = new HashMap<Integer, Double>();
        myMap.put(1,0.075d);
        myMap.put(2,0.0548d);
        myMap.put(3,0.0816d);
        myMap.put(4,0.1105d);
        myMap.put(5,0.1586d);
        myMap.put(6,0.134d);
        myMap.put(7,0.1102d);
        myMap.put(8,0.0958d);
        myMap.put(9,0.0822d);
        myMap.put(10,0.0969d);
        return myMap;
    }
	
	private static Map<Integer, Double> createNonSovIndustryMap() {
        Map<Integer, Double> myMap = new HashMap<Integer, Double>();
        myMap.put(1,0.23542d);
        myMap.put(2,0.16332d);
        myMap.put(3,0.18662d);
        myMap.put(4,0.07932d);
        myMap.put(5,0.08212d);
        myMap.put(6,0.05512d);
        myMap.put(7,0.03302d);
        myMap.put(8,0.03172d);
        myMap.put(9,0.03222d);
        myMap.put(10,0.10112d);
        return myMap;
    }
	
	private static Map<Integer, Double> createNonSovFinanceMap() {
        Map<Integer, Double> myMap = new HashMap<Integer, Double>();
        myMap.put(1,0.39527d);
        myMap.put(2,0.17547d);
        myMap.put(3,0.08907d);
        myMap.put(4,0.06727d);
        myMap.put(5,0.05557d);
        myMap.put(6,0.02847d);
        myMap.put(7,0.03557d);
        myMap.put(8,0.03677d);
        myMap.put(9,0.02287d);
        myMap.put(10,0.09367d);
        return myMap;
    }
	
	private static Map<Integer, Double> createNonSovInfraMap() {
        Map<Integer, Double> myMap = new HashMap<Integer, Double>();
        myMap.put(1,0.18467d);
        myMap.put(2,0.10017d);
        myMap.put(3,0.09607d);
        myMap.put(4,0.11717d);
        myMap.put(5,0.09907d);
        myMap.put(6,0.07377d);
        myMap.put(7,0.08377d);
        myMap.put(8,0.07217d);
        myMap.put(9,0.06337d);
        myMap.put(10,0.10977d);
        return myMap;
    }
	
	private static Map<Integer, Double> createNonSovEnergyMap() {
        Map<Integer, Double> myMap = new HashMap<Integer, Double>();
        myMap.put(1,0.12527d);
        myMap.put(2,0.12747d);
        myMap.put(3,0.16017d);
        myMap.put(4,0.08687d);
        myMap.put(5,0.08477d);
        myMap.put(6,0.09677d);
        myMap.put(7,0.06957d);
        myMap.put(8,0.04807d);
        myMap.put(9,0.08977d);
        myMap.put(10,0.11127d);
        return myMap;
    }
	
	public static  Map<Integer, Double> getProfile(String sovereignRiskType, String industrySector) {
		if (!sovereignRiskType.equals("N")) {
			return sovMap;
		}
		switch (industrySector) {
			case "Industry, Commerce & AgriBusiness": return nonSovIndustryMap;
			case "Financial Institutions": return nonSovFinanceMap;
			case "Infrastructure": return nonSovInfraMap;
			case "Energy": return nonSovEnergyMap;
		}
		Logger.getInstance().error("No disbursement profile for sovType " + sovereignRiskType + " and industrySector " + industrySector);
		return null;
	}
}
