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

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for Values class.
 *
 * @author David Conneely
 */
public class ValuesTest {
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    @Test
    public void testBooleanOrElse() {
        Assert.assertEquals(false, Values.booleanOrElse(null, false));
        Assert.assertEquals(true, Values.booleanOrElse(null, true));
        Assert.assertEquals(false, Values.booleanOrElse("", false));
        Assert.assertEquals(true, Values.booleanOrElse("", true));
        Assert.assertEquals(false, Values.booleanOrElse("cake", false));
        Assert.assertEquals(true, Values.booleanOrElse("cake", true));
        Assert.assertEquals(true, Values.booleanOrElse("on", false));
        Assert.assertEquals(true, Values.booleanOrElse("on", true));
        Assert.assertEquals(true, Values.booleanOrElse("  on", false));
        Assert.assertEquals(false, Values.booleanOrElse("off", false));
        Assert.assertEquals(false, Values.booleanOrElse("off", true));
        Assert.assertEquals(false, Values.booleanOrElse("  off", true));
        Assert.assertEquals(true, Values.booleanOrElse("ON", false));
        Assert.assertEquals(false, Values.booleanOrElse("OFF", true));
        Assert.assertEquals(true, Values.booleanOrElse("yes", false));
        Assert.assertEquals(true, Values.booleanOrElse("yes", true));
        Assert.assertEquals(true, Values.booleanOrElse("yes  ", false));
        Assert.assertEquals(false, Values.booleanOrElse("no", false));
        Assert.assertEquals(false, Values.booleanOrElse("no", true));
        Assert.assertEquals(false, Values.booleanOrElse("no  ", true));
        Assert.assertEquals(true, Values.booleanOrElse("YES", false));
        Assert.assertEquals(false, Values.booleanOrElse("NO", true));
        Assert.assertEquals(true, Values.booleanOrElse("true", false));
        Assert.assertEquals(true, Values.booleanOrElse("true", true));
        Assert.assertEquals(false, Values.booleanOrElse("false", false));
        Assert.assertEquals(false, Values.booleanOrElse("false", true));
        Assert.assertEquals(true, Values.booleanOrElse("TRUE", false));
        Assert.assertEquals(false, Values.booleanOrElse("FALSE", true));
        Assert.assertEquals(true, Values.booleanOrElse(" TRUE ", false));
        Assert.assertEquals(false, Values.booleanOrElse(" FALSE ", true));
        Assert.assertEquals(false, Values.booleanOrElse(" CAKE ", false));
        Assert.assertEquals(true, Values.booleanOrElse(" CAKE ", true));
        // (Maybe controversially?) don't accept "1" or "0" as true or false.
        Assert.assertEquals(false, Values.booleanOrElse("1", false));
        Assert.assertEquals(false, Values.booleanOrElse(" 1 ", false));
        Assert.assertEquals(false, Values.booleanOrElse("001", false));
        Assert.assertEquals(true, Values.booleanOrElse("0", true));
        Assert.assertEquals(true, Values.booleanOrElse(" 0 ", true));
        Assert.assertEquals(true, Values.booleanOrElse("000", true));
    }

    @Test
    public void testIsNullOrEmpty_String() {
        Assert.assertEquals(true, Values.isNullOrEmpty((String) null));
        Assert.assertEquals(true, Values.isNullOrEmpty(""));
        Assert.assertEquals(false, Values.isNullOrEmpty("  "));
        Assert.assertEquals(false, Values.isNullOrEmpty("\r\n"));
        Assert.assertEquals(false, Values.isNullOrEmpty("A"));
    }

    @Test
    public void testIsNullOrEmpty_Array() {
        Assert.assertEquals(true, Values.isNullOrEmpty((String[]) null));
        Assert.assertEquals(true, Values.isNullOrEmpty(EMPTY_STRING_ARRAY));
        Assert.assertEquals(false, Values.isNullOrEmpty(new String[] { null }));
        Assert.assertEquals(false, Values.isNullOrEmpty(new String[] { "" }));
        Assert.assertEquals(false, Values.isNullOrEmpty(new String[] { " " }));
        Assert.assertEquals(false, Values.isNullOrEmpty(new String[] { "", "" }));
        Assert.assertEquals(false, Values.isNullOrEmpty(new String[] { "A" }));
    }

    private static final String[][] STRING_ARRAY_INPUTS = { null, EMPTY_STRING_ARRAY,
            new String[] { null, "", "   " }, new String[] { null, "",  "Value 1 ", "    Value 2    ", "  " } };
    private static final String[] NOT_EMPTY_OR_ELSE_DEFAULTS = new String[] { "default" };
    private static final String[][] NOT_EMPTY_OR_ELSE_EXPECTS = new String[][] { NOT_EMPTY_OR_ELSE_DEFAULTS,
            NOT_EMPTY_OR_ELSE_DEFAULTS, STRING_ARRAY_INPUTS[2], STRING_ARRAY_INPUTS[3] };
    private static final String[][] TRIM_COPY_EXPECTS = { EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY,
            EMPTY_STRING_ARRAY, new String[] { "Value 1", "Value 2" } };

    @Test
    public void testNotEmptyOrElse() {
        for (int i = 0; i < STRING_ARRAY_INPUTS.length; ++i) {
            String[] inputs = STRING_ARRAY_INPUTS[i];
            String[] expects = NOT_EMPTY_OR_ELSE_EXPECTS[i];
            Assert.assertArrayEquals(expects, Values.notEmptyOrElse(inputs, NOT_EMPTY_OR_ELSE_DEFAULTS));
        }
    }

    @Test
    public void testExceptionMessage() {
        Assert.assertEquals("First message (null: no message)",
                Values.exceptionMessage("First message", null, "no message"));
        try {
            throw new UnsupportedOperationException();
        } catch (UnsupportedOperationException e) {
            Assert.assertEquals("Second message (UnsupportedOperationException: no message)",
                    Values.exceptionMessage("Second message", e, "no message"));
        }
        try {
            throw new IllegalArgumentException("Third message");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Fourth message (IllegalArgumentException: Third message)",
                    Values.exceptionMessage("Fourth message", e, "no message"));
        }
    }

    @Test
    public void testRequireCondition() {
        int arg0 = 1;
        int arg1 = -1;
        String arg2 = "A";
        String arg3 = "THIS_VALUE_IS_TOO_LONG";
        Assert.assertEquals((Integer) 1, Values.requireCondition(arg0, arg0 > 0, "arg0 must be positive"));
        try {
            Values.requireCondition(arg1, arg1 > 0, "arg1 must be positive");
            Assert.fail();
        } catch (IllegalArgumentException e) { }
        Assert.assertEquals("A", Values.requireCondition(arg2, arg2.length() <= 10, "arg2 must be 10 or fewer chars"));
        try {
            Values.requireCondition(arg3, arg3.length() <= 10, "arg3 must be 10 or fewer chars");
            Assert.fail();
        } catch (IllegalArgumentException e) { }
    }

    /**
     * Test of notEmptyOrElse method, of class Values.
     */
    @Test
    public void testRequireNotNull() {
        String arg0 = "A";
        String arg1 = null;
        Assert.assertEquals("A", Values.requireNotNull(arg0, "arg0 must be non-null"));
        try {
            Values.requireNotNull(arg1, "arg1 must be non-null");
            Assert.fail();
        } catch (NullPointerException e) { }
    }

    @Test
    public void testTextOrElse() {
        Assert.assertEquals(null, Values.textOrElse(null, null));
        Assert.assertEquals(null, Values.textOrElse("", null));
        Assert.assertEquals("A", Values.textOrElse("A", null));
        Assert.assertEquals("", Values.textOrElse(null, ""));
        Assert.assertEquals("A", Values.textOrElse(null, "A"));
        Assert.assertEquals("A", Values.textOrElse(null, "A"));
        Assert.assertEquals("A", Values.textOrElse("", "A"));
        Assert.assertEquals("", Values.textOrElse("", ""));
        Assert.assertEquals("A", Values.textOrElse("A", ""));
        Assert.assertEquals("A", Values.textOrElse("  ", "A"));
        Assert.assertEquals(" ", Values.textOrElse("  ", " "));
        Assert.assertEquals("A", Values.textOrElse(" A ", ""));
        Assert.assertEquals("A\r\nB", Values.textOrElse("\tA\r\nB\r\n", ""));
    }

    @Test
    public void testTrimCopy() {
        for (int i = 0; i < STRING_ARRAY_INPUTS.length; ++i) {
            String[] inputs = STRING_ARRAY_INPUTS[i];
            String[] expects = TRIM_COPY_EXPECTS[i];
            Assert.assertArrayEquals(expects, Values.trimCopy(inputs));
        }
    }
}
