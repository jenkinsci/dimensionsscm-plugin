package hudson.plugins.dimensionsscm;

import com.serena.dmclient.api.DimensionsConnection;
import com.serena.dmclient.api.ItemRevision;
import com.serena.dmclient.api.SystemAttributes;
import hudson.FilePath;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

class DimensionsAPICallback12 implements DimensionsAPICallback {

    @Override
    public boolean hasRepositoryBeenUpdated(DimensionsAPI dimensionsAPI, DimensionsConnection connection, String projectName, Calendar fromDate, Calendar toDate, TimeZone tz, FilePath workspace) throws IOException {

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
}
