package hudson.plugins.dimensionsscm;

import com.serena.dmclient.api.DimensionsResult;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.util.VariableResolver;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Deliver files to Dimensions CM repository using Java API.
 */
public class CheckInAPITask extends GenericAPITask {
    private final boolean isForceTip;
    private final boolean isForceCheckIn;
    private final VariableResolver<String> myResolver;

    private final int buildNo;

    private final String jobId;

    private final String projectId;
    private final String owningPart;

    private final String[] patterns;
    private final String[] patternsExc;
    private final String patternType;

    public CheckInAPITask(AbstractBuild<?, ?> build, DimensionsSCM parent, int buildNo, String jobId, int version,
            ArtifactUploader artifact, FilePath workspace, TaskListener listener) {
        super(parent, workspace, listener);
        Logger.debug("Creating task - " + this.getClass().getName());

        // Config details
        this.projectId = parent.getProjectName(build, listener);
        this.isForceCheckIn = artifact.isForceCheckIn();
        this.isForceTip = artifact.isForceTip();
        this.owningPart = artifact.getOwningPart();
        this.patterns = artifact.getPatterns();
        this.patternsExc = artifact.getPatternsExc();

        this.patternType = artifact.getPatternType();

        // Build details.
        this.myResolver = build.getBuildVariableResolver();
        this.buildNo = buildNo;
        this.jobId = jobId;
    }

    @Override
    public Boolean execute(File area, VirtualChannel channel) {
        boolean bRet = true;

        try {
            listener.getLogger().println("[DIMENSIONS] Scanning workspace for files to be saved into Dimensions...");
            listener.getLogger().flush();
            FilePath wd = new FilePath(area);
            Logger.debug("Scanning directory for files that match patterns '" + wd.getRemote() + "'");
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

            if (validFiles.length > 0) {
                listener.getLogger().println("[DIMENSIONS] Loading files into Dimensions project \"" + projectId
                        + "\"...");
                listener.getLogger().flush();

                PrintWriter fmtWriter = null;
                File tmpFile = null;

                try {
                    tmpFile = File.createTempFile("dmCm" + System.currentTimeMillis(), null, null);
                    // 'DELIVER/USER_FILELIST=' user filelist in platform-default encoding.
                    fmtWriter = new PrintWriter(new FileWriter(tmpFile), true);

                    for (File f : validFiles) {
                        if (!f.isDirectory()) {
                            Logger.debug("Found file '" + f.getAbsolutePath() + "'");
                            fmtWriter.println(f.getAbsolutePath());
                        }
                    }
                    fmtWriter.flush();
                } catch (IOException e) {
                    throw new IOException(Values.exceptionMessage("Unable to write user filelist: " + tmpFile, e,
                            "no message"), e);
                } finally {
                    if (fmtWriter != null) {
                        fmtWriter.close();
                    }
                }

                // Debug for printing out files
                //String filesToLoad = new String(FileUtils.loadFile(tmpFile));
                //if (filesToLoad != null) {
                //    filesToLoad += "\n";
                //    filesToLoad = filesToLoad.replaceAll("\n\n", "\n");
                //    listener.getLogger().println(filesToLoad.replaceAll("\n", "\n[DIMENSIONS] - "));
                //}

                {
                    String requests = myResolver.resolve("DM_TARGET_REQUEST");

                    if (requests != null) {
                        requests = requests.replaceAll(" ", "");
                        requests = requests.toUpperCase(Values.ROOT_LOCALE);
                    }

                    DimensionsResult res = dmSCM.UploadFiles(key, wd, projectId, tmpFile, jobId, buildNo, requests,
                            isForceCheckIn, isForceTip, owningPart);
                    if (res == null) {
                        listener.getLogger().println("[DIMENSIONS] New artifacts failed to get loaded into Dimensions");
                        listener.getLogger().flush();
                        bRet = false;
                    } else {
                        listener.getLogger().println("[DIMENSIONS] Build artifacts were successfully loaded into Dimensions");
                        listener.getLogger().println("[DIMENSIONS] (" + res.getMessage().replaceAll("\n",
                                "\n[DIMENSIONS] ") + ")");
                        listener.getLogger().flush();
                    }
                }

                tmpFile.delete();
            } else {
                listener.getLogger().println("[DIMENSIONS] No build artifacts found for checking in");
            }
            listener.getLogger().flush();
        } catch (Exception e) {
            String message = Values.exceptionMessage("Unable to run checkin callout", e, "no message - try again");
            listener.fatalError(message);
            Logger.debug(message, e);
            bRet = false;
        }
        return bRet;
    }
}
