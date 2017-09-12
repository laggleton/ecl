package financialobjects;
import java.util.*;

public class Instrument {
	private String identifier;
	private List<Position> posList;
	
	public Instrument(String ident){
        identifier = ident;
        this.posList = new ArrayList<>();
    }
	
	public String getIdentifier() {
        return identifier;
    }

	public void addPosition(Position pos) {
		posList.add(pos);
	}
}
