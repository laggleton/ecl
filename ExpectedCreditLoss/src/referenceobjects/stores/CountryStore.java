package referenceobjects.stores;

import java.util.HashMap;
import java.util.Map;

import referenceobjects.Country;

public class CountryStore {
	private static CountryStore instance;
	
	private Map<String, Country> countryMap = new HashMap<>();
	
	private CountryStore() {};
	
	public static synchronized CountryStore getInstance() {
		if (instance == null) {
			instance = new CountryStore();
		}
		return instance;
	}
	
	public void addCountry(Country ctry) {
		countryMap.put(ctry.getCountryName(), ctry);
	}
	
	public Country getCountry(String ctry) {
		if (!countryMap.containsKey(ctry)) {
			Country newCtry = new Country(ctry);
			countryMap.put(ctry,  newCtry);
		}
		return countryMap.get(ctry);
	}
	
	public int getSize() {
		return countryMap.size();
	}
	
	public void printAll() {
		System.out.println("Country,ProbGrowth,ProbRecession");
		for (Country ctry : countryMap.values()) {
			System.out.println(ctry.getCountryName() + "," + ctry.getProbabilityOfGrowth() + "," + ctry.getProbabilityOfRecession());
		}
	}

}
