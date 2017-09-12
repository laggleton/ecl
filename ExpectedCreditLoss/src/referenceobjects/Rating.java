package referenceobjects;

import java.util.ArrayList;
import java.util.List;

public class Rating {
	private String creditRating;
	private List<ProbabilityOfDefault> pds = new ArrayList<>();
	
	public Rating(String rating) {
		creditRating = rating;
	}
	
	public void addPD(ProbabilityOfDefault pd) {
		pds.add(pd);
	}
	
	public void addPDs(List<ProbabilityOfDefault> pds) {
		this.pds = pds;
	}
	
	public List<ProbabilityOfDefault> getPDs() {
		return pds;
	}
	
	public String getRating() {
		return creditRating;
	}
}
