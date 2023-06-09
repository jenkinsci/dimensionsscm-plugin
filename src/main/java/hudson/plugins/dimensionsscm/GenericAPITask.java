package hudson.plugins.dimensionsscm;

import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.IOException;

/**
 * Base class for Callables using Java API.
 */
abstract class GenericAPITask extends BaseCallable {
    /** listener is used by CheckInAPITask and CheckOutAPITask subclasses. */
    final TaskListener listener;

    final DimensionsSCM scm;

    final Run<?, ?> run;

    /** key is used by CheckInAPITask and CheckOutAPITask subclasses. */
    long key = -1L;

    /** scmAPI is used by CheckInAPITask and CheckOutAPITask subclasses. */
    DimensionsAPI scmAPI;

    GenericAPITask(Run<?, ?> run, DimensionsSCM parent, FilePath workspace, TaskListener listener) {
        Logger.debug("Creating task - " + this.getClass().getName());
        this.listener = listener;
        this.scm = parent;
        this.run = run;
        this.scm.fillInCredentials(this.run);
    }

    @Override
    public Boolean invoke(File area, VirtualChannel channel) throws IOException {
        boolean bRet = true;

        try {
            // This here code is executed on the slave.
            listener.getLogger().println("[DIMENSIONS] Running build in '" + area.getAbsolutePath() + "'...");

            if (scmAPI == null) {
                scmAPI = new DimensionsAPI();
            }
            scmAPI.setLogger(listener.getLogger());

            // Connect to Dimensions...
            key = scmAPI.login(scm, run);
            if (key > 0L) {
                Logger.debug("Login worked.");
                bRet = execute(area, channel);
            }
        } catch (Exception e) {
            throw new IOException(Values.exceptionMessage("Exception during login", e, "no message"), e);
        } finally {
            scmAPI.logout(key);
        }
        scmAPI = null;
        return bRet;
    }

    /**
     * Template method for subclasses to override.
     */
    abstract Boolean execute(File area, VirtualChannel channel) throws IOException, InterruptedException;
}
