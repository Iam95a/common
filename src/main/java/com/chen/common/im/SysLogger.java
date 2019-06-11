package com.chen.common.im;


import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * log4j2正常log包装类，用于定义log格式
 *
 * @author Richard
 */
public class SysLogger {

    private static final Logger logger = LogManager.getLogger(SysLogger.class.getName());

    public static void error(Class classname, String msg) {
        logMessage(classname, msg, Level.ERROR);
    }

    public static void error(Class classname, String msg, Throwable e) {
        logMessage(classname, msg, e, Level.ERROR);
    }

    public static void warn(Class classname, String msg) {
        logMessage(classname, msg, Level.WARN);
    }

    public static void warn(Class classname, String msg, Throwable e) {
        logMessage(classname, msg, e, Level.WARN);
    }

    public static void info(Class classname, String msg) {
        logMessage(classname, msg, Level.INFO);
    }

    public static void info(Class classname, String msg, Throwable e) {
        logMessage(classname, msg, Level.INFO);
    }

    public static void debug(Class classname, String msg) {
        logger.debug(classname + ":" + msg);
    }

    public static void debug(Class classname, String msg, Throwable e) {
        logger.debug(classname.getName() + ":" + msg, e);

    }


    private static void logMessage(Class classname, String msg, Level level) {
        msg = classname.getName() + ": " + msg;
        logger.log(level, msg);
    }

    private static void logMessage(Class classname, String msg, Throwable e, Level level) {
        msg = classname.getName() + ": " + msg;
        logger.log(level, msg, e);
    }
}
