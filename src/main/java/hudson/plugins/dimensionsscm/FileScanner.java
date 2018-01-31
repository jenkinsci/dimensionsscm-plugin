package hudson.plugins.dimensionsscm;

import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * Scan for files to deliver using regular expression patterns.
 */
public class FileScanner implements Serializable {
    private final Collection<File> xfiles;

    /**
     * File pattern matcher class.
     */
    static class ScannerFilter implements FilenameFilter {
        private final Set<String> artifactIncFilter;
        private final Set<String> artifactExcFilter;

        ScannerFilter(String[] inclusionsx, String[] exclusionsx) {
            // Remove empty, null, whitespace, duplicate values.
            artifactIncFilter = new TreeSet<String>();
            artifactIncFilter.addAll(Arrays.asList(Values.trimCopy(inclusionsx)));
            artifactExcFilter = new TreeSet<String>();
            artifactExcFilter.addAll(Arrays.asList(Values.trimCopy(exclusionsx)));
        }

        @Override
        public boolean accept(File basedir, String name) {
            for (String filter : artifactExcFilter) {
                if (Pattern.matches(filter, name)) {
                    return false;
                }
            }
            for (String filter : artifactIncFilter) {
                if (Pattern.matches(filter, name)) {
                    return true;
                }
            }
            return false;
        }
    }

    public FileScanner(File basedir, String[] patterns, String[] patternsExc, int depth) {
        ScannerFilter filter = new ScannerFilter(patterns, patternsExc);
        xfiles = scanFiles(basedir, basedir, filter, depth);
    }

    public File[] toArray() {
        File[] xarr = new File[xfiles.size()];
        return xfiles.toArray(xarr);
    }

    private static boolean isIgnorable(File folder) {
        if (folder == null) {
            return true;
        }
        if (folder.isDirectory()) {
            String name = folder.getName();
            // Dimensions CM work area metadata folders (old or new format).
            if (".metadata".equals(name) || ".dm".equals(name)) {
                return true;
            }
        }
        return false;
    }

    private Collection<File> scanFiles(File basedir, File subdir, FilenameFilter filter, int depth) {
        if (isIgnorable(subdir)) {
            return Collections.emptyList();
        }
        File[] childFiles = subdir.listFiles();
        if (childFiles == null || childFiles.length == 0) {
            return Collections.emptyList();
        }
        List<File> ret = new ArrayList<File>();
        for (File childFile : childFiles) {
            if (isIgnorable(childFile)) {
                continue;
            }
            String path = childFile.getAbsolutePath();
            path = path.substring(basedir.getAbsolutePath().length() + 1);
            if (filter == null || filter.accept(basedir, path)) {
                ret.add(childFile);
            }
            if (depth != 0 && childFile.isDirectory()) {
                ret.addAll(scanFiles(basedir, childFile, filter, depth - 1));
            }
        }
        return Collections.unmodifiableList(ret);
    }
}
