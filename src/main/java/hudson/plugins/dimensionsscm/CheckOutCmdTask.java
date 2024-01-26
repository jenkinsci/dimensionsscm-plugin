package hudson.plugins.dimensionsscm;

import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.plugins.dimensionsscm.model.StringVarStorage;
import hudson.util.Secret;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;

/**
 * Update local work area using dmcli command-line.
 */
public class CheckOutCmdTask extends GenericCmdTask {
    private final boolean bFreshBuild;
    private final boolean isDelete;
    private final boolean isRevert;
    private final boolean isForce;
    private final boolean isExpand;
    private final boolean isNoMetadata;
    private final boolean isNoTouch;

    private final String projectId;
    private String baseline;
    private String requests;
    private final String permissions;
    private final String eol;

    private final List<StringVarStorage> folders;

    /**
     * Utility routine to create command file for dmcli.
     */
    private File createCmdFile(final String reqId, final String projDir, final File area) throws IOException {
        PrintWriter fmtWriter = null;
        File tmpFile = null;

        try {
            tmpFile = File.createTempFile("dmCm" + System.currentTimeMillis(), null, null);
            // 'UPDATE' command file in platform-default encoding.
            fmtWriter = new PrintWriter(new FileWriter(tmpFile), true);

            String coCmd = "UPDATE /BRIEF ";
            if (version == 10) {
                coCmd = "DOWNLOAD ";
                if (requests != null) {
                    coCmd = "FCDI ";
                }
            }

            if (version == 2010) {
                coCmd += " /LEGACY_MODE ";
            }

            String cmd = coCmd;

            if (reqId != null && version == 10) {
                cmd += reqId;
            }

            if (projDir != null && !projDir.equals("\\") && !projDir.equals("/") && requests == null) {
                cmd += "/DIR=\"" + projDir + "\"";
            }

            if (requests != null && version != 10) {
                if (requests.indexOf(',') == -1) {
                    cmd += "/CHANGE_DOC_IDS=(\"" + requests + "\") ";
                } else {
                    cmd += "/CHANGE_DOC_IDS=(" + requests + ") ";
                }
                cmd += "/WORKSET=\"" + projectId + "\" ";
            } else if (baseline != null) {
                cmd += "/BASELINE=\"" + baseline + "\"";
            } else {
                cmd += "/WORKSET=\"" + projectId + "\" ";
            }

            cmd += "/USER_DIR=\"" + area.getAbsolutePath() + "\" ";

            if (isRevert) {
                cmd += " /OVERWRITE";
            }
            if (isExpand) {
                cmd += " /EXPAND";
            }
            if (isNoMetadata) {
                cmd += " /NOMETADATA";
            }
            if (isNoTouch) {
                cmd += " /NOTOUCH";
            }

            if (permissions != null && permissions.length() > 0) {
                if (!permissions.equals("DEFAULT") && reqId == null) {
                    cmd += "/PERMS=" + permissions;
                }
            }

            if (eol != null && eol.length() > 0) {
                if (!eol.equals("DEFAULT")) {
                    cmd += "/EOL=" + eol;
                }
            }

            fmtWriter.println(cmd);
            fmtWriter.flush();
        } catch (IOException e) {
            throw new IOException(Values.exceptionMessage("Unable to write UPDATE command file: " + tmpFile, e,
                    "no message"), e);
        } finally {
            if (fmtWriter != null) {
                fmtWriter.close();
            }
        }
        return tmpFile;
    }

    public CheckOutCmdTask(String userName, Secret passwd, String database, String dbConn, String server, String projectId,
                           String baselineId, String requestId, boolean isDelete, boolean isRevert, boolean isForce, boolean isExpand,
                           boolean isNoMetadata, boolean isNoTouch, boolean freshBuild, List<StringVarStorage> folders, int version,
                           String permissions, String eol, String certificatePath, Secret certificatePassword,
                           boolean isSecureAgentAuth, FilePath workspace, TaskListener listener) {
        super(userName, passwd, database, dbConn, server, version, certificatePath, certificatePassword, isSecureAgentAuth, workspace, listener);

        // Config details.
        this.isDelete = isDelete;
        this.projectId = projectId;
        this.isRevert = isRevert;
        this.isForce = isForce;
        this.isExpand = isExpand;
        this.isNoMetadata = isNoMetadata;
        this.isNoTouch = isNoTouch;
        this.folders = folders;
        this.requests = requestId;
        this.baseline = baselineId;
        this.permissions = permissions;
        this.eol = eol;

        // Build details.
        this.bFreshBuild = freshBuild;
    }

    /**
     * Process the checkout.
     */
    @Override
    public Boolean execute(final File exe, final File param, final File area) throws IOException, InterruptedException {

        FilePath wa = new FilePath(area);
        boolean bRet = true;

        if (!area.exists()) {
            area.mkdir();
        }

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

        if (baseline != null) {
            baseline = baseline.trim();
            baseline = baseline.toUpperCase(Values.ROOT_LOCALE);
        }
        if (requests != null) {
            requests = requests.replaceAll(" ", "");
            requests = requests.toUpperCase(Values.ROOT_LOCALE);
        }

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
                FilePath projectDir = new FilePath(fileName);
                String projDir = projectDir.getRemote();

                String remote = PathUtils.normalizePath(area.getAbsolutePath());

                File cmdFile = createCmdFile(reqId, projDir, new File(remote));
                if (cmdFile == null) {
                    listener.getLogger().println("[DIMENSIONS] Error: Cannot create UPDATE command file.");
                    param.delete();
                    return false;
                }

                String[] cmd = new String[5];
                cmd[0] = exe.getAbsolutePath();
                cmd[1] = "-param";
                cmd[2] = param.getAbsolutePath();
                cmd[3] = "-file";
                cmd[4] = cmdFile.getAbsolutePath();

                SCMLauncher proc = new SCMLauncher(cmd, listener, wa);
                bRet = proc.execute();
                String outputStr = proc.getResults();
                cmdFile.delete();

                if (bRet) {
                    // Check if any conflicts were identified.
                    int confl = outputStr.indexOf("C\t");
                    if (confl > 0) {
                        bRet = false;
                    }
                }

                if (cmdLog == null) {
                    cmdLog = "\n";
                }
                cmdLog += outputStr;
                cmdLog += "\n";

                if (!bRet && isForce) {
                    bRet = true;
                }
            }
        } else {
            // Iterate through the project folders and process them in Dimensions.
            for (StringVarStorage folderStrg : folders) {

                String folderN = folderStrg.getValue();
                if (!bRet) {
                    break;
                }
                File fileName = new File(folderN);
                FilePath projectDir = new FilePath(fileName);
                String projDir = projectDir.getRemote();

                String remote = PathUtils.normalizePath(area.getAbsolutePath());

                File cmdFile = createCmdFile(null, projDir, new File(remote));
                if (cmdFile == null) {
                    listener.getLogger().println("[DIMENSIONS] Error: Cannot create UPDATE command file.");
                    param.delete();
                    return false;
                }

                if (requests == null) {
                    listener.getLogger().println("[DIMENSIONS] Checking out directory '"
                            + (projDir != null ? projDir : "/") + "'...");
                    listener.getLogger().flush();
                }

                String[] cmd = new String[5];
                cmd[0] = exe.getAbsolutePath();
                cmd[1] = "-param";
                cmd[2] = param.getAbsolutePath();
                cmd[3] = "-file";
                cmd[4] = cmdFile.getAbsolutePath();

                SCMLauncher proc = new SCMLauncher(cmd, listener, wa);
                bRet = proc.execute();
                String outputStr = proc.getResults();
                cmdFile.delete();

                if (bRet) {
                    // Check if any conflicts were identified.
                    int confl = outputStr.indexOf("C\t");
                    if (confl > 0) {
                        bRet = false;
                    }
                }

                if (cmdLog == null) {
                    cmdLog = "\n";
                }
                cmdLog += outputStr;
                cmdLog += "\n";

                if (!bRet && isForce) {
                    bRet = true;
                }
                if (requests != null) {
                    break;
                }
            }
        }
        param.delete();

        PrintStream log = listener.getLogger();
        if (!Values.isNullOrEmpty(cmdLog) && log != null) {
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
        return bRet;
    }
}
