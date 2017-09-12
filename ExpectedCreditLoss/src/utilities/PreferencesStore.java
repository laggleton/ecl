package utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreferencesStore {
	private static PreferencesStore instance;
	
	public static final String CASHFLOW_FILE = "cashflow.file";
	public static final String CONTRACT_FILE = "contract.file";
	public static final String PD_FILE = "pd.file";
	public static final String GDP_FILE = "gdp.file";
	public static final String FX_FILE = "fx.file";
	public static final String DIRECTORY = "working.directory";
	public static final String BUSINESS_DATE = "business.date";
		
	private static final List<String> prefList = new ArrayList<>(
			Arrays.asList(
					CASHFLOW_FILE
					, CONTRACT_FILE
					, PD_FILE
					, GDP_FILE
					, DIRECTORY
					, FX_FILE
					, BUSINESS_DATE));
	
	private Map<String, String> preferencesMap = new HashMap<>();
	
	private PreferencesStore() {};
	
	public static synchronized PreferencesStore getInstance() {
		if (instance == null) {
			instance = new PreferencesStore();
		}
		return instance;
	}
	
	public void addPreference(String key, String value) {
		preferencesMap.put(key, value);
	}
	
	public String getPreference(String key) {
		if (!prefList.contains(key)) {
			System.out.println("WARN: invalid key requested from preferences : " + key);
			return "";
		}
		if (!preferencesMap.containsKey(key)) {
			return "";
		}
		return preferencesMap.get(key);
	}
	
	public int getSize() {
		return preferencesMap.size();
	}
	
	public void printAll() {
		System.out.println("Key,Value");
		for (String key : preferencesMap.keySet()) {
			System.out.println(key + "," + preferencesMap.get(key));
		}
	}
	
	public List<String> getPrefList() {
		return prefList;
	}
}
