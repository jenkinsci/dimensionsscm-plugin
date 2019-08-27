package hudson.plugins.dimensionsscm.model;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;

public class StringVarStorage extends AbstractDescribableImpl<StringVarStorage> implements Serializable {

    private String strVar;

    @DataBoundConstructor
    public StringVarStorage(String value) {
        this.strVar = value;
    }

    public String getValue() {
        return strVar;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof StringVarStorage)) {
            return false;
        }

        StringVarStorage varStorage = (StringVarStorage) obj;

        if (this.strVar == null && varStorage.getValue() == null) {
            return true;
        } else if (this.strVar == null || varStorage.getValue() == null) {
            return false;
        }

        return varStorage.getValue().equalsIgnoreCase(this.strVar);
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<StringVarStorage> {
        @Override
        public String getDisplayName() {
            return "";
        }
    }
}
