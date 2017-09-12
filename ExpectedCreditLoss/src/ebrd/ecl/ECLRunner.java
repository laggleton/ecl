package ebrd.ecl;

import referenceobjects.BusinessDate;
import utilities.PreferencesLoader;
import utilities.PreferencesStore;

public class ECLRunner {
	
	public static void main(String[] args) {
		
		PreferencesLoader pl = new PreferencesLoader("C:\\Users\\927624\\Documents\\EBRD Phase 2\\EBRD Test data\\EBRD (1)\\ebrd_ecl.preferences");
		pl.load();
		BusinessDate.getInstance().initialise(PreferencesStore.getInstance().getPreference(PreferencesStore.BUSINESS_DATE));
		
		ECLDataLoader dl = new ECLDataLoader();
		dl.loadFXRates();
		dl.loadData();
		dl.calculateECL();
		dl.printResults();
	}

}
