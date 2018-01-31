package hudson.plugins.dimensionsscm;

import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogSet;
import hudson.scm.RepositoryBrowser;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a list of changes from the changelog.
 */
public final class DimensionsChangeSetList extends ChangeLogSet<DimensionsChangeSet> {
    private final List<DimensionsChangeSet> changes;

    /**
     * When move to 1.568+, modify the following constructor:
     *
     * <pre>
     * DimensionsChangeSetList(Run run, RepositoryBrowser<?> browser, List<DimensionsChangeSet> changes) {
     *     super(run, browser);
     *     this.changes = incorporateChanges(changes, this);
     * }
     * </pre>
     */
    DimensionsChangeSetList(AbstractBuild build, RepositoryBrowser<?> browser, List<DimensionsChangeSet> changes) {
        super(build);
        this.changes = incorporateChanges(changes, this);
    }

    private static List<DimensionsChangeSet> incorporateChanges(List<DimensionsChangeSet> changes, DimensionsChangeSetList parent) {
        Collections.reverse(changes);
        for (DimensionsChangeSet change : changes) {
            change.setParent(parent);
        }
        return Collections.unmodifiableList(changes);
    }

    @Override
    public boolean isEmptySet() {
        return changes.isEmpty();
    }

    @Override
    public Iterator<DimensionsChangeSet> iterator() {
        return changes.iterator();
    }
}
