/* ===========================================================================
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

import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;

/**
 * This experimental plugin extends Jenkins/Hudson support for Dimensions SCM
 * repositories.
 *
 * @author Tim Payne
 */
public class FileScanner implements Serializable {
    private File[] arr;
    private Collection<File> xfiles;
    private File baseDir;
    private ScannerFilter filter;

    /**
     * File pattern matcher class.
     */
    public class ScannerFilter implements FilenameFilter {
        private Set<String> artifactIncFilter = new TreeSet<String>();
        private Set<String> artifactExcFilter = new TreeSet<String>();

        public ScannerFilter(String[] inclusionsx, String[] exclusionsx) {
            Iterator<String> artifactList = Arrays.asList(inclusionsx).iterator();
            while (artifactList.hasNext()) {
                artifactIncFilter.add(artifactList.next().trim());
            }
            artifactIncFilter.remove("");

            if (exclusionsx != null && exclusionsx.length > 0) {
                artifactList = Arrays.asList(exclusionsx).iterator();
                while (artifactList.hasNext()) {
                    String txt = artifactList.next();
                    if (txt != null && StringUtils.isNotEmpty(txt)) {
                        artifactExcFilter.add(txt.trim());
                    }
                }
                artifactExcFilter.remove("");
            }
        }

        public boolean accept(File dir, String name) {
            final Iterator<String> artifactList = artifactIncFilter.iterator();
            final Iterator<String> artifactListEx = artifactExcFilter.iterator();

            // Skip metadata no matter what.
            if (name.equals(".metadata") || name.equals(".dm")) {
                return false;
            }

            while (artifactListEx.hasNext()) {
                String filter = artifactListEx.next();
                if (Pattern.matches(filter, name)) {
                    return false;
                }
            }

            while (artifactList.hasNext()) {
                String filter = artifactList.next();
                if (Pattern.matches(filter, name)) {
                    return true;
                }
            }

            return false;
        }
    }

    public FileScanner(File dirName, String[] patterns, String[] patternsExc, int depth) {
        baseDir = dirName;
        filter = new ScannerFilter(patterns, patternsExc);
        xfiles = scanFiles(dirName, filter, depth);
    }

    public Collection<File> getFiles() {
        return xfiles;
    }

    public File[] toArray() {
        arr = new File[xfiles.size()];
        return xfiles.toArray(arr);
    }

    private Collection<File> scanFiles(File dirName, FilenameFilter filter, int depth) {
        List<File> files = new ArrayList<File>();
        File[] entFiles = dirName.listFiles();

        if (dirName.isDirectory() && (dirName.getName().equals(".metadata") || dirName.getName().equals(".dm"))) {
            /* ignore it. */
        } else {
            if (entFiles != null) {
                for (File afile : entFiles) {
                    String dname = afile.getAbsolutePath();
                    if (afile.getName().equals(".metadata") || afile.getName().equals(".dm")) {
                        continue;
                    }

                    dname = dname.substring(baseDir.getAbsolutePath().length() + 1, afile.getAbsolutePath().length());

                    if (filter == null || filter.accept(dirName, dname)) {
                        files.add(afile);
                    }

                    if (depth <= -1 || (depth > 0 && afile.isDirectory())) {
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
