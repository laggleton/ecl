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
	
	public static String upgradeRating(String r) {
		if (r.equals("1.7")) { r = "1.0"; }
		else if (r.equals("8.0")) { r = "7.3"; }
		else if (r.endsWith(".3")) {
			r = r.replaceAll(".3", ".0");
		}
		else if (r.endsWith(".7")) {
			r = r.replaceAll(".7", ".3");
		}
		else if (r.endsWith(".0")) {
			Double i = new Double(r);
			i -= 0.3d;
			r = i.toString();
		}
		
		return r;
	}
	
	public static String downgradeRating(String r) {
		if (r.equals("7.3")) { r = "8.0"; }
		else if (r.equals("1.0")) { r = "1.7"; }
		else if (r.endsWith(".3")) {
			r = r.replaceAll(".3", ".7");
		}
		else if (r.endsWith(".7")) {
			Double i = new Double(r);
			i += 0.3d;
			r = i.toString();
		}
		else if (r.endsWith(".0")) {
			r = r.replaceAll(".0", ".3");
		}
		
		return r;
	}
}
