package hudson.plugins.dimensionsscm;

import static hudson.plugins.dimensionsscm.LogInitializer.LOGGER;
import java.util.logging.Level;

final class Logger {
    private Logger() {
        /* prevent instantiation. */
    }

    static boolean isDebugEnabled() {
        return LOGGER.isLoggable(Level.FINE);
    }

    /**
     * Print a message to the Jenkins System Log under logger name `hudson.plugins.dimensionsscm`.
     */
    static void debug(String str) {
        Logger.debug(str, null);
    }

    /**
     * Print a message and an exception (with stack trace) to the Jenkins System Log under logger name `hudson.plugins.dimensionsscm`.
     */
    static void debug(String str, Throwable thr) {
        if (LOGGER.isLoggable(Level.FINE)) {
            // Otherwise <tt>getSourceClass</tt> and <tt>getSourceMethod</tt>
            // always return <tt>"hudson.plugins.dimensionsscm.Logger"</tt>
            // and <tt>"debug"</tt> respectively.
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            boolean found = false;
            String sourceClass = null;
            String sourceMethod = null;
            for (StackTraceElement stackTraceElement : stackTraceElements) {
                sourceClass = stackTraceElement.getClassName();
                sourceMethod = stackTraceElement.getMethodName();
                if (found && !Logger.class.getName().equals(sourceClass)) {
                    break;
                }
                if (!found && Logger.class.getName().equals(sourceClass)) {
                    found = true;
                }
            }
            // The actual JDK logging method call.
            if (sourceClass != null && sourceMethod != null) {
                if (thr == null) {
                    LOGGER.logp(Level.FINE, sourceClass, sourceMethod, str);
                } else {
                    LOGGER.logp(Level.FINE, sourceClass, sourceMethod, str, thr);
                }
            } else {
                if (thr == null) {
                    LOGGER.log(Level.FINE, str);
                } else {
                    LOGGER.log(Level.FINE, str, thr);
                }
            }
        }
    }
}
