package hudson.plugins.dimensionsscm;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.dimensionsscm.model.StringVarStorage;
import hudson.remoting.VirtualChannel;
import hudson.util.VariableResolver;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

/**
 * Update local work area using Java API.
 */
public class CheckOutAPITask extends GenericAPITask {
    private final boolean bFreshBuild;
    private final boolean isDelete;
    private final boolean isRevert;
    private final boolean isForce;
    private final boolean isExpand;
    private final boolean isNoMetadata;
    private final boolean isNoTouch;

    private final String projectId;
    private final List<StringVarStorage> folders;
    private final String permissions;
    private final String eol;

    private final int version;
    private String request;
    private String baseline;

    public CheckOutAPITask(Run<?, ?> build, DimensionsSCM parent, FilePath workspace, TaskListener listener,
                           int version) {
        super(parent, workspace, listener);
        Logger.debug("Creating task - " + this.getClass().getName());

        // Server details (see superclass).
        this.version = version;

        // Config details.
        this.isDelete = parent.isCanJobDelete();
        this.projectId = parent.getProjectVersion(build, listener);
        this.isRevert = parent.isCanJobRevert();
        this.isForce = parent.isCanJobForce();
        this.isExpand = parent.isCanJobExpand();
        this.isNoMetadata = parent.isCanJobNoMetadata();
        this.isNoTouch = parent.isCanJobNoTouch();

        this.folders = parent.getFolders();
        this.permissions = parent.getPermissions();
        this.eol = parent.getEol();

        // Build details.
        this.bFreshBuild = (build.getPreviousBuild() == null);

        this.baseline = null;
        this.request = null;

        if (build instanceof AbstractBuild) {

            VariableResolver<String> myResolver = ((AbstractBuild<?, ?>) build).getBuildVariableResolver();

            this.baseline = myResolver.resolve("DM_BASELINE");
            this.request = myResolver.resolve("DM_REQUEST");
        } else if (build instanceof WorkflowRun) {
            this.baseline = parent.getParameterFromBuild((WorkflowRun) build, "DM_BASELINE");
            this.request = parent.getParameterFromBuild((WorkflowRun) build, "DM_REQUEST");
        }
    }

    @Override
    public Boolean execute(File area, VirtualChannel channel) throws IOException, InterruptedException {
        boolean bRet = true;
        try {
            StringBuffer cmdOutput = new StringBuffer();
            FilePath wa = new FilePath(area);

            // Emulate SVN plugin - if workspace exists and it is not managed by this project, blow it away.
            if (bFreshBuild) {
                if (listener.getLogger() != null) {
                    listener.getLogger().println("[DIMENSIONS] Checking out a fresh workspace because this project has not been built before...");
                    listener.getLogger().flush();
                }
            }

            if (wa.exists() && (isDelete || bFreshBuild)) {
                Logger.debug("Deleting '" + wa.toURI() + "'...");
                listener.getLogger().println("[DIMENSIONS] Removing '" + wa.toURI() + "'...");
                listener.getLogger().flush();
                wa.deleteContents();
            }

            String baseline = this.baseline;
            String requests = this.request;

            if (baseline != null) {
                baseline = baseline.trim();
                baseline = baseline.toUpperCase(Values.ROOT_LOCALE);
            }
            if (requests != null) {
                requests = requests.replaceAll(" ", "");
                requests = requests.toUpperCase(Values.ROOT_LOCALE);
            }

            Logger.debug("Extra parameters - " + baseline + " " + requests);

            String cmdLog = null;

            if (baseline != null && baseline.length() == 0) {
                baseline = null;
            }
            if (requests != null && requests.length() == 0) {
                requests = null;
            }
            if (listener.getLogger() != null) {
                if (requests != null) {
                    listener.getLogger().println("[DIMENSIONS] Checking out request(s) \"" + requests + "\" - ignoring project folders...");
                } else if (baseline != null) {
                    listener.getLogger().println("[DIMENSIONS] Checking out baseline \"" + baseline + "\"...");
                } else {
                    listener.getLogger().println("[DIMENSIONS] Checking out project \"" + projectId + "\"...");
                }
                listener.getLogger().flush();
            }

            if (version == 10 && requests != null) {
                String[] requestsProcess = requests.split(",");
                if (requestsProcess.length == 0) {
                    requestsProcess = new String[]{requests};
                }

                listener.getLogger().println("[DIMENSIONS] Defaulting to read-only permissions as this is the only available mode...");

                for (String reqId : requestsProcess) {
                    if (!bRet) {
                        break;
                    }
                    String folderN = "/";
                    File fileName = new File(folderN);
                    FilePath dname = new FilePath(fileName);

                    Logger.debug("Checking out '" + folderN + "'...");

                    // Checkout the folder.
                    bRet = dmSCM.checkout(key, projectId, dname, wa, cmdOutput, baseline, reqId, isRevert, isExpand,
                            isNoMetadata, isNoTouch, "DEFAULT", eol);
                    Logger.debug("SCM checkout returned " + bRet);

                    if (!bRet && isForce) {
                        bRet = true;
                    }
                    if (cmdLog == null) {
                        cmdLog = "\n";
                    }
                    cmdLog += cmdOutput;
                    cmdOutput.setLength(0);
                    cmdLog += "\n";
                }
            } else {
                // Iterate through the project folders and process them in Dimensions.
                for (StringVarStorage folderStrg : folders) {

                    String folderN = folderStrg.getStrVar();
                    if (!bRet) {
                        break;
                    }
                    File fileName = new File(folderN);
                    FilePath dname = new FilePath(fileName);

                    Logger.debug("Checking out '" + folderN + "'...");

                    // Checkout the folder.
                    bRet = dmSCM.checkout(key, projectId, dname, wa, cmdOutput, baseline, requests, isRevert, isExpand,
                            isNoMetadata, isNoTouch, permissions, eol);
                    Logger.debug("SCM checkout returned " + bRet);

                    if (!bRet && isForce) {
                        bRet = true;
                    }
                    if (cmdLog == null) {
                        cmdLog = "\n";
                    }
                    cmdLog += cmdOutput;
                    cmdOutput.setLength(0);
                    cmdLog += "\n";
                    if (requests != null) {
                        break;
                    }
                }
            }

            PrintStream log = listener.getLogger();
            if (!Values.isNullOrEmpty(cmdLog) && log != null) {
                Logger.debug("Found command output to log to the build logger");
                log.println("[DIMENSIONS] (Note: Dimensions command output was - ");
                cmdLog = cmdLog.replaceAll("\n\n", "\n");
                log.println(cmdLog.replaceAll("\n", "\n[DIMENSIONS] ") + ")");
                log.flush();
            }

            if (!bRet && log != null) {
                log.println("[DIMENSIONS] ==========================================================");
                log.println("[DIMENSIONS] The Dimensions checkout command returned a failure status.");
                log.println("[DIMENSIONS] Please review the command output and correct any issues");
                log.println("[DIMENSIONS] that may have been detected.");
                log.println("[DIMENSIONS] ==========================================================");
                log.flush();
                throw new IOException("Error: the Dimensions checkout command returned a failure status.");
            }
        } catch (Exception e) {
            String message = Values.exceptionMessage("Unable to run checkout callout", e, "no message - try again");
            listener.fatalError(message);
            Logger.debug(message, e);
            throw new IOException(message);
        }
        return bRet;
    }
}
