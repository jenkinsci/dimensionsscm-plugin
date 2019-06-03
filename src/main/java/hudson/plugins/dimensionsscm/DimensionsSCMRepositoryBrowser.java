package hudson.plugins.dimensionsscm;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.scm.RepositoryBrowser;
import java.net.URL;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Dummy repository browser.
 */
@ExportedBean(defaultVisibility = 999)
public class DimensionsSCMRepositoryBrowser extends RepositoryBrowser<DimensionsChangeLogEntry> {
    @DataBoundConstructor
    public DimensionsSCMRepositoryBrowser() {
    }

    public URL getDiffLink(DimensionsChangeLogEntry.FileChange fileChange) throws java.net.MalformedURLException {
        return new URL("http://alink.com/link.html");
    }

    @Override
    public URL getChangeSetLink(DimensionsChangeLogEntry entry) {
        return null;
    }

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    @Override
    public DescriptorImpl getDescriptor() {
        return DESCRIPTOR;
    }

    /**
     * Implementation class for Dimensions plugin.
     */
    public static class DescriptorImpl extends Descriptor<RepositoryBrowser<?>> {
        public DescriptorImpl() {
            super(DimensionsSCMRepositoryBrowser.class);
        }

        @Override
        public String getDisplayName() {
            return "Dimensions";
        }
    }
}
