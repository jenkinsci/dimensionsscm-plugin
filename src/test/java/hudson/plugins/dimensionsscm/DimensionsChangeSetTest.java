package hudson.plugins.dimensionsscm;

import org.junit.Assert;
import org.junit.Test;

public class DimensionsChangeSetTest {
    @Test
    public void testStrip() {
        Assert.assertEquals("a", DimensionsChangeSet.DmFiles.strip("/a;1"));
        Assert.assertEquals("a", DimensionsChangeSet.DmFiles.strip("//a;"));
        Assert.assertEquals("a", DimensionsChangeSet.DmFiles.strip("///a"));

        Assert.assertEquals("a/b", DimensionsChangeSet.DmFiles.strip("/a/b;1"));
        Assert.assertEquals("a/b/", DimensionsChangeSet.DmFiles.strip("//a/b/;"));
        Assert.assertEquals("a//b//", DimensionsChangeSet.DmFiles.strip("///a//b//"));

        Assert.assertEquals("a;1", DimensionsChangeSet.DmFiles.strip("a;1;1"));
        Assert.assertEquals(";", DimensionsChangeSet.DmFiles.strip(";;"));
        Assert.assertEquals(";", DimensionsChangeSet.DmFiles.strip("///;;////"));

        Assert.assertEquals("a", DimensionsChangeSet.DmFiles.strip("a;1"));
        Assert.assertEquals("a", DimensionsChangeSet.DmFiles.strip("a;"));
        Assert.assertEquals("a", DimensionsChangeSet.DmFiles.strip("a"));

        Assert.assertEquals("", DimensionsChangeSet.DmFiles.strip(";1"));
        Assert.assertEquals("", DimensionsChangeSet.DmFiles.strip(";"));
        Assert.assertEquals("", DimensionsChangeSet.DmFiles.strip(""));
    }

}
