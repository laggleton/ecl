package utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreferencesStore {
	private static PreferencesStore instance;
	
	public static final String CASHFLOW_FILE = "cashflow.file";
	public static final String FEE_FILE = "fee.file";
	public static final String CONTRACT_FILE = "contract.file";
	public static final String PD_FILE = "pd.file";
	public static final String GDP_FILE = "gdp.file";
	public static final String CASHFLOW_FILE_DELIMITER = "cashflow.file.delimiter";
	public static final String FEE_FILE_DELIMITER = "fee.file.delimiter";
	public static final String CONTRACT_FILE_DELIMITER = "contract.file.delimiter";
	public static final String PD_FILE_DELIMITER = "pd.file.delimiter";
	public static final String GDP_FILE_DELIMITER = "gdp.file.delimiter";
	public static final String FX_FILE_DELIMITER = "fx.file.delimiter";
	public static final String FX_FILE = "fx.file";
	public static final String DIRECTORY = "working.directory";
	public static final String BUSINESS_DATE = "business.date";
	public static final String OUTPUT_DELIMITER = "outputfile.delimiter";
	public static final String LOGGER_LEVEL = "logger.level";
		
	private static final List<String> prefList = new ArrayList<>(
			Arrays.asList(
					CASHFLOW_FILE
					, CASHFLOW_FILE_DELIMITER
					, FEE_FILE_DELIMITER
					, FX_FILE_DELIMITER
					, CONTRACT_FILE_DELIMITER
					, PD_FILE_DELIMITER
					, GDP_FILE_DELIMITER
					, LOGGER_LEVEL
					, CONTRACT_FILE
					, PD_FILE
					, FEE_FILE
					, GDP_FILE
					, DIRECTORY
					, FX_FILE
					, BUSINESS_DATE
					, OUTPUT_DELIMITER));
	
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
