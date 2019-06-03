package hudson.plugins.dimensionsscm;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.ModelObject;
import hudson.model.Node;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.ChangeLogParser;
import hudson.scm.PollingResult;
import hudson.scm.PollingResult.Change;
import hudson.scm.RepositoryBrowsers;
import hudson.scm.SCM;
import hudson.scm.SCMDescriptor;
import hudson.scm.SCMRevisionState;
import hudson.util.FormValidation;
import hudson.util.Scrambler;
import hudson.util.Secret;
import hudson.util.VariableResolver;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

/**
 * An SCM that can poll, browse and update from Dimensions CM.
 */
public class DimensionsSCM extends SCM implements Serializable {
    private final String project;
    private final String directory;
    private final String permissions;
    private final String eol;

    private final String jobUserName;
    private transient String jobPasswd;
    private Secret jobPasswdSecret;
    private final String jobServer;
    private final String jobDatabase;

    private final String[] folders;
    private final String[] pathsToExclude;

    private final String jobTimeZone;
    private final String jobWebUrl;

    private final boolean canJobUpdate;
    private final boolean canJobDelete;
    private final boolean canJobForce;
    private final boolean canJobRevert;
    private final boolean canJobExpand;
    private final boolean canJobNoMetadata;
    private final boolean canJobNoTouch;
    private final boolean forceAsSlave;

    private transient DimensionsAPI cachedAPI;
    private transient DimensionsSCMRepositoryBrowser browser;

    /**
     * Patch matcher that rejects nothing and includes everything.
     */
    private static class NullPathMatcher implements PathMatcher {
        @Override
        public boolean match(String matchText) {
            return true;
        }

        @Override
        public String toString() {
            return "NullPathMatcher()";
        }
    }

    private static DimensionsAPI newDimensionsAPIWithCheck() {
        try {
            return new DimensionsAPI();
        } catch (NoClassDefFoundError e) {
            // One of the most common customer issues is not installing the API JAR files, make reporting of this clearer.
            final Jenkins jenkins = Jenkins.getInstance();
            final String path = jenkins != null ? new File(jenkins.getRootDir(),
                    "plugins/dimensionsscm/WEB-INF/lib").getAbsolutePath() : "$JENKINS_HOME/plugins/dimensionsscm/WEB-INF/lib";
            throw (NoClassDefFoundError) new NoClassDefFoundError("\r\n\r\n#\r\n"
                    + "# Check the required JAR files (darius.jar, dmclient.jar, dmfile.jar, dmnet.jar) were copied to\r\n#\r\n"
                    + "#     '" + path + "'\r\n#\r\n"
                    + "# directory as described in the 'Installation' section of the Dimensions Plugin wiki page:\r\n#\r\n"
                    + "#     https://wiki.jenkins-ci.org/display/JENKINS/Dimensions+Plugin\r\n#\r\n").initCause(e);
        }
    }

    public DimensionsAPI getAPI() {
        DimensionsAPI api = this.cachedAPI;
        if (api == null) {
            api = newDimensionsAPIWithCheck();
            this.cachedAPI = api;
        }
        return api;
    }

    @Override
    public DimensionsSCMRepositoryBrowser getBrowser() {
        return this.browser;
    }

    /**
     * Gets the unexpanded project name for the connection.
     *
     * @return the project spec
     */
    public String getProject() {
        return this.project;
    }

    /**
     * Gets the expanded project name for the connection. Any variables in the project value will be expanded.
     *
     * @return the project spec without a trailing version number (if there is one).
     */
    public String getProjectName(Run<?, ?> run, TaskListener log) {
        String projectVersion = getProjectVersion(run, log);
        int sc = projectVersion.lastIndexOf(';');
        return sc >= 0 ? projectVersion.substring(0, sc) : projectVersion;
    }

    /**
     * Gets the expanded project name and version for the connection. Any variables in the project value will be
     * expanded.
     *
     * @return the project spec including its trailing version (if there is one).
     */
    public String getProjectVersion(Run<?, ?> run, TaskListener log) {
        EnvVars env = null;
        if (run != null) {
            try {
                env = run.getEnvironment(log);
            } catch (IOException e) {
                /* don't expand */
            } catch (InterruptedException e) {
                /* don't expand */
            }
        }
        String ret;
        if (env != null) {
            ret = env.expand(this.project);
        } else {
            ret = this.project;
        }
        return ret;
    }

    /**
     * Gets the project path.
     */
    public String getDirectory() {
        return this.directory;
    }

    /**
     * Gets the permissions string.
     */
    public String getPermissions() {
        return this.permissions;
    }

    /**
     * Gets the eol value.
     */
    public String getEol() {
        return this.eol;
    }

    /**
     * Gets the project paths to monitor.
     */
    public String[] getFolders() {
        return this.folders;
    }

    /**
     * Gets paths excluded from monitoring.
     */
    public String[] getPathsToExclude() {
        return pathsToExclude;
    }

    /**
     * Gets the user ID for the connection.
     */
    public String getJobUserName() {
        return this.jobUserName;
    }

    /**
     * Gets the password for the connection as a Secret instance.
     */
    public Secret getJobPasswd() {
        if (jobPasswdSecret == null && jobPasswd != null) {
            jobPasswdSecret = Secret.fromString(Scrambler.descramble(jobPasswd));
            jobPasswd = null;
        }
        return jobPasswdSecret;
    }

    /**
     * Gets the server name for the connection.
     */
    public String getJobServer() {
        return this.jobServer;
    }

    /**
     * Gets the database name for the connection.
     */
    public String getJobDatabase() {
        return this.jobDatabase;
    }

    /**
     * Gets the time zone for the connection.
     */
    public String getJobTimeZone() {
        return this.jobTimeZone;
    }

    /**
     * Gets the web URL for the connection.
     */
    public String getJobWebUrl() {
        return this.jobWebUrl;
    }

    /**
     * Gets the expand flag.
     */
    public boolean isCanJobExpand() {
        return this.canJobExpand;
    }

    /**
     * Gets the no metadata flag.
     */
    public boolean isCanJobNoMetadata() {
        return this.canJobNoMetadata;
    }

    /**
     * Gets the no touch flag.
     */
    public boolean isCanJobNoTouch() {
        return this.canJobNoTouch;
    }

    /**
     * Gets the update flag.
     */
    public boolean isCanJobUpdate() {
        return this.canJobUpdate;
    }

    /**
     * Gets the delete flag.
     */
    public boolean isCanJobDelete() {
        return this.canJobDelete;
    }

    /**
     * Gets the force flag.
     */
    public boolean isCanJobForce() {
        return this.canJobForce;
    }

    /**
     * Gets the revert flag.
     */
    public boolean isCanJobRevert() {
        return this.canJobRevert;
    }

    /**
     * Gets force as slave flag.
     */
    public boolean isForceAsSlave() {
        return this.forceAsSlave;
    }

    @Extension
    public static final DescriptorImpl DM_DESCRIPTOR = new DescriptorImpl();

    /**
     * Does this SCM plugin require a workspace for polling?
     * <p>
     * {@inheritDoc}
     */
    @Override
    public boolean requiresWorkspaceForPolling() {
        return false;
    }

    /**
     * Does this SCM plugin support polling?
     * <p>
     * {@inheritDoc}
     */
    @Override
    public boolean supportsPolling() {
        return true;
    }

    /**
     * Build up environment variables for build support.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void buildEnvVars(AbstractBuild<?, ?> build, Map<String, String> env) {
        // To be implemented when build support put in.
        super.buildEnvVars(build, env);
    }

    private static final String[] EMPTY_STRING_ARRAY = new String[] { };
    private static final String[] DEFAULT_FOLDERS = new String[] { "/" };

    @DataBoundConstructor
    public DimensionsSCM(String project, String[] folders, String[] pathsToExclude, String workarea,
            boolean canJobDelete, boolean canJobForce, boolean canJobRevert, String jobUserName, String jobPasswd,
            String jobServer, String jobDatabase, boolean canJobUpdate, String jobTimeZone, String jobWebUrl,
            String directory, String permissions, String eol, boolean canJobExpand, boolean canJobNoMetadata,
            boolean canJobNoTouch, boolean forceAsSlave) {
        // Check the folders specified have data specified.
        this.folders = folders != null ? Values.notEmptyOrElse(Values.trimCopy(folders), DEFAULT_FOLDERS)
                : (Values.hasText(directory) ? new String[] { directory } : DEFAULT_FOLDERS);
        this.pathsToExclude = pathsToExclude != null ? Values.notEmptyOrElse(Values.trimCopy(pathsToExclude),
                EMPTY_STRING_ARRAY) : EMPTY_STRING_ARRAY;

        // Copying arguments to fields.
        this.project = Values.textOrElse(project, "${JOB_NAME}");
        this.directory = Values.textOrElse(directory, null);
        this.permissions = Values.textOrElse(permissions, "DEFAULT");
        this.eol = Values.textOrElse(eol, "DEFAULT");

        this.jobServer = Values.textOrElse(jobServer, getDescriptor().getServer());
        this.jobUserName = Values.textOrElse(jobUserName, getDescriptor().getUserName());
        this.jobDatabase = Values.textOrElse(jobDatabase, getDescriptor().getDatabase());
        this.jobPasswd = null; // no longer used in config.xml serialization
        this.jobPasswdSecret = (jobPasswd != null && jobPasswd.length() != 0) ? Secret.fromString(jobPasswd) : getDescriptor().getPasswd();

        this.canJobUpdate = Values.hasText(jobServer) ? canJobUpdate : getDescriptor().isCanUpdate();

        this.canJobDelete = canJobDelete;
        this.canJobForce = canJobForce;
        this.canJobRevert = canJobRevert;
        this.canJobExpand = canJobExpand;
        this.canJobNoMetadata = canJobNoMetadata;
        this.canJobNoTouch = canJobNoTouch;
        this.forceAsSlave = forceAsSlave;

        this.jobTimeZone = Values.textOrElse(jobTimeZone, getDescriptor().getTimeZone());
        this.jobWebUrl = Values.textOrElse(jobWebUrl, getDescriptor().getWebUrl());

        Logger.debug("Starting job for project '" + this.project + "' ('" + this.folders.length + "')"
                + ", connecting to " + this.jobServer + "-" + this.jobUserName + ":" + this.jobDatabase);
    }

    /**
     * Checkout method for the plugin.
     * <p>
     * {@inheritDoc}
     */
    @Deprecated
    @Override
    public boolean checkout(final AbstractBuild<?, ?> build, final Launcher launcher, final FilePath workspace,
            final BuildListener listener, final File changelogFile) {
        if (!isCanJobUpdate()) {
            Logger.debug("Skipping checkout - " + this.getClass().getName());
        }

        Logger.debug("Invoking checkout - " + this.getClass().getName());

        boolean bRet;
        try {
            // Load other Dimensions plugins if set.
            DimensionsBuildWrapper.DescriptorImpl bwplugin = (DimensionsBuildWrapper.DescriptorImpl)
                    Jenkins.getInstance().getDescriptor(DimensionsBuildWrapper.class);
            DimensionsBuildNotifier.DescriptorImpl bnplugin = (DimensionsBuildNotifier.DescriptorImpl)
                    Jenkins.getInstance().getDescriptor(DimensionsBuildNotifier.class);

            String nodeName = build.getBuiltOn().getNodeName();

            if (DimensionsChecker.isValidPluginCombination(build, listener)) {
                Logger.debug("Plugins are ok");
            } else {
                listener.fatalError("\n[DIMENSIONS] The plugin combinations you have selected are not valid.");
                listener.fatalError("\n[DIMENSIONS] Please review online help to determine valid plugin uses.");
                return false;
            }

            if (isCanJobUpdate()) {
                DimensionsAPI dmSCM = getAPI();
                int version = 2009;
                long key = dmSCM.login(getJobUserName(), getJobPasswd(), getJobDatabase(), getJobServer(), build);

                if (key > 0L) {
                    // Get the server version.
                    Logger.debug("Login worked.");
                    version = dmSCM.getDmVersion();
                    if (version == 0) {
                        version = 2009;
                    }
                    dmSCM.logout(key, build);
                }

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
                    listener.getLogger().println("[DIMENSIONS] Running checkout on master...");
                    listener.getLogger().flush();
                    // Using Java API because this allows the plugin to work on platforms where Dimensions has not
                    // been ported, e.g. MAC OS, which is what I use.
                    CheckOutAPITask task = new CheckOutAPITask(build, this, workspace, listener, version);
                    bRet = workspace.act(task);
                } else {
                    // Running on slave... Have to use the command line as Java API will not work on remote hosts.
                    // Cannot serialise it...
                    // VariableResolver does not appear to be serialisable either, so...
                    VariableResolver<String> myResolver = build.getBuildVariableResolver();

                    String baseline = myResolver.resolve("DM_BASELINE");
                    String requests = myResolver.resolve("DM_REQUEST");

                    listener.getLogger().println("[DIMENSIONS] Running checkout on slave...");
                    listener.getLogger().flush();

                    CheckOutCmdTask task = new CheckOutCmdTask(getJobUserName(), getJobPasswd(), getJobDatabase(),
                            getJobServer(), getProjectVersion(build, listener), baseline, requests, isCanJobDelete(),
                            isCanJobRevert(), isCanJobForce(), isCanJobExpand(), isCanJobNoMetadata(),
                            isCanJobNoTouch(), (build.getPreviousBuild() == null), getFolders(), version,
                            permissions, eol, workspace, listener);
                    bRet = workspace.act(task);
                }
            } else {
                bRet = true;
            }

            generateChangeSet(build, listener, changelogFile);
        } catch (Exception e) {
            String message = Values.exceptionMessage("Unable to run checkout callout", e, "no message - try again");
            listener.fatalError(message);
            Logger.debug(message, e);
            bRet = false;
        }
        return bRet;
    }

    /**
     * Generate the changeset.
     */
    private void generateChangeSet(final AbstractBuild<?, ?> build, final TaskListener listener, final File changelogFile) {
        long key = -1L;
        DimensionsAPI dmSCM = newDimensionsAPIWithCheck();

        try {
            // When are we building files for?
            // Looking for the last successful build and then go forward from there - could use the last build as well.
            Calendar lastBuildCal = (build.getPreviousBuild() != null) ? build.getPreviousBuild().getTimestamp() : null;
            // Calendar lastBuildCal = (build.getPreviousNotFailedBuild() != null) ?
            //         build.getPreviousNotFailedBuild().getTimestamp() : null;
            Calendar nowDateCal = Calendar.getInstance();

            TimeZone tz = (getJobTimeZone() != null && getJobTimeZone().length() > 0)
                    ? TimeZone.getTimeZone(getJobTimeZone()) : TimeZone.getDefault();
            if (getJobTimeZone() != null && getJobTimeZone().length() > 0) {
                Logger.debug("Job timezone setting is " + getJobTimeZone());
            }
            Logger.debug("Log updates between " + (lastBuildCal != null
                    ? DateUtils.getStrDate(lastBuildCal, tz) : "0") + " -> " + DateUtils.getStrDate(nowDateCal, tz)
                    + " (" + tz.getID() + ")");

            dmSCM.setLogger(listener.getLogger());

            // Connect to Dimensions...
            key = dmSCM.login(getJobUserName(), getJobPasswd(), getJobDatabase(), getJobServer(), build);

            if (key > 0L) {
                Logger.debug("Login worked.");
                VariableResolver<String> myResolver = build.getBuildVariableResolver();

                String baseline = myResolver.resolve("DM_BASELINE");
                String requests = myResolver.resolve("DM_REQUEST");

                if (baseline != null) {
                    baseline = baseline.trim();
                    baseline = baseline.toUpperCase(Values.ROOT_LOCALE);
                }
                if (requests != null) {
                    requests = requests.replaceAll(" ", "");
                    requests = requests.toUpperCase(Values.ROOT_LOCALE);
                }

                Logger.debug("Extra parameters - " + baseline + " " + requests);
                String[] folders = getFolders();

                if (baseline != null && baseline.length() == 0) {
                    baseline = null;
                }
                if (requests != null && requests.length() == 0) {
                    requests = null;
                }

                // Iterate through the project folders and process them in Dimensions.
                for (String folderN : folders) {
                    File fileName = new File(folderN);
                    FilePath dname = new FilePath(fileName);

                    Logger.debug("Looking for changes in '" + folderN + "'...");

                    // Check out the folder.
                    dmSCM.createChangeSetLogs(key, getProjectName(build, listener), dname, lastBuildCal, nowDateCal,
                            changelogFile, tz, jobWebUrl, baseline, requests);
                    if (requests != null) {
                        break;
                    }
                }

                // Add the changelog file's closing tag.
                {
                    PrintWriter pw = null;
                    try {
                        pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(changelogFile, true), "UTF-8"));
                        pw.println("</changelog>");
                        pw.flush();
                    } catch (IOException e) {
                        throw new IOException(Values.exceptionMessage("Unable to write changelog file: " + changelogFile, e, "no message"), e);
                    } finally {
                        if (pw != null) {
                            pw.close();
                        }
                    }
                }
            }
        } catch (Exception e) {
            String message = Values.exceptionMessage("Unable to run changelog callout", e, "no message - try again");
            listener.fatalError(message);
            Logger.debug(message, e);
        } finally {
            dmSCM.logout(key, build);
        }
    }

    /**
     * Has the repository had any changes since last build?
     * <p>
     * {@inheritDoc}
     */
    @Deprecated
    @Override
    public SCMRevisionState calcRevisionsFromBuild(AbstractBuild<?, ?> build, Launcher launcher, TaskListener listener) {
        // Stub function for now
        return null;
    }

    /**
     * Has the repository had any changes?
     * <p>
     * {@inheritDoc}
     */
    @Deprecated
    @Override
    protected PollingResult compareRemoteRevisionWith(AbstractProject<?, ?> project, Launcher launcher,
            FilePath workspace, TaskListener listener, SCMRevisionState baseline) {
        // New polling function - to use old polling function for the moment.
        Change change = Change.NONE;

        try {
            if (pollCMChanges(project, launcher, workspace, listener)) {
                return PollingResult.BUILD_NOW;
            }
        } catch (Exception e) {
            /* swallow exception. */
        }
        return new PollingResult(change);
    }

    /**
     * Okay to clear the area?
     * <p>
     * {@inheritDoc}
     */
    @Deprecated
    @Override
    public boolean processWorkspaceBeforeDeletion(AbstractProject<?, ?> project, FilePath workspace, Node node) {
        // Not used at the moment, so we have a stub...
        return true;
    }

    /**
     * Has the repository had any changes?
     * <p>
     * {@inheritDoc}
     */
    private boolean pollCMChanges(final Job<?, ?> project, final Launcher launcher, final FilePath workspace,
            final TaskListener listener) {
        boolean bChanged = false;

        Logger.debug("Invoking pollChanges - " + this.getClass().getName());
        Logger.debug("Checking job - " + project.getName());
        long key = -1L;

        if (getProject() == null || getProject().length() == 0) {
            return false;
        }
        if (project.getLastBuild() == null) {
            Logger.debug("There is no lastBuild, so returning true");
            return true;
        }
        DimensionsAPI dmSCM = getAPI();
        try {
            Calendar lastBuildCal = project.getLastBuild().getTimestamp();

            Calendar nowDateCal = Calendar.getInstance();
            TimeZone tz = (getJobTimeZone() != null && getJobTimeZone().length() > 0)
                    ? TimeZone.getTimeZone(getJobTimeZone()) : TimeZone.getDefault();
            if (getJobTimeZone() != null && getJobTimeZone().length() > 0) {
                Logger.debug("Job timezone setting is " + getJobTimeZone());
            }
            Logger.debug("Checking for any updates between " + (lastBuildCal != null
                    ? DateUtils.getStrDate(lastBuildCal, tz) : "0") + " -> " + DateUtils.getStrDate(nowDateCal, tz)
                    + " (" + tz.getID() + ")");

            dmSCM.setLogger(listener.getLogger());

            // Connect to Dimensions...
            key = dmSCM.login(jobUserName, getJobPasswd(), jobDatabase, jobServer);
            if (key > 0L) {
                String[] folders = getFolders();
                // Iterate through the project folders and process them in Dimensions
                for (String folderN : folders) {
                    if (bChanged) {
                        break;
                    }
                    File fileName = new File(folderN);
                    FilePath dname = new FilePath(fileName);

                    if (dmSCM.getPathMatcher() == null) {
                        dmSCM.setPathMatcher(createPathMatcher());
                    }
                    bChanged = dmSCM.hasRepositoryBeenUpdated(key, getProjectName(project.getLastBuild(), listener), dname,
                            lastBuildCal, nowDateCal, tz);
                    if (Logger.isDebugEnabled()) {
                        Logger.debug("Polled folder '" + dname.getRemote() + "' between lastBuild="
                                + Values.toString(lastBuildCal) + " and now=" + Values.toString(nowDateCal)
                                + " where jobTimeZone=[" + getJobTimeZone() + "]. "
                                + (bChanged ? "Found changes" : "No changes"));
                    }
                }
                if (Logger.isDebugEnabled()) {
                    Logger.debug(bChanged ? "Found changes in at least one of the folders, so returning true"
                            : "No changes in any of the folders, so returning false");
                }
            }
        } catch (Exception e) {
            String message = Values.exceptionMessage("Unable to run pollChanges callout", e, "no message - try again");
            Logger.debug(message, e);
            listener.fatalError(message);
            bChanged = false;
        } finally {
            dmSCM.logout(key);
        }
        return bChanged;
    }

    /**
     * Creates path matcher to ignore changes on certain paths.
     *
     * @return path matcher
     */
    public PathMatcher createPathMatcher() {
        return Values.isNullOrEmpty(getPathsToExclude()) ? new NullPathMatcher()
                : new DefaultPathMatcher(getPathsToExclude(), null);
    }

    /**
     * Create a log parser object.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public ChangeLogParser createChangeLogParser() {
        Logger.debug("Invoking createChangeLogParser - " + this.getClass().getName());
        return new DimensionsChangeLogParser();
    }

    /**
     * Return an SCM descriptor.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public DescriptorImpl getDescriptor() {
        return DM_DESCRIPTOR;
    }

    /**
     * Implementation class for Dimensions CM SCM plugin.
     */
    public static class DescriptorImpl extends SCMDescriptor<DimensionsSCM> implements ModelObject {
        private String server;
        private String userName;
        private transient String passwd;
        private Secret passwdSecret;
        private String database;

        private String timeZone;
        private String webUrl;

        private boolean canUpdate;

        /**
         * Loads the SCM descriptor.
         */
        public DescriptorImpl() {
            super(DimensionsSCM.class, DimensionsSCMRepositoryBrowser.class);
            load();
            Logger.debug("Loading " + this.getClass().getName());
        }

        @Override
        public String getDisplayName() {
            return "Dimensions";
        }

        /**
         * Save the SCM descriptor configuration.
         * <p>
         * {@inheritDoc}
         */
        @Override
        public boolean configure(StaplerRequest req, JSONObject jobj) throws FormException {
            // Get the values and check them.
            userName = req.getParameter("dimensionsscm.userName");
            passwd = null;
            passwdSecret = Secret.fromString(req.getParameter("dimensionsscm.passwd"));
            server = req.getParameter("dimensionsscm.server");
            database = req.getParameter("dimensionsscm.database");

            timeZone = req.getParameter("dimensionsscm.timeZone");
            webUrl = req.getParameter("dimensionsscm.webUrl");

            if (userName != null) {
                userName = Util.fixNull(req.getParameter("dimensionsscm.userName").trim());
            }
            if (server != null) {
                server = Util.fixNull(req.getParameter("dimensionsscm.server").trim());
            }
            if (database != null) {
                database = Util.fixNull(req.getParameter("dimensionsscm.database").trim());
            }
            if (timeZone != null) {
                timeZone = Util.fixNull(req.getParameter("dimensionsscm.timeZone").trim());
            }
            if (webUrl != null) {
                webUrl = Util.fixNull(req.getParameter("dimensionsscm.webUrl").trim());
            }
            req.bindJSON(DM_DESCRIPTOR, jobj);

            this.save();
            return super.configure(req, jobj);
        }

        @Override
        public SCM newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            // Get variables and then construct a new object.
            String[] folders = req.getParameterValues("dimensionsscm.folders");
            String[] pathsToExclude = req.getParameterValues("dimensionsscm.pathsToExclude");

            String project = req.getParameter("dimensionsscm.project");
            String directory = req.getParameter("dimensionsscm.directory");
            String permissions = req.getParameter("dimensionsscm.permissions");
            String eol = req.getParameter("dimensionsscm.eol");

            boolean canJobDelete = "on".equalsIgnoreCase(req.getParameter("dimensionsscm.canJobDelete"));
            boolean canJobForce = "on".equalsIgnoreCase(req.getParameter("dimensionsscm.canJobForce"));
            boolean canJobRevert = "on".equalsIgnoreCase(req.getParameter("dimensionsscm.canJobRevert"));
            boolean canJobUpdate = "on".equalsIgnoreCase(req.getParameter("dimensionsscm.canJobUpdate"));
            boolean canJobExpand = "on".equalsIgnoreCase(req.getParameter("dimensionsscm.canJobExpand"));
            boolean canJobNoMetadata = "on".equalsIgnoreCase(req.getParameter("dimensionsscm.canJobNoMetadata"));
            boolean canJobNoTouch = "on".equalsIgnoreCase(req.getParameter("dimensionsscm.canJobNoTouch"));
            boolean forceAsSlave = "on".equalsIgnoreCase(req.getParameter("dimensionsscm.forceAsSlave"));

            String jobUserName = req.getParameter("dimensionsscm.jobUserName");
            String jobPasswd = req.getParameter("dimensionsscm.jobPasswd");
            String jobServer = req.getParameter("dimensionsscm.jobServer");
            String jobDatabase = req.getParameter("dimensionsscm.jobDatabase");
            String jobTimeZone = req.getParameter("dimensionsscm.jobTimeZone");
            String jobWebUrl = req.getParameter("dimensionsscm.jobWebUrl");

            DimensionsSCM scm = new DimensionsSCM(project, folders, pathsToExclude, null, canJobDelete, canJobForce,
                    canJobRevert, jobUserName, jobPasswd, jobServer, jobDatabase, canJobUpdate, jobTimeZone, jobWebUrl,
                    directory, permissions, eol, canJobExpand, canJobNoMetadata, canJobNoTouch, forceAsSlave);

            scm.browser = RepositoryBrowsers.createInstance(DimensionsSCMRepositoryBrowser.class, req, formData,
                    "browser");
            scm.getAPI();
            return scm;
        }

        /**
         * Gets the timezone for the connection.
         *
         * @return the timezone
         */
        public String getTimeZone() {
            return this.timeZone;
        }

        /**
         * Gets the web URL for the connection.
         *
         * @return the web URL
         */
        public String getWebUrl() {
            return this.webUrl;
        }

        /**
         * Gets the user ID for the connection.
         *
         * @return the user ID of the user as whom to connect
         */
        public String getUserName() {
            return this.userName;
        }

        /**
         * Gets the base database for the connection (as "NAME@CONNECTION").
         *
         * @return the name of the base database to connect to
         */
        public String getDatabase() {
            return this.database;
        }

        /**
         * Gets the server for the connection.
         *
         * @return the name of the server to connect to
         */
        public String getServer() {
            return this.server;
        }

        /**
         * Gets the password as a Secret instance.
         *
         * @return the password (as a Secret instance)
         */
        public Secret getPasswd() {
            if (passwdSecret == null && passwd != null) {
                passwdSecret = Secret.fromString(Scrambler.descramble(passwd));
                passwd = null;
            }
            return passwdSecret;
        }

        /**
         * Gets the update.
         *
         * @return the update
         */
        public boolean isCanUpdate() {
            return this.canUpdate;
        }

        /**
         * Sets the update.
         */
        public void setCanUpdate(boolean x) {
            this.canUpdate = x;
        }

        /**
         * Sets the user ID for the connection.
         */
        public void setUserName(String userName) {
            this.userName = userName;
        }

        /**
         * Sets the base database for the connection (as "NAME@CONNECTION").
         */
        public void setDatabase(String database) {
            this.database = database;
        }

        /**
         * Sets the server for the connection.
         */
        public void setServer(String server) {
            this.server = server;
        }

        /**
         * Sets the password.
         */
        public void setPasswd(String password) {
            this.passwdSecret = Secret.fromString(password);
            this.passwd = null;
        }

        /**
         * Sets the timezone for the connection.
         */
        public void setTimeZone(String x) {
            this.timeZone = x;
        }

        /**
         * Sets the web URL for the connection.
         */
        public void setWebUrl(String x) {
            this.webUrl = x;
        }

        public FormValidation domanadatoryFieldCheck(StaplerRequest req, StaplerResponse rsp) {
            String value = Util.fixEmpty(req.getParameter("value"));
            String errorTxt = "This value is manadatory.";
            if (value == null) {
                return FormValidation.error(errorTxt);
            } else {
                // Some processing.
                return FormValidation.ok();
            }
        }

        /**
         * Check if the specified Dimensions server is valid.
         */
        @RequirePOST
        public FormValidation docheckTz(StaplerRequest req, StaplerResponse rsp,
                @QueryParameter("dimensionsscm.timeZone") final String timezone,
                @QueryParameter("dimensionsscm.jobTimeZone") final String jobtimezone) {
            try {
                String xtz = (jobtimezone != null) ? jobtimezone : timezone;
                Logger.debug("Invoking docheckTz - " + xtz);
                TimeZone ctz = TimeZone.getTimeZone(xtz);
                String lmt = ctz.getID();
                if (lmt.equalsIgnoreCase("GMT") && !(xtz.equalsIgnoreCase("GMT")
                        || xtz.equalsIgnoreCase("Greenwich Mean Time") || xtz.equalsIgnoreCase("UTC")
                        || xtz.equalsIgnoreCase("Coordinated Universal Time"))) {
                    return FormValidation.error("Timezone specified is not valid.");
                } else {
                    return FormValidation.ok("Timezone test succeeded!");
                }
            } catch (Exception e) {
                String message = Values.exceptionMessage("Timezone check error", e, "no message");
                Logger.debug(message, e);
                return FormValidation.error(message);
            }
        }

        /**
         * Check if the specified Dimensions server is valid.
         */
        @RequirePOST
        public FormValidation docheckServer(StaplerRequest req, StaplerResponse rsp,
                @QueryParameter("dimensionsscm.userName") final String user,
                @QueryParameter("dimensionsscm.passwd") final String passwd,
                @QueryParameter("dimensionsscm.server") final String server,
                @QueryParameter("dimensionsscm.database") final String database,
                @QueryParameter("dimensionsscm.jobUserName") final String jobuser,
                @QueryParameter("dimensionsscm.jobPasswd") final String jobPasswd,
                @QueryParameter("dimensionsscm.jobServer") final String jobServer,
                @QueryParameter("dimensionsscm.jobDatabase") final String jobDatabase,
                @AncestorInPath final Item item) {
            if (item == null) {
                Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
            } else {
                item.checkPermission(Item.CONFIGURE);
            }
            DimensionsAPI connectionCheck = newDimensionsAPIWithCheck();
            try {
                String xserver = (jobServer != null) ? jobServer : server;
                String xuser = (jobuser != null) ? jobuser : user;
                String xpasswd = (jobPasswd != null) ? jobPasswd : passwd;
                String xdatabase = (jobDatabase != null) ? jobDatabase : database;
                Logger.debug("Server connection check to user [" + xuser
                        + "], database [" + xdatabase + "], server [" + xserver + "]");
                long key = connectionCheck.login(xuser, Secret.fromString(xpasswd), xdatabase, xserver);
                Logger.debug("Server connection check returned key [" + key + "]");
                if (key < 1L) {
                    return FormValidation.error("Connection test failed");
                } else {
                    connectionCheck.logout(key);
                    return FormValidation.ok("Connection test succeeded!");
                }
            } catch (Exception e) {
                String message = Values.exceptionMessage("Server connection check error", e, "no message");
                Logger.debug(message, e);
                return FormValidation.error(message);
            }
        }
    }
}
