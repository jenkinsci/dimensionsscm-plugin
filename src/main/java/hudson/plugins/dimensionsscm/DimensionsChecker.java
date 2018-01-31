package hudson.plugins.dimensionsscm;

import hudson.model.Project;
import hudson.model.Run;
import hudson.model.TaskListener;

/**
 * Utility method for checking the consistency of a build's configuration with respect to the plugin.
 */
final class DimensionsChecker {
    private DimensionsChecker() {
        /* prevent instantiation. */
    }

    /**
     * Checks if all the plugins that need to be loaded are loaded.
     */
    static boolean isValidPluginCombination(Run build, TaskListener listener) {
        if (build.getParent() instanceof Project) {
            Project buildProject = (Project) build.getParent();
            if (!(buildProject.getScm() instanceof DimensionsSCM)) {
                Logger.debug("Not using a DimensionsSCM engine - bye");
                return false;
            }

            DimensionsBuildWrapper bwplugin = (DimensionsBuildWrapper) buildProject.getBuildWrappers().get(
                    DimensionsBuildWrapper.DMWBLD_DESCRIPTOR);
            DimensionsBuildNotifier bnplugin = (DimensionsBuildNotifier) buildProject.getPublishersList().get(
                    DimensionsBuildNotifier.class);
            ArtifactUploader anplugin = (ArtifactUploader) buildProject.getPublishersList().get(
                    ArtifactUploader.class);

            if (bwplugin != null) {
                Logger.debug("DimensionsBuildWrapper is activated");
            }
            if (bnplugin != null) {
                Logger.debug("DimensionsBuildNotifier is activated");
            }
            if (anplugin != null) {
                Logger.debug("ArtifactUploader is activated");
            }

            // Tagging plugin needs lock plugin.
            if (bnplugin != null && bwplugin == null) {
                listener.fatalError("\n[DIMENSIONS] Tags can only be created when the 'Lock Dimensions project while the build is in progress' option is enabled.");
                return false;
            }

            // Uploader plugin can work with the others at the moment but in the future it may not be able to, so let
            // it be for now, but have code to kill it if needed.
            // - The lock is released before the notifier plugins kick in.
            // - Baseline plugin kicks in after the uploader plugin.
            //if (anplugin != null && (bwplugin != null || bnplugin != null) {
            //    return false;
            //}
        }
        return true;
    }
}
