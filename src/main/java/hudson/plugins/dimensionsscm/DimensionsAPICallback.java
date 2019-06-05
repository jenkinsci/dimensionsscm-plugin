package hudson.plugins.dimensionsscm;

import com.serena.dmclient.api.DimensionsConnection;
import hudson.FilePath;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

interface DimensionsAPICallback {

    boolean hasRepositoryBeenUpdated(DimensionsAPI dimensionsAPI, DimensionsConnection connection,
                                     String projectName, Calendar fromDate, Calendar toDate, TimeZone tz, FilePath workspace) throws IOException;
}
