package hudson.plugins.dimensionsscm;

import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogParser;
import hudson.scm.RepositoryBrowser;
import hudson.util.Digester2;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.digester.Digester;
import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

/**
 * Parses a changelog file.
 */
public class DimensionsChangeLogParser extends ChangeLogParser {
    /**
     * When move to 1.568+, deprecate the AbstractBuild method and add the following method signature:
     *
     * <pre>
     * @Override
     * public DimensionsChangeSetList parse(Run run, RepositoryBrowser<?> browser, File changelogFile) throws IOException, SAXException { return new
     * DimensionsChangeSetList(run, browser, digest(changelogFile)); }
     * </pre>
     */
    @Override
    public DimensionsChangeSetList parse(AbstractBuild build, File changelogFile) throws IOException, SAXException {
        RepositoryBrowser<?> browser = build.getProject().getScm().getEffectiveBrowser();
        return new DimensionsChangeSetList(build, browser, digest(changelogFile));
    }

    private List<DimensionsChangeSet> digest(File changelogFile) throws IOException, SAXException {
        Logger.debug("Looking for '" + changelogFile.getPath() + "'");
        if (!changelogFile.canRead()) {
            String message = "Specified changelog file does not exist or is not readable: " + changelogFile.getPath();
            Logger.debug(message);
            throw new FileNotFoundException(message);
        }
        List<DimensionsChangeSet> changesets;
        try {
            // Try to parse as UTF-8 initially, changelog files created by > 0.8.11 use UTF-8 encoding.
            changesets = digest(changelogFile, "UTF-8");
        } catch (IOException e) {
            Logger.debug(Values.exceptionMessage("Failed to parse changelog file as UTF-8, retrying with default charset", e, "no message"));
            // If that fails, it may be a changelog file created by <= 0.8.11 using platform default encoding.
            changesets = digest(changelogFile, null);
        }
        return changesets;
    }

    private List<DimensionsChangeSet> digest(File changelogFile, String charEncoding)
            throws IOException, SAXException {
        Reader reader;
        if (charEncoding != null) {
            reader = new InputStreamReader(new FileInputStream(changelogFile), charEncoding);
        } else {
            reader = new FileReader(changelogFile);
        }
        try {
            return digest(reader);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    private List<DimensionsChangeSet> digest(Reader reader) throws IOException, SAXException {
        List<DimensionsChangeSet> changesets = new ArrayList<DimensionsChangeSet>();
        Digester digester = createDigester(changesets);
        digester.parse(reader);
        return changesets;
    }

    private Digester createDigester(Object top) {
        Digester digester = new Digester2();
        digester.push(top);

        digester.addObjectCreate("*/changeset", DimensionsChangeSet.class);
        digester.addSetProperties("*/changeset");
        digester.addBeanPropertySetter("*/changeset/date", "dateString");
        digester.addBeanPropertySetter("*/changeset/user");
        digester.addBeanPropertySetter("*/changeset/comment");
        digester.addSetNext("*/changeset", "add");

        digester.addObjectCreate("*/changeset/items/item", DimensionsChangeSet.DmFiles.class);
        digester.addSetProperties("*/changeset/items/item");
        digester.addBeanPropertySetter("*/changeset/items/item", "file");
        digester.addSetNext("*/changeset/items/item", "add");

        digester.addObjectCreate("*/changeset/requests/request", DimensionsChangeSet.DmRequests.class);
        digester.addSetProperties("*/changeset/requests/request");
        digester.addBeanPropertySetter("*/changeset/requests/request", "identifier");
        digester.addSetNext("*/changeset/requests/request", "addRequest");
        return digester;
    }
}
