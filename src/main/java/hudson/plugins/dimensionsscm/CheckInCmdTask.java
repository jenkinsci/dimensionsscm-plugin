package hudson.plugins.dimensionsscm;

import hudson.FilePath;
import hudson.model.TaskListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Deliver files to Dimensions CM repository using dmcli command-line.
 */
public class CheckInCmdTask extends GenericCmdTask {
    private final boolean forceCheckIn;
    private final boolean forceTip;
    private final boolean isStream;

    private final int buildNo;
    private final String jobId;

    private final String projectId;
    private String requests;
    private final String owningPart;

    private final String patternType;
    private final String[] patterns;
    private final String[] patternsExc;

    /**
     * Utility routine to create command file for dmcli.
     */
    private File createCmdFile(final File area, final File userFilelist) throws IOException {
        PrintWriter fmtWriter = null;
        File tmpFile = null;

        try {
            tmpFile = File.createTempFile("dmCm" + Long.toString(System.currentTimeMillis()), null, null);
            // 'DELIVER' command file in platform-default encoding.
            fmtWriter = new PrintWriter(new FileWriter(tmpFile), true);

            String ciCmd = "DELIVER /BRIEF /ADD /UPDATE /DELETE ";
            if (version == 10 || !isStream) {
                ciCmd = "UPLOAD ";
            }
            ciCmd += " /USER_FILELIST=\"" + userFilelist.getAbsolutePath() + "\"";
            ciCmd += " /WORKSET=\"" + projectId + "\"";
            ciCmd += " /COMMENT=\"Build artifacts delivered by Jenkins for job '" + jobId + "' - build "
                    + buildNo + "\"";
            ciCmd += " /USER_DIRECTORY=\"" + area.getAbsolutePath() + "\"";
            if (requests != null && requests.length() > 0) {
                if (requests.indexOf(',') == -1) {
                    ciCmd += "/CHANGE_DOC_IDS=(\"" + requests + "\") ";
                } else {
                    ciCmd += "/CHANGE_DOC_IDS=(" + requests + ") ";
                }
            }
            if (owningPart != null && owningPart.length() > 0) {
                ciCmd += "/PART=\"" + owningPart + "\"";
            }
            if (!isStream) {
                if (forceCheckIn) {
                    ciCmd += "/FORCE_CHECKIN ";
                }
                if (forceTip) {
                    ciCmd += "/FORCE_TIP ";
                }
            }

            fmtWriter.println(ciCmd);
            fmtWriter.flush();
        } catch (IOException e) {
            throw (IOException) new IOException(Values.exceptionMessage("Unable to write DELIVER command file: " + tmpFile, e,
                    "no message")).initCause(e);
        } finally {
            if (fmtWriter != null) {
                fmtWriter.close();
            }
        }
        return tmpFile;
    }

    public CheckInCmdTask(String userName, String passwd, String database, String server, String projectId,
            String requestId, boolean forceCheckIn, boolean forceTip, String[] patterns, String patternType,
            int version, boolean isStream, int buildNo, String jobId, String owningPart, FilePath workspace,
            TaskListener listener, String[] patternsExc) {
        super(userName, passwd, database, server, version, workspace, listener);
        this.isStream = isStream;

        // Config details.
        this.projectId = projectId;
        this.forceCheckIn = forceCheckIn;
        this.forceTip = forceTip;
        this.patterns = patterns;
        this.patternsExc = patternsExc;
        this.patternType = patternType;
        this.requests = requestId;
        this.buildNo = buildNo;
        this.jobId = jobId;
        this.owningPart = owningPart;
    }

    /**
     * Process the checkin.
     */
    @Override
    public Boolean execute(final File exe, final File param, final File area) throws IOException {
        FilePath wa = new FilePath(area);
        boolean bRet = true;
        try {
            listener.getLogger().println("[DIMENSIONS] Scanning workspace for files to be saved into Dimensions...");
            listener.getLogger().flush();

            FilePath wd = new FilePath(area);
            File dir = new File(wd.getRemote());
            File[] validFiles = new File[0];

            if (patternType.equals("regEx")) {
                listener.getLogger().println("[DIMENSIONS] Running RegEx pattern scanner...");
                FileScanner fs = new FileScanner(dir, patterns, patternsExc, -1);
                validFiles = fs.toArray();
                listener.getLogger().println("[DIMENSIONS] Found " + validFiles.length + " file(s) to check in...");
            } else if (patternType.equals("Ant")) {
                listener.getLogger().println("[DIMENSIONS] Running Ant pattern scanner...");
                FileAntScanner fs = new FileAntScanner(dir, patterns, patternsExc, -1);
                validFiles = fs.toArray();
                listener.getLogger().println("[DIMENSIONS] Found " + validFiles.length + " file(s) to check in...");
            }

            listener.getLogger().flush();

            String cmdLog = null;

            if (validFiles.length > 0) {
                if (requests != null) {
                    requests = requests.replaceAll(" ", "");
                    requests = requests.toUpperCase(Values.ROOT_LOCALE);
                }

                File tmpFile = null;
                PrintWriter fmtWriter = null;

                try {
                    tmpFile = File.createTempFile("dmCm" + Long.toString(System.currentTimeMillis()), null, null);
                    // 'DELIVER/USER_FILELIST=' user filelist in platform-default encoding.
                    fmtWriter = new PrintWriter(new FileWriter(tmpFile), true);

                    for (File f : validFiles) {
                        if (f.isDirectory()) {
                        } else {
                            fmtWriter.println(f.getAbsolutePath());
                        }
                    }
                    fmtWriter.flush();
                } catch (IOException e) {
                    throw (IOException) new IOException(Values.exceptionMessage("Unable to write user filelist: " + tmpFile, e,
                            "no message")).initCause(e);
                } finally {
                    if (fmtWriter != null) {
                        fmtWriter.close();
                    }
                }

                File cmdFile = createCmdFile(area, tmpFile);
                if (cmdFile == null) {
                    listener.getLogger().println("[DIMENSIONS] Error: Cannot create DELIVER command file.");
                    param.delete();
                    tmpFile.delete();
                    return false;
                }

                listener.getLogger().println("[DIMENSIONS] Loading files into Dimensions project \""
                        + projectId + "\"...");
                listener.getLogger().flush();

                /* Execute a Dimensions command */
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

                if (cmdLog == null) {
                    cmdLog = "\n";
                }
                cmdLog += outputStr;
                cmdLog += "\n";
            } else {
                listener.getLogger().println("[DIMENSIONS] No build artifacts found for checking in");
            }

            listener.getLogger().flush();

            param.delete();

            if (cmdLog != null && cmdLog.length() > 0 && listener.getLogger() != null) {
                listener.getLogger().println("[DIMENSIONS] (Note: Dimensions command output was - ");
                cmdLog = cmdLog.replaceAll("\n\n", "\n");
                listener.getLogger().println(cmdLog.replaceAll("\n", "\n[DIMENSIONS] ") + ")");
                listener.getLogger().flush();
            }

            if (!bRet) {
                listener.getLogger().println("[DIMENSIONS] ==========================================================");
                listener.getLogger().println("[DIMENSIONS] The Dimensions checkin command returned a failure status.");
                listener.getLogger().println("[DIMENSIONS] Please review the command output and correct any issues");
                listener.getLogger().println("[DIMENSIONS] that may have been detected.");
                listener.getLogger().println("[DIMENSIONS] ==========================================================");
                listener.getLogger().flush();
            }
            return bRet;
        } catch (Exception e) {
            param.delete();
            String message = Values.exceptionMessage("Unable to run checkin callout", e, "no message - try again");
            listener.fatalError(message);
            Logger.debug(message, e);
            bRet = false;
        }
        return bRet;
    }
}
