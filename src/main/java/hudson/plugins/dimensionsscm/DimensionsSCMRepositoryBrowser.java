package hudson.plugins.dimensionsscm;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.scm.RepositoryBrowser;
import java.net.MalformedURLException;
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

    public URL getDiffLink(DimensionsChangeLogEntry.FileChange fileChange) throws MalformedURLException {
        return null;
    }

    public URL getFileLink(DimensionsChangeLogEntry.FileChange fileChange) {
        if (fileChange != null) {
            String url = fileChange.getUrl();
            if (url != null && !url.isEmpty() && !url.equals("null")) {
                try {
                    return new URL(url);
                } catch (MalformedURLException mue) {
                    // Ignore the URL if it is invalid.
                }
            }
        }
        return null;
    }

    public URL getRequestLink(DimensionsChangeLogEntry.IRTRequest request) {
        if (request != null) {
            String url = request.getUrl();
            if (url != null && !url.isEmpty() && !url.equals("null")) {
                try {
                    return new URL(url);
                } catch (MalformedURLException mue) {
                    // Ignore the URL if it is invalid.
                }
            }            
        }
        return null;
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
