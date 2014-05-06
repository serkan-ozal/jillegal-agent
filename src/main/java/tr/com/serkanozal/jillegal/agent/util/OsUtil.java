/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.agent.util;

public class OsUtil {   

	public static final String OS = System.getProperty("os.name").toLowerCase();
	
	private static boolean isWindows;
	private static boolean isLinux;
	private static boolean isMac;
	private static boolean isSolaris;
	
	static {
		init();
	}
	
	private OsUtil() {
		
	}
	
	private static void init() {
		isWindows = OS.indexOf("win") >= 0;
		isLinux = OS.indexOf("nux") >= 0;
		isMac = OS.indexOf("mac") >= 0;
		isSolaris = OS.indexOf("sunos") >= 0;
	}
	
	public static boolean isWindows() {
		return isWindows;
	}
	
	public static boolean isLinux() {
		return isLinux;
	}
		
	public static boolean isMac() {
		return isMac;
	}

	public static boolean isSolaris() {
		return isSolaris;
	}

}
