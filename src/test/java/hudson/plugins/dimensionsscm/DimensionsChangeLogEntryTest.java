package hudson.plugins.dimensionsscm;

import org.junit.Assert;
import org.junit.Test;

public class DimensionsChangeLogEntryTest {
    @Test
    public void testStrip() {
        Assert.assertEquals("a", DimensionsChangeLogEntry.FileChange.strip("/a;1"));
        Assert.assertEquals("a", DimensionsChangeLogEntry.FileChange.strip("//a;"));
        Assert.assertEquals("a", DimensionsChangeLogEntry.FileChange.strip("///a"));

        Assert.assertEquals("a/b", DimensionsChangeLogEntry.FileChange.strip("/a/b;1"));
        Assert.assertEquals("a/b/", DimensionsChangeLogEntry.FileChange.strip("//a/b/;"));
        Assert.assertEquals("a//b//", DimensionsChangeLogEntry.FileChange.strip("///a//b//"));

        Assert.assertEquals("a;1", DimensionsChangeLogEntry.FileChange.strip("a;1;1"));
        Assert.assertEquals(";", DimensionsChangeLogEntry.FileChange.strip(";;"));
        Assert.assertEquals(";", DimensionsChangeLogEntry.FileChange.strip("///;;////"));

        Assert.assertEquals("a", DimensionsChangeLogEntry.FileChange.strip("a;1"));
        Assert.assertEquals("a", DimensionsChangeLogEntry.FileChange.strip("a;"));
        Assert.assertEquals("a", DimensionsChangeLogEntry.FileChange.strip("a"));

        Assert.assertEquals("", DimensionsChangeLogEntry.FileChange.strip(";1"));
        Assert.assertEquals("", DimensionsChangeLogEntry.FileChange.strip(";"));
        Assert.assertEquals("", DimensionsChangeLogEntry.FileChange.strip(""));
    }

}
