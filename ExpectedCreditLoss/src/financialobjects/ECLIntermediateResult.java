package financialobjects;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import referenceobjects.BusinessDate;
import referenceobjects.DateFormat;
import utilities.DateTimeUtils;

public class ECLIntermediateResult implements Comparable<ECLIntermediateResult> {
	private String dealId;
	private String facilityId;
	private String bookId;
	private String tradeIdentifier;
	private String firstDisbursementCurrency;
	private Date asOfDate = BusinessDate.getInstance().getDate();
	private Date periodDate;
	private Double probabilityOfDefault;
	private Double lgd;
	private Double ead;
	private Double discountFactor;
	private Double incrementalPD;
	private Double periodECL;
	
	public ECLIntermediateResult(String dealId, String facilityId, String bookId, String tradeIdentifier, String firstDisbursementCurrency) {
		this.dealId = dealId;
		this.facilityId = facilityId;
		this.bookId = bookId;
		this.tradeIdentifier = tradeIdentifier;
		this.firstDisbursementCurrency = firstDisbursementCurrency;
	}
	
	public String getTenor() {
		Date asOfDate = BusinessDate.getInstance().getDate();
		long dayDiff = DateTimeUtils.getDateDiff(asOfDate, getPeriodDate(), TimeUnit.DAYS);
		int monthDiff = (int) (dayDiff / DateTimeUtils.MONTHLY);
		
		return monthDiff + "M";
	}
	
	/*
	 * Method to return data for output
	 * If you change this method you *must* change the static getHeader() method below
	 */
	public String toString(String delimiter) {
		return getTenor()
				+ delimiter + getPeriodDate()
				+ delimiter + getProbabilityOfDefault()
				+ delimiter + getLgd()
				+ delimiter + getEad()
				+ delimiter + getFirstDisbursementCurrency()
				+ "\n";
	}
	
	public String toFullString(String delimiter) {
		return getTenor()
				+ delimiter + getPeriodDate()
				+ delimiter + getProbabilityOfDefault()
				+ delimiter + getIncrementalPD()
				+ delimiter + getLgd()
				+ delimiter + getEad()
				+ delimiter + getDiscountFactor()
				+ delimiter + getFirstDisbursementCurrency()
				+ "\n";
	}
	
	public String toFullString(String delimiter, int i) {
		return i + "M"
				+ delimiter + getPeriodDate()
				+ delimiter + getProbabilityOfDefault()
				+ delimiter + getIncrementalPD()
				+ delimiter + getLgd()
				+ delimiter + getEad()
				+ delimiter + getDiscountFactor()
				+ delimiter + getFirstDisbursementCurrency()
				+ "\n";
	}
	
	public static String getHeader(String delimiter) {
		return "Tenor" 
				+ delimiter + "PeriodDate"
				+ delimiter + "PD"
				+ delimiter + "LGD"
				+ delimiter + "EAD"
				+ delimiter + "Currency"
				+ "\n";
	}
	
	public static String getFullHeader(String delimiter) {
		return "Tenor" 
				+ delimiter + "PeriodDate"
				+ delimiter + "PD"
				+ delimiter + "IncrementalPD"
				+ delimiter + "LGD"
				+ delimiter + "EAD"
				+ delimiter + "DF"
				+ delimiter + "Currency"
				+ "\n";
	}
	
	public String getDealId() {
		return dealId;
	}

	public String getFacilityId() {
		return facilityId;
	}

	public String getBookId() {
		return bookId;
	}

	public String getTradeIdentifier() {
		return tradeIdentifier;
	}

	public String getFirstDisbursementCurrency() {
		return firstDisbursementCurrency;
	}

	public Double getIncrementalPD() {
		return incrementalPD;
	}

	public void setIncrementalPD(Double incrementalPD) {
		this.incrementalPD = incrementalPD;
	}

	public Double getPeriodECL() {
		return periodECL;
	}

	public void setPeriodECL(Double periodECL) {
		this.periodECL = periodECL;
	}

	public Date getPeriodDate() {
		return periodDate;
	}

	public void setPeriodDate(Date periodDate) {
		this.periodDate = periodDate;
	}

	public Double getProbabilityOfDefault() {
		return probabilityOfDefault;
	}

	public void setProbabilityOfDefault(Double probabilityOfDefault) {
		this.probabilityOfDefault = probabilityOfDefault;
	}

	public Double getLgd() {
		return lgd;
	}

	public void setLgd(Double lgd) {
		this.lgd = lgd;
	}

	public Double getEad() {
		return ead;
	}

	public void setEad(Double ead) {
		this.ead = ead;
	}

	public Double getDiscountFactor() {
		return discountFactor;
	}

	public void setDiscountFactor(Double discountFactor) {
		this.discountFactor = discountFactor;
	}

	@Override
	public int compareTo(ECLIntermediateResult e) {
		if (this.dealId.compareTo(e.dealId) > 0) {
			if (this.facilityId.compareTo(e.facilityId) > 0) {
				if (this.bookId.compareTo(e.bookId) > 0) {
					if (this.periodDate.after(e.periodDate)) {
						return -1;
					}
				}
			}
		}
		return 1;
	}
	
}
