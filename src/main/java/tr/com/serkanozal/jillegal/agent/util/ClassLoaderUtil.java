/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.agent.util;

public class ClassLoaderUtil {   
	
	private ClassLoaderUtil() {
		
	}
	
	public static ClassLoader getClassLoader() {
		ClassLoader classLoader = ClassLoaderUtil.class.getClassLoader();
    	if (classLoader == null) {
    		return ClassLoader.getSystemClassLoader();
    	}
    	else {
    		return classLoader;
    	}
	}
	
	public static Class<?> loadClass(String className) {
		try {
			return getClassLoader().loadClass(className);
		}
		catch (Throwable t) {
			LogUtil.error("Error at ClassLoaderUtil.loadClass(String className)", t);
			return null;
		}
	}
	
}
