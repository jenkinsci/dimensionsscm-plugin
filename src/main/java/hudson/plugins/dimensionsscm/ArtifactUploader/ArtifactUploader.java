
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

/**
 ** @brief This experimental plugin extends Hudson support for Dimensions SCM repositories
 **
 ** @author Tim Payne
 **
 **/

package hudson.plugins.dimensionsscm;

// Dimensions imports
import hudson.plugins.dimensionsscm.DimensionsAPI;
import hudson.plugins.dimensionsscm.DimensionsSCM;
import hudson.plugins.dimensionsscm.Logger;
import com.serena.dmclient.api.DimensionsResult;

// Hudson imports
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Descriptor.FormException;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import hudson.Util;
import hudson.FilePath;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

// General imports
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.*;
import java.util.Calendar;

import javax.servlet.ServletException;

public class ArtifactUploader extends Notifier implements Serializable {

    /**
     * File pattern matcher class.
     */
    public class ArtifactFilter implements FilenameFilter {
        private TreeSet<String> artifactFilter = new TreeSet<String>() ;

        public ArtifactFilter(String[] extensions) {
          Iterator<String> artifactList = Arrays.asList(extensions).iterator();
          while (artifactList.hasNext()) {
            artifactFilter.add(artifactList.next().trim());
          }
          artifactFilter.remove("");
        }

        public boolean accept(File dir, String name) {
          final Iterator<String> artifactList = artifactFilter.iterator();
          while (artifactList.hasNext()) {
            String filter = artifactList.next();
            if (Pattern.matches(filter,name)) {
                Logger.Debug("Matched '"+filter+"' against '"+name+"'");
                return true;
            }
          }
          return false;
        }
    }


    /**
     * File scanner class.
     */
    public class FileScanner {
        private File[] arr = null;
        private Collection<File> xfiles = null;
        private File baseDir = null;

        public FileScanner(File dirName, FilenameFilter filter, int depth) {
             baseDir = dirName;
             Logger.Debug("Scanning base directory for files that match patterns '" + baseDir.getAbsolutePath() + "'");
             xfiles = scanFiles(dirName,filter,depth);
        }

        public Collection<File> getFiles() {
            return xfiles;
        }

        public File[] toArray()
        {
            arr = new File[xfiles.size()];
            return xfiles.toArray(arr);
        }

        private Collection<File> scanFiles(
                File dirName,
                FilenameFilter filter,
                int depth)
        {
            if (dirName.isDirectory()) {
                Logger.Debug("Scanning '"+dirName.getAbsolutePath()+"' " + depth);
            }

            Vector<File> files = new Vector<File>();
            File[] entFiles = dirName.listFiles();

            if (dirName.isDirectory() && dirName.getName().equals(".metadata")) {
                Logger.Debug("Ignoring '"+dirName.getAbsolutePath()+"' " + depth);
            } else {
                if (entFiles != null) {
                    for (File afile : entFiles) {
                        String dname = afile.getAbsolutePath();
                        dname = dname.substring(baseDir.getAbsolutePath().length()+1,afile.getAbsolutePath().length());

                        if (filter == null || filter.accept(dirName, dname)) {
                            files.add(afile);
                        }
                        if ((depth<=-1) || (depth>0 && afile.isDirectory())) {
                            depth--;
                            files.addAll(scanFiles(afile, filter, depth));
                            depth++;
                        }
                    }
                }
            }
            return files;
        }
    }

    private static DimensionsSCM scm = null;
    private String[] patterns = new String[0];

    /**
     * Default constructor.
     */
    @DataBoundConstructor
    public ArtifactUploader(String[] patterns) {
        // Check the folders specified have data specified
        if (patterns != null) {
            Logger.Debug("patterns are populated");
            Vector<String> x = new Vector<String>();
            for(int t=0;t<patterns.length;t++) {
                if (StringUtils.isNotEmpty(patterns[t]))
                    x.add(patterns[t]);
            }
            this.patterns = (String[])x.toArray(new String[1]);
        }
        else {
            this.patterns[0] = ".*";
        }
    }

    /*
     * Gets the patterns to upload
     * @return patterns
     */
    public String[] getPatterns() {
        return this.patterns;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                           BuildListener listener) throws IOException, InterruptedException {
        Logger.Debug("Invoking perform callout " + this.getClass().getName());
        try {
            if (build.getResult() == Result.SUCCESS) {
                listener.getLogger().println("[DIMENSIONS] Scanning workspace for files to be saved into Dimensions...");
                listener.getLogger().flush();
                ArtifactFilter af = new ArtifactFilter(patterns);
                FilePath wd = build.getProject().getWorkspace();
                Logger.Debug("Scanning directory for files that match patterns '" + wd.getRemote() + "'");
                File dir = new File (wd.getRemote());
                FileScanner fs = new FileScanner(dir,af,-1);
                File[] validFiles = fs.toArray();

                if (fs.getFiles().size() > 0) {
                    if (scm == null)
                        scm = (DimensionsSCM)build.getProject().getScm();

                    listener.getLogger().println("[DIMENSIONS] Loading files into Dimensions project \""+scm.getProject()+"\"...");
                    listener.getLogger().flush();

                    Calendar nowDateCal = Calendar.getInstance();
                    File logFile = new File("a");
                    FileWriter logFileWriter = null;
                    PrintWriter fmtWriter = null;
                    File tmpFile = null;

                    try {
                        tmpFile = logFile.createTempFile("dmCm"+nowDateCal.getTimeInMillis(),null,null);
                        logFileWriter = new FileWriter(tmpFile);
                        fmtWriter = new PrintWriter(logFileWriter,true);

                        for (File f : validFiles) {
                            Logger.Debug("Found file '"+ f.getAbsolutePath() + "'");
                            fmtWriter.println(f.getAbsolutePath());
                        }
                        fmtWriter.flush();
                    } catch (Exception e) {
                        throw new IOException("Unable to write command log - " + e.getMessage());
                    } finally {
                        fmtWriter.close();
                    }

                    Logger.Debug("Dimensions user is "+scm.getJobUserName()+" , Dimensions installation is "+scm.getJobServer());
                    if (scm.getAPI().login(scm.getJobUserName(),
                                           scm.getJobPasswd(),
                                           scm.getJobDatabase(),
                                           scm.getJobServer()))
                    {
                        DimensionsResult res = scm.getAPI().UploadFiles(wd,scm.getProject(),tmpFile,build);
                        if (res==null) {
                            listener.getLogger().println("[DIMENSIONS] New artifacts failed to get loaded into Dimensions");
                            listener.getLogger().flush();
                        }
                        else {
                            listener.getLogger().println("[DIMENSIONS] Build artifacts were successfully loaded into Dimensions");
                            listener.getLogger().println("[DIMENSIONS] ("+res.getMessage().replaceAll("\n","\n[DIMENSIONS] ")+")");
                            listener.getLogger().flush();
                        }
                    }
                    if (tmpFile != null)
                        tmpFile.delete();
                } else {
                    listener.getLogger().println("[DIMENSIONS] No build artifacts were detected");
                    listener.getLogger().flush();
                }
            }
        } catch(Exception e) {
            listener.fatalError("Unable to load build artifacts into Dimensions - " + e.getMessage());
            return false;
        }
        finally
        {
            if (scm != null)
                scm.getAPI().logout();
        }
        return true;
    }

    /**
     * The ArtifactUploader Descriptor class.
     */
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        /*
         * Loads the descriptor
         */
        public DescriptorImpl() {
            super(ArtifactUploader.class);
            load();
            Logger.Debug("Loading " + this.getClass().getName());
        }

        public String getDisplayName() {
            return "Load any build artifacts into the Dimensions repository";
        }


        /*
         *  This builder can be used with all project types
         */
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        /*
         * Save the descriptor configuration
         */
        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            req.bindParameters(this,"ArtifactUploader");
            return super.configure(req, formData);
        }


        @Override
        public Notifier newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            // Get variables and then construct a new object
            String[] patterns = req.getParameterValues("artifactuploader.patterns");

            ArtifactUploader artifactor = new ArtifactUploader(patterns);

            return artifactor;
        }
    }
}


