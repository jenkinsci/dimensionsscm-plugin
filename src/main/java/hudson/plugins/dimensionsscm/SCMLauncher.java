package hudson.plugins.dimensionsscm;

import hudson.FilePath;
import hudson.Launcher;
import hudson.Launcher.LocalLauncher;
import hudson.model.StreamBuildListener;
import hudson.model.TaskListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * Launch an executable (dmcli).
 */
class SCMLauncher implements Serializable {
    private final String[] args;
    private final Launcher launcher;
    private final FilePath workArea;
    private String results;

    SCMLauncher(final String[] args, final TaskListener listener, final FilePath area) {
        this.args = args;
        this.launcher = new LocalLauncher(listener);
        this.workArea = area;
    }

    /**
     * Get the command results.
     */
    String getResults() {
        return this.results;
    }

    /**
     * Execute the process.
     */
    Boolean execute() throws IOException, InterruptedException {
        boolean bRet;
        TaskListener listener = launcher.getListener();

        // Need to capture output into a file so I can parse it.
        File tmpFile = File.createTempFile("dmCm" + System.currentTimeMillis(), null, null);

        FileOutputStream fos = new FileOutputStream(tmpFile);
        StreamBuildListener os = new StreamBuildListener(fos);
        boolean[] masks = new boolean[args.length];

        int i = 0;
        for (String astr : args) {
            if (astr.equalsIgnoreCase("-param")) {
                masks[i] = true;
                masks[i + 1] = true;
            }
            i++;
        }

        try {
            Launcher.ProcStarter ps = launcher.launch();
            ps.cmds(args);
            ps.stdout(os.getLogger());
            ps.stdin(null);
            ps.pwd(workArea);
            ps.masks(masks);
            int cmdResult = ps.join();
            if (cmdResult != 0) {
                listener.fatalError("Execution of Dimensions command failed with exit code " + cmdResult);
                bRet = false;
            } else {
                bRet = true;
            }
        } finally {
            os.getLogger().flush();
            fos.close();
        }

        // Get the log file into a string for processing...
        results = new String(FileUtils.readAllBytes(tmpFile));
        tmpFile.delete();

        return bRet;
    }
}
