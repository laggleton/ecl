package financialobjects;
import java.util.*;

import referenceobjects.BusinessDate;
import referenceobjects.DateFormat;
import referenceobjects.stores.FxRateStore;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class CashFlow implements Comparable<CashFlow>{
	private String currency;
	private Double amount;
	
	private String tradeDisbursementCurrency;
	private Double tradeDisbursementAmount;
	
	private Date cashFlowDate = new Date();
	private CashFlowType cashFlowType;
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	
	public CashFlow(String currency, Double amount, String date, CashFlowType type) {
		this.currency = currency;
		this.amount = amount;
		try {
			this.cashFlowDate = format.parse(date);
		}
		catch(ParseException pe) {
			System.out.println("ERROR: could not parse date in string \"" +
	                date + "\"");
			System.out.println(pe.toString());
			System.out.println(pe.getStackTrace());
		}
		this.cashFlowType = type;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public Date getCashFlowDate() {
		return cashFlowDate;
	}

	public void setCashFlowDate(Date cashFlowDate) {
		this.cashFlowDate = cashFlowDate;
	}

	public CashFlowType getCashFlowType() {
		return cashFlowType;
	}

	public void setCashFlowType(CashFlowType cashFlowType) {
		this.cashFlowType = cashFlowType;
	}
	
	public String getTradeDisbursementCurrency() {
		return tradeDisbursementCurrency;
	}

	public void setTradeDisbursementCurrency(String tradeDisbursementCurrency) {
		this.tradeDisbursementCurrency = tradeDisbursementCurrency;
		if (!tradeDisbursementCurrency.equals(currency)) {
			double fxRate = 1.0d;
			fxRate = FxRateStore.getInstance().getCurrency(getCurrency()).getFxRate(getCashFlowDate()).getRate() / FxRateStore.getInstance().getCurrency(getTradeDisbursementCurrency()).getFxRate(getCashFlowDate()).getRate();
			tradeDisbursementAmount = fxRate * amount;
		}
	}

	public Double getTradeDisbursementAmount() {
		return tradeDisbursementAmount;
	}

	@Override
	public int compareTo(CashFlow comparisonCF) {
		if (comparisonCF.getCashFlowDate().before(this.getCashFlowDate())) {
			return 1;
		}
		return -1;
	}
	
	public boolean isCashFlowInEAD() {
		if (cashFlowDate.after(BusinessDate.getInstance().getDate())) {
			if ((cashFlowType.equals(CashFlowType.DISBURSEMENT))
					|| (cashFlowType.equals(CashFlowType.INTEREST))
					|| (cashFlowType.equals(CashFlowType.REPAYMENT))
					|| (cashFlowType.equals(CashFlowType.PREPAYMENT))) {
				return true;
			}
		}
		return false;
	}
	
	public String toString(String delimiter) {
		return DateFormat.ISO_FORMAT.format(getCashFlowDate())
			+ delimiter + getCashFlowType()
			+ delimiter + getCurrency()
			+ delimiter + getAmount()
			+ delimiter + getTradeDisbursementCurrency()
			+ delimiter + getTradeDisbursementAmount()
			+ delimiter + isCashFlowInEAD()
			+ "\n";
	}
	
	public static String getHeader(String delimiter) {
		return "CashFlowDate"
			+ delimiter + "CashFlowType"
			+ delimiter + "CashFlowCurrency"
			+ delimiter + "CashFlowAmount"
			+ delimiter + "CommitmentCurrency"
			+ delimiter + "CommitmentAmount"
			+ delimiter + "IsCashFlowInEAD"
			+ "\n";
	}
}
