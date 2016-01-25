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

import hudson.model.User;
import hudson.scm.ChangeLogSet;
import hudson.scm.EditType;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.TimeZone;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Represents an individual change in the changelog.
 */
@ExportedBean(defaultVisibility = 999)
public class DimensionsChangeSet extends ChangeLogSet.Entry {
    private String developer;
    private String message;
    private final String identifier;
    private Calendar date;
    private String version;
    private final Collection<DmFiles> items;
    private final Collection<DmRequests> requests;

    // Digester class seems to need a no-parameter constructor else it crashes
    public DimensionsChangeSet() {
        this("", "", "", "", "", "", null);
    }

    public DimensionsChangeSet(String file, String developer, String op, String revision, String comment, String url,
            Calendar date) {
        this.identifier = file;
        this.developer = developer;
        this.message = comment;
        this.date = date;
        this.version = revision;
        this.items = new HashSet<DmFiles>();
        this.items.add(new DmFiles(file, op, url));
        this.requests = new HashSet<DmRequests>();
    }

    @Override
    public void setParent(ChangeLogSet parent) {
        super.setParent(parent);
    }

    public Date getDate() {
        return this.date.getTime();
    }

    public String getDeveloper() {
        return this.developer;
    }

    public String getSCMComment() {
        return this.message;
    }

    public void setDateString(String dateString) throws ParseException {
        date = Calendar.getInstance();
        TimeZone tz = TimeZone.getTimeZone("UTC");
        date.setTime(DateUtils.parse(dateString, tz));
    }

    public Collection<DmFiles> getFiles() {
        return this.items;
    }

    public Collection<DmRequests> getRequests() {
        return this.requests;
    }

    @Override
    public Collection<String> getAffectedPaths() {
        Collection<String> paths = new ArrayList<String>(items.size());
        for (DmFiles item : items) {
            paths.add(item.getFile());
        }
        return paths;
    }

    @Override
    public User getAuthor() {
        if (this.developer == null) {
            throw new RuntimeException("Unable to determine changeset developer");
        }
        return User.get(this.developer, true);
    }

    @Override
    public String getMsg() {
        return this.message;
    }

    public String getId() {
        return this.identifier;
    }

    public void setVersion(String x) {
        this.version = x;
    }

    public String getVersion() {
        return this.version;
    }

    public void setUser(String x) {
        this.developer = x;
    }

    public String getUser() {
        return this.developer;
    }

    public void setComment(String x) {
        this.message = x;
    }

    public String getComment() {
        return getSCMComment();
    }

    public void add(DimensionsChangeSet.DmFiles file) {
        items.add(file);
        file.setParent(this);
    }

    public void add(String file, String operation, String url) {
        DimensionsChangeSet.DmFiles x = new DmFiles(file, operation, url);
        items.add(x);
        x.setParent(this);
    }

    public void addRequest(DimensionsChangeSet.DmRequests newreq) {
        for (DmRequests req : requests) {
            if (req.getIdentifier().equals(newreq.getIdentifier())) {
                return;
            }
        }

        requests.add(newreq);
        newreq.setParent(this);
    }

    public void addRequest(String objectId, String url) {
        for (DmRequests req : requests) {
            if (req.getIdentifier().equals(objectId)) {
                return;
            }
        }

        DimensionsChangeSet.DmRequests x = new DmRequests(objectId, url);
        requests.add(x);
        x.setParent(this);
    }

    public void addRequest(String objectId, String url, String title) {
        for (DmRequests req : requests) {
            if (req.getIdentifier().equals(objectId)) {
                return;
            }
        }

        DimensionsChangeSet.DmRequests x = new DmRequests(objectId, url, title);
        requests.add(x);
        x.setParent(this);
    }

    /**
     * List of changes made in the repository for this changeset.
     */
    @ExportedBean(defaultVisibility = 999)
    public static class DmFiles {
        private String fileName;
        private String operation;
        private String url;
        private DimensionsChangeSet parent;

        public DmFiles() {
            this("", "", "");
        }

        public DmFiles(String fileName, String operation, String url) {
            this.fileName = fileName;
            this.url = url;
            this.operation = operation;
        }

        @Exported
        public String getUrl() {
            if (this.url.length() == 0) {
                return null;
            } else {
                return this.url;
            }
        }

        @Exported
        public String getOperation() {
            return this.operation;
        }

        @Exported
        public String getFile() {
            if (this.fileName.length() == 0) {
                return null;
            } else {
                return this.fileName;
            }
        }

        public DimensionsChangeSet getParent() {
            return this.parent;
        }

        @Exported
        public EditType getEditType() {
            if (operation.equalsIgnoreCase("delete")) {
                return EditType.DELETE;
            } else if (operation.equalsIgnoreCase("add")) {
                return EditType.ADD;
            } else {
                return EditType.EDIT;
            }
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public void setOperation(String operation) {
            this.operation = operation;
        }

        public void setFile(String fileName) {
            this.fileName = fileName;
        }

        public void setParent(DimensionsChangeSet parent) {
            this.parent = parent;
        }
    }

    @ExportedBean(defaultVisibility = 999)
    public static class DmRequests {
        private String identifier;
        private String url;
        private String title;
        private DimensionsChangeSet parent;

        public DmRequests() {
            this("", "", "");
        }

        public DmRequests(String objectID, String url) {
            this.identifier = objectID;
            this.url = url;
            this.title = "";
        }

        public DmRequests(String objectID, String url, String title) {
            this.identifier = objectID;
            this.url = url;
            this.title = title;
        }

        @Exported
        public String getUrl() {
            if (this.url == null || this.url.length() == 0) {
                return null;
            } else {
                return this.url;
            }
        }

        public void setUrl(String url) {
            this.url = url;
        }

        @Exported
        public String getIdentifier() {
            if (this.identifier == null || this.identifier.length() == 0) {
                return null;
            } else {
                return this.identifier;
            }
        }

        public void setIdentifier(String id) {
            this.identifier = id;
        }

        @Exported
        public String getTitle() {
            if (this.title == null || this.title.length() == 0) {
                return null;
            } else {
                return this.title;
            }
        }

        public void setTitle(String id) {
            this.title = id;
        }

        public void setParent(DimensionsChangeSet parent) {
            this.parent = parent;
        }
    }
}
