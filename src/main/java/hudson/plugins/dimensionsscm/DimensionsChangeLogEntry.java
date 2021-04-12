package hudson.plugins.dimensionsscm;

import hudson.model.User;
import hudson.scm.ChangeLogSet;
import hudson.scm.EditType;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import static java.util.Comparator.*;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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
    private final List<FileChange> fileChanges;
    private final List<IRTRequest> irtRequests;

    // Digester class seems to need a no-parameter constructor else it crashes
    public DimensionsChangeLogEntry() {
        this("", "", "", "", "", "", null);
    }

    /**
     * `file`, `op`, `url` are the first file-change within the entry.
     * Note that `file` is a path name and revision, separated by a ';' character.
     */
    public DimensionsChangeLogEntry(final String file, final String developer,
            final String op, final String revision, final String comment,
            final String url, final Calendar date) {
        this.identifier = file;
        this.developer = developer;
        this.message = comment;
        this.date = date;
        this.version = revision;
        this.fileChanges = new ArrayList<>();
        if ((file != null && !file.isEmpty()) || (op != null && !op.isEmpty())
                || (url != null && !url.isEmpty())) {
            add(file, op, url);
        }
        this.irtRequests = new ArrayList<>();
    }

    @Override
    public void setParent(@SuppressWarnings("rawtypes") final ChangeLogSet parent) {
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

    public void setDateString(final String dateString) {
        this.date = Calendar.getInstance();
        final TimeZone tz = TimeZone.getTimeZone("UTC");
        this.date.setTime(DateUtils.parse(dateString, tz));
    }

    public Collection<FileChange> getFiles() {
        return this.fileChanges.stream()
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());
    }

    public Collection<IRTRequest> getRequests() {
        return this.irtRequests.stream()
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public Collection<FileChange> getAffectedFiles() {
        return getFiles();
    }

    @Override
    public Collection<String> getAffectedPaths() {
        return this.fileChanges.stream()
                .filter(Objects::nonNull)
                .map(FileChange::getFile)
                .filter(Objects::nonNull)
                .sorted()
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public User getAuthor() {
        if (this.developer == null) {
            throw new RuntimeException("Unable to determine change's developer");
        }
        return User.getOrCreateByIdOrFullName(this.developer);
    }

    @Override
    public String getMsg() {
        return this.message;
    }

    public String getId() {
        return this.identifier;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public String getVersion() {
        return this.version;
    }

    public void setUser(final String developer) {
        this.developer = developer;
    }

    public String getUser() {
        return this.developer;
    }

    public void setComment(final String message) {
        this.message = message;
    }

    public String getComment() {
        return getSCMComment();
    }

    public void add(final FileChange fileChange) {
        this.fileChanges.add(fileChange);
    }

    public void add(final String file, final String operation, final String url) {
        add(new FileChange(file, operation, url));
    }

    public void addRequest(final IRTRequest irtRequest) {
        if (!this.irtRequests.stream()
                .filter(Objects::nonNull)
                .map(IRTRequest::getIdentifier)
                .anyMatch(irtRequest.identifier::equals)) {
            this.irtRequests.add(irtRequest);
        }
    }

    public void addRequest(final String identifier, final String url, final String title) {
        addRequest(new IRTRequest(identifier, url, title));
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
        public FileChange(final String file, final String operation, final String url) {
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
                final FileChange that = (FileChange) obj;
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

        public void setOperation(final String operation) {
            this.operation = operation;
        }

        public void setUrl(final String url) {
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
        public void setFile(final String file) {
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
        static String strip(final String file) {
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
            final int sc = file.lastIndexOf(';');
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

        public IRTRequest(final String identifier, final String url, final String title) {
            this.identifier = identifier;
            this.url = url;
            this.title = title;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof IRTRequest) {
                final IRTRequest that = (IRTRequest) obj;
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

        public void setUrl(final String url) {
            this.url = url;
        }

        @Exported
        public String getIdentifier() {
            return this.identifier == null || this.identifier.length() == 0 ? null : this.identifier;
        }

        public void setIdentifier(final String identifier) {
            this.identifier = identifier;
        }

        @Exported
        public String getTitle() {
            return this.title == null || this.title.length() == 0 ? null : this.title;
        }

        public void setTitle(final String title) {
            this.title = title;
        }
    }
}
