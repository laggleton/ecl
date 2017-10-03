package financialobjects;

import java.util.Date;

import referenceobjects.BusinessDate;
import referenceobjects.DateFormat;
import referenceobjects.stores.FxRateStore;

public class ECLResult implements Comparable<ECLResult> {
	private String dealId;
	private String facilityId;
	private String bookId;
	private Date asOfDate = BusinessDate.getInstance().getDate();
	private String tradeIdentifier; 
	private String firstDisbursementCurrency; 
	private Double twelveMonthECL;
	private Double lifetimeECL; 
	private int impairmentStageIFRS9; 
	private Double twelveMonthECLEUR; 
	private Double lifetimeECLEUR;
	private Date lastDateInStage2;
	private Date lastDateInStage3;
	private String stagingReason;

	private Double eir;
	private boolean converted = false;
	
	public ECLResult(String dealId, String facilityId, String bookId, String tradeIdentifier, String firstDisbursementCurrency) {
		this.dealId = dealId;
		this.facilityId = facilityId;
		this.bookId = bookId;
		this.tradeIdentifier = tradeIdentifier;
		this.firstDisbursementCurrency = firstDisbursementCurrency;
	}
	
	private void convertToEUR() {
		double eurFx = FxRateStore.getInstance().getCurrency("EUR").getAsOfDateRate();
		double disbFx = FxRateStore.getInstance().getCurrency(firstDisbursementCurrency).getAsOfDateRate();
		
		twelveMonthECLEUR = twelveMonthECL * disbFx / eurFx;
		lifetimeECLEUR = lifetimeECL * disbFx / eurFx;
		converted = true;
	}
	
	public double getProvisionEUR() {
		checkConvert();
		if (impairmentStageIFRS9 == 1) {
			return getTwelveMonthECLEUR();
		}
		else if (impairmentStageIFRS9 == 2) {
			return getLifetimeECLEUR();
		}
		else if (impairmentStageIFRS9 == 3) {
			return getLifetimeECLEUR();
		}
		return 0d;
	}
	
	private void checkConvert() {
		if (!converted) { convertToEUR(); }
	}

	/*
	 * Method to return data for output
	 * If you change this method you *must* change the static getHeader() method below
	 */
	public String toString(String delimiter) {
		checkConvert();
		return getTradeIdentifier() 
				+ delimiter + getFirstDisbursementCurrency() 
				+ delimiter + getTwelveMonthECL() 
				+ delimiter + getLifetimeECL() 
				+ delimiter + getImpairmentStageIFRS9() 
				+ delimiter + getStagingReason()
				+ delimiter + getTwelveMonthECLEUR() 
				+ delimiter + getLifetimeECLEUR() 
				+ delimiter + getProvisionEUR()
				+"\n";
	}
	
	/*
	 * Method to return data for output
	 * If you change this method you *must* change the static getFullReportHeader() method below
	 */
	public String toFullReport(String delimiter) {
		return getDealId() 
				+ delimiter + getFacilityId()
				+ delimiter + getTradeIdentifier()
				+ delimiter + getBookId()
				+ delimiter + DateFormat.ISO_FORMAT.format(getAsOfDate())
				+ delimiter + DateFormat.ISO_FORMAT.format(new Date())
				+ delimiter + getTwelveMonthECL() 
				+ delimiter + getLifetimeECL()
				+ delimiter + getFirstDisbursementCurrency()
				+ delimiter + getImpairmentStageIFRS9()
				+ delimiter + getStagingReason()
				+ delimiter + getEir()
				+ delimiter + getLastDateInStage2()
				+ delimiter + getLastDateInStage3()
				+"\n";
	}
	
	public static String getHeader(String delimiter) {
		return "ContractReference" 
				+ delimiter + "Currency" 
				+ delimiter + "12MECL" 
				+ delimiter + "LifetimeECL"
				+ delimiter + "ImpairmentStageIFRS9"
				+ delimiter + "StagingReason"
				+ delimiter + "12MECLEUR" 
				+ delimiter + "LifetimeECLEUR" 
				+ delimiter + "ProvisionEUR"
				+"\n";
	}
	
	public static String getFullReportHeader(String delimiter) {
		return "DealID"
				+ delimiter + "FacilityID"
				+ delimiter + "ContractReference"
				+ delimiter + "BookID"
				+ delimiter + "BalanceSheetDate"
				+ delimiter + "RunDate"
				+ delimiter + "12MECL" 
				+ delimiter + "LifetimeECL"
				+ delimiter + "Currency"
				+ delimiter + "StageAssessment"
				+ delimiter + "StagingReason"
				+ delimiter + "EIR"
				+ delimiter + "LastDateInStage2"
				+ delimiter + "LastDateInStage3"
				+"\n";
	}
	
	public String toString() {
		return toString(",");
	}
	
	public String toFullReport() {
		return toFullReport(",");
	}
	
	public String getStagingReason() {
		return stagingReason;
	}

	public void setStagingReason(String stagingReason) {
		this.stagingReason = stagingReason;
	}
	
	public Date getAsOfDate() {
		return asOfDate;
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

	public Double getTwelveMonthECL() {
		return twelveMonthECL;
	}

	public void setTwelveMonthECL(Double twelveMonthECL) {
		this.twelveMonthECL = twelveMonthECL;
	}

	public Double getLifetimeECL() {
		return lifetimeECL;
	}

	public void setLifetimeECL(Double lifetimeECL) {
		this.lifetimeECL = lifetimeECL;
	}

	public int getImpairmentStageIFRS9() {
		return impairmentStageIFRS9;
	}

	public void setImpairmentStageIFRS9(int impairmentStageIFRS9) {
		this.impairmentStageIFRS9 = impairmentStageIFRS9;
	}

	public Double getTwelveMonthECLEUR() {
		return twelveMonthECLEUR;
	}

	public void setTwelveMonthECLEUR(Double twelveMonthECLEUR) {
		this.twelveMonthECLEUR = twelveMonthECLEUR;
	}

	public Double getLifetimeECLEUR() {
		return lifetimeECLEUR;
	}

	public void setLifetimeECLEUR(Double lifetimeECLEUR) {
		this.lifetimeECLEUR = lifetimeECLEUR;
	}

	public Date getLastDateInStage2() {
		return lastDateInStage2;
	}

	public void setLastDateInStage2(Date lastDateInStage2) {
		this.lastDateInStage2 = lastDateInStage2;
	}

	public Date getLastDateInStage3() {
		return lastDateInStage3;
	}

	public void setLastDateInStage3(Date lastDateInStage3) {
		this.lastDateInStage3 = lastDateInStage3;
	}
	
	public Double getEir() {
		return eir;
	}

	public void setEir(Double eir) {
		this.eir = eir;
	}
	
	@Override
	public int compareTo(ECLResult e) {
		if (this.dealId.compareTo(e.dealId) > 0) {
			if (this.facilityId.compareTo(e.facilityId) > 0) {
				if (this.bookId.compareTo(e.bookId) > 0) {
					return -1;
				}
			}
		}
		return 1;
	}

}
