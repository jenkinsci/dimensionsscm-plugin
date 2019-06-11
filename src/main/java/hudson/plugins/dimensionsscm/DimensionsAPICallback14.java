package hudson.plugins.dimensionsscm;

import com.serena.dmclient.api.DimensionsChangeStep;
import com.serena.dmclient.api.DimensionsChangeStepType.STEP_TYPE;
import com.serena.dmclient.api.DimensionsConnection;
import com.serena.dmclient.api.Filter;
import com.serena.dmclient.api.ItemRevision;
import com.serena.dmclient.api.Project;
import com.serena.dmclient.api.Request;
import com.serena.dmclient.api.SystemAttributes;
import hudson.FilePath;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

class DimensionsAPICallback14 implements DimensionsAPICallback {

    @Override
    public boolean isCallback14() {
        return true;
    }

    @Override
    public boolean hasRepositoryBeenUpdated(DimensionsAPI dimensionsAPI, DimensionsConnection connection, String projectName,
                                            Calendar fromDate, Calendar toDate, TimeZone tz, FilePath workspace) throws IOException {

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


    @SuppressWarnings("unchecked")
    @Override
    public void saveChangesToXmlFile(DimensionsAPI dimensionsAPI, DimensionsConnection connection, String projectName,
                                     FilePath projectDir, Calendar fromDate, Calendar toDate, TimeZone tz,
                                     String baseline, String requests, final File changelogFile, final String url) throws IOException {

        try {
            // $GENERIC:$GLOBAL needed for case when we got: added/updated change step and deleted change step for the same file
            Project project = connection.getObjectFactory().getProject("$GENERIC:$GLOBAL");

            List<DimensionsChangeStep> changeSteps = dimensionsAPI.calcRepoDiffsWithChangesets(connection, projectName, fromDate, toDate, tz);
            Set<Long> stepUIDs = new HashSet<Long>();
            List<DimensionsChangeStep> deletedChgStep = new ArrayList<DimensionsChangeStep>();
            Map<String, DimensionsChangeLogEntry> changeLogEntryMap = new HashMap<String, DimensionsChangeLogEntry>();

            for (DimensionsChangeStep changeStep : changeSteps) {

                STEP_TYPE stepType = changeStep.getType();

                if (stepType.equals(STEP_TYPE.REMOVE))
                    deletedChgStep.add(changeStep);
                else
                    stepUIDs.add(changeStep.getObjUid());
            }

            Filter filter = new Filter();
            filter.criteria().add(Filter.Criterion.START_OR);

            for (Long uid : stepUIDs) {
                filter.criteria().add(new Filter.Criterion(SystemAttributes.OBJECT_UID, uid, Filter.Criterion.EQUALS));
            }

            filter.criteria().add(Filter.Criterion.END_OR);
            int[] attrs = DimensionsAPI.getItemFileAttributes(true);

            List<ItemRevision> items = DimensionsAPI.getItemRevisionByFilter(project, filter, connection, attrs);


            Logger.debug("CM Url : " + (url != null ? url : "(null)"));

            if (requests != null) {
                dimensionsAPI.getLogger().println("[DIMENSIONS] Calculating changes for request(s) '" + requests + "'...");
            } else {
                dimensionsAPI.getLogger().println("[DIMENSIONS] Calculating changes for directory '"
                        + (projectDir != null ? projectDir.getRemote() : "/") + "'...");
            }
            dimensionsAPI.getLogger().flush();


            if (items != null) {
                changeLogEntryMap.putAll(dimensionsAPI.createChangeList(items, tz, url));
            }

            if (!deletedChgStep.isEmpty()) {
                createChangeListForDeletedFiles(dimensionsAPI, deletedChgStep, tz, url, changeLogEntryMap);
            }

            if (!changeLogEntryMap.isEmpty()) {
                // Write the list of changes into a changelog file.
                List<DimensionsChangeLogEntry> entries = new ArrayList<DimensionsChangeLogEntry>(changeLogEntryMap.values());
                Logger.debug("Writing " + entries.size() + " changes to changelog file '" + changelogFile.getPath() + "'");
                DimensionsChangeLogWriter.writeLog(entries, changelogFile);
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

    private void createChangeListForDeletedFiles(DimensionsAPI dimensionsAPI, List<DimensionsChangeStep> deletedChgStep, TimeZone tz, final String url, Map<String, DimensionsChangeLogEntry> entries) {

        for (DimensionsChangeStep changeStep : deletedChgStep) {

            String revision = changeStep.getRevision();
            String fileName = changeStep.getProjectPath() + ";" + revision;
            String author = changeStep.getChangeSet().getUserName();
            String comment = changeStep.getChangeSet().getComment();
            Date date = DateUtils.parse(DateUtils.format(changeStep.getChangeSet().getDate()), tz);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            String operation = changeStep.getType().name();

            Logger.debug("Change details -" + comment + " " + revision + " " + fileName + " " + author
                    + " " + calendar + " " + operation + " (" + changeStep.getInternalType() + ")");

            DimensionsChangeLogEntry entry;

            String key = DimensionsAPI.createKeyForChangeMap(author, calendar.getTime());

            if (entries.containsKey(key)) {
                entry = entries.get(key);
                entry.add(fileName, operation, "");
            } else {
                entry = new DimensionsChangeLogEntry(fileName, author, operation, revision, comment, "", calendar);
                entries.put(key, entry);
            }

            for (Request request : changeStep.getRequests()) {

                request.queryAttribute(new int[]{
                        SystemAttributes.OBJECT_SPEC,
                        SystemAttributes.TITLE
                });

                String requestId = (String) request.getAttribute(SystemAttributes.OBJECT_SPEC);
                String requestUrl = DimensionsAPI.constructRequestURL(requestId, url, dimensionsAPI.getSCMDsn(), dimensionsAPI.getSCMBaseDb());
                String requestTitle = (String) request.getAttribute(SystemAttributes.TITLE);

                entry.addRequest(requestId, requestUrl, requestTitle);
                Logger.debug("Child Request Details IRT -" + requestId + " " + requestUrl + " " + requestTitle);
            }
        }
    }
}
