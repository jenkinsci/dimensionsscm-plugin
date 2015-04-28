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
import hudson.model.TaskListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Update local work area using dmcli command-line.
 * The Jenkins Dimensions Plugin provides support for Dimensions CM SCM repositories.
 * @author Tim Payne
 */
public class CheckOutCmdTask extends GenericCmdTask {
    private final boolean bFreshBuild;
    private final boolean isDelete;
    private final boolean isRevert;
    private final boolean isForce;
    private final boolean isExpand;
    private final boolean isNoMetadata;
    private final boolean isNoTouch;

    private final String projectId;
    private String baseline;
    private String requests;
    private final String permissions;
    private final String eol;

    private final String[] folders;

    /**
     * Utility routine to create command file for dmcli.
     */
    private File createCmdFile(final String reqId, final String projDir, final File area) throws IOException {
        PrintWriter fmtWriter = null;
        File tmpFile = null;

        try {
            tmpFile = File.createTempFile("dmCm" + Long.toString(System.currentTimeMillis()), null, null);
            // 'UPDATE' command file in platform-default encoding.
            fmtWriter = new PrintWriter(new FileWriter(tmpFile), true);

            String coCmd = "UPDATE /BRIEF ";
            if (version == 10) {
                coCmd = "DOWNLOAD ";
                if (requests != null) {
                    coCmd = "FCDI ";
                }
            }

            if (version == 2010) {
                coCmd += " /LEGACY_MODE ";
            }

            String cmd = coCmd;

            if (reqId != null && version == 10) {
                cmd += reqId;
            }

            if (projDir != null && !projDir.equals("\\") && !projDir.equals("/") && requests == null) {
                cmd += "/DIR=\"" + projDir + "\"";
            }

            if (requests != null && version != 10) {
                if (requests.indexOf(',') == -1) {
                    cmd += "/CHANGE_DOC_IDS=(\"" + requests + "\") ";
                } else {
                    cmd += "/CHANGE_DOC_IDS=(" + requests + ") ";
                }
                cmd += "/WORKSET=\"" + projectId + "\" ";
            }
            else if (baseline != null) {
                cmd += "/BASELINE=\"" + baseline + "\"";
            } else {
                cmd += "/WORKSET=\"" + projectId + "\" ";
            }

            cmd += "/USER_DIR=\"" + area.getAbsolutePath() + "\" ";

            if (isRevert) {
                cmd += " /OVERWRITE";
            }
            if (isExpand) {
                cmd += " /EXPAND";
            }
            if (isNoMetadata) {
                cmd += " /NOMETADATA";
            }
            if (isNoTouch) {
                cmd += " /NOTOUCH";
            }

            if (permissions != null && permissions.length() > 0) {
                if (!permissions.equals("DEFAULT") && reqId == null) {
                    cmd += "/PERMS=" + permissions;
                }
            }

            if (eol != null && eol.length() > 0) {
                if (!eol.equals("DEFAULT")) {
                    cmd += "/EOL=" + eol;
                }
            }

            fmtWriter.println(cmd);
            fmtWriter.flush();
        } catch (IOException e) {
            throw (IOException) new IOException(Values.exceptionMessage("Unable to write UPDATE command file: " + tmpFile, e,
                    "no message")).initCause(e);
        } finally {
            if (fmtWriter != null) {
                fmtWriter.close();
            }
        }
        return tmpFile;
    }

    public CheckOutCmdTask(String userName, String passwd, String database, String server, String projectId,
            String baselineId, String requestId, boolean isDelete, boolean isRevert, boolean isForce, boolean isExpand,
            boolean isNoMetadata, boolean isNoTouch, boolean freshBuild, String[] folders, int version,
            String permissions, String eol, FilePath workspace, TaskListener listener) {
        super(userName, passwd, database, server, version, workspace, listener);

        // Config details.
        this.isDelete = isDelete;
        this.projectId = projectId;
        this.isRevert = isRevert;
        this.isForce = isForce;
        this.isExpand = isExpand;
        this.isNoMetadata = isNoMetadata;
	this.isNoTouch = isNoTouch;
        this.folders = folders;
        this.requests = requestId;
        this.baseline = baselineId;
        this.permissions = permissions;
        this.eol = eol;

        // Build details.
        this.bFreshBuild = freshBuild;
    }

    /**
     * Process the checkout.
     */
    @Override
    public Boolean execute(final File exe, final File param, final File area) throws IOException {
        FilePath wa = new FilePath(area);
        boolean bRet = true;

        // Emulate SVN plugin - if workspace exists and it is not managed by this project, blow it away.
        try {
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

            if (baseline != null) {
                baseline = baseline.trim();
                baseline = baseline.toUpperCase(Values.ROOT_LOCALE);
            }
            if (requests != null) {
                requests = requests.replaceAll(" ", "");
                requests = requests.toUpperCase(Values.ROOT_LOCALE);
            }

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
                    FilePath projectDir = new FilePath(fileName);
                    String projDir = projectDir.getRemote();

                    File cmdFile = createCmdFile(reqId, projDir, area);
                    if (cmdFile == null) {
                        listener.getLogger().println("[DIMENSIONS] Error: Cannot create UPDATE command file.");
                        param.delete();
                        return false;
                    }

                    String[] cmd = new String[5];
                    cmd[0] = exe.getAbsolutePath();
                    cmd[1] = "-param";
                    cmd[2] = param.getAbsolutePath();
                    cmd[3] = "-file";
                    cmd[4] = cmdFile.getAbsolutePath();

                    SCMLauncher proc = new SCMLauncher(cmd, listener, wa);
                    bRet = proc.execute();
                    String outputStr = proc.getResults();
                    cmdFile.delete();

                    if (bRet) {
                        // Check if any conflicts were identified.
                        int confl = outputStr.indexOf("C\t");
                        if (confl > 0) {
                            bRet = false;
                        }
                    }

                    if (cmdLog == null) {
                        cmdLog = "\n";
                    }
                    cmdLog += outputStr;
                    cmdLog += "\n";

                    if (!bRet && isForce) {
                        bRet = true;
                    }
                }
            } else {
                // Iterate through the project folders and process them in Dimensions.
                for (String folderN : folders) {
                    if (!bRet) {
                        break;
                    }
                    File fileName = new File(folderN);
                    FilePath projectDir = new FilePath(fileName);
                    String projDir = projectDir.getRemote();

                    File cmdFile = createCmdFile(null, projDir, area);
                    if (cmdFile == null) {
                        listener.getLogger().println("[DIMENSIONS] Error: Cannot create UPDATE command file.");
                        param.delete();
                        return false;
                    }

                    if (requests == null) {
                        listener.getLogger().println("[DIMENSIONS] Checking out directory '" +
                                (projDir != null ? projDir : "/") + "'...");
                        listener.getLogger().flush();
                    }

                    String[] cmd = new String[5];
                    cmd[0] = exe.getAbsolutePath();
                    cmd[1] = "-param";
                    cmd[2] = param.getAbsolutePath();
                    cmd[3] = "-file";
                    cmd[4] = cmdFile.getAbsolutePath();

                    SCMLauncher proc = new SCMLauncher(cmd, listener, wa);
                    bRet = proc.execute();
                    String outputStr = proc.getResults();
                    cmdFile.delete();

                    if (bRet) {
                        // Check if any conflicts were identified.
                        int confl = outputStr.indexOf("C\t");
                        if (confl > 0) {
                            bRet = false;
                        }
                    }

                    if (cmdLog == null) {
                        cmdLog = "\n";
                    }
                    cmdLog += outputStr;
                    cmdLog += "\n";

                    if (!bRet && isForce) {
                        bRet = true;
                    }
                    if (requests != null) {
                        break;
                    }
                }
            }
            param.delete();

            PrintStream log = listener.getLogger();
            if (!Values.isNullOrEmpty(cmdLog) && log != null) {
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
            return bRet;
        } catch (Exception e) {
            param.delete();
            listener.fatalError(Values.exceptionMessage("Unable to run checkout callout", e, "no message - try again"));
            return false;
        }
    }
}
