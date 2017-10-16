package referenceobjects;

import java.util.ArrayList;
import java.util.List;

public class IndustryList {
	public static String ENERGY = "Energy";
	public static String FINANCE = "FinancialInstitutions";
	public static String INDUSTRY = "IndustryCommerceAgribusiness";
	public static String INFRA = "Infrastructure";
	
	private static List<String> industryList = setUpIndustryList(); 
	
	private static List<String> setUpIndustryList() {
		List<String> myList = new ArrayList<>();
		myList.add(ENERGY);
		myList.add(FINANCE);
		myList.add(INDUSTRY);
		myList.add(INFRA);
		return myList;
	}
	
	public static List<String> getIndustryList() {
		return industryList;
	}
}
