package financialobjects.stores;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import financialobjects.Trade;

public class TradeStore {
	private static TradeStore instance;
	
	private Map<String, Trade> tradeMap = new HashMap<>();
	
	private TradeStore() {};
	
	public static synchronized TradeStore getInstance() {
		if (instance == null) {
			instance = new TradeStore();
		}
		return instance;
	}
	
	public void addTrade(Trade t) {
		tradeMap.put(t.getTradeIdentifier(), t);
	}
	
	public Trade getTrade(String id) {
		if (!tradeMap.containsKey(id)) {
//			System.out.println("WARN: trade " + id + " not available");
		}
		return tradeMap.get(id);
	}
	
	public int getSize() {
		return tradeMap.size();
	}
	
	public Collection<Trade> getAllTrades() {
		return tradeMap.values();
	}
	
	public void printAll() {
		for (Trade t : tradeMap.values()) {
			t.print();
		}
	}

}
