package financialobjects;
import java.util.*;

public class Position {
	private String positionId;
	private String bookId;
	private List<Trade> tradeList;
	
	public Position(String instrumentId, String posId, String bookId) {
		this.positionId = posId;
		this.bookId = bookId;
		tradeList = new ArrayList<>();
		
		Instrument ins = new Instrument(instrumentId);
		//TODO get instrument
	}
	
	public void addTrade(Trade tr) {
		tradeList.add(tr);
	}
	
	public List<Trade> getTradeList() { return tradeList; }
}
