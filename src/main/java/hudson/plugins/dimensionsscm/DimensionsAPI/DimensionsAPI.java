
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

// Hudson imports
import hudson.FilePath;
import hudson.plugins.dimensionsscm.DateUtils;
import hudson.plugins.dimensionsscm.Logger;

// Dimensions imports
import com.serena.dmclient.api.Filter;
import com.serena.dmclient.api.ItemRevision;
import com.serena.dmclient.api.Project;
import com.serena.dmclient.api.DimensionsRelatedObject;
import com.serena.dmclient.api.SystemAttributes;

import com.serena.dmclient.api.DimensionsNetworkException;
import com.serena.dmclient.api.DimensionsRuntimeException;
import com.serena.dmclient.api.DimensionsResult;
import com.serena.dmclient.api.DimensionsObjectFactory;
import com.serena.dmclient.api.DimensionsDatabaseAdmin.CommandFailedException;
import com.serena.dmclient.api.DimensionsConnection;
import com.serena.dmclient.api.DimensionsConnectionDetails;
import com.serena.dmclient.api.DimensionsConnectionManager;
import com.serena.dmclient.api.BulkOperator;
import com.serena.dmclient.api.ItemRevisionHistoryRec;
import com.serena.dmclient.api.ActionHistoryRec;

// General imports
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import java.io.Serializable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.lang.IllegalArgumentException;
import java.net.URLDecoder;
import java.util.List;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.text.Collator;

import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.apache.commons.lang.StringEscapeUtils;


/*
 * Main Dimensions API class
 */
public class DimensionsAPI
{
    private static final String MISSING_SOURCE_PATH = "The nested element needs a valid 'srcpath' attribute"; //$NON-NLS-1$
    private static final String MISSING_PROJECT = "The nested element needs a valid project to work on"; //$NON-NLS-1$
    private static final String BAD_BASE_DATABASE_SPEC = "The <dimensions> task needs a valid 'database' attribute, in the format 'dbname@dbconn'"; //$NON-NLS-1$
    private static final String NO_COMMAND_LINE = "The <run> nested element need a valid 'cmd' attribute"; //$NON-NLS-1$
    private static final String SRCITEM_SRCPATH_CONFLICT = "The <getcopy> nested element needs exactly one of the 'srcpath' or 'srcitem' attributes"; //$NON-NLS-1$

    // Dimensions server details
    private String dmServer;
    private String dmDb;

    private String dbName;
    private String dbConn;

    // Dimensions user details
    private String dmUser;
    private String dmPasswd;

    // Dimensions project details
    private String dmProject;
    private String dmDirectory;
    private String dmRequest;
    private String projectPath;

    private String dateType = "edit";
    private boolean allRevisions = false;

    private DimensionsConnection connection = null;

    /*
     * Gets the user ID for the connection.
     * @return the user ID of the user as whom to connect
     */
    public final String getSCMUserID() {
        return this.dmUser;
    }

    /*
     * Gets the base database for the connection (as "NAME@CONNECTION").
     * @return the name of the base database to connect to
     */
    public final String getSCMDatabase() {
        return this.dmDb;
    }

    /*
     * Gets the base database for the connection
     * @return the name of the base database only
     */
    public final String getSCMBaseDb() {
        return this.dbName;
    }

    /*
     * Gets the database DNS for the connection
     * @return the name of the DSN only
     */
    public final String getSCMDsn() {
        return this.dbConn;
    }

    /*
     * Gets the server for the connection.
     * @return the name of the server to connect to
     */
    public final String getSCMServer() {
        return this.dmServer;
    }

    /*
     * Gets the project ID for the connection.
     * @return the project ID
     */
    public final String getSCMProject() {
        return this.dmProject;
    }

    /*
     * Gets the project path.
     * @return the project path
     */
    public final String getSCMPath() {
        return this.projectPath;
    }

    /*
     * Gets the repository connection class
     * @return the SCM repository connection
     */
    public final DimensionsConnection getCon() {
        return this.connection;
    }

    /**
     * Creates a Dimensions session using the supplied login credentials and
     * server details
     *
     * @param userID
     *            Dimensions user ID
     * @param password
     *            Dimensions password
     * @param database
     *            base database name
     * @param server
     *            hostname of the remote dimensions server
     * @return a boolean
     * @throws DimensionsNetworkException
     */
    public final boolean login(String userID, String password,
            String database, String server)
            throws IllegalArgumentException, ParseException
    {

        if (connection == null)
            connection = DimensionsConnectionManager.getThreadConnection();

        if (connection == null)
        {
            dmServer = server;
            dmDb = database;
            dmUser = userID;
            dmPasswd = password;


            Logger.Debug("Logging into Dimensions: " + dmUser + " " + dmServer + " " + dmDb);

            if (dmServer == null || dmServer.length() == 0 ||
                dmDb == null || dmDb.length() == 0 ||
                dmUser == null || dmUser.length() == 0 ||
                dmPasswd  == null || dmPasswd.length() == 0)
                throw new IllegalArgumentException("Invalid or not parameters have been specified");

            // check if we need to pre-process the login details
            String[] dbCompts = parseDatabaseString(dmDb);
            dbName = dbCompts[0];
            dbConn = dbCompts[1];

            DimensionsConnectionDetails details = new DimensionsConnectionDetails();
            details.setUsername(dmUser);
            details.setPassword(dmPasswd);
            details.setDbName(dbName);
            details.setDbConn(dbConn);
            details.setServer(dmServer);
            connection = DimensionsConnectionManager.getConnection(details);
            if (connection!=null)
                DimensionsConnectionManager.registerThreadConnection(connection);
        }
        return (connection != null);
    }

    /**
     * Disconnects from the Dimensions repository
     */
    public final void logout()
    {
        if (connection != null) {
            try {
                connection.close();
            } catch (DimensionsNetworkException dne) {
                /* do nothing */
            } catch (DimensionsRuntimeException dne) {
                /* do nothing */
            }
        }
        connection = null;
    }


    /**
     * Parses a base database specification
     * <p>
     * Valid patterns are dbName/dbPassword@dbConn or dbName@dbConn. Anything
     * else will cause a java.text.ParseException to be thrown. Returns an array
     * of either [dbName, dbConn, dbPassword] or [dbName, dbConn].
     *
     * @param database
     *            a base database specification
     * @return an array of base database specification components
     * @throws ParseException
     *             if the supplied String does not conform to the above rules
     */
    private static String[] parseDatabaseString(String database)
            throws ParseException {
        String[] dbCompts;
        int endName = database.indexOf('/');
        int startConn = database.indexOf('@');
        if (startConn < 1 || startConn == database.length() - 1) {
            throw new ParseException(BAD_BASE_DATABASE_SPEC, startConn);
        }
        String dbName = null;
        String dbConn = null;
        String dbPassword = null;
        if (endName < 0 || startConn <= endName) {
            // no '/' or '@' is before '/':
            dbName = database.substring(0, startConn);
            dbConn = database.substring(startConn + 1);
            dbCompts = new String[2];
            dbCompts[0] = dbName;
            dbCompts[1] = dbConn;
        } else if (endName == 0 || startConn == endName + 1) {
            // '/' at start or '/' immediately followed by '@':
            throw new ParseException(BAD_BASE_DATABASE_SPEC, endName);
        } else {
            dbName = database.substring(0, endName);
            dbPassword = database.substring(endName + 1, startConn);
            dbConn = database.substring(startConn + 1);
            dbCompts = new String[3];
            dbCompts[0] = dbName;
            dbCompts[1] = dbConn;
            dbCompts[2] = dbPassword;
        }
        return dbCompts;
    }

    /*
     *-----------------------------------------------------------------
     *  FUNCTION SPECIFICATION
     *  Name:
     *      hasRepositoryBeenUpdated
     *  Description:
     *      Has the repository had any changes made during a certain time?
     * Parameters:
     *      @param final String projectName
     *      @param final FilePath workspace
     *      @param final Calendar fromDate
     *      @param final Calendar toDate
     *      @param final TimeZone tz
     *  Return:
     *      @return boolean
     *-----------------------------------------------------------------
     */
    public boolean hasRepositoryBeenUpdated(final String projectName,
                               final FilePath workspace,
                               final Calendar fromDate,
                               final Calendar toDate,
                               final TimeZone tz)
                              throws IOException, InterruptedException
    {
        boolean bChanged = false;

        if (fromDate == null)
            return true;

        if (connection == null)
            throw new IOException("Not connected to an SCM repository");

        try
        {
            List items = calcRepositoryDiffs(projectName,workspace,fromDate,toDate, tz);
            if (items != null)
                bChanged = (items.size() > 0);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new IOException("Unable to run hasRepositoryBeenUpdated - " + e.getMessage());
        }

        return bChanged;
    }


    /*
     *-----------------------------------------------------------------
     *  FUNCTION SPECIFICATION
     *  Name:
     *      checkout
     *  Description:
     *      Get a copy of the code
     * Parameters:
     *      @param final String projectName
     *      @param final FilePath projectDir
     *      @param final FilePath workspaceName
     *      @param final Calendar fromDate
     *      @param final Calendar toDate
     *      @param final File changelogFile
     *      @param final TimeZone tz
     *      @param StringBuffer cmdOutput
     *      @param final String url
     *      @param final boolean doFullUpdate
     *      @param final boolean doRevert
     *  Return:
     *      @return boolean
     *-----------------------------------------------------------------
     */
    public boolean checkout(final String projectName,
                            final FilePath projectDir,
                            final FilePath workspaceName,
                            final Calendar fromDate,
                            final Calendar toDate,
                            final File changelogFile,
                            final TimeZone tz,
                            StringBuffer cmdOutput,
                            final String url,
                            final boolean doFullUpdate,
                            final boolean doRevert)
                    throws IOException, InterruptedException
    {
        boolean bRet = false;

        if (connection == null)
            throw new IOException("Not connected to an SCM repository");

        try
        {
            String coCmd = "UPDATE /BRIEF ";
            List items = calcRepositoryDiffs(projectName,projectDir,fromDate,toDate,tz);
            if (items != null || doFullUpdate)
            {
                File logFile = new File("a");
                FileWriter logFileWriter = null;
                PrintWriter fmtWriter = null;
                File tmpFile = null;

                if (items != null && !doFullUpdate) {
                    try {
                        tmpFile = logFile.createTempFile("dmCm"+toDate.getTimeInMillis(),null,null);
                        logFileWriter = new FileWriter(tmpFile);
                        fmtWriter = new PrintWriter(logFileWriter,true);

                        for (int i = 0; i < items.size(); ++i) {
                            ItemRevision item = (ItemRevision) items.get(i);
                            fmtWriter.println((String)item.getAttribute(SystemAttributes.OBJECT_SPEC));
                        }
                        fmtWriter.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new IOException("Unable to write command log - " + e.getMessage());
                    } finally {
                        fmtWriter.close();
                    }
                }

                String cmd = coCmd;
                String projDir = (projectDir!=null) ? projectDir.getRemote() : null;

                Logger.Debug("Do full update : " + doFullUpdate);
                Logger.Debug("CM Url : " + ((url != null) ? url : "(null)"));

                if (!doFullUpdate && tmpFile != null)
                    cmd += "/USER_ITEMLIST=\"" + tmpFile.getPath() + "\"";
                else {
                    if (projDir != null && !projDir.equals("\\") && !projDir.equals("/"))
                        cmd += "/DIR=\"" + projDir + "\"";
                }

                cmd += "/WORKSET=\"" + projectName + "\" /USER_DIR=\"" +
                             workspaceName.getRemote() + "\"";

                if (doRevert)
                    cmd += " /OVERWRITE";

                DimensionsResult res = run(connection,cmd);
                if (res != null )
                {
                    cmdOutput = cmdOutput.append(res.getMessage());
                    String outputStr = new String(cmdOutput.toString());
                    Logger.Debug(outputStr);

                    if (items != null)
                    {
                        if (tmpFile != null)
                            tmpFile.delete();
                        // Process the changesets...
                        List changes = createChangeList(items,tz,url);
                        Logger.Debug("Writing changeset to " + changelogFile.getPath());
                        DimensionsChangeLogWriter write = new DimensionsChangeLogWriter();
                        write.writeLog(changes,changelogFile);
                    }

                    bRet = true;

                    // Check if any conflicts were identified
                    int confl = outputStr.indexOf("C\t");
                    if (confl > 0)
                        bRet = false;
                }
            }
            else
                bRet = true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new IOException("Unable to run checkout callout - " + e.getMessage());
        }

        return bRet;
    }


    /*
     *-----------------------------------------------------------------
     *  FUNCTION SPECIFICATION
     *  Name:
     *  Name:
     *      calcRepositoryDiffs
     *  Description:
     *      Calculate any repository changes made during a certain time
     * Parameters:
     *      @param final String projectName
     *      @param final FilePath workspace
     *      @param final Calendar fromDate
     *      @param final Calendar toDate
     *      @param final TimeZone tz
     *  Return:
     *      @return boolean
     *-----------------------------------------------------------------
     */
    private List calcRepositoryDiffs(final String projectName,
                               final FilePath workspace,
                               final Calendar fromDate,
                               final Calendar toDate,
                               final TimeZone tz)
                              throws IOException, InterruptedException
    {
        if (connection == null)
            throw new IOException("Not connected to an SCM repository");

        if (fromDate == null)
            return null;

        try
        {
            // Get the dates for the last build
            int[] attrs = getItemFileAttributes(true);
            String dateAfter = (fromDate != null) ? formatDatabaseDate(fromDate.getTime(), tz) : "01-JAN-1970 00:00:00";
            String dateBefore = (toDate != null) ? formatDatabaseDate(toDate.getTime(), tz) : formatDatabaseDate(Calendar.getInstance().getTime(), tz);

            Filter filter = new Filter();

            filter.criteria().add(new Filter.Criterion(SystemAttributes.LAST_UPDATED_DATE, dateAfter, Filter.Criterion.GREATER_EQUAL));
            filter.criteria().add(new Filter.Criterion(SystemAttributes.LAST_UPDATED_DATE, dateBefore, Filter.Criterion.LESS_EQUAL));
            filter.criteria().add(new Filter.Criterion(SystemAttributes.CREATION_DATE, dateAfter, Filter.Criterion.GREATER_EQUAL));
            filter.criteria().add(new Filter.Criterion(SystemAttributes.CREATION_DATE, dateBefore, Filter.Criterion.LESS_EQUAL));
            filter.criteria().add(new Filter.Criterion(SystemAttributes.IS_EXTRACTED, "Y", Filter.Criterion.NOT)); //$NON-NLS-1$

            Logger.Debug("Looking between " + dateAfter + " -> " + dateBefore);
            String projName = projectName.toUpperCase();
            Project projectObj = connection.getObjectFactory().getProject(projName);
            List items = queryItems(connection, projectObj, workspace.getRemote(), filter, attrs, true, !allRevisions);
            return items;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new IOException("Unable to run hasRepositoryBeenUpdated - " + e.getMessage());
        }
    }

    /**
     * Construct the change list
     *
     * @param List
     * @param TimeZone
     * @param url
     * @return List
     * @throws DimensionsRuntimeException
     */
     private List createChangeList(List items, TimeZone tz, String url)
                            throws DimensionsRuntimeException
     {
        items = getSortedItemList(items);
        List changeSet = new ArrayList(items.size());
        String key = null;
        DimensionsChangeSet cs = null;
        for (int i = 0; i < items.size(); ++i) {
            ItemRevision item = (ItemRevision) items.get(i);
            int x = 0;

            Integer fileVersion = (Integer)item.getAttribute(SystemAttributes.FILE_VERSION);
            String operation;
            if (fileVersion != null)
                x = fileVersion.intValue();

            Logger.Debug("Creating a change set (" + x + ")");
            if (x < 2)
                operation = "add";
            else
                operation = "edit";

            String spec = (String)item.getAttribute(SystemAttributes.OBJECT_SPEC);
            String revision = (String)item.getAttribute(SystemAttributes.REVISION);
            String fileName = (String)item.getAttribute(SystemAttributes.FULL_PATH_NAME) + ";" + revision;
            String author = (String)item.getAttribute(SystemAttributes.LAST_UPDATED_USER);
            String comment = (String)item.getAttribute(SystemAttributes.REVISION_COMMENT);
            String date = (String)item.getAttribute(getDateTypeAttribute(operation));
            if (date == null)
                date = (String)item.getAttribute(getDateTypeAttribute("edit"));
            String urlString = constructURL(spec,url,getSCMDsn(),getSCMBaseDb());

            Logger.Debug("Change set details -" + comment + " " + revision + " " + fileName + " " + author +
                         " " + spec  + " " + date + " " + operation + " " + urlString);
            Calendar opDate = Calendar.getInstance();
            opDate.setTime(DateUtils.parse(date,tz));
            if (key == null) {
                cs = new DimensionsChangeSet(fileName,author,operation,revision,comment,urlString,opDate);
                key = comment + author;
                changeSet.add(cs);
            } else {
                String key1 = comment + author;
                if (key.equals(key1)) {
                    cs.add(fileName,operation,urlString);
                } else {
                    cs = new DimensionsChangeSet(fileName,author,operation,revision,comment,urlString,opDate);
                    key = comment + author;
                    changeSet.add(cs);
                }
            }
        }

        return changeSet;
    }

    /**
     * Sort the item list
     *
     * @param List
     * @return List
     * @throws DimensionsRuntimeException
     */
    private static List getSortedItemList(List items)
                            throws DimensionsRuntimeException
    {
        Collections.sort(items, new Comparator()
        {
            public int compare(Object oa1, Object oa2)
            {
                int result = 0;
                try
                {
                    ItemRevision o1 = (ItemRevision)oa1;
                    ItemRevision o2 = (ItemRevision)oa2;

                    String a1 = (String)o1.getAttribute(SystemAttributes.REVISION_COMMENT);
                    String a2 = (String)o2.getAttribute(SystemAttributes.REVISION_COMMENT);

                    a1 += (String)o1.getAttribute(SystemAttributes.LAST_UPDATED_USER);
                    a2 += (String)o2.getAttribute(SystemAttributes.LAST_UPDATED_USER);

                    result = a1.compareTo(a2);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    throw new DimensionsRuntimeException("Unable to sort item list - " + e.getMessage());
                }
                return result;
            }
        });
        return items;
    }

    /**
     * Sets the current project for the current user, which is deduced from the
     * current thread.
     *
     * @param connection
     *            the connection for which to set the current project.
     * @param projectName
     *            the project to switch to, in the form PRODUCT NAME:PROJECT
     *            NAME.
     * @throws DimensionsRuntimeException
     */
    private static void setCurrentProject(DimensionsConnection connection,
            String projectName) {
        connection.getObjectFactory().setCurrentProject(projectName, false, "",
                "", null, true);
    }

    private static Project getCurrentProject(DimensionsConnection connection) {
        return connection.getObjectFactory().getCurrentUser()
                .getCurrentProject();
    }


    static int[] getItemFileAttributes(boolean isDirectory) {
        if (isDirectory) {
            final int[] attrs = { SystemAttributes.OBJECT_SPEC,
                    SystemAttributes.PRODUCT_NAME, SystemAttributes.OBJECT_ID,
                    SystemAttributes.VARIANT, SystemAttributes.TYPE_NAME,
                    SystemAttributes.REVISION, SystemAttributes.FULL_PATH_NAME,
                    SystemAttributes.ITEMFILE_FILENAME,
                    SystemAttributes.LAST_UPDATED_USER,
                    SystemAttributes.FILE_VERSION,
                    SystemAttributes.REVISION_COMMENT,
                    SystemAttributes.LAST_UPDATED_DATE,
                    SystemAttributes.CREATION_DATE};
            return attrs;
        }
        final int[] attrs = { SystemAttributes.PRODUCT_NAME,
                SystemAttributes.OBJECT_ID, SystemAttributes.VARIANT,
                SystemAttributes.TYPE_NAME, SystemAttributes.REVISION,
                SystemAttributes.ITEMFILE_FILENAME,
                SystemAttributes.LAST_UPDATED_USER,
                SystemAttributes.FILE_VERSION,
                SystemAttributes.LAST_UPDATED_DATE,
                SystemAttributes.CREATION_DATE};
        return attrs;
    }

    private static String preProcessSrcPath(String srcPath) {
        String path = srcPath.equals("/") ? "" : srcPath; //$NON-NLS-1$ //$NON-NLS-2$
        if (!path.endsWith("/") & !path.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
            path += "/"; //$NON-NLS-1$
        }
        if (path.equals("\\/") || path.equals("/"))
            path = "";
        return path;
    }

    // URL encode a webclient path + spec for opening
    private static String constructURL(String spec, String url, String dsn, String db) {
        String urlString = "";
        if (spec != null && spec.length() > 0 &&
            url != null && url.length() > 0) {
            String host = url;
            if (host.endsWith("/"))
                host = host.substring(0,host.length()-1);

            if (host.startsWith("http:"))
                host = host.substring(7,host.length());

            String page = "/dimensions/";
            String urlQuery = "jsp=api&command=openi&object_id=";
            urlQuery += spec;
            urlQuery += "&DB_CONN=";
            urlQuery += dsn;
            urlQuery += "&DB_NAME=";
            urlQuery += db;
            try {
                Logger.Debug("Host URL - " + host + " " + page + " " + urlQuery);
                String urlStr = encodeUrl(host,page,urlQuery);
                Logger.Debug("Change URL - " + urlStr);
                urlString = urlStr;
            }   catch (Exception e) {
                e.printStackTrace();
            }
        }

        return urlString;
    }

    // Encode a URL correctly - handles spaces as %20
    // @param String
    // @param String
    // @param String
    // @return String
    // @throws MalformedURLException
    private static String encodeUrl(String host,String page,String query)
                        throws MalformedURLException, URISyntaxException {
        String urlStr = "";
        if (page != null && page.length() > 0 &&
            host != null && host.length() > 0 &&
            query != null && query.length() > 0) {
            URI uri = new URI("http",host,page,query,null);
            urlStr = uri.toASCIIString();
        }
        return urlStr;
    }

    // find items given a directory spec
    static List queryItems(DimensionsConnection connection, Project srcProject,
            String srcPath, Filter filter, int[] attrs, boolean isRecursive, boolean isLatest) {
        // check srcPath validity check srcPath trailing slash do query
        if (srcPath == null) {
            throw new IllegalArgumentException(MISSING_SOURCE_PATH);
        }
        if (srcProject == null) {
            throw new IllegalArgumentException(MISSING_PROJECT);
        }

        String path = preProcessSrcPath(srcPath);
        if (!(isRecursive && path.equals(""))) { //$NON-NLS-1$
            filter.criteria().add(
                    new Filter.Criterion(SystemAttributes.ITEMFILE_DIR,
                            (isRecursive ? path + '%' : path), 0));
        }

        if (isLatest) {
            filter.criteria().add(
                    new Filter.Criterion(SystemAttributes.IS_LATEST_REV,
                            Boolean.TRUE, 0));
        }

        //
        // Catch any exceptions that may be thrown by the Java API and
        // for now return no changes. Going forward it would be good to
        // trap all the possible exception types and do something about them
        //
        try {
            Logger.Debug("Looking for changed files in '" + path + "' in project: " + srcProject.getName());
            List rels = srcProject.getChildItems(filter);
            Logger.Debug("Found " + rels.size());
            if (rels.size()==0)
                return null;

            List items = new ArrayList(rels.size());
            for (int i = 0; i < rels.size(); ++i) {
                DimensionsRelatedObject rel = (DimensionsRelatedObject) rels.get(i);
                items.add(rel.getObject());
            }
            BulkOperator bo = connection.getObjectFactory().getBulkOperator(items);
            bo.queryAttribute(attrs);
            return items;
        } catch (Exception e) {
            // e.printStackTrace();
            Logger.Debug("Exception detected from the Java API: " + e.getMessage());
            return null;
        }
    }


    /**
     * Runs a Dimensions command.
     *
     * @param connection
     *            the connection for which to run the command
     * @param cmd
     *            the command line to run
     * @throws Exception
     *             if the command failed
     * @throws IllegalArgumentException
     *             if the command string was null or an emptry dtring
     *
     */
    static DimensionsResult run(DimensionsConnection connection, String cmd)
            throws CommandFailedException {
        if (cmd == null || cmd.equals("")) { //$NON-NLS-1$
            throw new IllegalArgumentException(NO_COMMAND_LINE);
        }
        Logger.Debug("Running the command '" + cmd + "'...");

        DimensionsObjectFactory dof = connection.getObjectFactory();
        DimensionsResult res = dof.runCommand(cmd);
        return res;
    }


    /**
     * Convert the human-readable <code>dateType</code> into a DMClient
     * attribute name.
     * <p>
     * Defaults to
     * {@link com.serena.dmclient.api.SystemAttributes#CREATION_DATE} if it is
     * not recognized.
     *
     * @param dateType
     *            created, updated, revised or actioned.
     * @return the corresponding field value from
     *         {@link com.serena.dmclient.api.SystemAttributes}
     */
    static int getDateTypeAttribute(String dateType) {
        int ret = SystemAttributes.CREATION_DATE;
        if (dateType != null) {
            if (dateType.equalsIgnoreCase("edit")) { //$NON-NLS-1$
                ret = SystemAttributes.LAST_UPDATED_DATE;
            } else if (dateType.equalsIgnoreCase("actioned")) { //$NON-NLS-1$
                ret = SystemAttributes.LAST_ACTIONED_DATE;
            } else if (dateType.equalsIgnoreCase("revised")) { //$NON-NLS-1$
                ret = SystemAttributes.UTC_MODIFIED_DATE;
            } else if (dateType.equalsIgnoreCase("add")) { //$NON-NLS-1$
                ret = SystemAttributes.CREATION_DATE;
            }
        }
        return ret;
    }

    // database times are in Oracle format, in a specified timezone
    static String formatDatabaseDate(Date date, TimeZone timeZone) {
        return (timeZone == null) ? DateUtils.format(date) : DateUtils.format(date, timeZone);
    }

    // database times are in Oracle format, in a specified timezone
    static Date parseDatabaseDate(String date, TimeZone timeZone) {
        return (timeZone == null) ? DateUtils.parse(date) : DateUtils.parse(date, timeZone);
    }
}
