package hudson.plugins.dimensionsscm;

import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.util.Secret;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;

/**
 * Base class for Callables using dmcli command-line.
 */
abstract class GenericCmdTask extends BaseCallable {
    /** listener is used by CheckInCmdTask and CheckOutCmdTask subclasses. */
    final TaskListener listener;

    private final String userName;
    private final Secret passwd;
    private final String database;
    private final String server;

    private final String certificatePath;
    private final Secret certificatePassword;
    private final boolean isSecureAgentAuth;
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
            tmpFile = Files.createTempFile("dmCm" + System.currentTimeMillis(), null).toFile();
            // dmcli parameter file in platform-default encoding.
            fmtWriter = new PrintWriter(new FileWriter(tmpFile), true);
            fmtWriter.println("-host " + server);
            fmtWriter.println("-dbname " + database);

            if (isSecureAgentAuth) {
                fmtWriter.println("-cac_cert_file " + certificatePath);
                fmtWriter.println("-cac_key_password " + certificatePassword.getPlainText());
            } else {
                fmtWriter.println("-user " + userName);
                fmtWriter.println("-pass " + passwd);
            }


            fmtWriter.flush();
        } catch (IOException e) {
            throw new IOException(Values.exceptionMessage("Unable to write dmcli parameter file: " + tmpFile, e,
                    "no message"), e);
        } finally {
            if (fmtWriter != null) {
                fmtWriter.close();
            }
        }
        return tmpFile;
    }

    GenericCmdTask(String userName, Secret passwd, String database, String server, int version,
                   String certificatePath, Secret certificatePassword, boolean isSecureAgentAuth, FilePath workspace, TaskListener listener) {
        this.listener = listener;

        // Server details.
        this.userName = userName;
        this.passwd = passwd;
        this.database = database;
        this.server = server;
        this.version = version;
        this.certificatePath = certificatePath;
        this.certificatePassword = certificatePassword;
        this.isSecureAgentAuth = isSecureAgentAuth;
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
            String message = Values.exceptionMessage("Unable to run command callout", e, "no message - try again");
            listener.fatalError(message);
            throw new IOException(message);
        }
    }

    /**
     * Process the task. Template method to override in subclasses.
     */
    abstract Boolean execute(final File exe, final File param, final File area) throws IOException, InterruptedException;
}
