package hudson.plugins.dimensionsscm;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.Secret;
import hudson.util.VariableResolver;

import java.io.IOException;
import java.io.Serializable;

import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * A Notifier that can deliver built artifacts back to the SCM project/stream
 * as a post-build step in a Jenkins build.
 */
public class ArtifactUploader extends Notifier implements Serializable {
    private static final String[] DEFAULT_INCLUDES_REGEX = new String[]{".*"};
    private static final String[] DEFAULT_INCLUDES_ANT = new String[]{"**/*"};

    private final String[] patternsRegEx;
    private final String[] patternsAnt;
    private final String[] patternsRegExExc;
    private final String[] patternsAntExc;

    private final boolean forceCheckIn;
    private final boolean forceTip;
    private final String owningPart;
    private final boolean forceAsSlave;
    private final String patternType;

    @DataBoundConstructor
    public ArtifactUploader(String[] pregEx, boolean fTip, boolean fMerge, String part, boolean fAsSlave,
                            String patternType, String[] pAnt, String[] pregExExc, String[] pAntExc) {
        this.patternsRegEx = Values.notEmptyOrElse(Values.trimCopy(pregEx), DEFAULT_INCLUDES_REGEX);
        this.patternsAnt = Values.notEmptyOrElse(Values.trimCopy(pAnt), DEFAULT_INCLUDES_ANT);
        this.patternsRegExExc = Values.trimCopy(pregExExc);
        this.patternsAntExc = Values.trimCopy(pAntExc);
        this.forceCheckIn = fTip;
        this.forceTip = fMerge;
        this.owningPart = Values.textOrElse(part, null);
        this.forceAsSlave = fAsSlave;
        this.patternType = Values.textOrElse(patternType, "regEx");
    }

    /**
     * Gets the patterns to upload.
     */
    public String[] getPatternsRegEx() {
        return this.patternsRegEx;
    }

    /**
     * Gets the patterns to upload.
     */
    public String[] getPatternsAnt() {
        return this.patternsAnt;
    }

    /**
     * Gets the patterns to exclude from upload.
     */
    public String[] getPatternsRegExExc() {
        return this.patternsRegExExc;
    }

    /**
     * Gets the patterns to exclude from upload.
     */
    public String[] getPatternsAntExc() {
        return this.patternsAntExc;
    }

    /**
     * Gets the patterns to exclude from upload.
     */
    public String[] getPatternsExc() {
        return getPatternType().equals("Ant") ? this.patternsAntExc : this.patternsRegExExc;
    }

    /**
     * Gets the patterns to upload.
     */
    public String[] getPatterns() {
        return getPatternType().equals("Ant") ? this.patternsAnt : this.patternsRegEx;
    }

    /**
     * Gets the owning part name.
     */
    public String getOwningPart() {
        return this.owningPart;
    }

    /**
     * Gets the pattern type name.
     */
    public String getPatternType() {
        return this.patternType;
    }

    /**
     * Gets force checkin flag.
     *
     * @return forceCheckIn
     */
    public boolean isForceCheckIn() {
        return this.forceCheckIn;
    }

    /**
     * Gets force merge flag.
     */
    public boolean isForceTip() {
        return this.forceTip;
    }

    /**
     * Gets force as slave flag.
     */
    public boolean isForceAsSlave() {
        return this.forceAsSlave;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        Logger.debug("Invoking perform callout " + this.getClass().getName());
        FilePath workspace = build.getWorkspace();
        boolean bRet;
        boolean isStream = false;

        try {
            if (!(build.getProject().getScm() instanceof DimensionsSCM)) {
                listener.fatalError("[DIMENSIONS] This plugin only works with the Dimensions SCM engine.");
                build.setResult(Result.FAILURE);
                throw new IOException("[DIMENSIONS] This plugin only works with a Dimensions SCM engine");
            }

            if (build.getResult() == Result.SUCCESS) {
                DimensionsSCM scm = (DimensionsSCM) build.getProject().getScm();
                DimensionsAPI dmSCM = new DimensionsAPI();
                String nodeName = build.getBuiltOn().getNodeName();

                Logger.debug("Calculating version of Dimensions...");

                int version = 2009;
                long key = dmSCM.login(scm, build);

                if (key > 0L) {
                    // Get the server version.
                    Logger.debug("Login worked.");
                    version = dmSCM.getDmVersion();
                    if (version == 0) {
                        version = 2009;
                    }
                    if (version != 10) {
                        isStream = dmSCM.isStream(key, scm.getProjectName(build, listener));
                    }
                    dmSCM.logout(key, build);
                }

                String projectName = build.getProject().getName();
                int buildNo = build.getNumber();

                boolean master = true;
                if (isForceAsSlave()) {
                    master = false;
                    Logger.debug("Forced processing as slave...");
                } else {
                    Logger.debug("Checking if master or slave...");
                    if (nodeName != null && nodeName.length() > 0) {
                        master = false;
                    }
                }

                if (master) {
                    // Running on master...
                    listener.getLogger().println("[DIMENSIONS] Running checkin on master...");
                    listener.getLogger().flush();

                    // Using Java API because this allows the plugin to work on platforms where Dimensions has not
                    // been ported, e.g. MAC OS, which is what I use.
                    CheckInAPITask task = new CheckInAPITask(build, scm, buildNo, projectName, version, this,
                            workspace, listener);
                    bRet = workspace.act(task);
                } else {
                    // Running on slave... Have to use the command line as Java API will not work on remote hosts.
                    // Cannot serialise it...
                    // VariableResolver does not appear to be serialisable either, so...
                    VariableResolver<String> myResolver = build.getBuildVariableResolver();

                    String requests = myResolver.resolve("DM_TARGET_REQUEST");

                    listener.getLogger().println("[DIMENSIONS] Running checkin on slave...");
                    listener.getLogger().flush();

                    CheckInCmdTask task = new CheckInCmdTask(scm.getJobUserName(), Secret.decrypt(scm.getJobPasswd()),
                            scm.getJobDatabase(), scm.getJobServer(), scm.getProjectName(build, listener), requests,
                            isForceCheckIn(), isForceTip(), getPatterns(), getPatternType(), version, isStream,
                            buildNo, projectName, getOwningPart(), workspace, listener, getPatternsExc());
                    bRet = workspace.act(task);
                }
            } else {
                bRet = true;
            }
            if (!bRet) {
                build.setResult(Result.FAILURE);
            }
        } catch (Exception e) {
            String message = Values.exceptionMessage("Unable to load build artifacts into Dimensions", e, "no message");
            listener.fatalError(message);
            Logger.debug(message, e);
            build.setResult(Result.FAILURE);
            return false;
        }
        return bRet;
    }

    /**
     * The ArtifactUploader Descriptor class.
     */
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        /**
         * Loads the descriptor.
         */
        public DescriptorImpl() {
            super(ArtifactUploader.class);
            load();
            Logger.debug("Loading " + this.getClass().getName());
        }

        @Override
        public String getDisplayName() {
            return "Load any build artifacts into the Dimensions repository";
        }

        /**
         * This builder can be used with all project types.
         */
        @Override
        public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType) {
            return true;
        }

        /**
         * Save the descriptor configuration.
         */
        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            req.bindParameters(this, "ArtifactUploader");
            return super.configure(req, formData);
        }

        @Override
        public Notifier newInstance(StaplerRequest req, JSONObject formData) {
            // Get variables and then construct a new object.
            String[] pregEx = req.getParameterValues("artifactuploader.patternsRegEx");
            String[] pAnt = req.getParameterValues("artifactuploader.patternsAnt");
            String[] pregExExc = req.getParameterValues("artifactuploader.patternsRegExExc");
            String[] pAntExc = req.getParameterValues("artifactuploader.patternsAntExc");
            String pType = Values.textOrElse(req.getParameter("artifactuploader.patternType"), null);
            if (pType == null) {
                // Some versions of Jenkins rename the f:radioBlock's request parameter, but the formData JSON is named correctly.
                JSONObject radioBlock = formData.getJSONObject("patternType");
                if (radioBlock != null) {
                    pType = radioBlock.getString("value");
                }
            }
            if (pType == null) {
                // Fallback value.
                pType = "regEx";
            }

            boolean fTip = Values.booleanOrElse(req.getParameter("artifactuploader.forceCheckIn"), false);
            boolean fMerge = Values.booleanOrElse(req.getParameter("artifactuploader.forceTip"), false);

            String oPart = Values.textOrElse(req.getParameter("artifactuploader.owningPart"), null);
            boolean fAsSlave = Values.booleanOrElse(req.getParameter("artifactuploader.forceAsSlave"), false);

            return new ArtifactUploader(pregEx, fTip, fMerge, oPart, fAsSlave, pType, pAnt, pregExExc, pAntExc);
        }
    }
}
