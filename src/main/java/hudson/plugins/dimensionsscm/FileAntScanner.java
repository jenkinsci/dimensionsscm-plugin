package hudson.plugins.dimensionsscm;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;

/**
 * Scan for files to deliver using Ant-style patterns.
 */
public class FileAntScanner implements Serializable {
    private static final String[] DEFAULT_EXCLUDES = new String[] {
            "**/.dm", "**/.dm/*", "**/.metadata", "**/.metadata/*" };

    private final Collection<File> xfiles;

    /**
     * File pattern matcher class.
     */
    static class ScannerFilter {
        private final FileSet fileSet;
        private final Project project;

        ScannerFilter(String[] inclusionsx, String[] exclusionsx, File dirName) {
            project = new Project();
            project.setBaseDir(dirName);
            fileSet = new FileSet();
            fileSet.setProject(project);
            fileSet.setDir(dirName);
            fileSet.appendIncludes(Values.trimCopy(inclusionsx));
            fileSet.appendExcludes(Values.trimCopy(exclusionsx));
            fileSet.appendExcludes(DEFAULT_EXCLUDES);
        }

        FileSet getFileSet() {
            return fileSet;
        }

        Project getProject() {
            return project;
        }
    }

    public FileAntScanner(File dirName, String[] patterns, String[] patternsExc, int depth) {
        ScannerFilter filter = new ScannerFilter(patterns, patternsExc, dirName);
        xfiles = scanFiles(dirName, filter, depth);
    }

    public File[] toArray() {
        File[] arr = new File[xfiles.size()];
        return xfiles.toArray(arr);
    }

    private Collection<File> scanFiles(File dirName, ScannerFilter filter, int depth) {
        String[] dfiles = filter.getFileSet().getDirectoryScanner(filter.getProject()).getIncludedFiles();
        List<File> files = new ArrayList<File>(dfiles.length);
        for (String dfile : dfiles) {
            files.add(new File(dirName.getAbsolutePath() + "/" + dfile));
        }
        return files;
    }
}
