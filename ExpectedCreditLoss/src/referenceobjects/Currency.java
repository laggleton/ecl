package referenceobjects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import utilities.Logger;

public class Currency {
	
	private List<FxRate> fxRates = new ArrayList<>();
	private double asOfDateRate = 1.0d;
	private String ccyName = "";
	
	public Currency(String ccy) {
		ccyName = ccy;
	}
	
	public List<FxRate> getFxRates() {
		Collections.sort(fxRates);
		return fxRates;
	}
	
	public void addFxRate(FxRate f) {
		fxRates.add(f);
		if (f.getRateDate().equals(BusinessDate.getInstance().getDate())) {
			asOfDateRate = f.getRate();
		}
	}
	
	public double getAsOfDateRate() {
		return asOfDateRate;
	}
	
	public String getCurrencyName() {
		return ccyName;
	}
	
	public FxRate getFxRate(Date d) {
		FxRate g = null;
		for (FxRate f : getFxRates()) {
			if (f.getRateDate().equals(d) || f.getRateDate().after(d)) {
				return f;
			}
			g = f;
		}
		Logger.getInstance().info("No FxRate for currency " + ccyName + " at Date " + DateFormat.ISO_FORMAT.format(d) + " returning latest rate");
		return g;
	}
	
}
