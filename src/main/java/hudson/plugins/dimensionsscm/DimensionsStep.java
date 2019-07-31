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
    private String jobUserName;
    private String jobServerUser;
    private String jobServerPlugin;
    private String jobDatabaseUser;
    private String jobDatabasePlugin;
    private String project;
    private String jobPasswd;
    private String credentialsType;
    private String jobTimeZone;
    private String jobWebUrl;
    private String permissions;
    private String eol;
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
    public DimensionsStep() {
    }


    public String getJobTimeZone() {
        return jobTimeZone;
    }

    @DataBoundSetter
    public void setJobTimeZone(String jobTimeZone) {
        this.jobTimeZone = Values.textOrElse(jobTimeZone, null);
    }

    public String getJobWebUrl() {
        return jobWebUrl;
    }

    @DataBoundSetter
    public void setJobWebUrl(String jobWebUrl) {
        this.jobWebUrl = Values.textOrElse(jobWebUrl, null);
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    @DataBoundSetter
    public void setCredentialsId(String credentialsId) {
        this.credentialsId = Values.textOrElse(credentialsId, null);
    }

    public String getJobUserName() {
        return jobUserName;
    }

    @DataBoundSetter
    public void setJobUserName(String jobUserName) {
        this.jobUserName = Values.textOrElse(jobUserName, null);
    }

    public String getJobServerUser() {
        return jobServerUser;
    }

    @DataBoundSetter
    public void setJobServerUser(String jobServerUser) {
        this.jobServerUser = Values.textOrElse(jobServerUser, null);
    }

    public String getJobServerPlugin() {
        return jobServerPlugin;
    }

    @DataBoundSetter
    public void setJobServerPlugin(String jobServerPlugin) {
        this.jobServerPlugin = Values.textOrElse(jobServerPlugin, null);
    }

    public String getJobDatabaseUser() {
        return jobDatabaseUser;
    }

    @DataBoundSetter
    public void setJobDatabaseUser(String jobDatabaseUser) {
        this.jobDatabaseUser = Values.textOrElse(jobDatabaseUser, null);
    }

    public String getJobDatabasePlugin() {
        return jobDatabasePlugin;
    }

    @DataBoundSetter
    public void setJobDatabasePlugin(String jobDatabasePlugin) {
        this.jobDatabasePlugin = Values.textOrElse(jobDatabasePlugin, null);
    }

    public String getProject() {
        return project;
    }

    @DataBoundSetter
    public void setProject(String project) {
        this.project = Values.textOrElse(project, null);
    }

    public String getJobPasswd() {
        return jobPasswd != null ? Secret.fromString(jobPasswd).getEncryptedValue() : null;
    }

    @DataBoundSetter
    public void setJobPasswd(String jobPasswd) {
        this.jobPasswd = Values.textOrElse(jobPasswd, null);
    }

    public String getCredentialsType() {
        return credentialsType;
    }

    @DataBoundSetter
    public void setCredentialsType(String credentialsType) {
        this.credentialsType = Values.textOrElse(credentialsType, null);
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
        this.pathsToExclude =Values.notBlankOrElseList(pathsToExclude, null);
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

    @Nonnull
    @Override
    protected SCM createSCM() {
        DimensionsSCM scm = new DimensionsSCM(project, credentialsType, jobUserName, jobPasswd, jobServerUser,
                jobServerPlugin, jobDatabaseUser, jobDatabasePlugin, credentialsId, canJobUpdate);
        scm.setJobTimeZone(jobTimeZone);
        scm.setJobWebUrl(jobWebUrl);
        scm.setCanJobDelete(canJobDelete);
        scm.setCanJobForce(canJobForce);
        scm.setCanJobRevert(canJobRevert);
        scm.setFolders(folders);
        scm.setPathsToExclude(pathsToExclude);
        scm.setEol(eol);
        scm.setPermissions(permissions);
        scm.setCanJobExpand(canJobExpand);
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
                                        @QueryParameter("dimensionsscm.timeZone") final String timezone,
                                        @QueryParameter("dimensionsscm.jobTimeZone") final String jobtimezone) {
            return delegate.docheckTz(req, rsp, timezone, jobtimezone);
        }

        @RequirePOST
        public FormValidation doCheckServerGlobal(StaplerRequest req, StaplerResponse rsp,
                                                  @QueryParameter("credentialsId") final String credentialsId,
                                                  @QueryParameter("credentialsType") final String credentialsType,
                                                  @QueryParameter("dimensionsscm.userName") final String user,
                                                  @QueryParameter("dimensionsscm.passwd") final String passwd,
                                                  @QueryParameter("dimensionsscm.server") final String server,
                                                  @QueryParameter("dimensionsscm.database") final String database,
                                                  @AncestorInPath final Item item) {
            return delegate.doCheckServerGlobal(req, rsp, credentialsId, credentialsType, user, passwd, server, database, item);
        }

        @RequirePOST
        public FormValidation doCheckServerConfig(StaplerRequest req, StaplerResponse rsp,
                                                  @QueryParameter("credentialsId") final String credentialsId,
                                                  @QueryParameter("credentialsType") final String credentialsType,
                                                  @QueryParameter("dimensionsscm.jobUserName") final String jobuser,
                                                  @QueryParameter("dimensionsscm.jobPasswd") final String jobPasswd,
                                                  @QueryParameter("dimensionsscm.jobServerUser") final String jobServerUser,
                                                  @QueryParameter("dimensionsscm.jobDatabaseUser") final String jobDatabaseUser,
                                                  @QueryParameter("dimensionsscm.jobServerPlugin") final String jobServerPlugin,
                                                  @QueryParameter("dimensionsscm.jobDatabasePlugin") final String jobDatabasePlugin,
                                                  @AncestorInPath final Item item) {
            return delegate.doCheckServerConfig(req, rsp, credentialsId, credentialsType, jobuser, jobPasswd, jobServerUser,
                    jobDatabaseUser, jobServerPlugin, jobDatabasePlugin, item);
        }

    }
}
