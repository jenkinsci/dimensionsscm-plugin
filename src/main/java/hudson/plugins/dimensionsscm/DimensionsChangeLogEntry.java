package hudson.plugins.dimensionsscm;

import hudson.model.User;
import hudson.scm.ChangeLogSet;
import hudson.scm.EditType;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import static java.util.Comparator.*;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Represents an individual change in the changelog.
 */
@ExportedBean(defaultVisibility = 999)
public class DimensionsChangeLogEntry extends ChangeLogSet.Entry {
    private String developer;
    private String message;
    private final String identifier;
    private Calendar date;
    private String version;
    private final Set<FileChange> fileChanges;
    private final Set<IRTRequest> irtRequests;

    // Digester class seems to need a no-parameter constructor else it crashes
    public DimensionsChangeLogEntry() {
        this("", "", "", "", "", "", null);
    }

    /**
     * `file`, `op`, `url` are the first file-change within the entry.
     * Note that `file` is a path name and revision, separated by a ';' character.
     */
    public DimensionsChangeLogEntry(String file, String developer, String op, String revision, String comment, String url,
            Calendar date) {
        this.identifier = file;
        this.developer = developer;
        this.message = comment;
        this.date = date;
        this.version = revision;
        this.fileChanges = new HashSet<>();
        this.fileChanges.add(new FileChange(file, op, url));
        this.irtRequests = new HashSet<>();
    }

    @Override
    public void setParent(@SuppressWarnings("rawtypes") ChangeLogSet parent) {
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

    public Collection<FileChange> getFiles() {
        List<FileChange> list = new ArrayList<>();
        list.addAll(this.fileChanges);
        Collections.sort(list);
        return list;
    }

    public Collection<IRTRequest> getRequests() {
        List<IRTRequest> list = new ArrayList<>();
        list.addAll(this.irtRequests);
        Collections.sort(list);
        return list;
    }

    @Override
    public Collection<FileChange> getAffectedFiles() {
        return Collections.unmodifiableCollection(getFiles());
    }

    @Override
    public Collection<String> getAffectedPaths() {
        List<String> paths = getFiles().stream().map(FileChange::getFile).collect(Collectors.toList());
        Collections.sort(paths);
        return paths;
    }

    @Override
    public User getAuthor() {
        if (this.developer == null) {
            throw new RuntimeException("Unable to determine change's developer");
        }
        return User.get(this.developer);
    }

    @Override
    public String getMsg() {
        return this.message;
    }

    public String getId() {
        return this.identifier;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return this.version;
    }

    public void setUser(String developer) {
        this.developer = developer;
    }

    public String getUser() {
        return this.developer;
    }

    public void setComment(String message) {
        this.message = message;
    }

    public String getComment() {
        return getSCMComment();
    }

    public void add(FileChange fileChange) {
        fileChanges.add(fileChange);
    }

    public void add(String file, String operation, String url) {
        FileChange fileChange = new FileChange(file, operation, url);
        fileChanges.add(fileChange);
    }

    public void addRequest(final IRTRequest newRequest) {
        for (IRTRequest irtRequest : irtRequests) {
            if (irtRequest.getIdentifier().equals(newRequest.getIdentifier())) {
                return;
            }
        }
        irtRequests.add(newRequest);
    }

    public void addRequest(final String requestId, final String requestUrl) {
        for (IRTRequest irtRequest : irtRequests) {
            if (irtRequest.getIdentifier().equals(requestId)) {
                return;
            }
        }
        final IRTRequest newRequest = new IRTRequest(requestId, requestUrl);
        irtRequests.add(newRequest);
    }

    public void addRequest(final String requestId, final String requestUrl, final String requestTitle) {
        for (IRTRequest irtRequest : irtRequests) {
            if (irtRequest.getIdentifier().equals(requestId)) {
                return;
            }
        }
        final IRTRequest newRequest = new IRTRequest(requestId, requestUrl, requestTitle);
        irtRequests.add(newRequest);
    }

    /**
     * An individual file-change made in the repository for this entry.
     */
    @ExportedBean(defaultVisibility = 999)
    public static final class FileChange implements ChangeLogSet.AffectedFile, Comparable<FileChange> {
        private String file;
        private String operation;
        private String url;

        public FileChange() {
            this("", "", "");
        }

        /**
         * `file` is path name and revision separated by ';' character.
         */
        public FileChange(String file, String operation, String url) {
            this.file = file;
            this.url = url;
            this.operation = operation;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            } else if (!(obj instanceof FileChange)) {
                return false;
            } else {
                FileChange that = (FileChange) obj;
                return Objects.equals(this.file, that.file)
                        && Objects.equals(this.operation, that.operation)
                        && Objects.equals(this.url, that.url);
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(file, operation, url);
        }

        private static final Comparator<FileChange> COMPARATOR = nullsFirst(
                comparing(FileChange::getFile, nullsFirst(naturalOrder()))
                        .thenComparing(FileChange::getOperation, nullsFirst(naturalOrder()))
                        .thenComparing(FileChange::getUrl, nullsFirst(naturalOrder())));

        @Override
        public int compareTo(final FileChange that) {
            return COMPARATOR.compare(this, that);
        }

        @Exported
        public String getUrl() {
            return this.url == null || this.url.length() == 0 ? null : this.url;
        }

        @Exported
        public String getOperation() {
            return this.operation;
        }

        public void setOperation(String operation) {
            this.operation = operation;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        /**
         * Returns the path name and revision (separated by ';') of the changed file.
         */
        @Exported
        public String getFile() {
            return this.file == null || this.file.length() == 0 ? null : this.file;
        }

        /**
         * Set the `file`, which is the path name and revision, separated by ';'.
         */
        public void setFile(String file) {
            this.file = file;
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
            return strip(file);
        }

        /**
         * Removes leading '/'s and trailing ";revision" from `file`.
         */
        static String strip(String file) {
            if (file == null) {
                return "";
            }
            // Strip leading '/'.
            int from = 0;
            int to = file.length();
            while (from < to && file.charAt(from) == '/') {
                ++from;
            }
            // Strip ";revision".
            int sc = file.lastIndexOf(';');
            if (sc >= from) {
                to = sc;
            }
            return file.substring(from, to);
        }


    }

    @ExportedBean(defaultVisibility = 999)
    public static final class IRTRequest implements Comparable<IRTRequest> {
        private String identifier;
        private String url;
        private String title;

        public IRTRequest() {
            this("", "", "");
        }

        public IRTRequest(String objectID, String url) {
            this.identifier = objectID;
            this.url = url;
            this.title = "";
        }

        public IRTRequest(String objectID, String url, String title) {
            this.identifier = objectID;
            this.url = url;
            this.title = title;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof IRTRequest) {
                IRTRequest that = (IRTRequest) obj;
                return Objects.equals(this.identifier, that.identifier)
                        && Objects.equals(this.title, that.title)
                        && Objects.equals(this.url, that.url);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(identifier, title, url);
        }

        private static final Comparator<IRTRequest> COMPARATOR = nullsFirst(
                comparing(IRTRequest::getIdentifier, nullsFirst(naturalOrder()))
                        .thenComparing(IRTRequest::getTitle, nullsFirst(naturalOrder()))
                        .thenComparing(IRTRequest::getUrl, nullsFirst(naturalOrder())));

        @Override
        public int compareTo(final IRTRequest that) {
            return COMPARATOR.compare(this, that);
        }

        @Exported
        public String getUrl() {
            return this.url == null || this.url.length() == 0 ? null : this.url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        @Exported
        public String getIdentifier() {
            return this.identifier == null || this.identifier.length() == 0 ? null : this.identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }

        @Exported
        public String getTitle() {
            return this.title == null || this.title.length() == 0 ? null : this.title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}
