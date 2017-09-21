package utilities;

import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    private static final String DEFAULT_DELIMITER = ",";
    private static final String DEFAULT_QUOTE = "\"";

    public static List<String> parseLine(String cvsLine) {
        return parseLine(cvsLine, DEFAULT_DELIMITER, DEFAULT_QUOTE);
    }

    public static List<String> parseLine(String cvsLine, String delimiter) {
        return parseLine(cvsLine, delimiter, DEFAULT_QUOTE);
    }

    public static List<String> parseLine(String line, String delimiter, String customQuote) {

        List<String> result = new ArrayList<>();

        //if empty, return!
        if (line == null && line.isEmpty()) {
            return result;
        }

        if (customQuote == " ") {
            customQuote = DEFAULT_QUOTE;
        }

        if (delimiter == " ") {
            delimiter = DEFAULT_DELIMITER;
        }
        
        line = line.replaceAll(DEFAULT_QUOTE, "");
        String[] lineArray = line.split(delimiter, -1);
        
        for (String s : lineArray ) {
        	if (null == s || s.isEmpty()) {
        		s = "";
        	}
        	result.add(s);
        }
        return result;
    }

}

