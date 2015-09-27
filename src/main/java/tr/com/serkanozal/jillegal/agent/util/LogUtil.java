/*
 * Copyright (c) 1986-2015, Serkan OZAL, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tr.com.serkanozal.jillegal.agent.util;

import java.lang.reflect.Field;

/**
 * Utility class for some common log operations.
 * 
 * @author Serkan OZAL
 */
public final class LogUtil {   

	private static final LogLevel DEFAULT_LOG_LEVEL = LogLevel.DEBUG_LOG_LEVEL;
	
	private static volatile LogLevel logLevel = DEFAULT_LOG_LEVEL;
	
	static {
	    String logLevelPropertyValue = System.getProperty("jillegal.agent.logLevel");
	    if (logLevelPropertyValue != null) {
    	    if ("debug".equalsIgnoreCase(logLevelPropertyValue)) {
    	        logLevel = LogLevel.DEBUG_LOG_LEVEL;
    	    } else if ("info".equalsIgnoreCase(logLevelPropertyValue)) {
                logLevel = LogLevel.INFO_LOG_LEVEL;
            } else if ("warn".equalsIgnoreCase(logLevelPropertyValue)) {
                logLevel = LogLevel.WARNING_LOG_LEVEL;
            } else if ("error".equalsIgnoreCase(logLevelPropertyValue)) {
                logLevel = LogLevel.ERROR_LOG_LEVEL;
            } else {
                throw new IllegalArgumentException("Invalid log level: " + logLevelPropertyValue);
            }
	    }    
	}
	
	private LogUtil() {
		
	}
	
	/**
	 * Supported log levels.
	 * 
	 * @author Serkan OZAL
	 */
	public static enum LogLevel {
	      
	    DEBUG_LOG_LEVEL(0),
	    INFO_LOG_LEVEL(1),
	    WARNING_LOG_LEVEL(2),
	    ERROR_LOG_LEVEL(3),
	    DISABLED(4);
	    
	    int value;
	    
	    LogLevel(int value) {
	        this.value = value;
	    }
	    
	}
	
	/**
	 * Checks about if logging is enabled or not.
	 * 
	 * @return <code>true</code> if logging is enabled, otherwise <code>false</code>
	 */
	public static boolean isLoggingEnabled() {
		return logLevel.value < LogLevel.DISABLED.value;
	}

	/**
	 * Disables logging.
	 */
	public static void disableLogging() {
        setLogLevel(LogLevel.DISABLED);
    }
	
	/**
	 * Gets the current log level.
	 * 
	 * @return the current log level 
	 */
	public static LogLevel getLogLevel() {
        return logLevel;
    }
	
	/**
     * Sets the current log level.
     * 
     * @param logLevel the log level to be set as current log level 
     */
	public static void setLogLevel(LogLevel logLevel) {
	    if (logLevel == null) {
	        throw new IllegalArgumentException("Log level cannot be null!");
	    }
	    try {
            Class<?> clazz = ClassLoaderUtil.loadClass(LogUtil.class.getName());
    
            Field logEnabledField = clazz.getDeclaredField("logLevel");
            logEnabledField.setAccessible(true);
            logEnabledField.set(null, logLevel);
        }
        catch (Throwable t) {
            error("Error at LogUtil.setLogLevel(boolean logEnabled)", t);
        }
    }
	
	/**
	 * Prints intro message.
	 */
	public static void intro() {
    	System.out.println("******************************************************************");
    	System.out.println("* Jillegal-Agent by Serkan OZAL (https://github.com/serkan-ozal) *");
    	System.out.println("******************************************************************");
    }
    
	/**
	 * Writes given log message at DEBUG level.
	 * 
	 * @param msg the log message to be written
	 */
	public static void debug(String msg) {
    	if (logLevel.value <= LogLevel.DEBUG_LOG_LEVEL.value) {
    		System.out.println("[DEBUG] " + msg);
    	}
    }
  
	/**
     * Writes given log message at INFO level.
     * 
     * @param msg the log message to be written
     */
	public static void info(String msg) {
    	if (logLevel.value <= LogLevel.INFO_LOG_LEVEL.value) {
    		System.out.println("[INFO ] " + msg);
    	}
    }
    
	/**
     * Writes given log message at WARNING level.
     * 
     * @param msg the log message to be written
     */
	public static void warn(String msg) {
    	if (logLevel.value <= LogLevel.WARNING_LOG_LEVEL.value) {
    		System.out.println("[WARN ] " + msg);
    	}
    }
    
	/**
     * Writes given log message at ERROR level.
     * 
     * @param msg the log message to be written
     */
	public static void error(String msg, Throwable t) {
    	if (logLevel.value <= LogLevel.ERROR_LOG_LEVEL.value) {
    		System.err.println(msg);
    		t.printStackTrace();
    	}
    }
	
}
