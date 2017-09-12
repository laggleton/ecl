package ebrd.ecl;

import utilities.PreferencesLoader;

public class ECLRunner {
	
	public static void main(String[] args) {
		
		PreferencesLoader pl = new PreferencesLoader("C:\\Users\\927624\\Documents\\EBRD Phase 2\\EBRD Test data\\EBRD (1)\\ebrd_ecl.preferences");
		pl.load();
		
		ECLDataLoader dl = new ECLDataLoader();
		dl.loadData();
		dl.calculateECL();
		dl.printResults();
	}

}
