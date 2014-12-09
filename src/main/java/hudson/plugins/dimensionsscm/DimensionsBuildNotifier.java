/*
 * ===========================================================================
 *  Copyright (c) 2007, 2014 Serena Software. All rights reserved.
 *
 *  Use of the Sample Code provided by Serena is governed by the following
 *  terms and conditions. By using the Sample Code, you agree to be bound by
 *  the terms contained herein. If you do not agree to the terms herein, do
 *  not install, copy, or use the Sample Code.
 *
 *  1.  GRANT OF LICENSE.  Subject to the terms and conditions herein, you
 *  shall have the nonexclusive, nontransferable right to use the Sample Code
 *  for the sole purpose of developing applications for use solely with the
 *  Serena software product(s) that you have licensed separately from Serena.
 *  Such applications shall be for your internal use only.  You further agree
 *  that you will not: (a) sell, market, or distribute any copies of the
 *  Sample Code or any derivatives or components thereof; (b) use the Sample
 *  Code or any derivatives thereof for any commercial purpose; or (c) assign
 *  or transfer rights to the Sample Code or any derivatives thereof.
 *
 *  2.  DISCLAIMER OF WARRANTIES.  TO THE MAXIMUM EXTENT PERMITTED BY
 *  APPLICABLE LAW, SERENA PROVIDES THE SAMPLE CODE AS IS AND WITH ALL
 *  FAULTS, AND HEREBY DISCLAIMS ALL WARRANTIES AND CONDITIONS, EITHER
 *  EXPRESSED, IMPLIED OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY
 *  IMPLIED WARRANTIES OR CONDITIONS OF MERCHANTABILITY, OF FITNESS FOR A
 *  PARTICULAR PURPOSE, OF LACK OF VIRUSES, OF RESULTS, AND OF LACK OF
 *  NEGLIGENCE OR LACK OF WORKMANLIKE EFFORT, CONDITION OF TITLE, QUIET
 *  ENJOYMENT, OR NON-INFRINGEMENT.  THE ENTIRE RISK AS TO THE QUALITY OF
 *  OR ARISING OUT OF USE OR PERFORMANCE OF THE SAMPLE CODE, IF ANY,
 *  REMAINS WITH YOU.
 *
 *  3.  EXCLUSION OF DAMAGES.  TO THE MAXIMUM EXTENT PERMITTED BY APPLICABLE
 *  LAW, YOU AGREE THAT IN CONSIDERATION FOR RECEIVING THE SAMPLE CODE AT NO
 *  CHARGE TO YOU, SERENA SHALL NOT BE LIABLE FOR ANY DAMAGES WHATSOEVER,
 *  INCLUDING BUT NOT LIMITED TO DIRECT, SPECIAL, INCIDENTAL, INDIRECT, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, DAMAGES FOR LOSS OF
 *  PROFITS OR CONFIDENTIAL OR OTHER INFORMATION, FOR BUSINESS INTERRUPTION,
 *  FOR PERSONAL INJURY, FOR LOSS OF PRIVACY, FOR NEGLIGENCE, AND FOR ANY
 *  OTHER LOSS WHATSOEVER) ARISING OUT OF OR IN ANY WAY RELATED TO THE USE
 *  OF OR INABILITY TO USE THE SAMPLE CODE, EVEN IN THE EVENT OF THE FAULT,
 *  TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY, OR BREACH OF CONTRACT,
 *  EVEN IF SERENA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.  THE
 *  FOREGOING LIMITATIONS, EXCLUSIONS AND DISCLAIMERS SHALL APPLY TO THE
 *  MAXIMUM EXTENT PERMITTED BY APPLICABLE LAW.  NOTWITHSTANDING THE ABOVE,
 *  IN NO EVENT SHALL SERENA'S LIABILITY UNDER THIS AGREEMENT OR WITH RESPECT
 *  TO YOUR USE OF THE SAMPLE CODE AND DERIVATIVES THEREOF EXCEED US$10.00.
 *
 *  4.  INDEMNIFICATION. You hereby agree to defend, indemnify and hold
 *  harmless Serena from and against any and all liability, loss or claim
 *  arising from this agreement or from (i) your license of, use of or
 *  reliance upon the Sample Code or any related documentation or materials,
 *  or (ii) your development, use or reliance upon any application or
 *  derivative work created from the Sample Code.
 *
 *  5.  TERMINATION OF THE LICENSE.  This agreement and the underlying
 *  license granted hereby shall terminate if and when your license to the
 *  applicable Serena software product terminates or if you breach any terms
 *  and conditions of this agreement.
 *
 *  6.  CONFIDENTIALITY.  The Sample Code and all information relating to the
 *  Sample Code (collectively "Confidential Information") are the
 *  confidential information of Serena.  You agree to maintain the
 *  Confidential Information in strict confidence for Serena.  You agree not
 *  to disclose or duplicate, nor allow to be disclosed or duplicated, any
 *  Confidential Information, in whole or in part, except as permitted in
 *  this Agreement.  You shall take all reasonable steps necessary to ensure
 *  that the Confidential Information is not made available or disclosed by
 *  you or by your employees to any other person, firm, or corporation.  You
 *  agree that all authorized persons having access to the Confidential
 *  Information shall observe and perform under this nondisclosure covenant.
 *  You agree to immediately notify Serena of any unauthorized access to or
 *  possession of the Confidential Information.
 *
 *  7.  AFFILIATES.  Serena as used herein shall refer to Serena Software,
 *  Inc. and its affiliates.  An entity shall be considered to be an
 *  affiliate of Serena if it is an entity that controls, is controlled by,
 *  or is under common control with Serena.
 *
 *  8.  GENERAL.  Title and full ownership rights to the Sample Code,
 *  including any derivative works shall remain with Serena.  If a court of
 *  competent jurisdiction holds any provision of this agreement illegal or
 *  otherwise unenforceable, that provision shall be severed and the
 *  remainder of the agreement shall remain in full force and effect.
 * ===========================================================================
 */
package hudson.plugins.dimensionsscm;

import com.serena.dmclient.api.DimensionsResult;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Descriptor.FormException;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.VariableResolver;
import java.io.IOException;
import java.io.Serializable;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

/**
 * This experimental plugin extends Jenkins/Hudson support for Dimensions SCM
 * repositories.
 *
 * @author Tim Payne
 */
public class DimensionsBuildNotifier extends Notifier implements Serializable {
    private DimensionsSCM scm;
    private boolean canBaselineDeploy;
    private boolean canBaselineAction;

    private final String actionState;
    private final String deployState;

    private final String blnScope;
    private final String blnName;
    private final String blnTemplate;
    private final String blnOwningPart;
    private final String blnType;

    private boolean canBaselineBuild;

    private final String area;
    private final String buildConfig;
    private final String buildOptions;
    private final String buildTargets;

    private final boolean batch;
    private final boolean buildClean;
    private final boolean capture;

    /**
     * Gets the baseline part spec.
     */
    public String getBlnOwningPart() {
        return this.blnOwningPart;
    }

    /**
     * Gets the baseline type name.
     */
    public String getBlnType() {
        return this.blnType;
    }

    /**
     * Gets the baseline template name.
     */
    public String getBlnTemplate() {
        return this.blnTemplate;
    }

    /**
     * Gets the baseline name.
     */
    public String getBlnName() {
        return this.blnName;
    }

    /**
     * Gets the baseline scope.
     */
    public String getBlnScope() {
        return this.blnScope;
    }

    /**
     * Gets the build flag.
     */
    public boolean isCanBaselineBuild() {
        return this.canBaselineBuild;
    }

    /**
     * Gets the action flag.
     */
    public boolean isCanBaselineAction() {
        return this.canBaselineAction;
    }

    /**
     * Gets the deploy flag.
     */
    public boolean isCanBaselineDeploy() {
        return this.canBaselineDeploy;
    }

    /**
     * Gets the batch flag.
     */
    public boolean isBatch() {
        return this.batch;
    }

    /**
     * Gets the buildClean flag.
     */
    public boolean isBuildClean() {
        return this.buildClean;
    }

    /**
     * Gets the capture flag.
     */
    public boolean isCapture() {
        return this.capture;
    }

    /**
     * Gets the action state name.
     */
    public String getActionState() {
        return this.actionState;
    }

    /**
     * Gets the deploy state name.
     */
    public String getDeployState() {
        return this.deployState;
    }

    /**
     * Gets the area name.
     */
    public String getArea() {
        return this.area;
    }

    /**
     * Gets the build config name.
     */
    public String getBuildConfig() {
        return this.buildConfig;
    }

    /**
     * Gets the build options string.
     */
    public String getBuildOptions() {
        return this.buildOptions;
    }

    /**
     * Gets the build target name.
     */
    public String getBuildTargets() {
        return this.buildTargets;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    public DimensionsBuildNotifier(boolean canDeploy, String deployState, boolean canAction, String actionState,
            boolean canBuild, String area, String buildConfig, String buildOptions, String buildTargets,
            String blnScope, String blnTemplate, String blnOwningPart, String blnType, String blnName, boolean batch,
            boolean buildClean, boolean capture) {
        this.canBaselineDeploy = canDeploy;
        this.canBaselineAction = canAction;
        this.canBaselineBuild = canBuild;
        this.actionState = actionState;
        this.deployState = deployState;

        this.area = area;
        this.buildConfig = buildConfig;
        this.buildOptions = buildOptions;
        this.buildTargets = buildTargets;
        this.blnScope = blnScope;
        this.blnTemplate = blnTemplate;
        this.blnOwningPart = blnOwningPart;
        this.blnType = blnType;
        this.blnName = blnName;

        this.batch = batch;
        this.buildClean = buildClean;
        this.capture = capture;
    }

    /**
     * Run this one last.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public boolean needsToRunAfterFinalized() {
        return true;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws IOException, InterruptedException {
        Logger.debug("Invoking perform callout " + this.getClass().getName());
        long key = -1L;
        try {
            if (!(build.getProject().getScm() instanceof DimensionsSCM)) {
                listener.fatalError("[DIMENSIONS] This plugin only works with the Dimensions SCM engine.");
                build.setResult(Result.FAILURE);
                throw new IOException("[DIMENSIONS] This plugin only works with a Dimensions SCM engine");
            }
            if (build.getResult() == Result.SUCCESS) {
                if (scm == null) {
                    scm = (DimensionsSCM) build.getProject().getScm();
                }
                Logger.debug("Dimensions user is " + scm.getJobUserName() + " , Dimensions installation is " +
                        scm.getJobServer());
                key = scm.getAPI().login(scm.getJobUserName(), scm.getJobPasswd(), scm.getJobDatabase(),
                        scm.getJobServer(), build);
                if (key > 0L) {
                    VariableResolver<String> myResolver = build.getBuildVariableResolver();
                    String requests = myResolver.resolve("DM_TARGET_REQUEST");
                    String blnId = myResolver.resolve("DM_BASELINE");
                    StringBuffer cblId = new StringBuffer();

                    if (requests != null) {
                        requests = requests.replaceAll(" ", "");
                        requests = requests.toUpperCase(Values.ROOT_LOCALE);
                    }

                    if (blnScope != null && blnScope.length() > 0 && blnScope.equals("REVISED")) {
                        if (requests == null || blnId == null ||
                            requests.length() == 0 ||
                            blnId.length() == 0) {
                            listener.fatalError("[DIMENSIONS] A revised baseline is only valid if you have specified DM_TARGET_REQUEST and DM_BASELINE.");
                            build.setResult(Result.FAILURE);
                            return false;
                        }
                    }

                    {
                        DimensionsResult res = scm.getAPI().createBaseline(key, scm.getProjectVersion(build), build,
                                blnScope, blnTemplate, blnOwningPart, blnType, requests, blnId, blnName, cblId);
                        if (res == null) {
                            listener.getLogger().println("[DIMENSIONS] The build failed to be tagged in Dimensions");
                            listener.getLogger().flush();
                            build.setResult(Result.FAILURE);
                            canBaselineDeploy = canBaselineAction = canBaselineBuild = false;
                        } else {
                            listener.getLogger().println("[DIMENSIONS] Build was successfully tagged in Dimensions as a baseline");
                            listener.getLogger().println("[DIMENSIONS] (" + res.getMessage().replaceAll("\n", "\n[DIMENSIONS] ") + ")");
                            listener.getLogger().flush();
                        }
                    }
                    if (canBaselineDeploy) {
                        listener.getLogger().println("[DIMENSIONS] Submitting a deployment job to Dimensions...");
                        listener.getLogger().flush();
                        DimensionsResult res = scm.getAPI().deployBaseline(key, scm.getProjectName(build), build,
                                deployState, cblId.toString());
                        if (res == null) {
                            listener.getLogger().println("[DIMENSIONS] The build baseline failed to be deployed in Dimensions");
                            listener.getLogger().flush();
                            build.setResult(Result.FAILURE);
                            canBaselineDeploy = canBaselineAction = canBaselineBuild = false;
                        } else {
                            listener.getLogger().println("[DIMENSIONS] Build baseline was successfully deployed in Dimensions");
                            listener.getLogger().println("[DIMENSIONS] (" + res.getMessage().replaceAll("\n",
                                    "\n[DIMENSIONS] ") + ")");
                            listener.getLogger().flush();
                        }
                    }

                    // This will active the build baseline functionality
                    if (canBaselineBuild) {
                        listener.getLogger().println("[DIMENSIONS] Submitting a build job to Dimensions...");
                        listener.getLogger().flush();
                        DimensionsResult res = scm.getAPI().buildBaseline(key, area, scm.getProjectName(build), batch,
                                buildClean, buildConfig, buildOptions, capture, requests, buildTargets, build,
                                cblId.toString());
                        if (res == null) {
                            listener.getLogger().println("[DIMENSIONS] The build baseline failed to be built in Dimensions");
                            listener.getLogger().flush();
                            build.setResult(Result.FAILURE);
                            canBaselineDeploy = canBaselineAction = canBaselineBuild = false;
                        } else {
                            listener.getLogger().println("[DIMENSIONS] Build baseline was successfully built in Dimensions");
                            listener.getLogger().println("[DIMENSIONS] (" + res.getMessage().replaceAll("\n",
                                    "\n[DIMENSIONS] ") + ")");
                            listener.getLogger().flush();
                        }
                    }

                    if (canBaselineAction) {
                        listener.getLogger().println("[DIMENSIONS] Actioning the build baseline in Dimensions...");
                        listener.getLogger().flush();
                        DimensionsResult res = scm.getAPI().actionBaseline(key, scm.getProjectName(build), build,
                                actionState, cblId.toString());
                        if (res == null) {
                            listener.getLogger().println("[DIMENSIONS] The build baseline failed to be actioned in Dimensions");
                            build.setResult(Result.FAILURE);
                            listener.getLogger().flush();
                        } else {
                            listener.getLogger().println("[DIMENSIONS] Build baseline was successfully actioned in Dimensions");
                            listener.getLogger().println("[DIMENSIONS] (" + res.getMessage().replaceAll("\n",
                                    "\n[DIMENSIONS] ") + ")");
                            listener.getLogger().flush();
                        }
                    }
                } else {
                    listener.fatalError("[DIMENSIONS] Login to Dimensions failed.");
                    build.setResult(Result.FAILURE);
                    return false;
                }
            }
        } catch (Exception e) {
            listener.fatalError("Unable to tag build in Dimensions - " + e.getMessage());
            build.setResult(Result.FAILURE);
            return false;
        } finally {
            if (scm != null) {
                scm.getAPI().logout(key, build);
            }
        }
        return true;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        // see Descriptor javadoc for more about what a descriptor is.
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * The DimensionsBuildNotifier Descriptor class.
     */
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        /*
         * Loads the descriptor
         */
        public DescriptorImpl() {
            super(DimensionsBuildNotifier.class);
            load();
            Logger.debug("Loading " + this.getClass().getName());
        }

        @Override
        public String getDisplayName() {
            return "Tag successful builds in Dimensions as a baseline";
        }

        @Override
        public Notifier newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            // Get variables and then construct a new object.
            boolean canDeploy = Values.booleanOrElse(req.getParameter("dimensionsbuildnotifier.canBaselineDeploy"), false);
            boolean canBuild = Values.booleanOrElse(req.getParameter("dimensionsbuildnotifier.canBaselineBuild"), false);
            boolean canAction = Values.booleanOrElse(req.getParameter("dimensionsbuildnotifier.canBaselineAction"), false);
            boolean batch = Values.booleanOrElse(req.getParameter("dimensionsbuildnotifier.batch"), false);
            boolean buildClean = Values.booleanOrElse(req.getParameter("dimensionsbuildnotifier.buildClean"), false);
            boolean capture = Values.booleanOrElse(req.getParameter("dimensionsbuildnotifier.capture"), false);
            String deploy = Values.textOrElse(req.getParameter("dimensionsbuildnotifier.deployState"), null);
            String action = Values.textOrElse(req.getParameter("dimensionsbuildnotifier.actionState"), null);
            String area = Values.textOrElse(req.getParameter("dimensionsbuildnotifier.area"), null);
            String buildConfig = Values.textOrElse(req.getParameter("dimensionsbuildnotifier.buildConfig"), null);
            String buildOptions = Values.textOrElse(req.getParameter("dimensionsbuildnotifier.buildOptions"), null);
            String buildTargets = Values.textOrElse(req.getParameter("dimensionsbuildnotifier.buildTargets"), null);
            String blnScope = Values.textOrElse(req.getParameter("dimensionsbuildnotifier.blnScope"), null);
            String blnTemplate = Values.textOrElse(req.getParameter("dimensionsbuildnotifier.blnTemplate"), null);
            String blnOwningPart = Values.textOrElse(req.getParameter("dimensionsbuildnotifier.blnOwningPart"), null);
            String blnType = Values.textOrElse(req.getParameter("dimensionsbuildnotifier.blnType"), null);
            String blnName = Values.textOrElse(req.getParameter("dimensionsbuildnotifier.blnName"), null);
            DimensionsBuildNotifier notif = new DimensionsBuildNotifier(canDeploy, deploy, canAction, action, canBuild,
                    area, buildConfig, buildOptions, buildTargets, blnScope, blnTemplate, blnOwningPart, blnType,
                    blnName, batch, buildClean, capture);

            return notif;
        }

        /**
         * This builder can be used with all project types.
         * <p>
         * {@inheritDoc}
         */
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        /**
         * Save the descriptor configuration.
         * <p>
         * {@inheritDoc}
         */
        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // Get the values and check them.
            String deploy = Values.textOrElse(req.getParameter("dimensionsbuildnotifier.deployState"), null);
            String action = Values.textOrElse(req.getParameter("dimensionsbuildnotifier.actionState"), null);
            this.save();
            return super.configure(req, formData);
        }

        /**
         * Get help file.
         * <p>
         * {@inheritDoc}
         */
        @Override
        public String getHelpFile() {
            return "/plugin/dimensionsscm/helpbnotifier.html";
        }
    }
}
