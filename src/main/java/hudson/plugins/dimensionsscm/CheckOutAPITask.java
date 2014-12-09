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

import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.util.VariableResolver;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * This experimental plugin extends Jenkins/Hudson support for Dimensions SCM
 * repositories. Class implementation of the checkout process.
 *
 * @author Tim Payne
 */
public class CheckOutAPITask extends GenericAPITask implements FileCallable<Boolean> {
    private final boolean bFreshBuild;
    private final boolean isDelete;
    private final boolean isRevert;
    private final boolean isForce;
    private final boolean isExpand;
    private final boolean isNoMetadata;
    private final boolean isNoTouch;

    private final VariableResolver<String> myResolver;

    private final String projectId;
    private final String[] folders;
    private final String permissions;
    private final String eol;

    private final int version;

    public CheckOutAPITask(AbstractBuild<?, ?> build, DimensionsSCM parent, FilePath workspace, TaskListener listener,
            int version) {
        super(parent, workspace, listener);
        Logger.debug("Creating task - " + this.getClass().getName());

        // Server details (see superclass).
        this.version = version;

        // Config details.
        this.isDelete = parent.isCanJobDelete();
        this.projectId = parent.getProjectVersion(build);
        this.isRevert = parent.isCanJobRevert();
        this.isForce = parent.isCanJobForce();
        this.isExpand = parent.isCanJobExpand();
        this.isNoMetadata = parent.isCanJobNoMetadata();
        this.isNoTouch = parent.isCanJobNoTouch();

        this.folders = parent.getFolders();
        this.permissions = parent.getPermissions();
        this.eol = parent.getEol();

        // Build details.
        this.bFreshBuild = (build.getPreviousBuild() == null);
        this.myResolver = build.getBuildVariableResolver();
    }

    @Override
    public Boolean execute(File area, VirtualChannel channel) throws IOException {
        boolean bRet = true;
        try {
            StringBuffer cmdOutput = new StringBuffer();
            FilePath wa = new FilePath(area);

            // Emulate SVN plugin - if workspace exists and it is not managed by this project, blow it away.
            if (bFreshBuild) {
                if (listener.getLogger() != null) {
                    listener.getLogger().println("[DIMENSIONS] Checking out a fresh workspace because this project has not been built before...");
                    listener.getLogger().flush();
                }
            }

            if (wa.exists() && (isDelete || bFreshBuild)) {
                Logger.debug("Deleting '" + wa.toURI() + "'...");
                listener.getLogger().println("[DIMENSIONS] Removing '" + wa.toURI() + "'...");
                listener.getLogger().flush();
                wa.deleteContents();
            }

            String baseline = myResolver.resolve("DM_BASELINE");
            String requests = myResolver.resolve("DM_REQUEST");

            if (baseline != null) {
                baseline = baseline.trim();
                baseline = baseline.toUpperCase(Values.ROOT_LOCALE);
            }
            if (requests != null) {
                requests = requests.replaceAll(" ", "");
                requests = requests.toUpperCase(Values.ROOT_LOCALE);
            }

            Logger.debug("Extra parameters - " + baseline + " " + requests);

            String cmdLog = null;

            if (baseline != null && baseline.length() == 0) {
                baseline = null;
            }
            if (requests != null && requests.length() == 0) {
                requests = null;
            }
            if (listener.getLogger() != null) {
                if (requests != null) {
                    listener.getLogger().println("[DIMENSIONS] Checking out request(s) \"" + requests + "\" - ignoring project folders...");
                } else if (baseline != null) {
                    listener.getLogger().println("[DIMENSIONS] Checking out baseline \"" + baseline + "\"...");
                } else {
                    listener.getLogger().println("[DIMENSIONS] Checking out project \"" + projectId + "\"...");
                }
                listener.getLogger().flush();
            }

            if (version == 10 && requests != null) {
                String[] requestsProcess = requests.split(",");
                if (requestsProcess.length == 0) {
                    requestsProcess[0] = requests;
                }

                listener.getLogger().println("[DIMENSIONS] Defaulting to read-only permissions as this is the only available mode...");

                for (String reqId : requestsProcess) {
                    if (!bRet) {
                        break;
                    }
                    String folderN = "/";
                    File fileName = new File(folderN);
                    FilePath dname = new FilePath(fileName);

                    Logger.debug("Checking out '" + folderN + "'...");

                    // Checkout the folder.
                    bRet = dmSCM.checkout(key, projectId, dname, wa, cmdOutput, baseline, reqId, isRevert, isExpand,
                            isNoMetadata, isNoTouch, "DEFAULT", eol);
                    Logger.debug("SCM checkout returned " + bRet);

                    if (!bRet && isForce) {
                        bRet = true;
                    }
                    if (cmdLog == null) {
                        cmdLog = "\n";
                    }
                    cmdLog += cmdOutput;
                    cmdOutput.setLength(0);
                    cmdLog += "\n";
                }
            } else {
                // Iterate through the project folders and process them in Dimensions.
                for (String folderN : folders) {
                    if (!bRet) {
                        break;
                    }
                    File fileName = new File(folderN);
                    FilePath dname = new FilePath(fileName);

                    Logger.debug("Checking out '" + folderN + "'...");

                    // Checkout the folder.
                    bRet = dmSCM.checkout(key, projectId, dname, wa, cmdOutput, baseline, requests, isRevert, isExpand,
                            isNoMetadata, isNoTouch, permissions, eol);
                    Logger.debug("SCM checkout returned " + bRet);

                    if (!bRet && isForce) {
                        bRet = true;
                    }
                    if (cmdLog == null) {
                        cmdLog = "\n";
                    }
                    cmdLog += cmdOutput;
                    cmdOutput.setLength(0);
                    cmdLog += "\n";
                    if (requests != null) {
                        break;
                    }
                }
            }

            PrintStream log = listener.getLogger();
            if (!Values.isNullOrEmpty(cmdLog) && log != null) {
                Logger.debug("Found command output to log to the build logger");
                log.println("[DIMENSIONS] (Note: Dimensions command output was - ");
                cmdLog = cmdLog.replaceAll("\n\n", "\n");
                log.println(cmdLog.replaceAll("\n", "\n[DIMENSIONS] ") + ")");
                log.flush();
            }

            if (!bRet && log != null) {
                log.println("[DIMENSIONS] ==========================================================");
                log.println("[DIMENSIONS] The Dimensions checkout command returned a failure status.");
                log.println("[DIMENSIONS] Please review the command output and correct any issues");
                log.println("[DIMENSIONS] that may have been detected.");
                log.println("[DIMENSIONS] ==========================================================");
                log.flush();
            }
        } catch (Exception e) {
            String errMsg = e.getMessage();
            if (errMsg == null) {
                errMsg = "An unknown error occurred. Please try the operation again.";
            }
            listener.fatalError("Unable to run checkout callout - " + errMsg);
            // e.printStackTrace();
            //throw new IOException("Unable to run checkout callout - " + e.getMessage());
            bRet = false;
        }
        return bRet;
    }
}
