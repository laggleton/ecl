package referenceobjects;

public class GrossDomesticProduct implements Comparable<GrossDomesticProduct> {
	private int referenceYear;
	private Double actualGrowth;
	private Double oneYearPredictedGrowth;
	private Double twoYearPredictedGrowth;
	
	public GrossDomesticProduct(int referenceYear) {
		this.referenceYear = referenceYear;
	}
	
	public int getReferenceYear() {
		return referenceYear;
	}
	public void setReferenceYear(int referenceYear) {
		this.referenceYear = referenceYear;
	}
	public Double getActualGrowth() {
		return actualGrowth;
	}
	public void setActualGrowth(Double actualGrowth) {
		this.actualGrowth = actualGrowth;
	}
	public Double getOneYearPredictedGrowth() {
		return oneYearPredictedGrowth;
	}
	public void setOneYearPredictedGrowth(Double oneYearPredictedGrowth) {
		this.oneYearPredictedGrowth = oneYearPredictedGrowth;
	}
	public Double getTwoYearPredictedGrowth() {
		return twoYearPredictedGrowth;
	}
	public void setTwoYearPredictedGrowth(Double twoYearPredictedGrowth) {
		this.twoYearPredictedGrowth = twoYearPredictedGrowth;
	}

	@Override
	public int compareTo(GrossDomesticProduct comparisonGDP) {
		if (comparisonGDP.getReferenceYear() < this.getReferenceYear()) { return 1; }
		return -1;
	}
	

}
