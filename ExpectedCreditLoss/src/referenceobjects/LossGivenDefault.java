package referenceobjects;

import java.util.Date;

public class LossGivenDefault implements Comparable<LossGivenDefault> { 
	private Date defaultDate;
	private Double loss;
	
	public LossGivenDefault(Date defDate, Double loss) {
		this.defaultDate = defDate;
		this.loss = loss;
	}
		
	public Date getDate() { return defaultDate; }
	public Double getLoss() { return loss; }

	@Override
	public int compareTo(LossGivenDefault comparisonLGD) {
		if (comparisonLGD.getDate().after(this.getDate())) {
			return 1;
		}
		return -1;
	}

}
