package referenceobjects.stores;

import java.util.HashMap;
import java.util.Map;

import referenceobjects.Currency;

public class FxRateStore {
	private static FxRateStore instance;
	
	private Map<String, Currency> currencyMap = new HashMap<>();
	
	private FxRateStore() {};
	
	public static synchronized FxRateStore getInstance() {
		if (instance == null) {
			instance = new FxRateStore();
		}
		return instance;
	}
	
	public void addCurrency(Currency ccy) {
		currencyMap.put(ccy.getCurrencyName(), ccy);
	}
	
	public Currency getCurrency(String ccy) {
		if (!currencyMap.containsKey(ccy)) {
			Currency newCcy = new Currency(ccy);
			currencyMap.put(ccy,  newCcy);
		}
		return currencyMap.get(ccy);
	}
	
	public void printAll() {
		System.out.println("Currency");
		for (Currency c : currencyMap.values()) {
			System.out.println(c.getCurrencyName());
		}
	}

}
