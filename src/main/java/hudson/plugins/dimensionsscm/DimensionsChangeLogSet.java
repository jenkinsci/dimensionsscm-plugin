package hudson.plugins.dimensionsscm;

import hudson.model.Run;
import hudson.scm.ChangeLogSet;
import hudson.scm.RepositoryBrowser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a list of entries from the changelog.
 */
public final class DimensionsChangeLogSet extends ChangeLogSet<DimensionsChangeLogEntry> {
    private final List<DimensionsChangeLogEntry> entries;

    DimensionsChangeLogSet(final Run<?, ?> run, final RepositoryBrowser<?> browser, final List<DimensionsChangeLogEntry> entries) {
        super(run, browser);
        final List<DimensionsChangeLogEntry> list = new ArrayList<>(entries.size());
        list.addAll(entries);
        Collections.reverse(list);
        list.forEach(entry -> entry.setParent(this));
        this.entries = list;
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
