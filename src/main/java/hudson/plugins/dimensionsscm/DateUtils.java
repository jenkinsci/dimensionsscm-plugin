package hudson.plugins.dimensionsscm;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Utility methods for converting Date objects to and from strings in a valid
 * date format, and also validating that strings are in a supported Oracle-like
 * date format. This class uses Locale.US at the moment - may need changing.
 */
final class DateUtils {
    private static final String DATE_PATTERN = "dd-MMM-yyyy";
    private static final String DATETIME_PATTERN = "dd-MMM-yyyy HH:mm:ss";
    private static final String RFCDATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getDefault();

    /**
     * Attempt to parse as date-and-time patterns first (since a timestamp
     * string with a date-and-time would also be parsed by a date pattern).
     */
    private static final String[] PATTERNS = { DATETIME_PATTERN, DATE_PATTERN, RFCDATETIME_PATTERN };

    private DateUtils() {
        /* prevent instantiation. */
    }

    /**
     * Parses a date from a string in known format, using default time zone.
     * NOTE: This should ONLY be used if the caller has no knowledge of the
     * time zone to be used. Most of the time the overloaded method below
     * should be used.
     * @param  dateStr  a String containing a date in known form
     * @return  a Date object, or null
     */
    static Date parse(String dateStr) {
        return parse(dateStr, DEFAULT_TIME_ZONE);
    }

    /**
     * Parses a date from a string in known form.
     * @param  dateStr  a String containing a date in known form
     * @param  tz  the TimeZone to be used when parsing the date string
     * @return  a Date object, or null
     */
    static Date parse(String dateStr, TimeZone tz) {
        Date date = null;
        dateStr = dateStr.trim();
        for (String pattern : PATTERNS) {
            try {
                SimpleDateFormat df = new SimpleDateFormat(pattern, Locale.US);
                df.setTimeZone(tz);
                date = df.parse(dateStr);
                if (date != null) {
                    break;
                }
            } catch (ParseException pe) {
                /* do nothing, try the next pattern. */
            }
        }
        return date;
    }

    /**
     * Validates whether a given string is in correct known date format or date-time format.
     * Note that this is stricter than the {@link #parse(String)} method is.
     * @param  dateStr  the String containing the date to be validated
     * @return  true if the string is in a valid format, false otherwise
     */
    private static boolean validate(String dateStr) {
        boolean ret = false;
        dateStr = dateStr.trim();
        for (String pattern : PATTERNS) {
            try {
                SimpleDateFormat df = new SimpleDateFormat(pattern, Locale.US);
                Date date = df.parse(dateStr);
                if (date != null && df.format(date).equalsIgnoreCase(dateStr)) {
                    ret = true;
                    break;
                }
            } catch (ParseException pe) {
                /* do nothing, try the next pattern. */
            }
        }
        return ret;
    }

    /**
     * Note that comparing dates that have been converted from one format into
     * another should always be done with a tolerance (like comparing doubles
     * or floats) to allow for the fact that some accuracy may be lost (for
     * instance by rounding to the nearest second).
     * @param d1 first date to compare.
     * @param d2 second date to compare.
     * @param toleranceMillis millisecond tolerance to allow.
     * @return 0 if the two dates are within toleranceMillis milliseconds of
     *        each other, negative if d1 < d2, positive if d1 > d2
     */
    private static int compare(Date d1, Date d2, long toleranceMillis) {
        int ret;
        long diff = d1.getTime() - d2.getTime();
        if (Math.abs(diff) <= Math.abs(toleranceMillis)) {
            ret = 0;
        } else if (diff < 0L) {
            ret = -1;
        } else if (diff > 0L) {
            ret = 1;
        } else {
            // doesn't happen.
            ret = 0;
        }
        return ret;
    }

    /**
     * Formats a string in known date-time form from a date object,
     * using default time zone.
     * NOTE: This should ONLY be used if the caller has no knowledge of the
     * time zone to be used. Most of the time the overloaded method below
     * should be used.
     * @param  date  the date to be formatted
     * @return  a String containing a date in known date-time form, or null
     */
    static String format(Date date) {
        return format(date, DEFAULT_TIME_ZONE);
    }

    /**
     * Formats a string in known date-time form from a date object,
     * using the specified time zone.
     * @param  date  the date to be formatted
     * @param  tz  the TimeZone to be used when parsing the date string
     * @return  a String containing a date in known date-time form, or null
     */
    static String format(Date date, TimeZone tz) {
        SimpleDateFormat df = new SimpleDateFormat(DATETIME_PATTERN, Locale.US);
        df.setTimeZone(tz);
        return df.format(date);
    }

    /**
     * Gets "now" in RFC format.
     * @return  a String containing a date in known RFC
     */
    private static String getNowStrDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Calendar opDate = Calendar.getInstance();
        df.setTimeZone(DEFAULT_TIME_ZONE);
        return df.format(opDate.getTime());
    }

    /**
     * Gets "now" in RFC format.
     * @param tz timezone
     * @return  a String containing a date in known RFC
     */
    private static String getNowStrDate(TimeZone tz) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Calendar opDate = Calendar.getInstance();
        df.setTimeZone(tz);
        return df.format(opDate.getTime());
    }

    /**
     * Gets "now" in verbose format.
     *
     * The verbose format is "yyyy.MMMMM.dd hh.mm.ss aaa z" unless the system
     * property "hudson.plugins.dimensionsscm.preferColonInBaselineName" is set
     * to "true", in which case the verbose format reverts to
     * "yyyy.MMMMM.dd hh:mm:ss aaa z", which was its value in legacy versions of
     * the Jenkins Dimensions Plugin, but is no longer a legal baseline name as
     * of Dimensions CM 14.4 and later.
     *
     * Left the month as "MMMMM" and timezone as "z" because these are unlikely
     * to be an issue unless they contain one of the "':;# characters. At some
     * point, it would be good to allow customers to specify a date/time format
     * to use (for example [CURRENT_DATE, "yyyy-MM-dd'T'hh-mm-ss"]).
     *
     * @return  a String containing a date in verbose format
     */
    static String getNowStrDateVerbose() {
        boolean preferColonInBaselineName = Boolean.getBoolean("hudson.plugins.dimensionsscm.preferColonInBaselineName");
        SimpleDateFormat df = new SimpleDateFormat(preferColonInBaselineName ? "yyyy.MMMMM.dd hh:mm:ss aaa z" : "yyyy.MMMMM.dd hh.mm.ss aaa z");
        Calendar opDate = Calendar.getInstance();
        df.setTimeZone(DEFAULT_TIME_ZONE);
        return df.format(opDate.getTime());
    }

    /**
     * Gets a date in RFC format.
     * @param opDate calendar date
     * @return  a String containing a date in known RFC
     */
    private static String getStrDate(Calendar opDate) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(DEFAULT_TIME_ZONE);
        return df.format(opDate.getTime());
    }

    /**
     * Gets a date in RFC format. Used by logging code.
     * @param opDate calendar date
     * @param tz timezone
     * @return  a String containing a date in known RFC
     */
    static String getStrDate(Calendar opDate, TimeZone tz) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(tz);
        return df.format(opDate.getTime());
    }
}
