package financialobjects;
import java.util.*;

import referenceobjects.BusinessDate;
import referenceobjects.DateFormat;
import referenceobjects.stores.FxRateStore;
import utilities.Logger;

import java.text.ParseException;

public class CashFlow implements Comparable<CashFlow>, Cloneable {
	private String currency;
	private Double amount;
	private CashFlowType cashFlowSubType;
	private String tradeDisbursementCurrency;
	private Double tradeDisbursementAmount;
	
	private Date cashFlowDate = new Date();
	private CashFlowType cashFlowType;
	private Logger l = Logger.getInstance();
		
	@Deprecated
	/*
	 * Should really format date before construction and not assume ISO
	 */
	public CashFlow(String currency, Double amount, String date, String type) {
		this.currency = currency;
		this.amount = amount;
		try {
			this.cashFlowDate = DateFormat.ISO_FORMAT.parse(date);
		}
		catch(ParseException pe) {
			System.out.println("ERROR: could not parse date in string \"" +
	                date + "\"");
			System.out.println(pe.toString());
			System.out.println(pe.getStackTrace());
		}
		mapCashFlowType(type);
	}
	
	private void mapCashFlowType(String type) {
		if ((type.equalsIgnoreCase("FEF")
				|| type.equalsIgnoreCase("F/F"))) {
			this.cashFlowType = CashFlowType.FEE;
			this.cashFlowSubType = CashFlowType.FEF;
		}
		else if (type.equalsIgnoreCase("APP")) {
			this.cashFlowType = CashFlowType.FEE;
			this.cashFlowSubType = CashFlowType.APP;
		}
		else if (type.equalsIgnoreCase("APR")) {
			this.cashFlowType = CashFlowType.FEE;
			this.cashFlowSubType = CashFlowType.APR;
		}
		else if (type.equalsIgnoreCase("FEE")) {
			this.cashFlowType = CashFlowType.FEE;
			this.cashFlowSubType = CashFlowType.FEE;
		}
		else if (type.equalsIgnoreCase("Principal")) {
			this.cashFlowType = CashFlowType.XNL;
			this.cashFlowSubType = CashFlowType.PRINCIPAL;
		}
		else if ((type.equalsIgnoreCase("Disbursement")) 
				|| (type.equalsIgnoreCase("DISB")) 
				|| (type.equalsIgnoreCase("XNL"))
				|| (type.equalsIgnoreCase("RDISB"))
				|| (type.equalsIgnoreCase("FUTURE_DISB"))) {
			this.cashFlowType = CashFlowType.XNL;
			this.cashFlowSubType = CashFlowType.DISBURSEMENT;
		}
		else if ((type.equalsIgnoreCase("Repayment")) 
				|| (type.equalsIgnoreCase("SREP"))
				|| (type.equalsIgnoreCase("FUTURE_SREP"))
				|| (type.equalsIgnoreCase("RREP"))
				|| (type.equalsIgnoreCase("ASREP"))) {
			this.cashFlowType = CashFlowType.XNL;
			this.cashFlowSubType = CashFlowType.REPAYMENT;
		}
		else if ((type.equalsIgnoreCase("Prepayment")) 
				|| (type.equalsIgnoreCase("FUTP"))
				|| (type.equalsIgnoreCase("APREP"))) {
			this.cashFlowType = CashFlowType.XNL;
			this.cashFlowSubType = CashFlowType.PREPAYMENT;
		}
		else if ((type.equalsIgnoreCase("Interest"))
				|| (type.equalsIgnoreCase("INT")) ) {
			this.cashFlowType = CashFlowType.INT;
			this.cashFlowSubType = CashFlowType.INTEREST;
		}
		else if ((type.equalsIgnoreCase("Expense"))
				|| (type.equalsIgnoreCase("EXP")) 
				|| (type.equalsIgnoreCase("Expenses")) ) {
			this.cashFlowType = CashFlowType.EXP;
			this.cashFlowSubType = CashFlowType.EXPENSE;
		}
		else {
			Logger.getInstance().warn("Cash Flow Type " + cashFlowType + "not recognised!");
		}
	}
	
	public CashFlow(String currency, Double amount, Date date, String type) {
		this.currency = currency;
		this.amount = amount;
		this.cashFlowDate = date;
		mapCashFlowType(type);
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
		updateTradeDisbursementAmount();
	}
	
	public void updateTradeDisbursementAmount() {
		FxRateStore fxS = FxRateStore.getInstance();
		if (!tradeDisbursementCurrency.equals(currency)) {
			double fxRate = 1.0d;
			double flowRate = fxS.getCurrency(currency).getFxRate(this.cashFlowDate).getRate();
			double disburseRate = fxS.getCurrency(tradeDisbursementCurrency).getFxRate(this.cashFlowDate).getRate();
			fxRate = flowRate / disburseRate; 
			tradeDisbursementAmount = fxRate * amount;
		}
		else {
			tradeDisbursementAmount = amount;
		}
	}

	public Double getTradeDisbursementAmount() {
		if (null == tradeDisbursementAmount) { updateTradeDisbursementAmount(); }
		return tradeDisbursementAmount;
	}
	
	public void setTradeDisbursementAmount(Double amt) {
		this.tradeDisbursementAmount = amt;
	}
	
	public CashFlowType getCashFlowSubType() {
		return cashFlowSubType;
	}

	public void setCashFlowSubType(CashFlowType cashFlowSubType) {
		this.cashFlowSubType = cashFlowSubType;
	}

	@Override
	public int compareTo(CashFlow comparisonCF) {
		if (comparisonCF.getCashFlowDate().before(this.getCashFlowDate())) {
			return 1;
		}
		if (comparisonCF.getCashFlowDate().equals(this.getCashFlowDate())) { return 0; }
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
	
	public String toDisbursementCcyFormat(String delimiter) {
		return DateFormat.ISO_FORMAT.format(getCashFlowDate())
			+ delimiter + DateFormat.ISO_FORMAT.format(BusinessDate.getInstance().getDate())
			+ delimiter + getCashFlowType().toString()
			+ delimiter + getCashFlowSubType()
			+ delimiter + getTradeDisbursementCurrency()
			+ delimiter + getTradeDisbursementAmount()
			+ "\n";
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
	
	public static String getDisbursementHeader(String delimiter) {
		return "CashFlowDate"
			+ delimiter + "BalanceSheetDate"
			+ delimiter + "CashFlowType"
			+ delimiter + "CashFlowSubType"
			+ delimiter + "CommitmentCurrency"
			+ delimiter + "CommitmentAmount"
			+ "\n";
	}
	
	
	public CashFlow clone() {
		CashFlow cf = new CashFlow(this.currency, this.amount, this.cashFlowDate, this.cashFlowSubType.toString());
		cf.setTradeDisbursementCurrency(getTradeDisbursementCurrency());
		return cf;
    }
	
}
