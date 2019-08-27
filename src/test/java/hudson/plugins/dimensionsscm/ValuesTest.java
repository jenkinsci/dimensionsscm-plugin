package hudson.plugins.dimensionsscm;

import hudson.plugins.dimensionsscm.model.StringVarStorage;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Test for Values class.
 */
public class ValuesTest {
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    @Test
    public void testBooleanOrElse() {
        Assert.assertFalse(Values.booleanOrElse(null, false));
        Assert.assertTrue(Values.booleanOrElse(null, true));
        Assert.assertFalse(Values.booleanOrElse("", false));
        Assert.assertTrue(Values.booleanOrElse("", true));
        Assert.assertFalse(Values.booleanOrElse("cake", false));
        Assert.assertTrue(Values.booleanOrElse("cake", true));
        Assert.assertTrue(Values.booleanOrElse("on", false));
        Assert.assertTrue(Values.booleanOrElse("on", true));
        Assert.assertTrue(Values.booleanOrElse("  on", false));
        Assert.assertFalse(Values.booleanOrElse("off", false));
        Assert.assertFalse(Values.booleanOrElse("off", true));
        Assert.assertFalse(Values.booleanOrElse("  off", true));
        Assert.assertTrue(Values.booleanOrElse("ON", false));
        Assert.assertFalse(Values.booleanOrElse("OFF", true));
        Assert.assertTrue(Values.booleanOrElse("yes", false));
        Assert.assertTrue(Values.booleanOrElse("yes", true));
        Assert.assertTrue(Values.booleanOrElse("yes  ", false));
        Assert.assertFalse(Values.booleanOrElse("no", false));
        Assert.assertFalse(Values.booleanOrElse("no", true));
        Assert.assertFalse(Values.booleanOrElse("no  ", true));
        Assert.assertTrue(Values.booleanOrElse("YES", false));
        Assert.assertFalse(Values.booleanOrElse("NO", true));
        Assert.assertTrue(Values.booleanOrElse("true", false));
        Assert.assertTrue(Values.booleanOrElse("true", true));
        Assert.assertFalse(Values.booleanOrElse("false", false));
        Assert.assertFalse(Values.booleanOrElse("false", true));
        Assert.assertTrue(Values.booleanOrElse("TRUE", false));
        Assert.assertFalse(Values.booleanOrElse("FALSE", true));
        Assert.assertTrue(Values.booleanOrElse(" TRUE ", false));
        Assert.assertFalse(Values.booleanOrElse(" FALSE ", true));
        Assert.assertFalse(Values.booleanOrElse(" CAKE ", false));
        Assert.assertTrue(Values.booleanOrElse(" CAKE ", true));
        // (Maybe controversially?) don't accept "1" or "0" as true or false.
        Assert.assertFalse(Values.booleanOrElse("1", false));
        Assert.assertFalse(Values.booleanOrElse(" 1 ", false));
        Assert.assertFalse(Values.booleanOrElse("001", false));
        Assert.assertTrue(Values.booleanOrElse("0", true));
        Assert.assertTrue(Values.booleanOrElse(" 0 ", true));
        Assert.assertTrue(Values.booleanOrElse("000", true));
    }

    @Test
    public void testIsNullOrEmpty_String() {
        Assert.assertTrue(Values.isNullOrEmpty((String) null));
        Assert.assertTrue(Values.isNullOrEmpty(""));
        Assert.assertFalse(Values.isNullOrEmpty("  "));
        Assert.assertFalse(Values.isNullOrEmpty("\r\n"));
        Assert.assertFalse(Values.isNullOrEmpty("A"));
    }

    @Test
    public void testIsNullOrEmpty_Array() {
        Assert.assertTrue(Values.isNullOrEmpty((String[]) null));
        Assert.assertTrue(Values.isNullOrEmpty(EMPTY_STRING_ARRAY));
        Assert.assertFalse(Values.isNullOrEmpty(new String[]{null}));
        Assert.assertFalse(Values.isNullOrEmpty(new String[]{""}));
        Assert.assertFalse(Values.isNullOrEmpty(new String[]{" "}));
        Assert.assertFalse(Values.isNullOrEmpty(new String[]{"", ""}));
        Assert.assertFalse(Values.isNullOrEmpty(new String[]{"A"}));
    }

    private static final String[][] STRING_ARRAY_INPUTS = {null, EMPTY_STRING_ARRAY,
            new String[]{null, "", "   "}, new String[]{null, "", "Value 1 ", "    Value 2    ", "  "}};
    private static final String[] NOT_EMPTY_OR_ELSE_DEFAULTS = new String[]{"default"};
    private static final String[][] NOT_EMPTY_OR_ELSE_EXPECTS = new String[][]{NOT_EMPTY_OR_ELSE_DEFAULTS,
            NOT_EMPTY_OR_ELSE_DEFAULTS, STRING_ARRAY_INPUTS[2], STRING_ARRAY_INPUTS[3]};
    private static final String[][] TRIM_COPY_EXPECTS = {EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY,
            EMPTY_STRING_ARRAY, new String[]{"Value 1", "Value 2"}};

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
        Assert.assertEquals(1, Values.requireCondition(arg0, arg0 > 0, "arg0 must be positive"));
        try {
            Values.requireCondition(arg1, arg1 > 0, "arg1 must be positive");
            Assert.fail();
        } catch (IllegalArgumentException e) {
            /* test passes. */
        }
        Assert.assertEquals("A", Values.requireCondition(arg2, arg2.length() <= 10, "arg2 must be 10 or fewer chars"));
        try {
            Values.requireCondition(arg3, arg3.length() <= 10, "arg3 must be 10 or fewer chars");
            Assert.fail();
        } catch (IllegalArgumentException e) {
            /* test passes. */
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
            /* test passes. */
        }
    }

    @Test
    public void testTextOrElse() {
        Assert.assertNull(Values.textOrElse(null, null));
        Assert.assertNull(Values.textOrElse("", null));
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


    @Test
    public void convertListToArrayTest() {

        List<StringVarStorage> stringVarStorageList = Arrays.asList(
                new StringVarStorage("1"),
                new StringVarStorage("2"),
                new StringVarStorage("3")
        );

        Assert.assertArrayEquals(new String[]{"1", "2", "3"}, Values.convertListToArray(stringVarStorageList));

        stringVarStorageList = Arrays.asList(
                new StringVarStorage("1"),
                new StringVarStorage(null),
                new StringVarStorage("")
        );

        Assert.assertArrayEquals(new String[]{"1", null, ""}, Values.convertListToArray(stringVarStorageList));


        stringVarStorageList = new ArrayList<StringVarStorage>();

        Assert.assertArrayEquals(new String[]{}, Values.convertListToArray(stringVarStorageList));
    }

    @Test
    public void convertArrayToListTest() {

        List<StringVarStorage> stringVarStorageList = Arrays.asList(
                new StringVarStorage("1"),
                new StringVarStorage("2"),
                new StringVarStorage("3")
        );

        Assert.assertEquals(stringVarStorageList, Values.convertArrayToList(new String[]{"1", "2", "3"}));

        stringVarStorageList = Arrays.asList(
                new StringVarStorage("1"),
                new StringVarStorage(null),
                new StringVarStorage("")
        );

        Assert.assertEquals(stringVarStorageList, Values.convertArrayToList(new String[]{"1", null, ""}));


        stringVarStorageList = new ArrayList<StringVarStorage>();

        Assert.assertEquals(stringVarStorageList, Values.convertArrayToList(new String[]{}));
    }

    @Test
    public void notBlankOrElseList() {

        List<StringVarStorage> stringVarStorageList = Arrays.asList(
                new StringVarStorage("1"),
                new StringVarStorage("2"),
                new StringVarStorage("3")
        );

        List<StringVarStorage> elseList = Arrays.asList(
                new StringVarStorage("4"),
                new StringVarStorage("5")
        );

        Assert.assertEquals(elseList, Values.notBlankOrElseList(new ArrayList<StringVarStorage>(), elseList));
        Assert.assertEquals(elseList, Values.notBlankOrElseList(null, elseList));
        Assert.assertEquals(stringVarStorageList, Values.notBlankOrElseList(stringVarStorageList, elseList));

        stringVarStorageList = Collections.singletonList(
                new StringVarStorage("")
        );

        Assert.assertEquals(elseList, Values.notBlankOrElseList(stringVarStorageList, elseList));

        stringVarStorageList = Arrays.asList(
                new StringVarStorage("1"),
                new StringVarStorage("")
        );

        Assert.assertEquals(Collections.singletonList(new StringVarStorage("1")), Values.notBlankOrElseList(stringVarStorageList, elseList));
    }
}
