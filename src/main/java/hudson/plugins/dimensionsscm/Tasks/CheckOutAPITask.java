
/* ===========================================================================
 *  Copyright (c) 2007 Serena Software. All rights reserved.
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

/*
 * This experimental plugin extends Hudson support for Dimensions SCM repositories
 *
 * @author Tim Payne
 *
 */

// Package name
package hudson.plugins.dimensionsscm;

// Dimensions imports
import hudson.plugins.dimensionsscm.DimensionsAPI;
import hudson.plugins.dimensionsscm.DimensionsSCM;
import hudson.plugins.dimensionsscm.DimensionsSCMRepositoryBrowser;
import hudson.plugins.dimensionsscm.Logger;
import hudson.plugins.dimensionsscm.DimensionsChangeLogParser;


// Hudson imports
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.Hudson;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.ModelObject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.ChangeLogParser;
import hudson.scm.RepositoryBrowsers;
import hudson.scm.SCM;
import hudson.scm.SCMDescriptor;
import hudson.util.Scrambler;
import hudson.util.VariableResolver;
import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.model.Node;
import hudson.model.Computer;
import hudson.model.Hudson.MasterComputer;
import hudson.remoting.Callable;
import hudson.remoting.DelegatingCallable;
import hudson.remoting.Channel;
import hudson.remoting.VirtualChannel;

// General imports
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

/*
 * Main Checkout
 */

/**
 * Class implementation of the checkout process.
 */
public class CheckOutAPITask extends GenericAPITask implements FileCallable<Boolean> {

    boolean bFreshBuild = false;
    boolean isDelete = false;
    boolean isRevert = false;
    boolean isForce = false;
    boolean isExpand = false;

    VariableResolver<String> myResolver;

    String workarea = "";
    String projectId = "";
    String[] folders;

    int version = 0;

    /*
     * Default constructor
     */
    public CheckOutAPITask(AbstractBuild<?,?> build, DimensionsSCM parent,
                        FilePath workspace, TaskListener listener, int version) {

        Logger.Debug("Creating task - " + this.getClass().getName());

        this.workspace = workspace;
        this.listener = listener;
        this.version = version;

        // Server details
        userName = parent.getJobUserName();
        passwd = parent.getJobPasswd();
        database = parent.getJobDatabase();
        server = parent.getJobServer();

        // Config details
        this.workarea = parent.getWorkarea();
        this.isDelete = parent.isCanJobDelete();
        this.projectId = parent.getProject();
        this.isRevert = parent.isCanJobRevert();
        this.isForce = parent.isCanJobForce();
        this.isExpand = parent.isCanJobExpand();

        this.folders = parent.getFolders();
        permissions = parent.getPermissions();

        // Build details
        this.bFreshBuild = (build.getPreviousBuild() == null);
        this.myResolver = build.getBuildVariableResolver();
    }

    /*
     * Execute method
     *
     * @param File
     * @param VirtualChannel
     * @return boolean
     * @throws IOException
     */
    public Boolean execute(File area, VirtualChannel channel) throws IOException {

        boolean bRet = true;

        try
        {
            StringBuffer cmdOutput = new StringBuffer();
            FilePath wa = null;
            if (workarea != null)
            {
                File waN = new File(workarea);
                wa = new FilePath(waN);
            }
            else
                wa = new FilePath(area);

            // Emulate SVN plugin
            // - if workspace exists and it is not managed by this project, blow it away
            //
            if (bFreshBuild) {
                if (listener.getLogger() != null) {
                    listener.getLogger().println("[DIMENSIONS] Checking out a fresh workspace because this project has not been built before...");
                    listener.getLogger().flush();
                }
            }

            if (wa.exists() && (isDelete || bFreshBuild)) {
                Logger.Debug("Deleting '" + wa.toURI() + "'...");
                listener.getLogger().println("[DIMENSIONS] Removing '" + wa.toURI() + "'...");
                listener.getLogger().flush();
                wa.deleteContents();
            }

            String baseline = myResolver.resolve("DM_BASELINE");
            String requests = myResolver.resolve("DM_REQUEST");

            if (baseline != null) {
                baseline = baseline.trim();
                baseline = baseline.toUpperCase();
            }
            if (requests != null) {
                requests = requests.replaceAll(" ","");
                requests = requests.toUpperCase();
            }

            Logger.Debug("Extra parameters - " + baseline + " " + requests);

            String cmdLog = null;

            if (baseline != null && baseline.length() == 0)
                baseline = null;
            if (requests != null && requests.length() == 0)
                requests = null;

            if (listener.getLogger() != null) {
                if (requests != null)
                    listener.getLogger().println("[DIMENSIONS] Checking out request(s) \"" + requests + "\" - ignoring project folders...");
                else if (baseline != null)
                    listener.getLogger().println("[DIMENSIONS] Checking out baseline \"" + baseline + "\"...");
                else
                    listener.getLogger().println("[DIMENSIONS] Checking out project \"" + projectId + "\"...");
                listener.getLogger().flush();
            }

            if (version == 10 && requests != null) {
                String[] requestsProcess = requests.split(",");
                if (requestsProcess.length == 0) {
                    requestsProcess[0] = requests;
                }

                listener.getLogger().println("[DIMENSIONS] Defaulting to read-only permissions as this is the only available mode...");

                for (int xx=0;xx<requestsProcess.length; xx++) {
                    if (!bRet)
                        break;

                    String folderN = "/";
                    File fileName = new File(folderN);
                    FilePath dname = new FilePath(fileName);
                    String reqId = requestsProcess[xx];

                    Logger.Debug("Checking out '" + folderN + "'...");

                    // Checkout the folder
                    bRet = dmSCM.checkout(key,projectId,dname,wa,
                                          cmdOutput,baseline,reqId,
                                          isRevert,isExpand,"DEFAULT");
                    Logger.Debug("SCM checkout returned " + bRet);

                    if (!bRet && isForce)
                        bRet = true;

                    if (cmdLog==null)
                        cmdLog = "\n";

                    cmdLog += cmdOutput;
                    cmdOutput.setLength(0);
                    cmdLog += "\n";
                }
            } else {

                // Iterate through the project folders and process them in Dimensions
                for (int ii=0;ii<folders.length; ii++) {
                    if (!bRet)
                        break;

                    String folderN = folders[ii];
                    File fileName = new File(folderN);
                    FilePath dname = new FilePath(fileName);

                    Logger.Debug("Checking out '" + folderN + "'...");

                    // Checkout the folder
                    bRet = dmSCM.checkout(key,projectId,dname,wa,
                                          cmdOutput,baseline,requests,
                                          isRevert,isExpand,permissions);
                    Logger.Debug("SCM checkout returned " + bRet);

                    if (!bRet && isForce)
                        bRet = true;

                    if (cmdLog==null)
                        cmdLog = "\n";

                    cmdLog += cmdOutput;
                    cmdOutput.setLength(0);
                    cmdLog += "\n";
                    if (requests != null)
                        break;
                }
            }

            if (cmdLog.length() > 0 && listener.getLogger() != null) {
                Logger.Debug("Found command output to log to the build logger");
                listener.getLogger().println("[DIMENSIONS] (Note: Dimensions command output was - ");
                cmdLog = cmdLog.replaceAll("\n\n","\n");
                listener.getLogger().println(cmdLog.replaceAll("\n","\n[DIMENSIONS] ") + ")");
                listener.getLogger().flush();
            }

            if (!bRet) {
                listener.getLogger().println("[DIMENSIONS] ==========================================================");
                listener.getLogger().println("[DIMENSIONS] The Dimensions checkout command returned a failure status.");
                listener.getLogger().println("[DIMENSIONS] Please review the command output and correct any issues");
                listener.getLogger().println("[DIMENSIONS] that may have been detected.");
                listener.getLogger().println("[DIMENSIONS] ==========================================================");
                listener.getLogger().flush();
            }
        }
        catch(Exception e)
        {
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


