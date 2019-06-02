package hudson.plugins.dimensionsscm;

import hudson.model.User;
import hudson.scm.ChangeLogSet;
import hudson.scm.EditType;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
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

    /**
     * `file`, `op`, `url` are the first changestep within the changeset.
     * Note that `file` is a path name and revision, separated by a ';' character.
     */
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

    public void setDateString(String dateString) {
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
    public Collection<DmFiles> getAffectedFiles() {
        return Collections.unmodifiableCollection(items);
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
    public static class DmFiles implements ChangeLogSet.AffectedFile {
        private String fileName;
        final private String operation;
        final private String url;
        private DimensionsChangeSet parent;

        public DmFiles() {
            this("", "", "");
        }

        /**
         * `fileName` is path name and revision separated by ';' character.
         */
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

        /**
         * Returns the path name and revision (separated by ';') of the changed file.
         */
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

        @Override
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

        /**
         * Returns just the path name of the changed file.
         */
        @Override
        public String getPath() {
            return strip(fileName);
        }

        /**
         * Removes leading '/'s and trailing ";revision" from `fileName`.
         */
        static String strip(String fileName) {
            if (fileName == null) {
                return "";
            }
            // Strip leading '/'.
            int from = 0;
            int to = fileName.length();
            while (from < to && fileName.charAt(from) == '/') {
                ++from;
            }
            // Strip ";revision".
            int sc = fileName.lastIndexOf(';');
            if (sc >= from) {
                to = sc;
            }
            return fileName.substring(from, to);
        }

        /**
         * Set the `fileName`, which is the path name and revision, separated by ';'.
         */
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
