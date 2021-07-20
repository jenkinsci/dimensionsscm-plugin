package hudson.plugins.dimensionsscm;

import hudson.Extension;
import hudson.model.AdministrativeMonitor;
import hudson.security.Permission;
import hudson.util.HttpResponses;
import java.io.IOException;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

/**
 * Show an alert if the API JAR files are missing.
 */
@Extension
public class MissingJarsAdministrativeMonitor extends AdministrativeMonitor {
    @Override
    public boolean isActivated() {
        try {
            Class.forName("com.serena.dmclient.api.DimensionsRuntimeException");
            return false;
        } catch (ClassNotFoundException e) {
            return true;
        }
    }

    @Override
    public String getDisplayName() {
        return "Dimensions Plugin Incompletely Installed Warning";
    }

    //@Override
    public Permission getRequiredPermission() {
        return Jenkins.SYSTEM_READ;
    }

    /**
     * Depending on whether the user said "yes" or "no", send to the right place.
     */
    @RequirePOST
    public HttpResponse doAct(@QueryParameter String no) throws IOException {
        if (no != null) { // dismiss
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            disable(true);
            return HttpResponses.forwardToPreviousPage();
        } else {
            return new HttpRedirect("https://github.com/jenkinsci/dimensionsscm-plugin/blob/master/docs/user-guide.md#installation");
        }
    }}