package com.chty.autocard.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerUtils {
    
    public enum LogLevel {DEBUG, INFO, ERROR;}
    
    public static void print(String message, Throwable t, LogLevel level, String className) {
        Logger logger = LoggerFactory.getLogger(className);
        if(t != null && message == null)  {
            message = t.getMessage();
        }
        switch (level) {
            case INFO:  logger.info(message, t); break;
            case DEBUG: logger.debug(message, t); break;
            case ERROR: logger.error(message, t); break;
        }
    }
    
    public static void print(String message, LogLevel level) {
        StackTraceElement[] stack = new Throwable().getStackTrace();
        String className = stack[stack.length > 1 ? 1 : 0].getClassName();
        LoggerUtils.print(message, null, level, className);
    }
    
    public static void debug(String message) {
        LoggerUtils.print(message, LogLevel.DEBUG);
    }
    
    public static void info(String message) {
        LoggerUtils.print(message, LogLevel.INFO);
    }
    
    public static void error(String message) {
        LoggerUtils.print(message, LogLevel.ERROR);
    }
    
}
