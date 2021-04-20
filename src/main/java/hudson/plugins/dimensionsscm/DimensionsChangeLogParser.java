package hudson.plugins.dimensionsscm;

import hudson.model.Run;
import hudson.scm.ChangeLogParser;
import hudson.scm.RepositoryBrowser;
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
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Parses a changelog file.
 */
public class DimensionsChangeLogParser extends ChangeLogParser {
    @Override
    public DimensionsChangeLogSet parse(final Run run, final RepositoryBrowser<?> browser, final File changelogFile) throws IOException, SAXException {
        return new DimensionsChangeLogSet(run, browser, digest(changelogFile));
    }

    private List<DimensionsChangeLogEntry> digest(final File changelogFile) throws IOException, SAXException {
        Logger.debug("Looking for '" + changelogFile.getPath() + "'");
        if (!changelogFile.canRead()) {
            final String message = "Specified changelog file does not exist or is not readable: " + changelogFile.getPath();
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

    private List<DimensionsChangeLogEntry> digest(final File changelogFile, final String charEncoding) throws IOException, SAXException {
        try (final Reader reader = (charEncoding != null)
                ? new InputStreamReader(new FileInputStream(changelogFile), charEncoding)
                : new FileReader(changelogFile)) {
            return digest(reader);
        }
    }

    private List<DimensionsChangeLogEntry> digest(final Reader reader) throws IOException, SAXException {
        final List<DimensionsChangeLogEntry> entries = new ArrayList<>();
        final Digester digester = createDigester(entries);
        digester.parse(reader);
        return entries;
    }

    private Digester createDigester(final Object top) throws SAXException {
        final Digester digester = new Digester();
        digester.setXIncludeAware(false);
        if (!Boolean.getBoolean(DimensionsChangeLogParser.class.getName() + ".UNSAFE")) {
            try {
                digester.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                digester.setFeature("http://xml.org/sax/features/external-general-entities", false);
                digester.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                digester.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            }
            catch ( ParserConfigurationException ex) {
                throw new SAXException("Failed to securely configure Dimensions changelog parser", ex);
            }
        }
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
        digester.addBeanPropertySetter("*/changeset/items/item/editType", "operation");
        digester.addBeanPropertySetter("*/changeset/items/item/url", "url");
        digester.addSetNext("*/changeset/items/item", "add");
        digester.addObjectCreate("*/changeset/requests/request", DimensionsChangeLogEntry.IRTRequest.class);
        digester.addSetProperties("*/changeset/requests/request");
        digester.addBeanPropertySetter("*/changeset/requests/request", "identifier");
        digester.addSetNext("*/changeset/requests/request", "addRequest");
        return digester;
    }
}
