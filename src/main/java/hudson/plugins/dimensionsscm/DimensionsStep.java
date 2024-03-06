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

import edu.umd.cs.findbugs.annotations.NonNull;
import jakarta.inject.Inject;
import java.util.List;

public final class DimensionsStep extends SCMStep {

    private String credentialsId;
    private String userName;
    private String userServer;
    private String pluginServer;
    private String userDatabase;
    private String userDbConn;
    private String pluginDatabase;
    private String pluginDbConn;
    private String project;
    private String password;
    private String certificatePassword;
    private String remoteCertificatePassword;
    private String keystorePassword;
    private String keystoreServer;
    private String keystoreDatabase;
    private String keystoreDbConn;
    private String credentialsType;
    private String timeZone;
    private String webUrl;
    private String permissions;
    private String eol;
    private String keystorePath;
    private String certificateAlias;
    private String certificatePath;
    private List<StringVarStorage> folders;
    private List<StringVarStorage> pathsToExclude;
    private boolean canJobDelete;
    private boolean canJobForce;
    private boolean canJobRevert;
    private boolean canJobExpand;
    private boolean canJobNoMetadata;
    private boolean canJobNoTouch;
    private boolean canJobUpdate;
    private boolean secureAgentAuth;


    @DataBoundConstructor
    public DimensionsStep(String project, String credentialsType) {
        this.project = Values.textOrElse(project, null);
        this.credentialsType = Values.textOrElse(credentialsType, null);
    }

    @DataBoundSetter
    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    @DataBoundSetter
    public void setUserName(String userName) {
        this.userName = userName;
    }

    @DataBoundSetter
    public void setUserServer(String userServer) {
        this.userServer = userServer;
    }

    @DataBoundSetter
    public void setPluginServer(String pluginServer) {
        this.pluginServer = pluginServer;
    }

    @DataBoundSetter
    public void setUserDatabase(String userDatabase) {
        this.userDatabase = userDatabase;
    }

    @DataBoundSetter
    public void setUserDbConn(String userDbConn) {
        this.userDbConn = userDbConn;
    }

    @DataBoundSetter
    public void setPluginDatabase(String pluginDatabase) {
        this.pluginDatabase = pluginDatabase;
    }

    @DataBoundSetter
    public void setPluginDbConn(String pluginDbConn) {
        this.pluginDbConn = pluginDbConn;
    }

    @DataBoundSetter
    public void setPassword(String password) {
        this.password = password;
    }

    @DataBoundSetter
    public void setCertificatePassword(String certificatePassword) {
        this.certificatePassword = certificatePassword;
    }

    @DataBoundSetter
    public void setRemoteCertificatePassword(String remoteCertificatePassword) {
        this.remoteCertificatePassword = remoteCertificatePassword;
    }

    @DataBoundSetter
    public void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    @DataBoundSetter
    public void setKeystoreServer(String keystoreServer) {
        this.keystoreServer = keystoreServer;
    }

    @DataBoundSetter
    public void setKeystoreDatabase(String keystoreDatabase) {
        this.keystoreDatabase = keystoreDatabase;
    }

    @DataBoundSetter
    public void setKeystoreDbConn(String keystoreDbConn) {
        this.keystoreDbConn = keystoreDbConn;
    }

    @DataBoundSetter
    public void setKeystorePath(String keystorePath) {
        this.keystorePath = keystorePath;
    }

    @DataBoundSetter
    public void setCertificateAlias(String certificateAlias) {
        this.certificateAlias = certificateAlias;
    }

    @DataBoundSetter
    public void setCertificatePath(String certificatePath) {
        this.certificatePath = certificatePath;
    }

    @DataBoundSetter
    public void setSecureAgentAuth(boolean secureAgentAuth) {
        this.secureAgentAuth = secureAgentAuth;
    }

    @DataBoundSetter
    public void setTimeZone(String timeZone) {
        this.timeZone = Values.textOrElse(timeZone, null);
    }

    @DataBoundSetter
    public void setWebUrl(String webUrl) {
        this.webUrl = Values.textOrElse(webUrl, null);
    }

    @DataBoundSetter
    public void setFolders(List<StringVarStorage> folders) {
        this.folders = Values.notBlankOrElseList(folders, null);
    }

    @DataBoundSetter
    public void setCanJobRevert(boolean canJobRevert) {
        this.canJobRevert = canJobRevert;
    }

    @DataBoundSetter
    public void setPathsToExclude(List<StringVarStorage> pathsToExclude) {
        this.pathsToExclude = Values.notBlankOrElseList(pathsToExclude, null);
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
    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    @DataBoundSetter
    public void setEol(String eol) {
        this.eol = eol;
    }

    @DataBoundSetter
    public void setCanJobNoMetadata(boolean canJobNoMetadata) {
        this.canJobNoMetadata = canJobNoMetadata;
    }

    @DataBoundSetter
    public void setCanJobExpand(boolean canJobExpand) {
        this.canJobExpand = canJobExpand;
    }

    @DataBoundSetter
    public void setCanJobNoTouch(boolean canJobNoTouch) {
        this.canJobNoTouch = canJobNoTouch;
    }

    @DataBoundSetter
    public void setCanJobUpdate(boolean canJobUpdate) {
        this.canJobUpdate = canJobUpdate;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public String getProject() {
        return project;
    }

    public List<StringVarStorage> getFolders() {
        return folders;
    }

    public List<StringVarStorage> getPathsToExclude() {
        return pathsToExclude;
    }

    public boolean isCanJobDelete() {
        return canJobDelete;
    }

    public boolean isCanJobForce() {
        return canJobForce;
    }

    public boolean isCanJobRevert() {
        return canJobRevert;
    }

    public String getPermissions() {
        return permissions;
    }

    public String getEol() {
        return eol;
    }

    public boolean isCanJobExpand() {
        return canJobExpand;
    }

    public boolean isCanJobNoMetadata() {
        return canJobNoMetadata;
    }

    public boolean isCanJobNoTouch() {
        return canJobNoTouch;
    }

    public boolean isCanJobUpdate() {
        return canJobUpdate;
    }

    public String getCredentialsId() {
        return Credentials.isPluginDefined(credentialsType) ? credentialsId : null;
    }

    public String getUserName() {
        return Credentials.isUserDefined(credentialsType) ? userName : null;
    }

    public String getUserServer() {
        return Credentials.isUserDefined(credentialsType) ? userServer : null;
    }

    public String getPluginServer() {
        return Credentials.isPluginDefined(credentialsType) ? pluginServer : null;
    }

    public String getUserDatabase() {
        return Credentials.isUserDefined(credentialsType) ? userDatabase : null;
    }

    public String getUserDbConn() {
        return Credentials.isUserDefined(credentialsType) ? userDbConn : null;
    }

    public String getPluginDatabase() {
        return Credentials.isPluginDefined(credentialsType) ? pluginDatabase : null;
    }

    public String getPluginDbConn() {
        return Credentials.isPluginDefined(credentialsType) ? pluginDbConn : null;
    }

    public String getPassword() {
        return password != null && Credentials.isUserDefined(credentialsType) ? Secret.fromString(password).getEncryptedValue() : null;
    }

    public String getCredentialsType() {
        return credentialsType;
    }

    public String getCertificatePassword() {
        return certificatePassword != null && Credentials.isKeystoreDefined(credentialsType) ? Secret.fromString(certificatePassword).getEncryptedValue() : null;
    }

    public String getRemoteCertificatePassword() {
        return Credentials.isKeystoreDefined(credentialsType) ? remoteCertificatePassword : null;
    }

    public String getCertificatePath() {
        return Credentials.isKeystoreDefined(credentialsType) ? certificatePath : null;
    }

    public boolean isSecureAgentAuth() {
        return Credentials.isKeystoreDefined(credentialsType) && secureAgentAuth;
    }

    public String getKeystorePassword() {
        return keystorePassword != null && Credentials.isKeystoreDefined(credentialsType) ? Secret.fromString(keystorePassword).getEncryptedValue() : null;
    }

    public String getKeystoreServer() {
        return Credentials.isKeystoreDefined(credentialsType) ? keystoreServer : null;
    }

    public String getKeystoreDatabase() {
        return Credentials.isKeystoreDefined(credentialsType) ? keystoreDatabase : null;
    }

    public String getKeystoreDbConn() {
        return Credentials.isKeystoreDefined(credentialsType) ? keystoreDbConn : null;
    }

    public String getKeystorePath() {
        return Credentials.isKeystoreDefined(credentialsType) ? keystorePath : null;
    }

    public String getCertificateAlias() {
        return Credentials.isKeystoreDefined(credentialsType) ? certificateAlias : null;
    }

    @NonNull
    @Override
    protected SCM createSCM() {
        DimensionsSCM scm = new DimensionsSCM(project, credentialsType, userName, password, pluginServer,
                userServer, keystoreServer, pluginDatabase, pluginDbConn, userDatabase, userDbConn, keystoreDatabase, keystoreDbConn, keystorePath, certificateAlias, credentialsId, certificatePassword, keystorePassword,
                certificatePath, remoteCertificatePassword, secureAgentAuth);
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
                                                  @QueryParameter("dimensionsscm.userDbConn") final String jobDbConnUser,
                                                  @QueryParameter("dimensionsscm.pluginServer") final String jobServerPlugin,
                                                  @QueryParameter("dimensionsscm.pluginDatabase") final String jobDatabasePlugin,
                                                  @QueryParameter("dimensionsscm.pluginDbConn") final String jobDbConnPlugin,
                                                  @AncestorInPath final Item item) {
            return delegate.doCheckServerConfig(req, rsp, credentialsId, credentialsType, jobuser, jobPasswd, jobServerUser,
                    jobDatabaseUser, jobDbConnUser, jobServerPlugin, jobDatabasePlugin, jobDbConnPlugin, item);
        }

        @RequirePOST
        public FormValidation doCheckServerKeystore(StaplerRequest req, StaplerResponse rsp,
                                                    @QueryParameter("dimensionsscm.keystorePath") final String keystorePath,
                                                    @QueryParameter("dimensionsscm.keystorePassword") final String keystorePassword,
                                                    @QueryParameter("dimensionsscm.keystoreServer") final String keystoreServer,
                                                    @QueryParameter("dimensionsscm.keystoreDatabase") final String keystoreDatabase,
                                                    @QueryParameter("dimensionsscm.keystoreDbConn") final String keystoreDbConn,
                                                    @QueryParameter("dimensionsscm.certificatePassword") final String certificatePassword,
                                                    @QueryParameter("dimensionsscm.certificateAlias") final String certificateAlias,
                                                    @AncestorInPath final Item item) {
            return delegate.doCheckServerKeystore(req, rsp, keystorePath, keystorePassword, keystoreServer, keystoreDatabase,
                    keystoreDbConn, certificatePassword, certificateAlias, item);
        }
    }
}
