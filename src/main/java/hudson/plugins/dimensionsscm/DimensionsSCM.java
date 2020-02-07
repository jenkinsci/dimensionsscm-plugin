package hudson.plugins.dimensionsscm;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.ModelObject;
import hudson.model.Node;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.dimensionsscm.model.StringVarStorage;
import hudson.scm.ChangeLogParser;
import hudson.scm.PollingResult;
import hudson.scm.PollingResult.Change;
import hudson.scm.RepositoryBrowser;
import hudson.scm.RepositoryBrowsers;
import hudson.scm.SCM;
import hudson.scm.SCMDescriptor;
import hudson.scm.SCMRevisionState;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Scrambler;
import hudson.util.Secret;
import hudson.util.VariableResolver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.kohsuke.stapler.*;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * An SCM that can poll, browse and update from Dimensions CM.
 */
public class DimensionsSCM extends SCM implements Serializable {

    @Extension
    public static final DescriptorImpl DM_DESCRIPTOR = new DescriptorImpl();

    private static final List<StringVarStorage> EMPTY_STRING_LIST = new ArrayList<StringVarStorage>();
    private static final List<StringVarStorage> DEFAULT_FOLDERS = Collections.singletonList(new StringVarStorage("/"));

    private transient String jobPasswd;
    private transient DimensionsAPI cachedAPI;
    private transient DimensionsSCMRepositoryBrowser browser;

    private String credentialsId;
    private String jobUserName;
    private String jobServer;
    private String jobDatabase;
    private String project;

    private Secret jobPasswdSecret;
    private Secret certificatePassword;
    private Secret remoteCertificatePassword;
    private Secret keystorePassword;
    private String permissions;
    private String eol;
    private String jobTimeZone;
    private String jobWebUrl;
    private String credentialsType;
    private String keystorePath;
    private String certificateAlias;
    private String certificatePath;
    private String[] folders;
    private String[] pathsToExclude;
    private List<StringVarStorage> foldersList;
    private List<StringVarStorage> pathsToExcludeList;
    private boolean canJobUpdate;
    private boolean canJobDelete;
    private boolean canJobForce;
    private boolean canJobRevert;
    private boolean canJobExpand;
    private boolean canJobNoMetadata;
    private boolean canJobNoTouch;
    private boolean secureAgentAuth;


    @DataBoundConstructor
    public DimensionsSCM(String project, String credentialsType, String userName, String password,
                         String pluginServer, String userServer, String keystoreServer, String pluginDatabase, String userDatabase, String keystoreDatabase, String keystorePath, String certificateAlias,
                         String credentialsId, String certificatePassword, String keystorePassword,
                         String certificatePath, String remoteCertificatePassword, boolean secureAgentAuth) {

        this.credentialsId = StringUtils.EMPTY;
        this.jobUserName = StringUtils.EMPTY;
        this.jobPasswdSecret = null;
        this.keystorePath = StringUtils.EMPTY;
        this.certificateAlias = StringUtils.EMPTY;
        this.certificatePassword = null;
        this.keystorePassword = null;
        this.certificatePath = StringUtils.EMPTY;
        this.remoteCertificatePassword = null;
        this.secureAgentAuth = false;
        this.jobServer = null;
        this.jobDatabase = null;


        if (Credentials.isUserDefined(credentialsType)) {

            this.jobUserName = userName;
            this.jobPasswdSecret = Secret.fromString(password);
            this.jobServer = userServer;
            this.jobDatabase = userDatabase;

        } else if (Credentials.isPluginDefined(credentialsType)) {
            UsernamePasswordCredentials credentials = initializeCredentials(credentialsId);

            if (credentials != null) {
                this.jobUserName = credentials.getUsername();
                this.jobPasswdSecret = credentials.getPassword();
            }

            this.jobServer = pluginServer;
            this.jobDatabase = pluginDatabase;
            this.credentialsId = credentialsId;

        } else if (Credentials.isKeystoreDefined(credentialsType)) {
            this.keystorePath = keystorePath;
            this.certificateAlias = certificateAlias;
            this.secureAgentAuth = secureAgentAuth;
            this.jobServer = keystoreServer;
            this.jobDatabase = keystoreDatabase;
            this.certificatePassword = Secret.fromString(certificatePassword);
            this.keystorePassword = Secret.fromString(keystorePassword);

            if (this.secureAgentAuth) {
                this.certificatePath = certificatePath;
                this.remoteCertificatePassword = Secret.fromString(remoteCertificatePassword);
            }

        }

        this.credentialsType = credentialsType;
        this.project = Values.textOrElse(project, "${JOB_NAME}");
        this.jobPasswd = null; // no longer used in config.xml serialization
        this.browser = getDescriptor().getBrowser();
        getAPI();

        Logger.debug("Starting job for project '" + getProject() + "' "
                + ", connecting to " + getServer() + "-" + getUserName() + ":" + getDatabase());
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

    private static UsernamePasswordCredentials initializeCredentials(String credentialsId) {

        UsernamePasswordCredentials credentials = null;

        if (credentialsId != null && !credentialsId.isEmpty()) {

            Item dummy = null;
            credentials = CredentialsMatchers.firstOrNull(
                    CredentialsProvider.lookupCredentials(
                            UsernamePasswordCredentials.class, dummy, ACL.SYSTEM,
                            Collections.<DomainRequirement>emptyList()),
                    CredentialsMatchers.allOf(
                            CredentialsMatchers.withId(credentialsId))
            );
        }
        return credentials;
    }

    public boolean isChecked(String type) {

        boolean isActive = false;

        boolean isPluginDefined = Credentials.isPluginDefined(credentialsType);
        boolean isGlobalDefined = Credentials.isGlobalDefined(credentialsType);
        boolean isUserDefined = Credentials.isUserDefined(credentialsType);
        boolean isKeystoreDefined = Credentials.isKeystoreDefined(credentialsType);

        if (Credentials.isPluginDefined(type)) {
            isActive = isPluginDefined;
        }

        if (Credentials.isGlobalDefined(type)) {
            isActive = isGlobalDefined;
        }

        if (Credentials.isKeystoreDefined(type)) {
            isActive = isKeystoreDefined;
        }

        if (Credentials.isUserDefined(type)) {
            boolean isAllNotActive = !isKeystoreDefined && !isPluginDefined && !isGlobalDefined;
            //the second part of 'or' needed in case when user updates from plugin version where it was no credential types yet
            isActive = isUserDefined || (isAllNotActive && !Values.isNullOrEmpty(jobUserName));
        }

        return isActive;
    }

    public DimensionsAPI getAPI() {
        DimensionsAPI api = this.cachedAPI;
        if (api == null) {
            api = newDimensionsAPIWithCheck();
            this.cachedAPI = api;
        }
        return api;
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
     * Gets selected credentialsId for the project
     */
    public String getCredentialsId() {
        return Values.textOrElse(this.credentialsId, null);
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
    public List<StringVarStorage> getFolders() {
        if (folders != null && folders.length > 0) {
            if (foldersList == null) {
                foldersList = new ArrayList<StringVarStorage>();
            }
            foldersList.addAll(Values.convertArrayToList(folders));
            folders = null;
        }

        return foldersList;
    }

    /**
     * Gets paths excluded from monitoring.
     */
    public List<StringVarStorage> getPathsToExclude() {
        if (pathsToExclude != null && pathsToExclude.length > 0) {
            if (pathsToExcludeList == null) {
                pathsToExcludeList = new ArrayList<StringVarStorage>();
            }
            pathsToExcludeList.addAll(Values.convertArrayToList(pathsToExclude));
            pathsToExclude = null;
        }

        return pathsToExcludeList;
    }

    /**
     * Gets the user ID for the connection.
     */
    public String getUserName() {
        if (Credentials.isGlobalDefined(credentialsType)) {
            return getDescriptor().getUserName();
        }
        return Values.textOrElse(this.jobUserName, null);
    }

    /**
     * Gets the password for the connection (not null).
     */
    public String getPasswordNN() {
        if (jobPasswdSecret == null && jobPasswd != null) {
            jobPasswdSecret = Secret.fromString(Scrambler.descramble(jobPasswd));
            jobPasswd = null;
        }

        Secret currentPassword = jobPasswdSecret;
        if (Credentials.isGlobalDefined(credentialsType)) {
            currentPassword = getDescriptor().getPassword();
        }

        return currentPassword != null && !currentPassword.getPlainText().isEmpty() ? currentPassword.getEncryptedValue() : StringUtils.EMPTY;
    }

    /**
     * Gets the password for the connection (can be null).
     */
    public String getPassword() {
        return Values.textOrElse(getPasswordNN(), null);
    }

    /**
     * Gets the certificate password as String.
     */
    public String getCertificatePassword() {
        Secret currentPassword = getCertificatePasswordSecret();
        return currentPassword != null ? currentPassword.getEncryptedValue() : null;
    }


    /**
     * Gets the certificate password as Secret object.
     */
    public Secret getCertificatePasswordSecret() {
        if (Credentials.isGlobalDefined(credentialsType)) {
            return getDescriptor().getCertificatePassword();
        }
        return certificatePassword;
    }

    /**
     * Gets the keystore password as Secret object.
     */
    public Secret getKeystorePasswordSecret() {
        if (Credentials.isGlobalDefined(credentialsType)) {
            return getDescriptor().getKeystorePassword();
        }
        return keystorePassword;
    }

    /**
     * Gets the keystore password as String.
     */
    public String getKeystorePassword() {
        Secret currentPassword = getKeystorePasswordSecret();
        return currentPassword != null ? currentPassword.getEncryptedValue() : null;
    }

    /**
     * Gets the remote certificate password as Secret instance.
     */
    public Secret getRemoteCertificatePasswordSecret() {
        if (Credentials.isGlobalDefined(credentialsType)) {
            return getDescriptor().getRemoteCertificatePassword();
        }
        return remoteCertificatePassword;
    }

    /**
     * Gets the remote certificate password as String.
     */
    public String getRemoteCertificatePassword() {
        Secret currentPassword = getRemoteCertificatePasswordSecret();
        return currentPassword != null ? currentPassword.getEncryptedValue() : null;
    }

    /**
     * Gets the remote certificate path as String.
     */
    public String getCertificatePath() {
        if (Credentials.isGlobalDefined(credentialsType)) {
            return getDescriptor().getCertificatePath();
        }

        return certificatePath;
    }

    /**
     * Check if need perform secure auth for remote.
     *
     * @return secure auth
     */
    public boolean isSecureAgentAuth() {
        if (Credentials.isGlobalDefined(credentialsType)) {
            return getDescriptor().isSecureAgentAuth();
        }

        return secureAgentAuth;
    }

    /**
     * Gets the server name for the connection.
     */
    public String getServer() {
        if (Credentials.isGlobalDefined(credentialsType)) {
            return getDescriptor().getServer();
        }
        return this.jobServer;
    }

    //this getter is needed for snippet generator
    public String getUserServer() {
        return Credentials.isUserDefined(credentialsType) ? this.jobServer : null;
    }

    //this getter is needed for snippet generator
    public String getPluginServer() {
        return Credentials.isPluginDefined(credentialsType) ? this.jobServer : null;
    }

    //this getter is needed for snippet generator
    public String getKeystoreServer() {
        return Credentials.isKeystoreDefined(credentialsType) ? this.jobServer : null;
    }

    /**
     * Gets the database name for the connection.
     */
    public String getDatabase() {
        if (Credentials.isGlobalDefined(credentialsType)) {
            return getDescriptor().getDatabase();
        }
        return this.jobDatabase;
    }

    //this getter is needed for snippet generator
    public String getUserDatabase() {
        return Credentials.isUserDefined(credentialsType) ? this.jobDatabase : null;
    }

    //this getter is needed for snippet generator
    public String getPluginDatabase() {
        return Credentials.isPluginDefined(credentialsType) ? this.jobDatabase : null;
    }

    //this getter is needed for snippet generator
    public String getKeystoreDatabase() {
        return Credentials.isKeystoreDefined(credentialsType) ? this.jobDatabase : null;
    }

    /**
     * Gets the time zone for the connection.
     */
    public String getTimeZone() {
        return this.jobTimeZone;
    }

    /**
     * Gets the web URL for the connection.
     */
    public String getWebUrl() {
        return this.jobWebUrl;
    }

    /**
     * Gets the credentials type.
     */
    public String getCredentialsType() {
        if (Credentials.isGlobalDefined(credentialsType)) {
            return getDescriptor().getCredentialsType();
        }
        return credentialsType;
    }

    /**
     * Gets the keystore path.
     */
    public String getKeystorePath() {
        if (Credentials.isGlobalDefined(credentialsType)) {
            return getDescriptor().getKeystorePath();
        }

        return Values.textOrElse(this.keystorePath, null);
    }

    /**
     * Gets the certificate alias.
     */
    public String getCertificateAlias() {
        if (Credentials.isGlobalDefined(credentialsType)) {
            return getDescriptor().getCertificateAlias();
        }
        return Values.textOrElse(this.certificateAlias, null);
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

    @DataBoundSetter
    public void setFolders(List<StringVarStorage> folders) {
        this.foldersList = Values.notBlankOrElseList(folders, DEFAULT_FOLDERS);
    }

    @DataBoundSetter
    public void setPathsToExclude(List<StringVarStorage> pathsToExclude) {
        this.pathsToExcludeList = Values.notBlankOrElseList(pathsToExclude, EMPTY_STRING_LIST);
    }

    @DataBoundSetter
    public void setPermissions(String permissions) {
        this.permissions = canJobUpdate ? Values.textOrElse(permissions, "DEFAULT") : StringUtils.EMPTY;
    }

    @DataBoundSetter
    public void setEol(String eol) {
        this.eol = canJobUpdate ? Values.textOrElse(eol, "DEFAULT") : StringUtils.EMPTY;
    }

    @DataBoundSetter
    public void setTimeZone(String timeZone) {
        this.jobTimeZone = Values.textOrElse(timeZone, getDescriptor().getTimeZone());
    }


    @DataBoundSetter
    public void setWebUrl(String webUrl) {
        this.jobWebUrl = Values.textOrElse(webUrl, getDescriptor().getWebUrl());
    }

    @DataBoundSetter
    public void setCanJobUpdate(boolean canJobUpdate) {
        this.canJobUpdate = Values.hasText(this.jobServer) ? canJobUpdate : getDescriptor().isCanUpdate();
    }

    @DataBoundSetter
    public void setCanJobDelete(boolean canJobDelete) {
        this.canJobDelete = canJobDelete;
    }

    @DataBoundSetter
    public void setCanJobForce(boolean canJobForce) {
        this.canJobForce = canJobForce;
    }

    @DataBoundSetter
    public void setCanJobRevert(boolean canJobRevert) {
        this.canJobRevert = canJobRevert;
    }

    @DataBoundSetter
    public void setCanJobExpand(boolean canJobExpand) {
        this.canJobExpand = canJobUpdate && canJobExpand;
    }

    @DataBoundSetter
    public void setCanJobNoMetadata(boolean canJobNoMetadata) {
        this.canJobNoMetadata = canJobUpdate && canJobNoMetadata;
    }

    @DataBoundSetter
    public void setCanJobNoTouch(boolean canJobNoTouch) {
        this.canJobNoTouch = canJobUpdate && canJobNoTouch;
    }


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

    @Override
    public DimensionsSCMRepositoryBrowser getBrowser() {
        return this.browser;
    }

    @Nonnull
    @Override
    public String getKey() {
        return "dimensions " + getUserName() + "@" + getServer() + "/" + getDatabase() + "/" + getProject();
    }

    @CheckForNull
    @Override
    public RepositoryBrowser<?> guessBrowser() {
        return new DimensionsSCMRepositoryBrowser();
    }

    /**
     * Get build parameters for WorkflowRun
     */
    public String getParameterFromBuild(WorkflowRun build, String parameterName) {

        String parValue = null;

        for (ParametersAction parametersAction : build.getActions(ParametersAction.class)) {
            ParameterValue parameterValue = parametersAction.getParameter(parameterName);

            if (parameterValue != null) {
                parValue = String.valueOf(parameterValue.getValue());
                break;
            }
        }
        return parValue;
    }

    /**
     * Checkout method for the plugin.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void checkout(@Nonnull Run<?, ?> build, @Nonnull Launcher launcher, @Nonnull FilePath workspace, @Nonnull TaskListener listener,
                         @CheckForNull File changelogFile, @CheckForNull SCMRevisionState baseln) throws IOException, InterruptedException {
        if (!isCanJobUpdate()) {
            Logger.debug("Skipping checkout - " + this.getClass().getName());
        }

        Logger.debug("Invoking checkout - " + this.getClass().getName());

        // Load other Dimensions plugins if set.
        DimensionsBuildWrapper.DescriptorImpl bwplugin = (DimensionsBuildWrapper.DescriptorImpl)
                Jenkins.getInstance().getDescriptor(DimensionsBuildWrapper.class);
        DimensionsBuildNotifier.DescriptorImpl bnplugin = (DimensionsBuildNotifier.DescriptorImpl)
                Jenkins.getInstance().getDescriptor(DimensionsBuildNotifier.class);

        if (DimensionsChecker.isValidPluginCombination(build, listener)) {
            Logger.debug("Plugins are ok");
        } else {
            listener.fatalError("\n[DIMENSIONS] The plugin combinations you have selected are not valid.");
            listener.fatalError("\n[DIMENSIONS] Please review online help to determine valid plugin uses.");
            throw new IOException("Error: you have selected wrong plugin combinations.");
        }

        if (isCanJobUpdate()) {
            DimensionsAPI dmSCM = getAPI();
            int version = 2009;

            long key = dmSCM.login(this, build);

            if (key > 0L) {
                // Get the server version.
                Logger.debug("Login worked.");
                version = dmSCM.getDmVersion();
                if (version == 0) {
                    version = 2009;
                }
                dmSCM.logout(key, build);
            }

            if (!workspace.isRemote()) {
                // Running on master...
                Logger.debug("Checking if master or slave...");
                listener.getLogger().println("[DIMENSIONS] Running checkout on master...");
                listener.getLogger().flush();
                // Using Java API because this allows the plugin to work on platforms where Dimensions has not
                // been ported, e.g. MAC OS, which is what I use.
                CheckOutAPITask task = new CheckOutAPITask(build, this, workspace, listener, version);
                workspace.act(task);
            } else {
                // Running on slave... Have to use the command line as Java API will not work on remote hosts.
                // Cannot serialise it...
                // VariableResolver does not appear to be serialisable either, so...
                Logger.debug("Forced processing as slave...");
                String baseline = null;
                String request = null;

                if (build instanceof AbstractBuild) {

                    VariableResolver<String> myResolver = ((AbstractBuild<?, ?>) build).getBuildVariableResolver();

                    baseline = myResolver.resolve("DM_BASELINE");
                    request = myResolver.resolve("DM_REQUEST");

                } else if (build instanceof WorkflowRun) {
                    baseline = getParameterFromBuild((WorkflowRun) build, "DM_BASELINE");
                    request = getParameterFromBuild((WorkflowRun) build, "DM_REQUEST");
                }


                listener.getLogger().println("[DIMENSIONS] Running checkout on slave...");
                listener.getLogger().flush();

                if (Credentials.isKeystoreDefined(getCredentialsType())) {
                    if (StringUtils.isBlank(getCertificatePath())) {
                        throw new IOException("User certificate path from remote machine must be specified.");
                    }
                    if (getRemoteCertificatePasswordSecret() == null) {
                        throw new IOException("User certificate password from remote machine must be specified.");
                    }

                }

                CheckOutCmdTask task = new CheckOutCmdTask(getUserName(), Secret.decrypt(getPasswordNN()), getDatabase(),
                        getServer(), getProjectVersion(build, listener), baseline, request, isCanJobDelete(),
                        isCanJobRevert(), isCanJobForce(), isCanJobExpand(), isCanJobNoMetadata(),
                        isCanJobNoTouch(), (build.getPreviousBuild() == null), getFolders(), version,
                        permissions, eol, getCertificatePath(), getRemoteCertificatePasswordSecret(),
                        isSecureAgentAuth(), workspace, listener);
                workspace.act(task);
            }
        }
        generateChangeSet(build, listener, changelogFile);
    }

    /**
     * Generate the changeset.
     */
    private void generateChangeSet(final Run<?, ?> build, final TaskListener listener, final File changelogFile) throws IOException {
        long key = -1L;
        DimensionsAPI dmSCM = newDimensionsAPIWithCheck();

        try {
            // When are we building files for?
            // Looking for the last successful build and then go forward from there - could use the last build as well.
            Calendar lastBuildCal = (build.getPreviousBuild() != null) ? build.getPreviousBuild().getTimestamp() : null;
            // Calendar lastBuildCal = (build.getPreviousNotFailedBuild() != null) ?
            //         build.getPreviousNotFailedBuild().getTimestamp() : null;
            Calendar nowDateCal = Calendar.getInstance();

            TimeZone tz = (getTimeZone() != null && getTimeZone().length() > 0)
                    ? TimeZone.getTimeZone(getTimeZone()) : TimeZone.getDefault();
            if (getTimeZone() != null && getTimeZone().length() > 0) {
                Logger.debug("Job timezone setting is " + getTimeZone());
            }
            Logger.debug("Log updates between " + (lastBuildCal != null
                    ? DateUtils.getStrDate(lastBuildCal, tz) : "0") + " -> " + DateUtils.getStrDate(nowDateCal, tz)
                    + " (" + tz.getID() + ")");

            dmSCM.setLogger(listener.getLogger());

            // Connect to Dimensions...
            key = dmSCM.login(this, build);

            if (key > 0L) {
                Logger.debug("Login worked.");


                String baseline = null;
                String request = null;

                if (build instanceof AbstractBuild) {

                    VariableResolver<String> myResolver = ((AbstractBuild<?, ?>) build).getBuildVariableResolver();

                    baseline = myResolver.resolve("DM_BASELINE");
                    request = myResolver.resolve("DM_REQUEST");

                } else if (build instanceof WorkflowRun) {
                    baseline = getParameterFromBuild((WorkflowRun) build, "DM_BASELINE");
                    request = getParameterFromBuild((WorkflowRun) build, "DM_REQUEST");
                }

                if (baseline != null) {
                    baseline = baseline.trim();
                    baseline = baseline.toUpperCase(Values.ROOT_LOCALE);
                }
                if (request != null) {
                    request = request.replaceAll(" ", "");
                    request = request.toUpperCase(Values.ROOT_LOCALE);
                }

                Logger.debug("Extra parameters - " + baseline + " " + request);
                List<StringVarStorage> folders = getFolders();

                if (baseline != null && baseline.length() == 0) {
                    baseline = null;
                }
                if (request != null && request.length() == 0) {
                    request = null;
                }

                // Iterate through the project folders and process them in Dimensions.
                for (StringVarStorage folderStrg : folders) {

                    String folderN = folderStrg.getValue();

                    File fileName = new File(folderN);
                    FilePath dname = new FilePath(fileName);

                    Logger.debug("Looking for changes in '" + folderN + "'...");

                    // Check out the folder.
                    dmSCM.createChangeSetLogs(key, getProjectName(build, listener), dname, lastBuildCal, nowDateCal,
                            changelogFile, tz, jobWebUrl, baseline, request);
                    if (request != null) {
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
            throw new IOException(e);
        } finally {
            dmSCM.logout(key, build);
        }
    }

    /**
     * Has the repository had any changes since last build?
     * <p>
     * {@inheritDoc}
     */
    @Override
    public SCMRevisionState calcRevisionsFromBuild(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener) {
        return SCMRevisionState.NONE;
    }


    /**
     * Has the repository had any changes?
     * <p>
     * {@inheritDoc}
     */
    @Override
    public PollingResult compareRemoteRevisionWith(Job<?, ?> project, Launcher launcher, FilePath workspace,
                                                   TaskListener listener, SCMRevisionState baseline) throws IOException, InterruptedException {

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
    @Override
    public boolean processWorkspaceBeforeDeletion(@Nonnull Job<?, ?> project, @Nonnull FilePath workspace, @Nonnull Node node) throws IOException, InterruptedException {
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
            TimeZone tz = (getTimeZone() != null && getTimeZone().length() > 0)
                    ? TimeZone.getTimeZone(getTimeZone()) : TimeZone.getDefault();
            if (getTimeZone() != null && getTimeZone().length() > 0) {
                Logger.debug("Job timezone setting is " + getTimeZone());
            }
            Logger.debug("Checking for any updates between " + (lastBuildCal != null
                    ? DateUtils.getStrDate(lastBuildCal, tz) : "0") + " -> " + DateUtils.getStrDate(nowDateCal, tz)
                    + " (" + tz.getID() + ")");

            dmSCM.setLogger(listener.getLogger());

            // Connect to Dimensions...
            key = dmSCM.login(this, null);
            if (key > 0L) {
                List<StringVarStorage> folders = getFolders();
                // Iterate through the project folders and process them in Dimensions
                for (StringVarStorage folderStrg : folders) {

                    String folderN = folderStrg.getValue();
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
                                + " where jobTimeZone=[" + getTimeZone() + "]. "
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

        String[] pathToExcludeArr = Values.convertListToArray(getPathsToExclude());

        return Values.isNullOrEmpty(pathToExcludeArr) ? new NullPathMatcher()
                : new DefaultPathMatcher(pathToExcludeArr, null);
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

    /**
     * Implementation class for Dimensions CM SCM plugin.
     */
    public static class DescriptorImpl extends SCMDescriptor<DimensionsSCM> implements ModelObject {
        private transient DimensionsSCMRepositoryBrowser browser;
        private transient String passwd;
        private String server;
        private String userName;
        private Secret passwdSecret;
        private Secret keystoreSecret;
        private Secret certificateSecret;
        private Secret remoteCertificateSecret;
        private String database;
        private String credentialsId;
        private String keystorePath;
        private String certificatePath;
        private String certificateAlias;
        private String timeZone;
        private String webUrl;
        private String credentialsType;
        private boolean canUpdate;
        private boolean secureAgentAuth;

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

            this.timeZone = req.getParameter("dimensionsscm.timeZone");
            this.webUrl = req.getParameter("dimensionsscm.webUrl");
            this.passwd = null;
            this.credentialsType = jobj.getJSONObject("credentialsType").getString("value");

            this.userName = StringUtils.EMPTY;
            this.passwdSecret = null;
            this.keystoreSecret = null;
            this.certificateSecret = null;
            this.remoteCertificateSecret = null;
            this.userName = StringUtils.EMPTY;
            this.credentialsId = StringUtils.EMPTY;
            this.keystorePath = StringUtils.EMPTY;
            this.certificatePath = StringUtils.EMPTY;
            this.certificateAlias = StringUtils.EMPTY;
            this.secureAgentAuth = false;


            if (Credentials.isPluginDefined(this.credentialsType)) {
                this.credentialsId = jobj.getJSONObject("credentialsType").getString("credentialsId");

                UsernamePasswordCredentials credentials = initializeCredentials(this.credentialsId);

                if (credentials != null) {
                    this.userName = credentials.getUsername();
                    this.passwdSecret = credentials.getPassword();
                }

                this.server = Values.textOrElse(req.getParameter("dimensionsscm.serverPlugin"), null);
                this.database = Values.textOrElse(req.getParameter("dimensionsscm.databasePlugin"), null);

            } else if (Credentials.isUserDefined(this.credentialsType)) {

                this.userName = Values.textOrElse(req.getParameter("dimensionsscm.userName"), null);
                this.passwdSecret = Secret.fromString(req.getParameter("dimensionsscm.passwd"));
                this.server = Values.textOrElse(req.getParameter("dimensionsscm.serverUser"), null);
                this.database = Values.textOrElse(req.getParameter("dimensionsscm.databaseUser"), null);

            } else if (Credentials.isKeystoreDefined(this.credentialsType)) {

                this.keystorePath = Values.textOrElse(req.getParameter("dimensionsscm.keystorePath"), null);
                this.certificateAlias = Values.textOrElse(req.getParameter("dimensionsscm.certificateAlias"), null);
                this.keystoreSecret = Secret.fromString(req.getParameter("dimensionsscm.keystorePassword"));
                this.certificateSecret = Secret.fromString(req.getParameter("dimensionsscm.certificatePassword"));
                this.server = Values.textOrElse(req.getParameter("dimensionsscm.keystoreServer"), null);
                this.database = Values.textOrElse(req.getParameter("dimensionsscm.keystoreDatabase"), null);
                this.secureAgentAuth = "on".equalsIgnoreCase(req.getParameter("dimensionsscm.secureAgentAuth"));

                if (this.secureAgentAuth) {
                    this.remoteCertificateSecret = Secret.fromString(req.getParameter("dimensionsscm.remoteCertificatePassword"));
                    this.certificatePath = Values.textOrElse(req.getParameter("dimensionsscm.certificatePath"), null);
                }
            }

            if (this.userName != null) {
                this.userName = this.userName.trim();
            }
            if (this.server != null) {
                this.server = this.server.trim();
            }
            if (this.database != null) {
                this.database = this.database.trim();
            }
            if (this.timeZone != null) {
                this.timeZone = this.timeZone.trim();
            }
            if (this.webUrl != null) {
                this.webUrl = this.webUrl.trim();
            }

            req.bindJSON(DM_DESCRIPTOR, jobj);

            this.save();
            return super.configure(req, jobj);
        }

        @Override
        public SCM newInstance(StaplerRequest req, JSONObject formData) throws FormException {

            this.browser = RepositoryBrowsers.createInstance(DimensionsSCMRepositoryBrowser.class, req, formData,
                    "browser");

            return super.newInstance(req, formData);
        }

        @Override
        public boolean isApplicable(Job project) {
            return true;
        }

        public boolean isChecked(String type) {

            boolean isPluginDefined = Credentials.isPluginDefined(credentialsType);
            boolean isUserDefined = Credentials.isUserDefined(credentialsType);
            boolean isKeystoreDefined = Credentials.isKeystoreDefined(credentialsType);

            if (Credentials.isPluginDefined(type)) {
                return isPluginDefined;
            }

            if (Credentials.isKeystoreDefined(type)) {
                return isKeystoreDefined;
            }

            if (Credentials.isUserDefined(type)) {
                boolean isAllNotActive = !isKeystoreDefined && !isPluginDefined;
                return isUserDefined || (isAllNotActive && !Values.isNullOrEmpty(this.userName));
            }

            return false;
        }

        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item project, @QueryParameter String credentialsId) {

            return new StandardListBoxModel()
                    .includeEmptyValue()
                    .includeMatchingAs(
                            ACL.SYSTEM,
                            project,
                            StandardUsernamePasswordCredentials.class,
                            Collections.<DomainRequirement>emptyList(),
                            CredentialsMatchers.always()
                    ).includeCurrentValue(credentialsId);
        }


        public DimensionsSCMRepositoryBrowser getBrowser() {
            return browser;
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
         * Gets the credentials id.
         *
         * @return the credentials ID of user
         */
        public String getCredentialsId() {
            return credentialsId;
        }


        /**
         * Gets the credentials type.
         *
         * @return the credentials type
         */
        public String getCredentialsType() {
            return credentialsType;
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
         * Check if need perform secure auth for remote.
         *
         * @return secure auth
         */
        public boolean isSecureAgentAuth() {
            return secureAgentAuth;
        }

        /**
         * Gets the password as a Secret instance.
         *
         * @return the password (as a Secret instance)
         */
        public Secret getPassword() {
            if (passwdSecret == null && passwd != null) {
                passwdSecret = Secret.fromString(Scrambler.descramble(passwd));
                passwd = null;
            }
            return passwdSecret;
        }

        /**
         * Gets the keystore password as a Secret instance.
         *
         * @return the password (as a Secret instance)
         */
        public Secret getKeystorePassword() {
            return keystoreSecret;
        }

        /**
         * Gets the certificate password as a Secret instance.
         *
         * @return the password (as a Secret instance)
         */
        public Secret getCertificatePassword() {
            return certificateSecret;
        }

        /**
         * Gets the remote certificate password as a Secret instance.
         *
         * @return the password (as a Secret instance)
         */
        public Secret getRemoteCertificatePassword() {
            return remoteCertificateSecret;
        }

        /**
         * Gets the certificate alias.
         *
         * @return alias
         */
        public String getCertificateAlias() {
            return certificateAlias;
        }

        /**
         * Gets the keystore path.
         *
         * @return the path
         */
        public String getKeystorePath() {
            return keystorePath;
        }


        /**
         * Gets the certificate remote path.
         *
         * @return the path
         */
        public String getCertificatePath() {
            return certificatePath;
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
                                        @QueryParameter("dimensionsscm.timeZone") final String timezone) {
            try {
                Logger.debug("Invoking docheckTz - " + timezone);
                TimeZone ctz = TimeZone.getTimeZone(timezone);
                String lmt = ctz.getID();
                if (lmt.equalsIgnoreCase("GMT") && !(timezone.equalsIgnoreCase("GMT")
                        || timezone.equalsIgnoreCase("Greenwich Mean Time") || timezone.equalsIgnoreCase("UTC")
                        || timezone.equalsIgnoreCase("Coordinated Universal Time"))) {
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
         * Check if the specified Dimensions server is valid (global.jelly).
         */
        @RequirePOST
        public FormValidation doCheckServerGlobal(StaplerRequest req, StaplerResponse rsp,
                                                  @QueryParameter("credentialsId") final String credentialsId,
                                                  @QueryParameter("credentialsType") final String credentialsType,
                                                  @QueryParameter("dimensionsscm.userName") final String user,
                                                  @QueryParameter("dimensionsscm.passwd") final String passwd,
                                                  @QueryParameter("dimensionsscm.serverUser") final String serverUser,
                                                  @QueryParameter("dimensionsscm.serverPlugin") final String serverPlugin,
                                                  @QueryParameter("dimensionsscm.databaseUser") final String databaseUser,
                                                  @QueryParameter("dimensionsscm.databasePlugin") final String databasePlugin,
                                                  @AncestorInPath final Item item) {

            String xuser = null;
            String xpasswd = null;
            String xserver = null;
            String xdatabase = null;


            if (Credentials.isPluginDefined(credentialsType)) {

                UsernamePasswordCredentials credentials = initializeCredentials(credentialsId);

                if (credentials != null) {
                    xuser = credentials.getUsername();
                    xpasswd = credentials.getPassword().getPlainText();
                }

                xserver = serverPlugin;
                xdatabase = databasePlugin;
            } else if (Credentials.isUserDefined(credentialsType)) {
                xuser = user;
                xpasswd = passwd;
                xserver = serverUser;
                xdatabase = databaseUser;
            }

            return checkServer(item, xuser, xpasswd, xserver, xdatabase);
        }

        @RequirePOST
        public FormValidation doCheckServerKeystore(StaplerRequest req, StaplerResponse rsp,
                                                    @QueryParameter("dimensionsscm.keystorePath") final String keystorePath,
                                                    @QueryParameter("dimensionsscm.keystorePassword") final String keystorePassword,
                                                    @QueryParameter("dimensionsscm.keystoreServer") final String keystoreServer,
                                                    @QueryParameter("dimensionsscm.keystoreDatabase") final String keystoreDatabase,
                                                    @QueryParameter("dimensionsscm.certificatePassword") final String certificatePassword,
                                                    @QueryParameter("dimensionsscm.certificateAlias") final String certificateAlias,
                                                    @AncestorInPath final Item item) {


            try {
                Logger.debug("Server connection check to keystore [" + keystorePath
                        + "], certificate [" + certificateAlias + "]");

                Secret keystorePassSecret = Secret.fromString(keystorePassword);
                Secret certPassSecret = Secret.fromString(certificatePassword);
                DimensionsAPI connectionCheck = newDimensionsAPIWithCheck();

                long key = connectionCheck.login(keystoreServer, keystoreDatabase, certificateAlias, certPassSecret, keystorePath, keystorePassSecret);

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

        /**
         * Check if the specified Dimensions server is valid (config.jelly).
         */
        @RequirePOST
        public FormValidation doCheckServerConfig(StaplerRequest req, StaplerResponse rsp,
                                                  @QueryParameter("credentialsId") final String credentialsId,
                                                  @QueryParameter("credentialsType") final String credentialsType,
                                                  @QueryParameter("dimensionsscm.userName") final String jobuser,
                                                  @QueryParameter("dimensionsscm.password") final String jobPasswd,
                                                  @QueryParameter("dimensionsscm.userServer") final String jobServerUser,
                                                  @QueryParameter("dimensionsscm.userDatabase") final String jobDatabaseUser,
                                                  @QueryParameter("dimensionsscm.pluginServer") final String jobServerPlugin,
                                                  @QueryParameter("dimensionsscm.pluginDatabase") final String jobDatabasePlugin,
                                                  @AncestorInPath final Item item) {

            String xuser = null;
            String xpasswd = null;
            String xserver;
            String xdatabase;

            if (Credentials.isPluginDefined(credentialsType)) {

                UsernamePasswordCredentials credentials = initializeCredentials(credentialsId);

                if (credentials != null) {
                    xuser = credentials.getUsername();
                    xpasswd = credentials.getPassword().getPlainText();
                }

                xserver = jobServerPlugin;
                xdatabase = jobDatabasePlugin;

            } else if (Credentials.isUserDefined(credentialsType)) {

                xuser = jobuser;
                xpasswd = jobPasswd;
                xserver = jobServerUser;
                xdatabase = jobDatabaseUser;

            } else {


                boolean keystoreDefined = Credentials.isKeystoreDefined(credentialsType);
                boolean globalDefined = Credentials.isGlobalDefined(credentialsType);
                boolean keystoreGlobalDefined = Credentials.isKeystoreDefined(this.credentialsType);

                if (keystoreDefined || (globalDefined && keystoreGlobalDefined)) {
                    if (this.keystoreSecret == null || this.certificateSecret == null)
                        return FormValidation.error("Keystore or certificate password not specified in global configuration.");

                    return doCheckServerKeystore(req, rsp, this.keystorePath, this.keystoreSecret.getPlainText(), this.server, this.database, this.certificateSecret.getPlainText(), this.certificateAlias, item);
                }

                if (this.passwdSecret == null) {
                    return FormValidation.error("Password not specified in global configuration.");
                }

                xuser = this.userName;
                xpasswd = this.passwdSecret.getPlainText();
                xserver = this.server;
                xdatabase = this.database;

                if (Values.isNullOrEmpty(xuser)) {
                    return FormValidation.error("User name not specified in global configuration.");
                }

                if (Values.isNullOrEmpty(xdatabase)) {
                    return FormValidation.error("Database not specified in global configuration.");
                }

                if (Values.isNullOrEmpty(xserver)) {
                    return FormValidation.error("User server not specified in global configuration.");
                }
            }

            return checkServer(item, xuser, xpasswd, xserver, xdatabase);
        }

        private FormValidation checkServer(final Item item, String xuser, String xpasswd, String xserver, String xdatabase) {

            if (item == null) {
                Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
            } else {
                item.checkPermission(Item.CONFIGURE);
            }

            DimensionsAPI connectionCheck = newDimensionsAPIWithCheck();
            try {
                if (xpasswd == null || xuser == null) {
                    return FormValidation.error("User name and password must be specified.");
                }

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
