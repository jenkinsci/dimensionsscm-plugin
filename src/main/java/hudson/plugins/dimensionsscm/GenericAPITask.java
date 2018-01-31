package hudson.plugins.dimensionsscm;

import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import java.io.File;
import java.io.IOException;

/**
 * Base class for Callables using Java API.
 */
abstract class GenericAPITask extends BaseCallable {
    private final FilePath workspace;

    /** listener is used by CheckInAPITask and CheckOutAPITask subclasses. */
    final TaskListener listener;

    private final String userName;
    private final String passwd;
    private final String database;
    private final String server;

    /** key is used by CheckInAPITask and CheckOutAPITask subclasses. */
    long key = -1L;

    /** dmSCM is used by CheckInAPITask and CheckOutAPITask subclasses. */
    DimensionsAPI dmSCM;

    GenericAPITask(DimensionsSCM parent, FilePath workspace, TaskListener listener) {
        Logger.debug("Creating task - " + this.getClass().getName());

        this.workspace = workspace;
        this.listener = listener;

        // Server details.
        userName = parent.getJobUserName();
        passwd = parent.getJobPasswd();
        database = parent.getJobDatabase();
        server = parent.getJobServer();
    }

    @Override
    public Boolean invoke(File area, VirtualChannel channel) throws IOException {
        boolean bRet = true;

        try {
            // This here code is executed on the slave.
            listener.getLogger().println("[DIMENSIONS] Running build in '" + area.getAbsolutePath() + "'...");

            if (dmSCM == null) {
                dmSCM = new DimensionsAPI();
            }
            dmSCM.setLogger(listener.getLogger());

            // Connect to Dimensions...
            key = dmSCM.login(userName, passwd, database, server);
            if (key > 0L) {
                Logger.debug("Login worked.");
                bRet = execute(area, channel);
            }
        } catch (Exception e) {
            bRet = false;
            throw (IOException) new IOException(Values.exceptionMessage("Exception during login", e, "no message")).initCause(e);
        } finally {
            dmSCM.logout(key);
        }
        dmSCM = null;
        return bRet;
    }

    /**
     * Template method for subclasses to override.
     */
    abstract Boolean execute(File area, VirtualChannel channel) throws IOException;
}
