package hudson.plugins.dimensionsscm;

import com.serena.dmclient.api.DimensionsConnection;
import hudson.FilePath;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

interface DimensionsAPICallback {

    boolean isCallback14();

    boolean hasRepositoryBeenUpdated(DimensionsAPI dimensionsAPI, DimensionsConnection connection,
                                     String projectName, Calendar fromDate, Calendar toDate, TimeZone tz, FilePath workspace) throws IOException;

    void saveChangesToXmlFile(DimensionsAPI dimensionsAPI, DimensionsConnection connection, final String projectName, final FilePath projectDir,
                              final Calendar fromDate, final Calendar toDate, final TimeZone tz,
                              final String baseline, final String requests, final File changelogFile, final String url) throws IOException;
}
