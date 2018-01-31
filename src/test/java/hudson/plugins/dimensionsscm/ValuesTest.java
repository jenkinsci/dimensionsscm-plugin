package hudson.plugins.dimensionsscm;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for Values class.
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
            new String[] { null, "", "   " }, new String[] { null, "", "Value 1 ", "    Value 2    ", "  " } };
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
        Assert.assertEquals("First message (null: no message)", Values.exceptionMessage("First message", null, "no message"));
        try {
            throw new UnsupportedOperationException();
        } catch (UnsupportedOperationException e) {
            Assert.assertEquals("Second message (UnsupportedOperationException: no message)", Values.exceptionMessage("Second message", e, "no message"));
        }
        try {
            throw new IllegalArgumentException("Third message");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Fourth message (IllegalArgumentException: Third message)", Values.exceptionMessage("Fourth message", e, "no message"));
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
        } catch (IllegalArgumentException e) {
        }
        Assert.assertEquals("A", Values.requireCondition(arg2, arg2.length() <= 10, "arg2 must be 10 or fewer chars"));
        try {
            Values.requireCondition(arg3, arg3.length() <= 10, "arg3 must be 10 or fewer chars");
            Assert.fail();
        } catch (IllegalArgumentException e) {
        }
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
        } catch (NullPointerException e) {
        }
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
