package hudson.plugins.dimensionsscm;

import hudson.Extension;
import hudson.model.Item;
import hudson.plugins.dimensionsscm.model.StringVarStorage;
import hudson.scm.SCM;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import org.jenkinsci.plugins.workflow.steps.scm.SCMStep;
import org.kohsuke.stapler.*;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.List;

public final class DimensionsStep extends SCMStep {

    private String credentialsId;
    private String userName;
    private String userServer;
    private String pluginServer;
    private String userDatabase;
    private String pluginDatabase;
    private String project;
    private String password;
    private String certificatePassword;
    private String keystorePassword;
    private String keystoreServer;
    private String keystoreDatabase;
    private String credentialsType;
    private String timeZone;
    private String webUrl;
    private String permissions;
    private String eol;
    private String keystorePath;
    private String certificateAlias;
    private List<StringVarStorage> folders;
    private List<StringVarStorage> pathsToExclude;
    private boolean canJobDelete;
    private boolean canJobForce;
    private boolean canJobRevert;
    private boolean canJobExpand;
    private boolean canJobNoMetadata;
    private boolean canJobNoTouch;
    private boolean forceAsSlave;
    private boolean canJobUpdate;


    @DataBoundConstructor
    public DimensionsStep(String credentialsId, String userName, String userServer, String pluginServer,
                          String userDatabase, String pluginDatabase, String password, String credentialsType,
                          String keystorePath, String certificateAlias, String certificatePassword, String keystorePassword,
                          String keystoreServer, String keystoreDatabase) {

        this.credentialsId = null;
        this.pluginServer = null;
        this.pluginDatabase = null;
        this.userName = null;
        this.userServer = null;
        this.userDatabase = null;
        this.password = null;
        this.keystorePath = null;
        this.certificateAlias = null;
        this.keystoreServer = null;
        this.keystoreDatabase = null;
        this.certificatePassword = null;
        this.keystorePassword = null;

        if (DimensionsSCM.PLUGIN_DEFINED.equalsIgnoreCase(credentialsType)) {
            this.credentialsId = credentialsId;
            this.pluginServer = pluginServer;
            this.pluginDatabase = pluginDatabase;
        } else if (DimensionsSCM.USER_DEFINED.equalsIgnoreCase(credentialsType)) {
            this.userName = userName;
            this.userServer = userServer;
            this.userDatabase = userDatabase;
            this.password = password;
        } else if (DimensionsSCM.KEYSTORE_DEFINED.equalsIgnoreCase(credentialsType)) {
            this.keystorePath = keystorePath;
            this.certificateAlias = certificateAlias;
            this.keystoreServer = keystoreServer;
            this.keystoreDatabase = keystoreDatabase;
            this.certificatePassword = certificatePassword;
            this.keystorePassword = keystorePassword;
        }

        this.credentialsType = credentialsType;
    }

    public String getTimeZone() {
        return timeZone;
    }

    @DataBoundSetter
    public void setTimeZone(String timeZone) {
        this.timeZone = Values.textOrElse(timeZone, null);
    }

    public String getWebUrl() {
        return webUrl;
    }

    @DataBoundSetter
    public void setWebUrl(String webUrl) {
        this.webUrl = Values.textOrElse(webUrl, null);
    }

    public String getProject() {
        return project;
    }

    @DataBoundSetter
    public void setProject(String project) {
        this.project = Values.textOrElse(project, null);
    }

    public List<StringVarStorage> getFolders() {
        return folders;
    }

    @DataBoundSetter
    public void setFolders(List<StringVarStorage> folders) {
        this.folders = Values.notBlankOrElseList(folders, null);
    }

    public List<StringVarStorage> getPathsToExclude() {
        return pathsToExclude;
    }

    @DataBoundSetter
    public void setPathsToExclude(List<StringVarStorage> pathsToExclude) {
        this.pathsToExclude = Values.notBlankOrElseList(pathsToExclude, null);
    }

    public boolean isCanJobDelete() {
        return canJobDelete;
    }

    @DataBoundSetter
    public void setCanJobDelete(boolean canJobDelete) {
        this.canJobDelete = canJobDelete;
    }

    public boolean isCanJobForce() {
        return canJobForce;
    }

    @DataBoundSetter
    public void setCanJobForce(boolean canJobForce) {
        this.canJobForce = canJobForce;
    }

    public boolean isCanJobRevert() {
        return canJobRevert;
    }

    @DataBoundSetter
    public void setCanJobRevert(boolean canJobRevert) {
        this.canJobRevert = canJobRevert;
    }

    public String getPermissions() {
        return permissions;
    }

    @DataBoundSetter
    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    public String getEol() {
        return eol;
    }

    @DataBoundSetter
    public void setEol(String eol) {
        this.eol = eol;
    }

    public boolean isCanJobExpand() {
        return canJobExpand;
    }

    @DataBoundSetter
    public void setCanJobExpand(boolean canJobExpand) {
        this.canJobExpand = canJobExpand;
    }

    public boolean isCanJobNoMetadata() {
        return canJobNoMetadata;
    }

    @DataBoundSetter
    public void setCanJobNoMetadata(boolean canJobNoMetadata) {
        this.canJobNoMetadata = canJobNoMetadata;
    }

    public boolean isCanJobNoTouch() {
        return canJobNoTouch;
    }

    @DataBoundSetter
    public void setCanJobNoTouch(boolean canJobNoTouch) {
        this.canJobNoTouch = canJobNoTouch;
    }

    public boolean isForceAsSlave() {
        return forceAsSlave;
    }

    @DataBoundSetter
    public void setForceAsSlave(boolean forceAsSlave) {
        this.forceAsSlave = forceAsSlave;
    }

    public boolean isCanJobUpdate() {
        return canJobUpdate;
    }

    @DataBoundSetter
    public void setCanJobUpdate(boolean canJobUpdate) {
        this.canJobUpdate = canJobUpdate;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserServer() {
        return userServer;
    }

    public String getPluginServer() {
        return pluginServer;
    }

    public String getUserDatabase() {
        return userDatabase;
    }

    public String getPluginDatabase() {
        return pluginDatabase;
    }

    public String getPassword() {
        return password != null ? Secret.fromString(password).getEncryptedValue() : null;
    }

    public String getCredentialsType() {
        return credentialsType;
    }

    public String getCertificatePassword() {
        return certificatePassword != null ? Secret.fromString(certificatePassword).getEncryptedValue() : null;
    }

    public String getKeystorePassword() {
        return keystorePassword != null ? Secret.fromString(keystorePassword).getEncryptedValue() : null;
    }

    public String getKeystoreServer() {
        return keystoreServer;
    }

    public String getKeystoreDatabase() {
        return keystoreDatabase;
    }

    public String getKeystorePath() {
        return keystorePath;
    }

    public String getCertificateAlias() {
        return certificateAlias;
    }

    @Nonnull
    @Override
    protected SCM createSCM() {
        DimensionsSCM scm = new DimensionsSCM(project, credentialsType, userName, password, userServer,
                pluginServer, keystoreServer, keystoreDatabase, userDatabase, pluginDatabase, keystorePath,
                certificateAlias, credentialsId, certificatePassword, keystorePassword);
        scm.setTimeZone(timeZone);
        scm.setWebUrl(webUrl);
        scm.setCanJobDelete(canJobDelete);
        scm.setCanJobForce(canJobForce);
        scm.setCanJobRevert(canJobRevert);
        scm.setFolders(folders);
        scm.setPathsToExclude(pathsToExclude);
        scm.setEol(eol);
        scm.setPermissions(permissions);
        scm.setCanJobExpand(canJobExpand);
        scm.setCanJobUpdate(canJobUpdate);
        scm.setCanJobNoMetadata(canJobNoMetadata);
        scm.setCanJobNoTouch(canJobNoTouch);
        scm.setForceAsSlave(forceAsSlave);
        return scm;
    }


    @Extension
    public static final class DescriptorImpl extends SCMStepDescriptor {

        @Inject
        private DimensionsSCM.DescriptorImpl delegate;


        @Override
        public String getFunctionName() {
            return "dimensionsscm";
        }

        @Override
        public String getDisplayName() {
            return Messages.DimensionsStep_DimensionsSCM();
        }

        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item project, @QueryParameter String credentialsId) {
            return delegate.doFillCredentialsIdItems(project, credentialsId);
        }

        @RequirePOST
        public FormValidation docheckTz(StaplerRequest req, StaplerResponse rsp,
                                        @QueryParameter("dimensionsscm.timeZone") final String timezone) {
            return delegate.docheckTz(req, rsp, timezone);
        }

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
            return delegate.doCheckServerConfig(req, rsp, credentialsId, credentialsType, jobuser, jobPasswd, jobServerUser,
                    jobDatabaseUser, jobServerPlugin, jobDatabasePlugin, item);
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
            return delegate.doCheckServerKeystore(req, rsp, keystorePath, keystorePassword, keystoreServer, keystoreDatabase,
                    certificatePassword, certificateAlias, item);
        }
    }
}
