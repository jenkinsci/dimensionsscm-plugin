package hudson.plugins.dimensionsscm;

import com.serena.dmclient.api.DimensionsResult;
import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.tasks.Builder;
import hudson.util.VariableResolver;
import java.io.IOException;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

/**
 * A Builder to launch a Serena Build command on the SCM project as a step in a Jenkins build.
 */
public class DimensionsBuilder extends Builder {
    private DimensionsSCM scm;

    private final String projectArea;
    private final String projectConfig;
    private final String projectOptions;
    private final String projectTargets;
    private final String projectType;
    private final String projectStage;

    private final boolean batch;
    private final boolean buildClean;
    private final boolean capture;

    private final boolean audit;
    private final boolean populate;
    private final boolean touch;

    /**
     * Gets the audit flag.
     */
    public boolean isProjectAudit() {
        return this.audit;
    }

    /**
     * Gets the populate flag.
     */
    public boolean isProjectPopulate() {
        return this.populate;
    }

    /**
     * Gets the touch flag.
     */
    public boolean isProjectTouch() {
        return this.touch;
    }

    /**
     * Gets the batch flag.
     */
    public boolean isProjectBatch() {
        return this.batch;
    }

    /**
     * Gets the buildClean flag.
     */
    public boolean isProjectClean() {
        return this.buildClean;
    }

    /**
     * Gets the capture flag.
     */
    public boolean isProjectCapture() {
        return this.capture;
    }

    /**
     * Gets the project type name.
     */
    public String getProjectType() {
        return this.projectType;
    }

    /**
     * Gets the project stage name.
     */
    public String getProjectStage() {
        return this.projectStage;
    }

    /**
     * Gets the area name.
     */
    public String getProjectArea() {
        return this.projectArea;
    }

    /**
     * Gets the build config name.
     */
    public String getProjectConfig() {
        return this.projectConfig;
    }

    /**
     * Gets the build options string.
     */
    public String getProjectOptions() {
        return this.projectOptions;
    }

    /**
     * Gets the build target names.
     */
    public String getProjectTargets() {
        return this.projectTargets;
    }

    public DimensionsBuilder(String area, String buildConfig, String buildOptions, String buildTargets,
            String buildType, String buildStage, boolean batch, boolean buildClean, boolean capture, boolean audit,
            boolean populate, boolean touch) {
        this.projectArea = area;
        this.projectConfig = buildConfig;
        this.projectOptions = buildOptions;
        this.projectTargets = buildTargets;
        this.projectType = buildType;
        this.projectStage = buildStage;
        this.batch = batch;
        this.buildClean = buildClean;
        this.capture = capture;
        this.audit = audit;
        this.populate = populate;
        this.touch = touch;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        Logger.debug("Invoking perform callout " + this.getClass().getName());
        long key = -1L;
        try {
            if (!(build.getParent().getScm() instanceof DimensionsSCM)) {
                listener.fatalError("[DIMENSIONS] This plugin only works with the Dimensions SCM engine.");
                build.setResult(Result.FAILURE);
                throw new IOException("[DIMENSIONS] This plugin only works with a Dimensions SCM engine");
            }

            if (build.isBuilding()) {
                if (scm == null) {
                    scm = (DimensionsSCM) build.getParent().getScm();
                }
                Logger.debug("Dimensions user is " + scm.getJobUserName() + " , Dimensions installation is "
                        + scm.getJobServer());
                Logger.debug("Running a project build step...");
                key = scm.getAPI().login(scm, build);
                if (key > 0L) {
                    VariableResolver<String> myResolver = build.getBuildVariableResolver();
                    String requests = myResolver.resolve("DM_TARGET_REQUEST");
                    if (requests != null) {
                        requests = requests.replaceAll(" ", "");
                        requests = requests.toUpperCase(Values.ROOT_LOCALE);
                    }

                    {
                        String projectXType = projectType;
                        if (projectXType.equals("NONE")) {
                            projectXType = null;
                        }
                        // This will activate the build baseline functionality.
                        listener.getLogger().println("[DIMENSIONS] Submitting a build job to Dimensions...");
                        listener.getLogger().flush();
                        DimensionsResult res = scm.getAPI().buildProject(key, projectArea, scm.getProjectName(build, listener),
                                batch, buildClean, projectConfig, projectOptions, capture, requests, projectTargets,
                                projectStage, projectXType, audit, populate, touch, build);
                        if (res == null) {
                            listener.getLogger().println("[DIMENSIONS] The project failed to be built in Dimensions");
                            listener.getLogger().flush();
                            build.setResult(Result.FAILURE);
                        } else {
                            listener.getLogger().println("[DIMENSIONS] Build step was successfully run in Dimensions");
                            listener.getLogger().println("[DIMENSIONS] (" + res.getMessage().replaceAll("\n",
                                    "\n[DIMENSIONS] ") + ")");
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
    public Descriptor<Builder> getDescriptor() {
        // See Descriptor javadoc for more about what a descriptor is.
        return DMBLD_DESCRIPTOR;
    }

    /**
     * Descriptor should be singleton.
     */
    @Extension
    public static final DescriptorImpl DMBLD_DESCRIPTOR = new DescriptorImpl();

    /**
     * Descriptor for {@link DimensionsBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     */
    public static final class DescriptorImpl extends Descriptor<Builder> {
        /**
         * To persist global configuration information, simply store it in a field and call save().
         * <p>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */
        public DescriptorImpl() {
            super(DimensionsBuilder.class);
            Logger.debug("Loading " + this.getClass().getName());
        }

        @Override
        public Builder newInstance(StaplerRequest req, JSONObject formData) {
            // Get variables and then construct a new object.
            boolean batch = "on".equalsIgnoreCase(req.getParameter("dimensionsbuilder.projectBatch"));
            boolean buildClean = "on".equalsIgnoreCase(req.getParameter("dimensionsbuilder.projectClean"));
            boolean capture = "on".equalsIgnoreCase(req.getParameter("dimensionsbuilder.projectCapture"));

            boolean audit = "on".equalsIgnoreCase(req.getParameter("dimensionsbuilder.projectAudit"));
            boolean populate = "on".equalsIgnoreCase(req.getParameter("dimensionsbuilder.projectPopulate"));
            boolean touch = "on".equalsIgnoreCase(req.getParameter("dimensionsbuilder.projectTouch"));

            String area = req.getParameter("dimensionsbuilder.projectArea");
            String buildConfig = req.getParameter("dimensionsbuilder.projectConfig");
            String buildOptions = req.getParameter("dimensionsbuilder.projectOptions");
            String buildTargets = req.getParameter("dimensionsbuilder.projectTargets");
            String buildType = req.getParameter("dimensionsbuilder.projectType");
            String buildStage = req.getParameter("dimensionsbuilder.projectStage");

            if (area != null) {
                area = Util.fixNull(req.getParameter("dimensionsbuilder.projectArea").trim());
            }
            if (buildConfig != null) {
                buildConfig = Util.fixNull(req.getParameter("dimensionsbuilder.projectConfig").trim());
            }
            if (buildOptions != null) {
                buildOptions = Util.fixNull(req.getParameter("dimensionsbuilder.projectOptions").trim());
            }
            if (buildTargets != null) {
                buildTargets = Util.fixNull(req.getParameter("dimensionsbuilder.projectTargets").trim());
            }
            if (buildType != null) {
                buildType = Util.fixNull(req.getParameter("dimensionsbuilder.projectType").trim());
            }
            if (buildStage != null) {
                buildStage = Util.fixNull(req.getParameter("dimensionsbuilder.projectStage").trim());
            }

            return new DimensionsBuilder(area, buildConfig, buildOptions, buildTargets, buildType,
                    buildStage, batch, buildClean, capture, audit, populate, touch);
        }

        /**
         * This human readable name is used in the configuration screen.
         * <p>
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return "Execute Dimensions Build";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject o) throws FormException {
            // To persist global configuration information, set that to properties and call save().
            save();
            return super.configure(req, o);
        }
    }
}
