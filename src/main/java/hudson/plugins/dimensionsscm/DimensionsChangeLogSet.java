package hudson.plugins.dimensionsscm;

import hudson.model.Run;
import hudson.scm.ChangeLogSet;
import hudson.scm.RepositoryBrowser;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a list of entries from the changelog.
 */
public final class DimensionsChangeLogSet extends ChangeLogSet<DimensionsChangeLogEntry> {
    private final List<DimensionsChangeLogEntry> entries;

    /**
     * When move to 1.568+, modify the following constructor:
     *
     * <pre>
     * DimensionsChangeLogSet(Run run, RepositoryBrowser<?> browser, List<DimensionsChangeLogEntry> entries) {
     *     super(run, browser);
     *     this.entries = incorporateChanges(entries, this);
     * }
     * </pre>
     */
    DimensionsChangeLogSet(Run<?, ?> build, RepositoryBrowser<?> browser, List<DimensionsChangeLogEntry> entries) {
        super(build, browser);
        this.entries = incorporateChanges(entries, this);
    }

    private static List<DimensionsChangeLogEntry> incorporateChanges(List<? extends DimensionsChangeLogEntry> entries, DimensionsChangeLogSet parent) {
        Collections.reverse(entries);
        for (DimensionsChangeLogEntry entry : entries) {
            entry.setParent(parent);
        }
        return Collections.unmodifiableList(entries);
    }

    @Override
    public boolean isEmptySet() {
        return entries.isEmpty();
    }

    @Override
    public Iterator<DimensionsChangeLogEntry> iterator() {
        return entries.iterator();
    }
}
