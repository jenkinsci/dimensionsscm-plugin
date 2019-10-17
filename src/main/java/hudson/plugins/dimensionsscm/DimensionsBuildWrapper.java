package hudson.plugins.dimensionsscm;

import com.serena.dmclient.api.DimensionsResult;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import java.io.IOException;
import java.util.Map;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * A BuildWrapper to lock an SCM project (not stream) for the duration of the execution of a Jenkins build.
 */
public class DimensionsBuildWrapper extends BuildWrapper {
    private DimensionsSCM scm;

    /**
     * Descriptor should be singleton.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public Descriptor<BuildWrapper> getDescriptor() {
        return DMWBLD_DESCRIPTOR;
    }

    @Extension
    public static final DescriptorImpl DMWBLD_DESCRIPTOR = new DescriptorImpl();

    @DataBoundConstructor
    public DimensionsBuildWrapper() {
    }

    /**
     * Default environment setup.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public Environment setUp(@SuppressWarnings("rawtypes") final AbstractBuild build, Launcher launcher, final BuildListener listener)
            throws IOException {
        long key = -1L;
        if (build.getProject().getScm() instanceof DimensionsSCM) {
            Logger.debug("Invoking build setup callout " + this.getClass().getName());
            if (scm == null) {
                scm = (DimensionsSCM) build.getProject().getScm();
            }
            Logger.debug("Dimensions user is " + scm.getJobUserName() + " , Dimensions installation is "
                    + scm.getJobServer());
            try {
                key = scm.getAPI().login(scm, build);
                if (key > 0L) {
                    DimensionsResult res = scm.getAPI().lockProject(key, scm.getProjectName(build, listener));
                    if (res == null) {
                        listener.getLogger().println("[DIMENSIONS] Locking the project in Dimensions failed");
                        build.setResult(Result.FAILURE);
                        listener.getLogger().flush();
                    } else {
                        listener.getLogger().println("[DIMENSIONS] Dimensions project was successfully locked");
                        listener.getLogger().flush();
                    }
                }
            } catch (Exception e) {
                String message = Values.exceptionMessage("Unable to lock Dimensions project", e, "no message");
                listener.fatalError(message);
                Logger.debug(message, e);
            } finally {
                scm.getAPI().logout(key, build);
            }
        } else {
            String message = "[DIMENSIONS] This plugin only works with a Dimensions SCM engine";
            listener.fatalError(message);
            build.setResult(Result.FAILURE);
            throw new IOException(message);
        }
        return new EnvironmentImpl(build);
    }

    /**
     * Implementation class for Dimensions plugin.
     */
    public static final class DescriptorImpl extends BuildWrapperDescriptor {
        /**
         * Loads the descriptor.
         */
        public DescriptorImpl() {
            super(DimensionsBuildWrapper.class);
            load();
            Logger.debug("Loading " + this.getClass().getName());
        }

        @Override
        public String getDisplayName() {
            return "Lock Dimensions project while the build is in progress";
        }

        /**
         * This builder can be used with all project types.
         * <p>
         * {@inheritDoc}
         */
        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }

        /**
         * Save the descriptor configuration.
         * <p>
         * {@inheritDoc}
         */
        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) {
            req.bindParameters(this, "DimensionsBuildWrapper");
            save();
            return true;
        }

        /**
         * Get help file.
         * <p>
         * {@inheritDoc}
         */
        @Override
        public String getHelpFile() {
            return "/plugin/dimensionsscm/helpbwrapper.html";
        }
    }

    /**
     * Implementation class for Dimensions environment plugin.
     */
    class EnvironmentImpl extends Environment {
        final AbstractBuild<?, ?> elbuild;

        EnvironmentImpl(AbstractBuild<?, ?> build) {
            this.elbuild = build;
        }

        /**
         * Build environment.
         * <p>
         * {@inheritDoc}
         */
        @Override
        public void buildEnvVars(Map<String, String> env) {
        }

        /**
         * Post build step - always called.
         * <p>
         * {@inheritDoc}
         */
        @Override
        public boolean tearDown(@SuppressWarnings("rawtypes") AbstractBuild build, BuildListener listener) {
            long key = -1L;
            if (scm != null) {
                Logger.debug("Invoking build tearDown callout " + this.getClass().getName());
                Logger.debug("Dimensions user is " + scm.getJobUserName() + " , Dimensions installation is "
                        + scm.getJobServer());
                try {
                    key = scm.getAPI().login(scm, build);
                    if (key > 0L) {
                        Logger.debug("Unlocking the project");
                        DimensionsResult res = scm.getAPI().unlockProject(key, scm.getProjectName(build, listener));
                        if (res == null) {
                            listener.getLogger().println("[DIMENSIONS] Unlocking the project in Dimensions failed");
                            build.setResult(Result.FAILURE);
                            listener.getLogger().flush();
                        } else {
                            listener.getLogger().println("[DIMENSIONS] Dimensions project was successfully unlocked");
                            listener.getLogger().flush();
                        }
                    } else {
                        listener.fatalError("[DIMENSIONS] Login to Dimensions failed.");
                        build.setResult(Result.FAILURE);
                        return false;
                    }
                } catch (Exception e) {
                    String message = Values.exceptionMessage("Unable to unlock Dimensions project", e, "no message");
                    listener.fatalError(message);
                    Logger.debug(message, e);
                    build.setResult(Result.FAILURE);
                    return false;
                } finally {
                    scm.getAPI().logout(key, build);
                }
            }
            return true;
        }
    }
}
