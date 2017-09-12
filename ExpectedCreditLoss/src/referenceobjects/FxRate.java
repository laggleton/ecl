package referenceobjects;

import java.util.Date;

public class FxRate implements Comparable<FxRate> {
	private Date rateDate;
	private double rate = 0d;
	
	public Date getRateDate() {
		return rateDate;
	}

	public double getRate() {
		return rate;
	}

	public FxRate(Date d, double r) {
		this.rateDate = d;
		this.rate = r;
	}
	
	@Override
	public int compareTo(FxRate f) {
		if (f.getRateDate().before(this.getRateDate())) { return 1; }
		return -1;
	}
}
