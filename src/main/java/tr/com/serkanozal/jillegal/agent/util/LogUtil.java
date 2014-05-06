/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.agent.util;

import java.lang.reflect.Field;

public class LogUtil {   

	private static boolean logEnabled = true;
	
	private LogUtil() {
		
	}
	
	public static boolean isLogEnabled() {
		return logEnabled;
	}
	
	public static void setLogEnabled(boolean logEnabled) {
		LogUtil.logEnabled = logEnabled;
		
		try {
	    	Class<?> clazz = ClassLoaderUtil.loadClass(LogUtil.class.getName());
	
	        Field logEnabledField = clazz.getDeclaredField("logEnabled");
	        logEnabledField.setAccessible(true);
	        logEnabledField.set(null, logEnabled);
    	}
    	catch (Throwable t) {
        	error("Error at LogUtil.setLogEnabled(boolean logEnabled)", t);
        }
	}
	
	public static void intro() {
    	System.out.println("******************************************************************");
    	System.out.println("* Jillegal-Agent by Serkan OZAL (https://github.com/serkan-ozal) *");
    	System.out.println("******************************************************************");
    }
    
	public static void debug(String log) {
    	if (logEnabled) {
    		System.out.println("[DEBUG] " + log);
    	}
    }
  
	public static void info(String log) {
    	if (logEnabled) {
    		System.out.println("[INFO ] " + log);
    	}
    }
    
	public static void warn(String log) {
    	if (logEnabled) {
    		System.out.println("[WARN ] " +log);
    	}
    }
    
	public static void error(String msg, Throwable t) {
    	if (logEnabled) {
    		System.err.println(msg);
    		t.printStackTrace();
    	}
    }
	
}
