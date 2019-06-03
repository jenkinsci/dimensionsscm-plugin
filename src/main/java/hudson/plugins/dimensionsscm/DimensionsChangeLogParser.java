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
     * \@Override
     * public DimensionsChangeLogSet parse(Run run, RepositoryBrowser<?> browser, File changelogFile) throws IOException, SAXException {
     *     return new DimensionsChangeLogSet(run, browser, digest(changelogFile));
     * }
     * </pre>
     */
    @Deprecated
    @Override
    public DimensionsChangeLogSet parse(@SuppressWarnings("rawtypes") AbstractBuild build, File changelogFile)
            throws IOException, SAXException {
        RepositoryBrowser<?> browser = build.getProject().getScm().getEffectiveBrowser();
        return new DimensionsChangeLogSet(build, browser, digest(changelogFile));
    }

    private List<DimensionsChangeLogEntry> digest(File changelogFile) throws IOException, SAXException {
        Logger.debug("Looking for '" + changelogFile.getPath() + "'");
        if (!changelogFile.canRead()) {
            String message = "Specified changelog file does not exist or is not readable: " + changelogFile.getPath();
            Logger.debug(message);
            throw new FileNotFoundException(message);
        }
        List<DimensionsChangeLogEntry> entries;
        try {
            // Try to parse as UTF-8 initially, changelog files created by > 0.8.11 use UTF-8 encoding.
            entries = digest(changelogFile, "UTF-8");
        } catch (IOException e) {
            Logger.debug(Values.exceptionMessage("Failed to parse changelog file as UTF-8, retrying with default charset", e, "no message"));
            // If that fails, it may be a changelog file created by <= 0.8.11 using platform default encoding.
            entries = digest(changelogFile, null);
        }
        return entries;
    }

    private List<DimensionsChangeLogEntry> digest(File changelogFile, String charEncoding)
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

    private List<DimensionsChangeLogEntry> digest(Reader reader) throws IOException, SAXException {
        List<DimensionsChangeLogEntry> entries = new ArrayList<DimensionsChangeLogEntry>();
        Digester digester = createDigester(entries);
        digester.parse(reader);
        return entries;
    }

    private Digester createDigester(Object top) {
        Digester digester = new Digester2();
        digester.push(top);

        digester.addObjectCreate("*/changeset", DimensionsChangeLogEntry.class);
        digester.addSetProperties("*/changeset");
        digester.addBeanPropertySetter("*/changeset/date", "dateString");
        digester.addBeanPropertySetter("*/changeset/user");
        digester.addBeanPropertySetter("*/changeset/comment");
        digester.addSetNext("*/changeset", "add");

        digester.addObjectCreate("*/changeset/items/item", DimensionsChangeLogEntry.FileChange.class);
        digester.addSetProperties("*/changeset/items/item");
        digester.addBeanPropertySetter("*/changeset/items/item", "file");
        digester.addSetNext("*/changeset/items/item", "add");

        digester.addObjectCreate("*/changeset/requests/request", DimensionsChangeLogEntry.IRTRequest.class);
        digester.addSetProperties("*/changeset/requests/request");
        digester.addBeanPropertySetter("*/changeset/requests/request", "identifier");
        digester.addSetNext("*/changeset/requests/request", "addRequest");
        return digester;
    }
}
