package test.referenceobjects;

import static org.junit.Assert.*;

import java.text.ParseException;

import org.junit.Test;

import referenceobjects.DateFormat;
import referenceobjects.FxRate;
import utilities.Logger;

public class FxRateTest {

	@Test
	public void evaluateCompareTo() {
		try {
			FxRate f = new FxRate(DateFormat.ISO_FORMAT.parse("2017-09-03"), 1.23455d);
			FxRate g = new FxRate(DateFormat.ISO_FORMAT.parse("2017-09-05"), 1.32455d);
			
			int result = f.compareTo(g);
			int expectedResult = -1;
			assertEquals(expectedResult, result);
		} catch (ParseException e) {
			Logger.getInstance().error(e);
		}
	}

}
