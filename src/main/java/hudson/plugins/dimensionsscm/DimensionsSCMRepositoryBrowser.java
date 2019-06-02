package hudson.plugins.dimensionsscm;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.scm.RepositoryBrowser;
import java.io.IOException;
import java.net.URL;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Dummy repository browser.
 */
@ExportedBean(defaultVisibility = 999)
public class DimensionsSCMRepositoryBrowser extends RepositoryBrowser<DimensionsChangeSet> {
    @DataBoundConstructor
    public DimensionsSCMRepositoryBrowser() {
    }

    public URL getDiffLink(DimensionsChangeSet.DmFiles item) throws java.net.MalformedURLException {
        return new URL("http://alink.com/link.html");
    }

    @Override
    public URL getChangeSetLink(DimensionsChangeSet item) {
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
