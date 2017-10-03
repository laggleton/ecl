package referenceobjects;
import java.util.*;

public class ProbabilityOfDefault implements Comparable<ProbabilityOfDefault> {
	private Date defaultDate;
	private String rating;
	private Double pd;
	private Double growthPD;
	private Double recessionPD;
	private double recessionCoefficient = 0.17d;
	private double recessionToGrowthRatio = 2.44d;
	private double growthCoefficient = 0.83d;
	
	/*
	 * Use following variables to remove multi-economic scenario effect
	 */
	//private double recessionCoefficient = 0.5d;
	//private double recessionToGrowthRatio = 1.0d;
	//private double growthCoefficient = 0.5d;
	
	
	public ProbabilityOfDefault(String rating, Date defDate, Double pd) {
		this.defaultDate = defDate;
		this.rating = rating;
		this.pd = pd;
		
		growthPD = pd / ((recessionCoefficient * recessionToGrowthRatio) + growthCoefficient);
		recessionPD = growthPD * recessionToGrowthRatio;
	}
	
	public String getRating() { return rating; }
	public Date getDate() { return defaultDate; }
	public Double getPD() { return pd; }

	@Override
	public int compareTo(ProbabilityOfDefault comparisonPD) {
		if (comparisonPD.getDate().before(this.getDate())) {
			return 1;
		}
		return -1;
	}
	
	public Double getGrowthPD() { return growthPD; }
	public Double getRecessionPD() { return recessionPD; }

	public void setPD(double d) {
		this.pd = d;
	}

	public String toString(String delimiter) {
		return getRating()
				+ delimiter + DateFormat.ISO_FORMAT.format(getDate())
				+ delimiter + getGrowthPD()
				+ delimiter + getRecessionPD()
				+ delimiter + getPD();
	}
	
	public static String getHeader(String delimiter) {
		return "Rating"
				+ delimiter + "Date"
				+ delimiter + "GrowthPD"
				+ delimiter + "RecessionPD"
				+ delimiter + "PD";
	}
}
