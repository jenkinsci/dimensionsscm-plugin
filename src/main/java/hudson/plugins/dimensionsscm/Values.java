package hudson.plugins.dimensionsscm;

import com.serena.dmclient.api.Filter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * This class provide some utility methods on strings, arrays and collections.
 */
final class Values {
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    static final Locale ROOT_LOCALE = Locale.US;

    private Values() {
        /* prevent instantiation. */
    }

    /**
     * Interpret a string as a primitive boolean. Similar rules to Ant: "true", "yes" and "on" are true; "false", "no"
     * and "off" are false. Case-insensitive and ignores leading and trailing whitespace.
     *
     * @param value string to try to interpret
     * @param defaultBooleanValue how to interpret the string if it doesn't match one of the known strings
     * @return boolean interpreted from the string; defaultValue if not able to interpret
     */
    static boolean booleanOrElse(String value, boolean defaultBooleanValue) {
        if (!isNullOrEmpty(value)) {
            value = value.trim();
            if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("on")) {
                return true;
            } else if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("no") || value.equalsIgnoreCase("off")) {
                return false;
            }
        }
        return defaultBooleanValue;
    }

    /**
     * Is there a value left after trimming?
     *
     * @param value value to check if null, empty or just whitespace
     * @return false if value is null, empty or just whitespace; true otherwise
     */
    static boolean hasText(String value) {
        return value != null && value.trim().length() != 0;
    }

    /**
     * Is the value null or empty?
     *
     * @param value value to check if null or empty
     * @return true if value is null or empty; false otherwise
     */
    static boolean isNullOrEmpty(String value) {
        return value == null || value.length() == 0;
    }

    /**
     * Is the array null or empty?
     *
     * @param <T> type of the array to check
     * @param values array to check if null or empty
     * @return true if array is null or empty; false otherwise
     */
    static <T> boolean isNullOrEmpty(T[] values) {
        return values == null || values.length == 0;
    }

    /**
     * Return a default array if the values array is null or empty.
     *
     * @param <T> type of the array to check
     * @param values array to check if null or empty
     * @param defaultValues array to return if values is null or empty
     * @return defaultValues if values is null or empty; values otherwise
     */
    static <T> T[] notEmptyOrElse(T[] values, T[] defaultValues) {
        return isNullOrEmpty(values) ? defaultValues : values;
    }

    /**
     * Return a default value if the value string is null, empty or just whitespace.
     *
     * @param value string to check if null, empty or just whitespace
     * @param defaultValue string to return if value is null, empty or just whitespace
     * @return defaultValue if value is null, empty or just whitespace; value otherwise
     */
    static String textOrElse(String value, String defaultValue) {
        if (!isNullOrEmpty(value)) {
            value = value.trim();
            if (value.length() != 0) {
                return value;
            }
        }
        return defaultValue;
    }

    /**
     * Generate a message for the new exception when re-throwing an exception.
     */
    static String exceptionMessage(String newMessage, Exception e, String nullMessage) {
        String className = e != null ? e.getClass().getSimpleName() : "null";
        String msg = e != null ? e.getMessage() : nullMessage;
        if (msg == null) {
            msg = nullMessage;
        }
        return newMessage + " (" + className + ": " + msg + ")";
    }

    /**
     * Check method argument matches some condition. Throws IllegalArgumentException if it does not.
     *
     * @param argument The argument to check
     * @param condition The condition that must apply to the argument
     * @param message A message for the IllegalArgumentException if the condition does not apply
     * @return The passed argument
     * @throws IllegalArgumentException if the condition is false
     */
    static Object requireCondition(Object argument, boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
        return argument;
    }

    /**
     * Check method argument is not null. Throws NullPointerException if it is null.
     *
     * @param argument The argument to check
     * @param message A message for the NullPointerException if the argument is null
     * @return The passed argument
     * @throws NullPointerException if the argument is null
     */
    static Object requireNotNull(Object argument, String message) {
        if (argument == null) {
            throw new NullPointerException(message);
        }
        return argument;
    }

    /**
     * Trim insignificant values from a String array, by skipping all null, empty or just whitespace strings.
     *
     * @param values input array (could be null array or empty array already).
     * @return a new array that contains just the significant values (if any); otherwise returns an empty array (if the
     * input array was null, empty already, or has no significant values left after skipping).
     */
    static String[] trimCopy(String[] values) {
        if (values == null || values.length == 0) {
            return EMPTY_STRING_ARRAY;
        }
        List<String> outlist = new ArrayList<String>(values.length);
        for (String value : values) {
            if (!isNullOrEmpty(value)) {
                value = value.trim();
                if (value.length() != 0) {
                    outlist.add(value);
                }
            }
        }
        return outlist.isEmpty() ? EMPTY_STRING_ARRAY : outlist.toArray(EMPTY_STRING_ARRAY);
    }

    /**
     * Convert a <tt>Calendar</tt> instance into a string suitable for logging.
     * The format is language-neutral, but includes time zone information.
     *
     * @param cal Calendar instance to convert to a String.
     * @return String form including time zone.
     */
    static String toString(Calendar cal) {
        if (cal == null) {
            return "null";
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        df.setTimeZone(cal.getTimeZone());
        return "Calendar(" + df.format(cal.getTime()) + ")";
    }

    /**
     * Convert a <tt>Date</tt> instance into a string suitable for logging.
     * The format is language-neutral, and is in UTC time zone (since Date instances don't have a time zone).
     *
     * @param date Date instance to convert to a String.
     * @return String form in UTC.
     */
    static String toString(Date date) {
        if (date == null) {
            return "null";
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return "Date(" + df.format(date) + ")";
    }

    /**
     * Convert a <tt>Collection</tt> instance into a string suitable for logging.
     * The format is that of Arrays.toString.
     *
     * @param collection Collection instance to convert to a String.
     * @return String form.
     */
    static String toString(Collection<?> collection) {
        if (collection == null) {
            return "null";
        }
        return Arrays.toString(collection.toArray());
    }

    /**
     * Convert a <tt>Filter</tt> instance into a string suitable for logging.
     *
     * @param filter Filter instance to convert to a String.
     * @return String form.
     */
    static String toString(Filter filter) {
        List<Filter.Criterion> criteria = filter.criteria();
        List<Filter.Order> orders = filter.orders();
        StringBuilder sb = new StringBuilder();
        sb.append("Filter([");
        boolean comma = false;
        for (Filter.Criterion criterion : criteria) {
            if (comma) {
                sb.append(',');
                sb.append(' ');
            } else {
                comma = true;
            }
            sb.append(debugDecodeAttribute(criterion.getAttribute()));
            sb.append(debugDecodeFlags(criterion.getFlags()));
            sb.append(debugDecodeValue(criterion.getValue()));
        }
        sb.append("], [");
        comma = false;
        for (Filter.Order order : orders) {
            if (comma) {
                sb.append(',');
                sb.append(' ');
            } else {
                comma = true;
            }
            sb.append(debugDecodeAttribute(order.getAttribute()));
            sb.append(debugDecodeDirection(order.getDirection()));
        }
        sb.append("])");
        return sb.toString();
    }

    /** Helper method used by {@linkplain #toString(Filter)}. */
    private static String debugDecodeAttribute(int attrNum) {
        switch (attrNum) {
            case -1201:
                return "CREATION_DATE";
            case -1801:
                return "ITEMFILE_FILENAME";
            case -1802:
                return "ITEMFILE_DIR";
            case -1803:
                return "REVISION";
            case -1804:
                return "IS_LATEST_REV";
            case -1805:
                return "IS_EXTRACTED";
            case -1806:
                return "FULL_PATH_NAME";
            case -1807:
                return "REVISION_COMMENT";
            default:
                return "#" + attrNum;
        }
    }

    /** Helper method used by {@linkplain #toString(Filter)}. */
    private static String debugDecodeFlags(int flags) {
        switch (flags) {
            case 0:
            case 8:
                return " == ";
            case 16:
                return " < ";
            case 32:
                return " > ";
            case 64:
            case 72:
                return " <> ";
            case 80:
                return " >= ";
            case 96:
                return " <= ";
            default:
                return " ~" + flags + "~ ";
        }
    }

    /** Helper method used by {@linkplain #toString(Filter)}. */
    private static String debugDecodeDirection(int direction) {
        switch (direction) {
            case 1:
                return " ASC";
            case -1:
                return " DESC";
            default:
                return " ~" + direction;
        }
    }

    /** Helper method used by {@linkplain #toString(Filter)}. */
    private static String debugDecodeValue(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof String) {
            return "'" + value + "'";
        } else {
            return value.toString();
        }
    }
}
