/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.agent;

import java.io.File;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.StringTokenizer;
import java.util.jar.JarFile;

import sun.management.VMManagement;
import tr.com.serkanozal.jillegal.agent.util.ClassLoaderUtil;
import tr.com.serkanozal.jillegal.agent.util.LogUtil;
import tr.com.serkanozal.jillegal.agent.util.OsUtil;

import com.sun.tools.attach.VirtualMachine;

@SuppressWarnings("restriction")
public class JillegalAgent {   

	public static String VERSION = "1.1.0-RELEASE";
	
	final static public String INSTR_JAR_PREFIX = "jillegal-agent";
	
	private static Instrumentation inst;
	private static boolean initialized = false;
	
	private JillegalAgent() {
	    
	}

	public static void agentmain(String arguments, Instrumentation i) {   
	    initAtMain(arguments, i);
	    LogUtil.debug("Agentmain: " + inst + " - " + "Arguments: " + arguments);
	}
	
    public static void premain(String arguments, Instrumentation i) {
        initAtMain(arguments, i);
        LogUtil.debug("Premain: " + inst + " - " + "Arguments: " + arguments);
    }

    private static String getClassPath() {
    	ClassLoader classLoader = ClassLoaderUtil.getClassLoader();

    	StringBuilder classPathBuilder = new StringBuilder();
    	
    	classPathBuilder.
    		append(System.getProperty("java.class.path")).
    		append(File.pathSeparator);
    	
    	if (System.getProperty("surefire.test.class.path") != null) {
    		classPathBuilder.
    			append(System.getProperty("surefire.test.class.path")).
    			append(File.pathSeparator);		
    	}
    	
    	if (classLoader instanceof URLClassLoader) {
	    	URLClassLoader urlClassLoader = (URLClassLoader)classLoader;
	    	URL[] urls = urlClassLoader.getURLs();
	    	for (URL u : urls) {
	    		String filePath = u.getFile();
	    		if (OsUtil.isWindows() && filePath.startsWith("/")) {
	    			filePath = filePath.substring(1);
	    		}
	    		classPathBuilder.
	    			append(filePath).
	    			append(File.pathSeparator);
	    	}
    	}
    	
    	return classPathBuilder.toString();
    }
    
    private static void initAtMain(String arguments, Instrumentation i) {
        try {
            inst = i;

            JarFile agentJarFile = null;
            
            final StringTokenizer st = new StringTokenizer(getClassPath(), File.pathSeparator);
            while (st.hasMoreTokens()) {
                String classpathEntry = st.nextToken().trim();
                File f = new File(classpathEntry);
                if (f.exists() && f.getName().startsWith(INSTR_JAR_PREFIX)) {
                    agentJarFile = new JarFile(classpathEntry);
                    break;
                }
            }
            
            LogUtil.debug("Agent Jar File: " + agentJarFile.getName());
            
            if (agentJarFile != null) {
                inst.appendToBootstrapClassLoaderSearch(agentJarFile);
            }    
            if (agentJarFile != null) {
                inst.appendToSystemClassLoaderSearch(agentJarFile);
            }   
            
            initialized = true;
            
            Class<?> clazz = 
            		ClassLoader.getSystemClassLoader().loadClass(JillegalAgent.class.getName());
            
            Field instField = clazz.getDeclaredField("inst");
            instField.setAccessible(true);
            instField.set(null, inst);
            
            Field initializedField = clazz.getDeclaredField("initialized");
            initializedField.setAccessible(true);
            initializedField.set(null, initialized);
        }
        catch (Throwable t) {
        	LogUtil.error("Error at JillegalAgent.initAtMain()", t);
        }
    }
    
    private static void initInstrumentationIfNeeded() {
    	if (inst == null) {
    		try {
    			Class<?> clazz = 
    					ClassLoader.getSystemClassLoader().loadClass(JillegalAgent.class.getName());
    			
    			Field instField = clazz.getDeclaredField("inst");
                instField.setAccessible(true);
                inst = (Instrumentation) instField.get(null);
    		}
    		catch (Throwable t) {
    			LogUtil.error("Error at JillegalAgent.initInstrumentationIfNeeded()", t);
            }
    	}
    }

    public static Instrumentation getInstrumentation() {
    	initInstrumentationIfNeeded();
        return inst;
    }
    
    public static void init() {
    	init(null);
    }
    
    public static void init(String arguments) {
    	if (initialized) {
    		LogUtil.warn("Agent has been already initialized");
    		return;
    	}
    	
        try {
        	LogUtil.intro();
        	
            loadAgent();
        }
        catch (Throwable t) {
        	LogUtil.error("Error at JillegalAgent.init(String arguments)", t);
        }
    }
    
    private static void loadAgent() throws Exception {
        loadAgent(null);
    }
    
	private static void loadAgent(String arguments) throws Exception {
    	VirtualMachine vm = VirtualMachine.attach(getPidFromRuntimeMBean());
    	String agentPath = null;
    	
    	String classPath = getClassPath();
    	
    	LogUtil.info("OS Name: " + OsUtil.OS);
    	LogUtil.info("Class Path: " + classPath);
    	
    	for (String entry : classPath.split(File.pathSeparator)) {
    		File f = new File(entry);
            if (f.exists() && f.getName().startsWith(INSTR_JAR_PREFIX)) {
    			agentPath = entry;
    			break;
    		}
    	}
    	
    	LogUtil.info("Agent path: " + agentPath);
    	if (agentPath == null) {
    		throw new RuntimeException("Profiler agent is not in classpath ...");
    	}

    	if (arguments != null) {
    	    vm.loadAgent(agentPath, arguments);
    	}    
    	else {
    	    vm.loadAgent(agentPath);
    	}
    	vm.detach();
    }

    public static void redefineClass(Class<?> clazz, byte[] byteCodes) {
    	initInstrumentationIfNeeded();
    	
        try {
            inst.redefineClasses(new ClassDefinition(clazz, byteCodes));
        }
        catch (UnmodifiableClassException e) {
        	LogUtil.error("Error at JillegalAgent.redefineClass(Class<?> clazz, byte[] byteCodes)", e);
        }
        catch (ClassNotFoundException e) {
        	LogUtil.error("Error at JillegalAgent.redefineClass(Class<?> clazz, byte[] byteCodes)", e);
        }
    }
    
    public static void retransformClass(Class<?> clazz) {
    	initInstrumentationIfNeeded();
    	
        try {
            inst.retransformClasses(clazz);
        }
        catch (UnmodifiableClassException e) {
        	LogUtil.error("Error at JillegalAgent.redefineClass(Class<?> clazz)", e);
        }
    }
    
    public static long sizeOf(Object obj) {
    	initInstrumentationIfNeeded();
    	
        if (obj == null) {
            return 0;
        }    
        else {
            return inst.getObjectSize(obj);
        }    
    }
    
    public static boolean isInitialized() {
    	return initialized;
    }
    
    private static String getPidFromRuntimeMBean() throws Exception {
        RuntimeMXBean mxbean = ManagementFactory.getRuntimeMXBean();
        Field jvmField = mxbean.getClass().getDeclaredField("jvm");

        jvmField.setAccessible(true);
        VMManagement management = (VMManagement) jvmField.get(mxbean);
        Method method = management.getClass().getDeclaredMethod("getProcessId");
        method.setAccessible(true);
        Integer processId = (Integer) method.invoke(management);

        return processId.toString();
    }

}
