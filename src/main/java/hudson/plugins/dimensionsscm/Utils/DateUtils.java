
/* ===========================================================================
 *  Copyright (c) 2007 Serena Software. All rights reserved.
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

/*
 * This experimental plugin extends Hudson support for Dimensions SCM repositories
 *
 * @author Tim Payne
 *
 */

// Package name
package hudson.plugins.dimensionsscm;

// General imports
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.io.Serializable;

/**
 * A set of methods for converting Date objects to and from strings in a valid
 * date format, and also validating that strings are in an Oracle date format.
 * Note: this class uses only Locale.US at the moment - this may need changing.
 * @author Tim Payne
 */
public class DateUtils implements Serializable {
    private static final String DATE_PATTERN = "dd-MMM-yyyy";
    private static final String DATETIME_PATTERN = "dd-MMM-yyyy HH:mm:ss";
    private static final String RFCDATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC"); // should this be "GMT"?
    private static TimeZone tzl = TimeZone.getDefault();

    /**
     * Attempt to parse as date-and-time patterns first (since a timestamp
     * string with a date-and-time would also be parsed by a date pattern).
     */
    private static final String PATTERNS[] = {
        DATETIME_PATTERN,
        DATE_PATTERN,
        RFCDATETIME_PATTERN
    };


    /**
     * Parses a date from a string in known format, using UTC time zone.
     * NOTE: This should ONLY be used if the caller has no knowledge of the
     * time zone to be used. Most of the time the overloaded method below
     * should be used.
     * @param  dateStr  a String containing a date in known form
     * @return  a Date object, or null
     */
    public static Date parse(String dateStr) {
        return parse(dateStr, tzl);
    }

    /**
     * Parses a date from a string in known form.
     * @param  dateStr  a String containing a date in known form
     * @param  tz  the TimeZone to be used when parsing the date string
     * @return  a Date object, or null
     */
    public static Date parse(String dateStr, TimeZone tz) {
        Date date = null;
        dateStr = dateStr.trim();
        for (int i = 0; i < PATTERNS.length; ++i) {
            try {
                SimpleDateFormat df= new SimpleDateFormat(PATTERNS[i], Locale.US);
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
    public static boolean validate(String dateStr) {
        boolean ret = false;
        dateStr = dateStr.trim();
        for (int i = 0; i < PATTERNS.length; ++i) {
            try {
                SimpleDateFormat df = new SimpleDateFormat(PATTERNS[i], Locale.US);
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
    public static int compare(Date d1, Date d2, long toleranceMillis) {
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
     * using UTC time zone.
     * NOTE: This should ONLY be used if the caller has no knowledge of the
     * time zone to be used. Most of the time the overloaded method below
     * should be used.
     * @param  date  the date to be formatted
     * @return  a String containing a date in known date-time form, or null
     */
    public static String format(Date date) {
        return format(date, tzl);
    }

    /**
     * Formats a string in known date-time form from a date object,
     * using UTC time zone.
     * @param  date  the date to be formatted
     * @param  tz  the TimeZone to be used when parsing the date string
     * @return  a String containing a date in known date-time form, or null
     */
    public static String format(Date date, TimeZone tz) {
        SimpleDateFormat df = new SimpleDateFormat(DATETIME_PATTERN, Locale.US);
        df.setTimeZone(tz);
        return df.format(date);
    }

    /**
     * Gets "now" in RFC format
     * @return  a String containing a date in known RFC
     */
    public static String getNowStrDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Calendar opDate = Calendar.getInstance();
        df.setTimeZone(tzl);
        return df.format(opDate.getTime());
    }

    /**
     * Gets "now" in RFC format
     * @param timezone tz
     * @return  a String containing a date in known RFC
     */
    public static String getNowStrDate(TimeZone tz) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Calendar opDate = Calendar.getInstance();
        df.setTimeZone(tz);
        return df.format(opDate.getTime());
    }

    /**
     * Gets a date in RFC format
     * @param calendar date
     * @return  a String containing a date in known RFC
     */
    public static String getStrDate(Calendar opDate) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(tzl);
        return df.format(opDate.getTime());
    }

    /**
     * Gets a date in RFC format
     * @param calendar date
     * @param timezone tz
     * @return  a String containing a date in known RFC
     */
    public static String getStrDate(Calendar opDate, TimeZone tz) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(tz);
        return df.format(opDate.getTime());
    }

}
