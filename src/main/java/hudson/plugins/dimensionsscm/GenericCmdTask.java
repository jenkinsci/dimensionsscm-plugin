package hudson.plugins.dimensionsscm;

import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.util.Secret;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Base class for Callables using dmcli command-line.
 */
abstract class GenericCmdTask extends BaseCallable {
    private final FilePath workspace;

    /** listener is used by CheckInCmdTask and CheckOutCmdTask subclasses. */
    final TaskListener listener;

    private final String userName;
    private final Secret passwd;
    private final String database;
    private final String server;

    /** version is used by CheckInCmdTask and CheckOutCmdTask subclasses. */
    final int version;

    private static final String EXEC = "dmcli";

    /**
     * Utility routine to look for an executable in the path.
     */
    private File getExecutable(String exeName) {
        // Get the path environment.
        return PathUtils.getExecutable(exeName);
    }

    /**
     * Utility routine to create parameter file for dmcli.
     */
    private File createParamFile() throws IOException {
        PrintWriter fmtWriter = null;
        File tmpFile = null;

        try {
            tmpFile = File.createTempFile("dmCm" + Long.toString(System.currentTimeMillis()), null, null);
            // dmcli parameter file in platform-default encoding.
            fmtWriter = new PrintWriter(new FileWriter(tmpFile), true);
            fmtWriter.println("-host " + server);
            fmtWriter.println("-user " + userName);
            fmtWriter.println("-pass " + passwd);
            fmtWriter.println("-dbname " + database);
            fmtWriter.flush();
        } catch (IOException e) {
            throw (IOException) new IOException(Values.exceptionMessage("Unable to write dmcli parameter file: " + tmpFile, e,
                    "no message")).initCause(e);
        } finally {
            if (fmtWriter != null) {
                fmtWriter.close();
            }
        }
        return tmpFile;
    }

    GenericCmdTask(String userName, Secret passwd, String database, String server, int version,
            FilePath workspace, TaskListener listener) {
        this.workspace = workspace;
        this.listener = listener;

        // Server details.
        this.userName = userName;
        this.passwd = passwd;
        this.database = database;
        this.server = server;
        this.version = version;
    }

    @Override
    public Boolean invoke(File area, VirtualChannel channel) throws IOException {
        boolean retStatus = false;

        // This here code is executed on the slave.
        try {
            listener.getLogger().println("[DIMENSIONS] Running build in '" + area.getAbsolutePath() + "'...");

            File exe = getExecutable(EXEC);
            if (exe == null) {
                listener.getLogger().println("[DIMENSIONS] Error: Cannot locate '" + EXEC + "' on the slave node.");
            } else {
                listener.getLogger().println("[DIMENSIONS] Located '" + exe.getAbsolutePath() + "' on the slave node.");

                File param = createParamFile();
                if (param == null) {
                    listener.getLogger().println("[DIMENSIONS] Error: Cannot create parameter file for Dimensions login.");
                    return false;
                }

                retStatus = execute(exe, param, area);
                param.delete();
            }
            return retStatus;
        } catch (Exception e) {
            listener.fatalError(Values.exceptionMessage("Unable to run command callout", e, "no message - try again"));
            return false;
        }
    }

    /**
     * Process the task. Template method to override in subclasses.
     */
    abstract Boolean execute(final File exe, final File param, final File area) throws IOException;
}
