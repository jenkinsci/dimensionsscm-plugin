package hudson.plugins.dimensionsscm;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Recommended usage of this class is <code>import static hudson.plugins.dimensionsscm.LogInitializer.LOGGER;</code> and
 * then use <code>LOGGER.log(Level.FINE, "Doing stuff");</code>.
 * <p>
 * Set the System property <code>hudson.plugins.dimensionsscm.log</code> to the value <code>stderr</code> or a filename to
 * force log output other than to the standard Jenkins System Log.
 */
final class LogInitializer {
    static final Logger LOGGER;

    static {
        Logger logger = Logger.getLogger("hudson.plugins.dimensionsscm");
        String dest = System.getProperty("hudson.plugins.dimensionsscm.log");
        if (dest == null) {
            dest = System.getenv("DM_JENKINS_LOGGING");
        }
        if (dest != null) {
            try {
                if (dest.equalsIgnoreCase("stderr")) {
                    logger.addHandler(new ConsoleHandler());
                    logger.setLevel(Level.FINE);
                } else {
                    logger.addHandler(new FileHandler(dest));
                    logger.setLevel(Level.FINE);
                }
            } catch (IOException e) {
                logger.log(Level.INFO, "Could not add handler for [" + dest + "]", e);
            }
        }
        LOGGER = logger;
    }

    private LogInitializer() {
        /* prevent instantiation. */
    }
}
