package org.apache.ivy.util;

import java.util.List;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.IvyContext;

/**
 * Logging utility class.
 * <p>
 * This class provides static methods for easy access to the current logger in {@link IvyContext}.
 * </p>
 * <p>
 * To configure logging, you should use the methods provided by the {@link MessageLoggerEngine}
 * associated with the {@link Ivy} engine.
 * </p>
 */
public final class Message {
    /** Message priority of "error". */
    public static final int MSG_ERR = 0;

    /** Message priority of "warning". */
    public static final int MSG_WARN = 1;

    /** Message priority of "information". */
    public static final int MSG_INFO = 2;

    /** Message priority of "verbose". */
    public static final int MSG_VERBOSE = 3;

    /** Message priority of "debug". */
    public static final int MSG_DEBUG = 4;


    private static boolean showedInfo = false;

    private static MessageLogger defaultLogger = new DefaultMessageLogger(Message.MSG_INFO);

    /**
     * Returns the current default logger.
     * @return the current default logger; is never <code>null</code>.
     */
    public static MessageLogger getDefaultLogger() {
        return defaultLogger;
    }

    /**
     * Change the default logger used when no other logger is currently configured
     * @param logger the new default logger, must not be <code>null</code>
     */
    public static void setDefaultLogger(MessageLogger logger) {
        Checks.checkNotNull(logger, "logger");
        defaultLogger = logger;
    }

    private static MessageLogger getLogger() {
        return IvyContext.getContext().getMessageLogger();
    }

    public static void showInfo() {
        if (!showedInfo) {
            info(":: Ivy " + Ivy.getIvyVersion() + " - "
                   + Ivy.getIvyDate() + " :: " + Ivy.getIvyHomeURL() + " ::");
            showedInfo = true;
        }
    }

    public static void debug(String msg) {
        getLogger().debug(msg);
    }

    public static void verbose(String msg) {
        getLogger().verbose(msg);
    }

    public static void info(String msg) {
        getLogger().info(msg);
    }

    public static void rawinfo(String msg) {
        getLogger().rawinfo(msg);
    }

    public static void deprecated(String msg) {
        getLogger().deprecated(msg);
    }

    public static void warn(String msg) {
        getLogger().warn(msg);
    }

    public static void error(String msg) {
        getLogger().error(msg);
    }

    public static List getProblems() {
        return getLogger().getProblems();
    }

    public static void sumupProblems() {
        getLogger().sumupProblems();
    }

    public static void progress() {
        getLogger().progress();
    }

    public static void endProgress() {
        getLogger().endProgress();
    }

    public static void endProgress(String msg) {
        getLogger().endProgress(msg);
    }

    public static boolean isShowProgress() {
        return getLogger().isShowProgress();
    }

    public static void setShowProgress(boolean progress) {
        getLogger().setShowProgress(progress);
    }

    private Message() {
    }
}
