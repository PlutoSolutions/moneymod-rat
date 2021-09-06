/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.Level
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package net.minecraft.launchwrapper;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogWrapper {
    public static LogWrapper log = new LogWrapper();
    private Logger myLog;
    private static boolean configured;

    private static void configureLogging() {
        LogWrapper.log.myLog = LogManager.getLogger((String)"LaunchWrapper");
        configured = true;
    }

    public static void retarget(Logger to) {
        LogWrapper.log.myLog = to;
    }

    public static void log(String logChannel, Level level, String format, Object ... data) {
        LogWrapper.makeLog(logChannel);
        LogManager.getLogger((String)logChannel).log(level, String.format(format, data));
    }

    public static void log(Level level, String format, Object ... data) {
        if (!configured) {
            LogWrapper.configureLogging();
        }
        LogWrapper.log.myLog.log(level, String.format(format, data));
    }

    public static void log(String logChannel, Level level, Throwable ex, String format, Object ... data) {
        LogWrapper.makeLog(logChannel);
        LogManager.getLogger((String)logChannel).log(level, String.format(format, data), ex);
    }

    public static void log(Level level, Throwable ex, String format, Object ... data) {
        if (!configured) {
            LogWrapper.configureLogging();
        }
        LogWrapper.log.myLog.log(level, String.format(format, data), ex);
    }

    public static void severe(String format, Object ... data) {
        LogWrapper.log(Level.ERROR, format, data);
    }

    public static void warning(String format, Object ... data) {
        LogWrapper.log(Level.WARN, format, data);
    }

    public static void info(String format, Object ... data) {
        LogWrapper.log(Level.INFO, format, data);
    }

    public static void fine(String format, Object ... data) {
        LogWrapper.log(Level.DEBUG, format, data);
    }

    public static void finer(String format, Object ... data) {
        LogWrapper.log(Level.TRACE, format, data);
    }

    public static void finest(String format, Object ... data) {
        LogWrapper.log(Level.TRACE, format, data);
    }

    public static void makeLog(String logChannel) {
        LogManager.getLogger((String)logChannel);
    }
}

