
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

import hudson.plugins.dimensionsscm.DimensionsChangeSet;
import hudson.plugins.dimensionsscm.DimensionsChangeSetList;
import hudson.plugins.dimensionsscm.DateUtils;

import hudson.Util;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * Write a change set.
 */
public class DimensionsChangeLogWriter
{
    /*
     * Save the change list to the changelogFile
     * @param List<DimensionsChangeSet> changeSets
     * @param File changelogFile
     */
    public boolean writeLog(List<DimensionsChangeSet> changeSets,File changelogFile)
                                            throws IOException
    {
        boolean bRet = false;
        FileWriter logFile = null;
        try {
            logFile = new FileWriter(changelogFile);
            write(changeSets,logFile);
            logFile.flush();
            bRet=true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Unable to write change log - " + e.getMessage());
        } finally {
            logFile.close();
        }

        return bRet;
    }


    /*
     * Save the change list to the changelogFile
     * @param List<DimensionsChangeSet> changeSets
     * @param File changelogFile
     */
    private void write(List<DimensionsChangeSet> changeSets,Writer logFile)
    {
        PrintWriter writer = new PrintWriter(logFile);
        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.println("<changelog>");
        if (changeSets != null) {
            for (DimensionsChangeSet changeSet : changeSets) {
                writer.println(String.format("\t<changeset version=\"%s\">", escapeXML(changeSet.getVersion())));
                writer.println(String.format("\t\t<date>%s</date>", Util.XS_DATETIME_FORMATTER.format(changeSet.getDate())));
                writer.println(String.format("\t\t<user>%s</user>", escapeXML(changeSet.getDeveloper())));
                writer.println(String.format("\t\t<comment>%s</comment>", escapeXML(changeSet.getSCMComment())));
                writer.println("\t\t<items>");
                for (DimensionsChangeSet.DmFiles item : changeSet.getFiles()) {
                    writer.println(String.format("\t\t\t<item operation=\"%s\" url=\"%s\">%s</item>", item.getOperation(),
                                    escapeHTML(item.getUrl()),
                                    escapeXML(item.getFile())));
                }
                writer.println("\t\t</items>");
                writer.println("\t\t<requests>");
                for (DimensionsChangeSet.DmRequests req : changeSet.getRequests()) {
                    writer.println(String.format("\t\t\t<request url=\"%s\">%s</request>",
                                    escapeHTML(req.getUrl()),
                                    escapeXML(req.getIdentifier())));
                }
                writer.println("\t\t</requests>");
                writer.println("\t</changeset>");
            }
        }
        writer.println("</changelog>");

        return;
    }

    /*
     * Escape an XML string
     * @param String
     */
    private static String escapeXML(String inTxt) {
        if (inTxt == null || inTxt.length() == 0)
            return inTxt;

        final StringBuilder outTxt = new StringBuilder();
        final StringCharacterIterator iterator = new StringCharacterIterator(inTxt);
        char character =  iterator.current();

        // Scan through strings and escape as necessary...
        while (character != CharacterIterator.DONE ) {
            if (character == '<') {
                outTxt.append("&lt;");
            }
            else if (character == '>') {
                outTxt.append("&gt;");
            }
            else if (character == '\"') {
                outTxt.append("&quot;");
            }
            else if (character == '\'') {
                outTxt.append("&#039;");
            }
            else if (character == '&') {
                outTxt.append("&amp;");
            }
            else {
                outTxt.append(character);
            }
            character = iterator.next();
        }
        return outTxt.toString();
    }

    /*
     * Escape an HTML string
     * @param String
     */
    private static String escapeHTML(String inTxt) {
        if (inTxt == null || inTxt.length() == 0)
            return inTxt;

        final StringBuilder outTxt = new StringBuilder();
        final StringCharacterIterator iterator = new StringCharacterIterator(inTxt);
        char character =  iterator.current();

        // Scan through strings and escape as necessary...
        while (character != CharacterIterator.DONE ) {
            if (character == '<') {
                outTxt.append("&lt;");
            }
            else if (character == '>') {
                outTxt.append("&gt;");
            }
            else if (character == '\"') {
                outTxt.append("&quot;");
            }
            else if (character == '\'') {
                outTxt.append("&#039;");
            }
            else if (character == '&') {
                outTxt.append("&amp;");
            }
            else if (character == ' ') {
                outTxt.append("&nbsp;");
            }
            else {
                outTxt.append(character);
            }
            character = iterator.next();
        }
        return outTxt.toString();   }
}
