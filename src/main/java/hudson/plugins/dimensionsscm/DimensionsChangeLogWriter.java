package hudson.plugins.dimensionsscm;

import hudson.Util;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

/**
 * Write an XML changelog (or append to it if it exists), without a closing tag.
 */
final class DimensionsChangeLogWriter {
    private DimensionsChangeLogWriter() {
        /* prevent instantiation. */
    }

    /**
     * Save the list of changes to the changelogFile.
     */
    static void writeLog(List<? extends DimensionsChangeLogEntry> entries, File changelogFile) throws IOException {
        boolean appendFile = false;
        if (changelogFile.exists()) {
            if (changelogFile.length() > 0) {
                appendFile = true;
            }
        }
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(changelogFile, appendFile), "UTF-8"));
            write(entries, writer, appendFile);
            writer.flush();
        } catch (IOException e) {
            String message = Values.exceptionMessage("Unable to write changelog file: " + changelogFile, e, "no message");
            Logger.debug(message, e);
            throw new IOException(message, e);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     * Write the list of changes to the PrintWriter.
     */
    private static void write(List<? extends DimensionsChangeLogEntry> entries, PrintWriter pw, boolean appendFile) {
        Logger.debug("Writing logfile in append mode = " + appendFile);
        String logStr = "";
        if (!appendFile) {
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            pw.println("<changelog>");
        }
        if (entries != null) {
            for (DimensionsChangeLogEntry entry : entries) {
                logStr += String.format("\t<changeset version=\"%s\">\n", escapeXML(entry.getVersion()));
                logStr += String.format("\t\t<date>%s</date>\n", Util.XS_DATETIME_FORMATTER2.format(entry.getDate().toInstant()));
                logStr += String.format("\t\t<user>%s</user>\n", escapeXML(entry.getDeveloper()));
                logStr += String.format("\t\t<comment>%s</comment>\n", escapeXML(entry.getSCMComment()));
                logStr += "\t\t<items>\n";
                for (DimensionsChangeLogEntry.FileChange item : entry.getFiles()) {
                    logStr += String.format("\t\t\t<item operation=\"%s\" url=\"%s\">%s</item>\n", item.getOperation(),
                            escapeXML(item.getUrl()), escapeXML(item.getFile()));
                }
                logStr += "\t\t</items>\n";
                logStr += "\t\t<requests>\n";
                for (DimensionsChangeLogEntry.IRTRequest req : entry.getRequests()) {
                    logStr += String.format("\t\t\t<request url=\"%s\" title=\"%s\">%s</request>\n",
                            escapeXML(req.getUrl()), escapeXML(req.getTitle()), escapeXML(req.getIdentifier()));
                }
                logStr += "\t\t</requests>\n";
                logStr += "\t</changeset>\n";
            }
        }
        Logger.debug("Writing to changelog: '" + logStr + "'");
        pw.print(logStr);
        pw.flush();
    }

    /**
     * Escape an XML string.
     */
    private static String escapeXML(String str) {
        if (Values.isNullOrEmpty(str)) {
            return str;
        }
        int len = str.length();
        StringBuilder sb = new StringBuilder(Math.max(16, len));
        for (int i = 0; i < len; ++i) {
            char ch = str.charAt(i);
            switch (ch) {
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&#039;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                default:
                    sb.append(ch);
                    break;
            }
        }
        return sb.toString();
    }
}
