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

	
}
