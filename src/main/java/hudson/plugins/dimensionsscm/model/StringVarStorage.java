package hudson.plugins.dimensionsscm.model;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;

public class StringVarStorage  extends AbstractDescribableImpl<StringVarStorage> implements Serializable {

    private String strVar;

    @DataBoundConstructor
    public StringVarStorage(String strVar){
        this.strVar = strVar;
    }

    public String getStrVar() {
        return strVar;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<StringVarStorage> {
        @Override
        public String getDisplayName() {
            return "";
        }
    }
}
