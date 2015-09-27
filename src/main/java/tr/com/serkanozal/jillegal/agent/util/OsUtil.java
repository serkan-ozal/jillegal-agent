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

/**
 * Utility class for some common OS related operations.
 * 
 * @author Serkan OZAL
 */
public final class OsUtil {   

	public static final String OS = System.getProperty("os.name").toLowerCase();
	
	private static boolean isWindows;
	private static boolean isUnix;
	private static boolean isMac;
	private static boolean isSolaris;
	
	static {
		init();
	}
	
	private OsUtil() {
		
	}
	
	private static void init() {
		isWindows = OS.indexOf("win") >= 0;
		isUnix = OS.indexOf("nux") >= 0;
		isMac = OS.indexOf("mac") >= 0;
		isSolaris = OS.indexOf("sunos") >= 0;
	}
	
	/**
	 * Checks the current operation system about if it is <b>Windows</b> or not.
	 * 
	 * @return <code>true</code> if the current operation system is <b>Windows</b>, 
	 *         otherwise <code>false</code>
	 */
	public static boolean isWindows() {
		return isWindows;
	}
	
	/**
     * Checks the current operation system about if it is <b>UNIX</b> or not.
     * 
     * @return <code>true</code> if the current operation system is <b>Windows</b>, 
     *         otherwise <code>false</code>
     */
	public static boolean isUnix() {
		return isUnix;
	}
		
	/**
     * Checks the current operation system about if it is <b>MacOSX</b> or not.
     * 
     * @return <code>true</code> if the current operation system is <b>Windows</b>, 
     *         otherwise <code>false</code>
     */
	public static boolean isMac() {
		return isMac;
	}

	/**
     * Checks the current operation system about if it is <b>Solaris</b> or not.
     * 
     * @return <code>true</code> if the current operation system is <b>Windows</b>, 
     *         otherwise <code>false</code>
     */
	public static boolean isSolaris() {
		return isSolaris;
	}

}
