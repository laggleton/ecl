package utilities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import utilities.PreferencesStore;

public class PreferencesLoader {
	
	String fileName = null;
	
	public PreferencesLoader(String fileName) {
		this.fileName = fileName;
	}
	
	public void load() {
		if (null == fileName) {
			Logger.getInstance().error("No fileName specified for preferences file!");
			return;
		}
		String line = "";
		String[] lineArray;
				
		PreferencesStore prefStore = PreferencesStore.getInstance();
				
		FileReader fr = null;
		BufferedReader sc = null;
		String key = "";
		String value = "";
		
		try {
			
			fr = new FileReader(fileName);
			sc = new BufferedReader(fr);
			
			while ((line = sc.readLine()) != null) {
			
				lineArray = line.split("=");
				
				key = cleanMe(lineArray[0]);
				value = cleanMe(lineArray[1]);
				
				if (!prefStore.getPrefList().contains(key)) {
					System.out.println("WARN: preference " + key + " is not a recognised preference");
				}
				prefStore.addPreference(key, value);
			}
		}
		catch (Exception fe) {
			fe.printStackTrace();
			System.out.println(line);
		}
		finally {
			try {
				if (sc != null) { sc.close(); }
				if (fr != null) { fr.close(); }
			} 
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		System.out.println("Loaded " + prefStore.getSize() + " preferences");
	}
	
	private String cleanMe(String s) {
		String p = "^\\s+";
		String q = "\\s+$";
		
		s = s.replaceAll(p, "");
		s = s.replaceAll(q, "");
		
		return s;
	}
}
