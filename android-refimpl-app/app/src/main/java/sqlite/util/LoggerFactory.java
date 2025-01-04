package org.sqlite.util;

import android.os.Build;

import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * A factory for {@link Logger} instances that uses SLF4J if present, falling back on a
 * java.util.logging implementation otherwise.
 */
public class LoggerFactory {
    static final boolean USE_SLF4J = false;

    /**
     * Get a {@link Logger} instance for the given host class.
     *
     * @param hostClass the host class from which log messages will be issued
     * @return a Logger
     */
    public static Logger getLogger(Class<?> hostClass) {
        return new JDKLogger(hostClass);
    }

    private static class JDKLogger implements Logger {
        final java.util.logging.Logger logger;

        public JDKLogger(Class<?> hostClass) {
            logger = java.util.logging.Logger.getLogger(hostClass.getCanonicalName());
        }

        @Override
        public void trace(Supplier<String> message) {
            if (logger.isLoggable(Level.FINEST)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                {
                    logger.log(Level.FINEST, message.get());
                }
            }
        }

        @Override
        public void info(Supplier<String> message) {
            if (logger.isLoggable(Level.INFO)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                {
                    logger.log(Level.INFO, message.get());
                }
            }
        }

        @Override
        public void warn(Supplier<String> message) {
            if (logger.isLoggable(Level.WARNING)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                {
                    logger.log(Level.WARNING, message.get());
                }
            }
        }

        @Override
        public void error(Supplier<String> message, Throwable t) {
            if (logger.isLoggable(Level.SEVERE)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                {
                    logger.log(Level.SEVERE, message.get(), t);
                }
            }
        }
    }
}
