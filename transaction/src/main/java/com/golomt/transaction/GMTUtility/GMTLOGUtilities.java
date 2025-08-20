package com.golomt.transaction.GMTUtility;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.slf4j.LoggerFactory;

public class GMTLOGUtilities {
    public static void log(String key, Integer level, String message, Throwable throwable) {
        switch (level) {
            case Priority.DEBUG_INT:
                LoggerFactory.getLogger(key).debug(message);
                break;
            case Priority.INFO_INT:
                LoggerFactory.getLogger(key).info(message);
                break;
            case Priority.WARN_INT:
                LoggerFactory.getLogger(key).warn(message);
                break;
            case Priority.ERROR_INT:
                LoggerFactory.getLogger(key).error(message);
                break;
            case Priority.FATAL_INT:
                LoggerFactory.getLogger(key).error(message, throwable);
                break;
        }
    }

    public static void debug(String key, String message) {
        log(key, Priority.DEBUG_INT, message, null);
    }

    public static void info(String key, String message) {
        log(key, Priority.INFO_INT, message, null);
    }

    public static void warn(String key, String message) {
        log(key, Priority.WARN_INT, message, null);
    }

    public static void error(String key, String message) {
        log(key, Priority.ERROR_INT, message, null);
    }

    public static void fatal(String key, String message, Throwable throwable) {
        log(key, Priority.FATAL_INT, message, throwable);
    }
}
