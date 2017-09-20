package utilities;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateTimeUtils {
	public static final double DAILY = 1.0d;
	public static final double MONTHLY = 30.4375d;
	public static final double QUARTERLY = 91.3125d;
	
	public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
	    long diffInMillies = date2.getTime() - date1.getTime();
	    return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
	}
}
