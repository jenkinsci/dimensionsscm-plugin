package hudson.plugins.dimensionsscm;

import java.io.File;

/**
 * Utility method for finding an executable on the system path.
 */
final class PathUtils {
    private PathUtils() {
        /* prevent instantiation. */
    }

    /**
     * Find an executable in the path.
     */
    static File getExecutable(String exeName) {
        // Get the path environment.
        String exec = exeName;
        if (System.getProperty("os.name").toLowerCase(Values.ROOT_LOCALE).startsWith("windows")) {
            exec += ".exe";
        }

        String path = System.getenv("PATH");
        if (path == null) {
            path = System.getenv("path");
        }
        if (path == null) {
            path = System.getenv("Path");
        }
        if (path == null) {
            Logger.debug("getExecutable file [" + exec + "] not found in null path");
            return null;
        }

        // Split it into directories.
        String[] pathDirs = path.split(File.pathSeparator);

        // Hunt through the directories to find the file I want.
        File exe = null;
        for (String pathDir : pathDirs) {
            File file = new File(pathDir, exec);
            if (file.isFile()) {
                exe = file;
                break;
            }
        }
        if (exe == null) {
            Logger.debug("getExecutable file [" + exec + "] not found in path [" + path + "]");
        }
        return exe;
    }
}
