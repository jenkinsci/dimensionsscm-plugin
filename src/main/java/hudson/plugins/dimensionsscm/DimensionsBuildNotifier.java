package hudson.plugins.dimensionsscm;

import com.serena.dmclient.api.DimensionsResult;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.VariableResolver;
import java.io.IOException;
import java.io.Serializable;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

/**
 * A Notifier that can create, deploy, build or action a baseline (from the SCM project) as a post-build step in a Jenkins build.
 */
public class DimensionsBuildNotifier extends Notifier implements Serializable {
    private DimensionsSCM scm;
    private boolean canBaselineDeploy;
    private boolean canBaselineAction;

    private final String actionState;
    private final String deployState;

    private final String blnScope;
    private final String blnName;
    private final String blnTemplate;
    private final String blnOwningPart;
    private final String blnType;

    private boolean canBaselineBuild;

    private final String area;
    private final String buildConfig;
    private final String buildOptions;
    private final String buildTargets;

    private final boolean batch;
    private final boolean buildClean;
    private final boolean capture;

    /**
     * Gets the baseline part spec.
     */
    public String getBlnOwningPart() {
        return this.blnOwningPart;
    }

    /**
     * Gets the baseline type name.
     */
    public String getBlnType() {
        return this.blnType;
    }

    /**
     * Gets the baseline template name.
     */
    public String getBlnTemplate() {
        return this.blnTemplate;
    }

    /**
     * Gets the baseline name.
     */
    public String getBlnName() {
        return this.blnName;
    }

    /**
     * Gets the baseline scope.
     */
    public String getBlnScope() {
        return this.blnScope;
    }

    /**
     * Gets the build flag.
     */
    public boolean isCanBaselineBuild() {
        return this.canBaselineBuild;
    }

    /**
     * Gets the action flag.
     */
    public boolean isCanBaselineAction() {
        return this.canBaselineAction;
    }

    /**
     * Gets the deploy flag.
     */
    public boolean isCanBaselineDeploy() {
        return this.canBaselineDeploy;
    }

    /**
     * Gets the batch flag.
     */
    public boolean isBatch() {
        return this.batch;
    }

    /**
     * Gets the buildClean flag.
     */
    public boolean isBuildClean() {
        return this.buildClean;
    }

    /**
     * Gets the capture flag.
     */
    public boolean isCapture() {
        return this.capture;
    }

    /**
     * Gets the action state name.
     */
    public String getActionState() {
        return this.actionState;
    }

    /**
     * Gets the deploy state name.
     */
    public String getDeployState() {
        return this.deployState;
    }

    /**
     * Gets the area name.
     */
    public String getArea() {
        return this.area;
    }

    /**
     * Gets the build config name.
     */
    public String getBuildConfig() {
        return this.buildConfig;
    }

    /**
     * Gets the build options string.
     */
    public String getBuildOptions() {
        return this.buildOptions;
    }

    /**
     * Gets the build target name.
     */
    public String getBuildTargets() {
        return this.buildTargets;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    public DimensionsBuildNotifier(boolean canDeploy, String deployState, boolean canAction, String actionState,
            boolean canBuild, String area, String buildConfig, String buildOptions, String buildTargets,
            String blnScope, String blnTemplate, String blnOwningPart, String blnType, String blnName, boolean batch,
            boolean buildClean, boolean capture) {
        this.canBaselineDeploy = canDeploy;
        this.canBaselineAction = canAction;
        this.canBaselineBuild = canBuild;
        this.actionState = actionState;
        this.deployState = deployState;

        this.area = area;
        this.buildConfig = buildConfig;
        this.buildOptions = buildOptions;
        this.buildTargets = buildTargets;
        this.blnScope = blnScope;
        this.blnTemplate = blnTemplate;
        this.blnOwningPart = blnOwningPart;
        this.blnType = blnType;
        this.blnName = blnName;

        this.batch = batch;
        this.buildClean = buildClean;
        this.capture = capture;
    }

    /**
     * Changes the build result if baseline operation fails. (So cannot override {@linkplain #needsToRunAfterFinalized()}).
     */
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        Logger.debug("Invoking perform callout " + this.getClass().getName());
        long key = -1L;
        try {
            if (!(build.getProject().getScm() instanceof DimensionsSCM)) {
                final String message = "[DIMENSIONS] This plugin only works with the Dimensions SCM engine.";
                listener.fatalError(message);
                build.setResult(Result.FAILURE);
                throw new IOException(message);
            }
            // Create baseline if SUCCESS or UNSTABLE (could be just some Checkstyle violations).
            Result result = build.getResult();
            if (result != null && result.isBetterThan(Result.FAILURE)) {
                if (scm == null) {
                    scm = (DimensionsSCM) build.getProject().getScm();
                }
                Logger.debug("Dimensions user is " + scm.getJobUserName() + " , Dimensions installation is " + scm.getJobServer());
                key = scm.getAPI().login(scm.getJobUserName(), scm.getJobPasswd(), scm.getJobDatabase(), scm.getJobServer(), build);
                if (key > 0L) {
                    VariableResolver<String> myResolver = build.getBuildVariableResolver();
                    String requests = myResolver.resolve("DM_TARGET_REQUEST");
                    String blnId = myResolver.resolve("DM_BASELINE");
                    StringBuffer cblId = new StringBuffer();

                    if (requests != null) {
                        requests = requests.replaceAll(" ", "");
                        requests = requests.toUpperCase(Values.ROOT_LOCALE);
                    }

                    if (blnScope != null && blnScope.length() > 0 && blnScope.equals("REVISED")) {
                        if (requests == null || blnId == null || requests.length() == 0 || blnId.length() == 0) {
                            listener.fatalError("[DIMENSIONS] A revised baseline is only valid if you have specified DM_TARGET_REQUEST and DM_BASELINE.");
                            build.setResult(Result.FAILURE);
                            return false;
                        }
                    }

                    {
                        DimensionsResult res = scm.getAPI().createBaseline(key, scm.getProjectVersion(build, listener), build,
                                blnScope, blnTemplate, blnOwningPart, blnType, requests, blnId, blnName, cblId);
                        if (res == null) {
                            listener.getLogger().println("[DIMENSIONS] The build failed to be tagged in Dimensions");
                            listener.getLogger().flush();
                            build.setResult(Result.FAILURE);
                            canBaselineDeploy = canBaselineAction = canBaselineBuild = false;
                        } else {
                            listener.getLogger().println("[DIMENSIONS] Build was successfully tagged in Dimensions as a baseline");
                            listener.getLogger().println("[DIMENSIONS] (" + res.getMessage().replaceAll("\n", "\n[DIMENSIONS] ") + ")");
                            listener.getLogger().flush();
                        }
                    }
                    if (canBaselineDeploy) {
                        listener.getLogger().println("[DIMENSIONS] Submitting a deployment job to Dimensions...");
                        listener.getLogger().flush();
                        DimensionsResult res = scm.getAPI().deployBaseline(key, scm.getProjectName(build, listener), build, deployState, cblId.toString());
                        if (res == null) {
                            listener.getLogger().println("[DIMENSIONS] The build baseline failed to be deployed in Dimensions");
                            listener.getLogger().flush();
                            build.setResult(Result.FAILURE);
                            canBaselineDeploy = canBaselineAction = canBaselineBuild = false;
                        } else {
                            listener.getLogger().println("[DIMENSIONS] Build baseline was successfully deployed in Dimensions");
                            listener.getLogger().println("[DIMENSIONS] (" + res.getMessage().replaceAll("\n", "\n[DIMENSIONS] ") + ")");
                            listener.getLogger().flush();
                        }
                    }

                    // This will active the build baseline functionality
                    if (canBaselineBuild) {
                        listener.getLogger().println("[DIMENSIONS] Submitting a build job to Dimensions...");
                        listener.getLogger().flush();
                        DimensionsResult res = scm.getAPI().buildBaseline(key, area, scm.getProjectName(build, listener), batch,
                                buildClean, buildConfig, buildOptions, capture, requests, buildTargets, build, cblId.toString());
                        if (res == null) {
                            listener.getLogger().println("[DIMENSIONS] The build baseline failed to be built in Dimensions");
                            listener.getLogger().flush();
                            build.setResult(Result.FAILURE);
                            canBaselineDeploy = canBaselineAction = canBaselineBuild = false;
                        } else {
                            listener.getLogger().println("[DIMENSIONS] Build baseline was successfully built in Dimensions");
                            listener.getLogger().println("[DIMENSIONS] (" + res.getMessage().replaceAll("\n", "\n[DIMENSIONS] ") + ")");
                            listener.getLogger().flush();
                        }
                    }

                    if (canBaselineAction) {
                        listener.getLogger().println("[DIMENSIONS] Actioning the build baseline in Dimensions...");
                        listener.getLogger().flush();
                        DimensionsResult res = scm.getAPI().actionBaseline(key, scm.getProjectName(build, listener), build, actionState, cblId.toString());
                        if (res == null) {
                            listener.getLogger().println("[DIMENSIONS] The build baseline failed to be actioned in Dimensions");
                            build.setResult(Result.FAILURE);
                            listener.getLogger().flush();
                        } else {
                            listener.getLogger().println("[DIMENSIONS] Build baseline was successfully actioned in Dimensions");
                            listener.getLogger().println("[DIMENSIONS] (" + res.getMessage().replaceAll("\n", "\n[DIMENSIONS] ") + ")");
                            listener.getLogger().flush();
                        }
                    }
                } else {
                    listener.fatalError("[DIMENSIONS] Login to Dimensions failed.");
                    build.setResult(Result.FAILURE);
                    return false;
                }
            }
        } catch (Exception e) {
            String message = Values.exceptionMessage("Unable to tag build in Dimensions", e, "no message");
            listener.fatalError(message);
            Logger.debug(message, e);
            build.setResult(Result.FAILURE);
            return false;
        } finally {
            if (scm != null) {
                scm.getAPI().logout(key, build);
            }
        }
        return true;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        // see Descriptor javadoc for more about what a descriptor is.
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * The DimensionsBuildNotifier Descriptor class.
     */
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        /**
         * Loads the descriptor
         */
        public DescriptorImpl() {
            super(DimensionsBuildNotifier.class);
            load();
            Logger.debug("Loading " + this.getClass().getName());
        }

        @Override
        public String getDisplayName() {
            return "Tag successful builds in Dimensions as a baseline";
        }

        @Override
        public Notifier newInstance(StaplerRequest req, JSONObject formData) {
            // Get variables and then construct a new object.
            boolean canDeploy = Values.booleanOrElse(req.getParameter("dimensionsbuildnotifier.canBaselineDeploy"), false);
            boolean canBuild = Values.booleanOrElse(req.getParameter("dimensionsbuildnotifier.canBaselineBuild"), false);
            boolean canAction = Values.booleanOrElse(req.getParameter("dimensionsbuildnotifier.canBaselineAction"), false);
            boolean batch = Values.booleanOrElse(req.getParameter("dimensionsbuildnotifier.batch"), false);
            boolean buildClean = Values.booleanOrElse(req.getParameter("dimensionsbuildnotifier.buildClean"), false);
            boolean capture = Values.booleanOrElse(req.getParameter("dimensionsbuildnotifier.capture"), false);
            String deploy = Values.textOrElse(req.getParameter("dimensionsbuildnotifier.deployState"), null);
            String action = Values.textOrElse(req.getParameter("dimensionsbuildnotifier.actionState"), null);
            String area = Values.textOrElse(req.getParameter("dimensionsbuildnotifier.area"), null);
            String buildConfig = Values.textOrElse(req.getParameter("dimensionsbuildnotifier.buildConfig"), null);
            String buildOptions = Values.textOrElse(req.getParameter("dimensionsbuildnotifier.buildOptions"), null);
            String buildTargets = Values.textOrElse(req.getParameter("dimensionsbuildnotifier.buildTargets"), null);
            String blnScope = Values.textOrElse(req.getParameter("dimensionsbuildnotifier.blnScope"), null);
            String blnTemplate = Values.textOrElse(req.getParameter("dimensionsbuildnotifier.blnTemplate"), null);
            String blnOwningPart = Values.textOrElse(req.getParameter("dimensionsbuildnotifier.blnOwningPart"), null);
            String blnType = Values.textOrElse(req.getParameter("dimensionsbuildnotifier.blnType"), null);
            String blnName = Values.textOrElse(req.getParameter("dimensionsbuildnotifier.blnName"), null);
            return new DimensionsBuildNotifier(canDeploy, deploy, canAction, action, canBuild,
                    area, buildConfig, buildOptions, buildTargets, blnScope, blnTemplate, blnOwningPart, blnType,
                    blnName, batch, buildClean, capture);
        }

        /**
         * This builder can be used with all project types.
         * <p>
         * {@inheritDoc}
         */
        @Override
        public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType) {
            return true;
        }

        /**
         * Save the descriptor configuration.
         * <p>
         * {@inheritDoc}
         */
        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // Get the values and check them.
            String deploy = Values.textOrElse(req.getParameter("dimensionsbuildnotifier.deployState"), null);
            String action = Values.textOrElse(req.getParameter("dimensionsbuildnotifier.actionState"), null);
            this.save();
            return super.configure(req, formData);
        }

        /**
         * Get help file.
         * <p>
         * {@inheritDoc}
         */
        @Override
        public String getHelpFile() {
            return "/plugin/dimensionsscm/helpbnotifier.html";
        }
    }
}
