package hudson.plugins.dimensionsscm;

import com.serena.dmclient.api.DimensionsConnection;
import com.serena.dmclient.api.ItemRevision;
import com.serena.dmclient.api.SystemAttributes;
import hudson.FilePath;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

class DimensionsAPICallback12 implements DimensionsAPICallback {

    @Override
    public boolean isCallback14() {
        return false;
    }

    @Override
    public boolean hasRepositoryBeenUpdated(DimensionsAPI dimensionsAPI, DimensionsConnection connection, String projectName,
                                            Calendar fromDate, Calendar toDate, TimeZone tz, FilePath workspace) throws IOException {

        try {
            List<ItemRevision> itemRevisions = dimensionsAPI.calcRepoDiffsWithRevisions(connection, projectName, null, null, workspace, fromDate, toDate, tz);
            if (itemRevisions != null) {
                PathMatcher pathMatcher = dimensionsAPI.getPathMatcher();
                for (ItemRevision itemRevision : itemRevisions) {
                    String fullPathName = (String) itemRevision.getAttribute(SystemAttributes.FULL_PATH_NAME);
                    // Match when fullPathName is not ignored, false otherwise.
                    if (pathMatcher.match(fullPathName)) {
                        Logger.debug("Found " + itemRevisions.size() + " changed item(s), "
                                + "and at least one ('" + fullPathName + "') passed the " + pathMatcher);
                        return true;
                    }
                }
            }
            Logger.debug("Found " + (itemRevisions == null ? "nil" : itemRevisions.size()) + " changed item(s), "
                    + ((itemRevisions == null || itemRevisions.isEmpty()) ? "so" : "but") + " none passed the " + dimensionsAPI.getPathMatcher());
        } catch (Exception e) {
            String message = Values.exceptionMessage("Unable to run hasRepositoryBeenUpdated", e, "no message");
            Logger.debug(message, e);
            throw new IOException(message, e);
        }

        return false;
    }

    @Override
    public void saveChangesToXmlFile(DimensionsAPI dimensionsAPI, DimensionsConnection connection, final String projectName, final FilePath projectDir,
                                     final Calendar fromDate, final Calendar toDate, final TimeZone tz,
                                     final String baseline, final String requests, final File changelogFile, final String url) throws IOException {

        try {
            List<ItemRevision> items = dimensionsAPI.calcRepoDiffsWithRevisions(connection, projectName, baseline, requests, projectDir, fromDate, toDate, tz);

            Logger.debug("CM Url : " + (url != null ? url : "(null)"));
            if (requests != null) {
                dimensionsAPI.getLogger().println("[DIMENSIONS] Calculating changes for request(s) '" + requests + "'...");
            } else {
                dimensionsAPI.getLogger().println("[DIMENSIONS] Calculating changes for directory '"
                        + (projectDir != null ? projectDir.getRemote() : "/") + "'...");
            }
            dimensionsAPI.getLogger().flush();

            if (items != null) {
                // Write the list of changes into a changelog file.
                Map<String, DimensionsChangeLogEntry> changeMap = dimensionsAPI.createChangeList(items, tz, url);
                Logger.debug("Writing " + changeMap.size() + " changes to changelog file '" + changelogFile.getPath() + "'");
                DimensionsChangeLogWriter.writeLog(new ArrayList<DimensionsChangeLogEntry>(changeMap.values()), changelogFile);
            } else {
                // No changes, so create an empty changelog file.
                Logger.debug("Writing null changes to changelog file '" + changelogFile.getPath() + "'");
                DimensionsChangeLogWriter.writeLog(null, changelogFile);
            }
        } catch (Exception e) {
            throw new IOException(Values.exceptionMessage("Exception calculating changes", e,
                    "no message"), e);
        }
    }
}
