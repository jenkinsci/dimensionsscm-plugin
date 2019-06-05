package hudson.plugins.dimensionsscm;


import com.serena.dmclient.api.DimensionsChangeStep;
import com.serena.dmclient.api.DimensionsConnection;
import hudson.FilePath;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

class DimensionsAPICallback14 implements DimensionsAPICallback {

    @Override
    public boolean hasRepositoryBeenUpdated(DimensionsAPI dimensionsAPI, DimensionsConnection connection, String projectName, Calendar fromDate, Calendar toDate, TimeZone tz, FilePath workspace) throws IOException {

        try {
            List<DimensionsChangeStep> changeSteps = dimensionsAPI.calcRepoDiffsWithChangesets(connection, projectName, fromDate, toDate, tz);
            if (changeSteps != null) {
                PathMatcher pathMatcher = dimensionsAPI.getPathMatcher();
                for (DimensionsChangeStep changeStep : changeSteps) {
                    String fullPathName = changeStep.getProjectPath();
                    // Match when fullPathName is not ignored, false otherwise.
                    if (pathMatcher.match(fullPathName)) {
                        Logger.debug("Found " + changeSteps.size() + " changed item(s), "
                                + "and at least one ('" + fullPathName + "') passed the " + pathMatcher);
                        return true;
                    }
                }
            }
            Logger.debug("Found " + (changeSteps == null ? "nil" : changeSteps.size()) + " changed item(s), "
                    + ((changeSteps == null || changeSteps.isEmpty()) ? "so" : "but") + " none passed the " + dimensionsAPI.getPathMatcher());
        } catch (Exception e) {
            String message = Values.exceptionMessage("Unable to run hasRepositoryBeenUpdated", e, "no message");
            Logger.debug(message, e);
            throw new IOException(message, e);
        }

        return false;
    }
}
