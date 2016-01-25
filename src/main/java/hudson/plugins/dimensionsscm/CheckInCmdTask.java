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
import java.io.PrintWriter;

/**
 * Deliver files to Dimensions CM repository using dmcli command-line.
 */
public class CheckInCmdTask extends GenericCmdTask {
    private final boolean forceCheckIn;
    private final boolean forceTip;
    private final boolean isStream;

    private final int buildNo;
    private final String jobId;

    private final String projectId;
    private String requests;
    private final String owningPart;

    private final String patternType;
    private final String[] patterns;
    private final String[] patternsExc;

    /**
     * Utility routine to create command file for dmcli.
     */
    private File createCmdFile(final File area, final File userFilelist) throws IOException {
        PrintWriter fmtWriter = null;
        File tmpFile = null;

        try {
            tmpFile = File.createTempFile("dmCm" + Long.toString(System.currentTimeMillis()), null, null);
            // 'DELIVER' command file in platform-default encoding.
            fmtWriter = new PrintWriter(new FileWriter(tmpFile), true);

            String ciCmd = "DELIVER /BRIEF /ADD /UPDATE /DELETE ";
            if (version == 10 || !isStream) {
                ciCmd = "UPLOAD ";
            }
            ciCmd += " /USER_FILELIST=\"" + userFilelist.getAbsolutePath() + "\"";
            ciCmd += " /WORKSET=\"" + projectId + "\"";
            ciCmd += " /COMMENT=\"Build artifacts delivered by Jenkins for job '" + jobId + "' - build "
                    + buildNo + "\"";
            ciCmd += " /USER_DIRECTORY=\"" + area.getAbsolutePath() + "\"";
            if (requests != null && requests.length() > 0) {
                if (requests.indexOf(',') == -1) {
                    ciCmd += "/CHANGE_DOC_IDS=(\"" + requests + "\") ";
                } else {
                    ciCmd += "/CHANGE_DOC_IDS=(" + requests + ") ";
                }
            }
            if (owningPart != null && owningPart.length() > 0) {
                ciCmd += "/PART=\"" + owningPart + "\"";
            }
            if (!isStream) {
                if (forceCheckIn) {
                    ciCmd += "/FORCE_CHECKIN ";
                }
                if (forceTip) {
                    ciCmd += "/FORCE_TIP ";
                }
            }

            fmtWriter.println(ciCmd);
            fmtWriter.flush();
        } catch (IOException e) {
            throw (IOException) new IOException(Values.exceptionMessage("Unable to write DELIVER command file: " + tmpFile, e,
                    "no message")).initCause(e);
        } finally {
            if (fmtWriter != null) {
                fmtWriter.close();
            }
        }
        return tmpFile;
    }

    public CheckInCmdTask(String userName, String passwd, String database, String server, String projectId,
            String requestId, boolean forceCheckIn, boolean forceTip, String[] patterns, String patternType,
            int version, boolean isStream, int buildNo, String jobId, String owningPart, FilePath workspace,
            TaskListener listener, String[] patternsExc) {
        super(userName, passwd, database, server, version, workspace, listener);
        this.isStream = isStream;

        // Config details.
        this.projectId = projectId;
        this.forceCheckIn = forceCheckIn;
        this.forceTip = forceTip;
        this.patterns = patterns;
        this.patternsExc = patternsExc;
        this.patternType = patternType;
        this.requests = requestId;
        this.buildNo = buildNo;
        this.jobId = jobId;
        this.owningPart = owningPart;
    }

    /**
     * Process the checkin.
     */
    @Override
    public Boolean execute(final File exe, final File param, final File area) throws IOException {
        FilePath wa = new FilePath(area);
        boolean bRet = true;
        try {
            listener.getLogger().println("[DIMENSIONS] Scanning workspace for files to be saved into Dimensions...");
            listener.getLogger().flush();

            FilePath wd = new FilePath(area);
            File dir = new File(wd.getRemote());
            File[] validFiles = new File[0];

            if (patternType.equals("regEx")) {
                listener.getLogger().println("[DIMENSIONS] Running RegEx pattern scanner...");
                FileScanner fs = new FileScanner(dir, patterns, patternsExc, -1);
                validFiles = fs.toArray();
                listener.getLogger().println("[DIMENSIONS] Found " + validFiles.length + " file(s) to check in...");
            } else if (patternType.equals("Ant")) {
                listener.getLogger().println("[DIMENSIONS] Running Ant pattern scanner...");
                FileAntScanner fs = new FileAntScanner(dir, patterns, patternsExc, -1);
                validFiles = fs.toArray();
                listener.getLogger().println("[DIMENSIONS] Found " + validFiles.length + " file(s) to check in...");
            }

            listener.getLogger().flush();

            String cmdLog = null;

            if (validFiles.length > 0) {
                if (requests != null) {
                    requests = requests.replaceAll(" ", "");
                    requests = requests.toUpperCase(Values.ROOT_LOCALE);
                }

                File tmpFile = null;
                PrintWriter fmtWriter = null;

                try {
                    tmpFile = File.createTempFile("dmCm" + Long.toString(System.currentTimeMillis()), null, null);
                    // 'DELIVER/USER_FILELIST=' user filelist in platform-default encoding.
                    fmtWriter = new PrintWriter(new FileWriter(tmpFile), true);

                    for (File f : validFiles) {
                        if (f.isDirectory()) {
                        } else {
                            fmtWriter.println(f.getAbsolutePath());
                        }
                    }
                    fmtWriter.flush();
                } catch (IOException e) {
                    throw (IOException) new IOException(Values.exceptionMessage("Unable to write user filelist: " + tmpFile, e,
                            "no message")).initCause(e);
                } finally {
                    if (fmtWriter != null) {
                        fmtWriter.close();
                    }
                }

                File cmdFile = createCmdFile(area, tmpFile);
                if (cmdFile == null) {
                    listener.getLogger().println("[DIMENSIONS] Error: Cannot create DELIVER command file.");
                    param.delete();
                    tmpFile.delete();
                    return false;
                }

                listener.getLogger().println("[DIMENSIONS] Loading files into Dimensions project \""
                        + projectId + "\"...");
                listener.getLogger().flush();

                /* Execute a Dimensions command */
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

                if (cmdLog == null) {
                    cmdLog = "\n";
                }
                cmdLog += outputStr;
                cmdLog += "\n";
            } else {
                listener.getLogger().println("[DIMENSIONS] No build artifacts found for checking in");
            }

            listener.getLogger().flush();

            param.delete();

            if (cmdLog != null && cmdLog.length() > 0 && listener.getLogger() != null) {
                listener.getLogger().println("[DIMENSIONS] (Note: Dimensions command output was - ");
                cmdLog = cmdLog.replaceAll("\n\n", "\n");
                listener.getLogger().println(cmdLog.replaceAll("\n", "\n[DIMENSIONS] ") + ")");
                listener.getLogger().flush();
            }

            if (!bRet) {
                listener.getLogger().println("[DIMENSIONS] ==========================================================");
                listener.getLogger().println("[DIMENSIONS] The Dimensions checkin command returned a failure status.");
                listener.getLogger().println("[DIMENSIONS] Please review the command output and correct any issues");
                listener.getLogger().println("[DIMENSIONS] that may have been detected.");
                listener.getLogger().println("[DIMENSIONS] ==========================================================");
                listener.getLogger().flush();
            }
            return bRet;
        } catch (Exception e) {
            param.delete();
            String message = Values.exceptionMessage("Unable to run checkin callout", e, "no message - try again");
            listener.fatalError(message);
            Logger.debug(message, e);
            bRet = false;
        }
        return bRet;
    }
}
