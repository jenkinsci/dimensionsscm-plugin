/*
 * ===========================================================================
 *  Copyright (c) 2014 Serena Software. All rights reserved.
 *
 *  Use of the Sample Code provided by Serena is governed by the following
 *  terms and conditions. By using the Sample Code, you agree to be bound by
 *  the terms contained herein. If you do not agree to the terms herein, do
 *  not install, copy, or use the Sample Code.
 *
 *  1.  GRANT OF LICENSE.  Subject to the terms and conditions herein, you
 *  shall have the nonexclusive, nontransferable right to use the Sample Code
 *  for the sole purpose of developing applications for use solely with the
 *  Serena software product(s) that you have licensed separately from Serena.
 *  Such applications shall be for your internal use only.  You further agree
 *  that you will not: (a) sell, market, or distribute any copies of the
 *  Sample Code or any derivatives or components thereof; (b) use the Sample
 *  Code or any derivatives thereof for any commercial purpose; or (c) assign
 *  or transfer rights to the Sample Code or any derivatives thereof.
 *
 *  2.  DISCLAIMER OF WARRANTIES.  TO THE MAXIMUM EXTENT PERMITTED BY
 *  APPLICABLE LAW, SERENA PROVIDES THE SAMPLE CODE AS IS AND WITH ALL
 *  FAULTS, AND HEREBY DISCLAIMS ALL WARRANTIES AND CONDITIONS, EITHER
 *  EXPRESSED, IMPLIED OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY
 *  IMPLIED WARRANTIES OR CONDITIONS OF MERCHANTABILITY, OF FITNESS FOR A
 *  PARTICULAR PURPOSE, OF LACK OF VIRUSES, OF RESULTS, AND OF LACK OF
 *  NEGLIGENCE OR LACK OF WORKMANLIKE EFFORT, CONDITION OF TITLE, QUIET
 *  ENJOYMENT, OR NON-INFRINGEMENT.  THE ENTIRE RISK AS TO THE QUALITY OF
 *  OR ARISING OUT OF USE OR PERFORMANCE OF THE SAMPLE CODE, IF ANY,
 *  REMAINS WITH YOU.
 *
 *  3.  EXCLUSION OF DAMAGES.  TO THE MAXIMUM EXTENT PERMITTED BY APPLICABLE
 *  LAW, YOU AGREE THAT IN CONSIDERATION FOR RECEIVING THE SAMPLE CODE AT NO
 *  CHARGE TO YOU, SERENA SHALL NOT BE LIABLE FOR ANY DAMAGES WHATSOEVER,
 *  INCLUDING BUT NOT LIMITED TO DIRECT, SPECIAL, INCIDENTAL, INDIRECT, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, DAMAGES FOR LOSS OF
 *  PROFITS OR CONFIDENTIAL OR OTHER INFORMATION, FOR BUSINESS INTERRUPTION,
 *  FOR PERSONAL INJURY, FOR LOSS OF PRIVACY, FOR NEGLIGENCE, AND FOR ANY
 *  OTHER LOSS WHATSOEVER) ARISING OUT OF OR IN ANY WAY RELATED TO THE USE
 *  OF OR INABILITY TO USE THE SAMPLE CODE, EVEN IN THE EVENT OF THE FAULT,
 *  TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY, OR BREACH OF CONTRACT,
 *  EVEN IF SERENA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.  THE
 *  FOREGOING LIMITATIONS, EXCLUSIONS AND DISCLAIMERS SHALL APPLY TO THE
 *  MAXIMUM EXTENT PERMITTED BY APPLICABLE LAW.  NOTWITHSTANDING THE ABOVE,
 *  IN NO EVENT SHALL SERENA'S LIABILITY UNDER THIS AGREEMENT OR WITH RESPECT
 *  TO YOUR USE OF THE SAMPLE CODE AND DERIVATIVES THEREOF EXCEED US$10.00.
 *
 *  4.  INDEMNIFICATION. You hereby agree to defend, indemnify and hold
 *  harmless Serena from and against any and all liability, loss or claim
 *  arising from this agreement or from (i) your license of, use of or
 *  reliance upon the Sample Code or any related documentation or materials,
 *  or (ii) your development, use or reliance upon any application or
 *  derivative work created from the Sample Code.
 *
 *  5.  TERMINATION OF THE LICENSE.  This agreement and the underlying
 *  license granted hereby shall terminate if and when your license to the
 *  applicable Serena software product terminates or if you breach any terms
 *  and conditions of this agreement.
 *
 *  6.  CONFIDENTIALITY.  The Sample Code and all information relating to the
 *  Sample Code (collectively "Confidential Information") are the
 *  confidential information of Serena.  You agree to maintain the
 *  Confidential Information in strict confidence for Serena.  You agree not
 *  to disclose or duplicate, nor allow to be disclosed or duplicated, any
 *  Confidential Information, in whole or in part, except as permitted in
 *  this Agreement.  You shall take all reasonable steps necessary to ensure
 *  that the Confidential Information is not made available or disclosed by
 *  you or by your employees to any other person, firm, or corporation.  You
 *  agree that all authorized persons having access to the Confidential
 *  Information shall observe and perform under this nondisclosure covenant.
 *  You agree to immediately notify Serena of any unauthorized access to or
 *  possession of the Confidential Information.
 *
 *  7.  AFFILIATES.  Serena as used herein shall refer to Serena Software,
 *  Inc. and its affiliates.  An entity shall be considered to be an
 *  affiliate of Serena if it is an entity that controls, is controlled by,
 *  or is under common control with Serena.
 *
 *  8.  GENERAL.  Title and full ownership rights to the Sample Code,
 *  including any derivative works shall remain with Serena.  If a court of
 *  competent jurisdiction holds any provision of this agreement illegal or
 *  otherwise unenforceable, that provision shall be severed and the
 *  remainder of the agreement shall remain in full force and effect.
 * ===========================================================================
 */
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
 * This experimental plugin extends Jenkins/Hudson support for Dimensions SCM repositories.
 * <p>
 * This class provide some utility methods on strings, arrays and collections.
 *
 * @author David Conneely
 */
class Values {
    static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    static final String[] EMPTY_STRING_ARRAY = new String[0];
    static final Locale ROOT_LOCALE = Locale.US;

    /**
     * Prevent casual instantiation.
     */
    private Values() {
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
     * Interpret a string as a decimal representation of a primitive integer. Ignores leading and trailing whitespace.
     * <p>
     * Currently not used.
     *
     * @param value string to try to interpret
     * @param defaultIntValue how to interpret the string if non-integer
     * @return integer interpreted from the string; defaultValue if not able to interpret
     */
    static int intOrElse(String value, int defaultIntValue) {
        if (!isNullOrEmpty(value)) {
            value = value.trim();
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                /* do nothing, fall through. */
            }
        }
        return defaultIntValue;
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
     * Is the collection null or empty?
     *
     * @param <T> type of the collection to check
     * @param values collection to check if null or empty
     * @return true if collection is null or empty; false otherwise
     */
    static <T> boolean isNullOrEmpty(Collection<? extends T> values) {
        return values == null || values.isEmpty();
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
            sb.append(decodeAttribute(criterion.getAttribute()));
            sb.append(decodeFlags(criterion.getFlags()));
            sb.append(decodeValue(criterion.getValue()));
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
            sb.append(decodeAttribute(order.getAttribute()));
            sb.append(decodeDirection(order.getDirection()));
        }
        sb.append("])");
        return sb.toString();
    }

    /** Helper method used by {@linkplain #toString(Filter)}. */
    private static String decodeAttribute(int attrNum) {
        switch (attrNum) {
            case -1201: return "CREATION_DATE";
            case -1801: return "ITEMFILE_FILENAME";
            case -1802: return "ITEMFILE_DIR";
            case -1803: return "REVISION";
            case -1804: return "IS_LATEST_REV";
            case -1805: return "IS_EXTRACTED";
            case -1806: return "FULL_PATH_NAME";
            case -1807: return "REVISION_COMMENT";
            default: return "#" + attrNum;
        }
    }

    /** Helper method used by {@linkplain #toString(Filter)}. */
    private static String decodeFlags(int flags) {
        switch (flags) {
        case 0: case 8: return " == ";
        case 16: return " < ";
        case 32: return " > ";
        case 64: case 72: return " <> ";
        case 80: return " >= ";
        case 96: return " <= ";
        default: return " ~" + flags + "~ ";
        }
    }

    /** Helper method used by {@linkplain #toString(Filter)}. */
    private static String decodeDirection(int direction) {
        switch (direction) {
        case 1: return " ASC";
        case -1: return " DESC";
        default: return " ~" + direction;
        }
    }

    /** Helper method used by {@linkplain #toString(Filter)}. */
    private static String decodeValue(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof String) {
            return "'" + (String) value + "'";
        } else {
            return value.toString();
        }
    }
}
