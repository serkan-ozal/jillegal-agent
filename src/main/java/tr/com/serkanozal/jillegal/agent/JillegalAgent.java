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
import java.util.StringTokenizer;
import java.util.jar.JarFile;

import sun.management.VMManagement;

import com.sun.tools.attach.VirtualMachine;

@SuppressWarnings("restriction")
public class JillegalAgent {   

	public static String VERSION = "1.0.0-RELEASE";
	
    final static public String CLASS_PATH = System.getProperty("java.class.path");
	final static public String INSTR_JAR_NAME = "jillegal-agent" + "-" + VERSION + ".jar";
	final static public String OS_NAME = System.getProperty("os.name");
	
	private static Instrumentation inst;
	private static boolean agentLoaded = false;
	
	public static boolean logEnabled = true;
	
	private JillegalAgent() {
	    
	}

	public static void agentmain(String arguments, Instrumentation i) {   
	    initAtMain(arguments, i);
	    info("Agentmain: " + inst + " - " + "Arguments: " + arguments);
	}
	
    public static void premain(String arguments, Instrumentation i) {
        initAtMain(arguments, i);
        info("Premain: " + inst + " - " + "Arguments: " + arguments);
    }
    
    private static void initAtMain(String arguments, Instrumentation i) {
        try {
            inst = i;

            JarFile agentJarFile = null;
            
            final StringTokenizer st = new StringTokenizer(CLASS_PATH, File.pathSeparator);
            while (st.hasMoreTokens()) {
                String classpathEntry = st.nextToken().trim();
                if (classpathEntry.endsWith(INSTR_JAR_NAME)) {
                    agentJarFile = new JarFile(classpathEntry);
                    break;
                }
            }
            
            if (agentJarFile != null) {
                inst.appendToBootstrapClassLoaderSearch(agentJarFile);
            }    
            if (agentJarFile != null) {
                inst.appendToSystemClassLoaderSearch(agentJarFile);
            }   
        }
        catch (Throwable t) {
        	error("Error at JillegalAgent.initAtMain()", t);
        }
    }

    public static Instrumentation getInstrumentation() {
        return inst;
    }
    
    public static void init() {
        try {
            loadAgent();
        }
        catch (Throwable t) {
        	error("Error at JillegalAgent.init()", t);
        }
    }
  
    public static void loadAgent() throws Exception {
        loadAgent(null);
    }
    
	public static void loadAgent(String arguments) throws Exception {
    	if (agentLoaded) {
    		return;
    	}
    	VirtualMachine vm = VirtualMachine.attach(getPidFromRuntimeMBean());
    	String agentPath = null;
    	info("Class Path: " + CLASS_PATH);
    	info("OS Name: " + OS_NAME );
    	
    	String classPathToUse = CLASS_PATH;
      
    	if (System.getProperty("surefire.test.class.path") != null) {
    		classPathToUse = System.getProperty("surefire.test.class.path");
    	}
    	info("Using ClassPath: " + classPathToUse);

    	for (String entry : classPathToUse.split(File.pathSeparator)) {
    		if (entry.endsWith(INSTR_JAR_NAME)) {
    			agentPath = entry;
    			break;
    		}
    	}
    	
    	info("Agent path: " + agentPath);
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

    	agentLoaded = true;
    }

    public static void redefineClass(Class<?> cls, byte[] byteCodes) {
        try {
            inst.redefineClasses(new ClassDefinition(cls, byteCodes));
        }
        catch (UnmodifiableClassException e) {
        	error("Error at JillegalAgent.redefineClass()", e);
        }
        catch (ClassNotFoundException e) {
        	error("Error at JillegalAgent.redefineClass()", e);
        }
    }
    
    public static void retransformClass(Class<?> cls) {
        try {
            inst.retransformClasses(cls);
        }
        catch (UnmodifiableClassException e) {
        	error("Error at JillegalAgent.redefineClass()", e);
        }
    }
    
    public static long sizeOf(Object obj) {
        if (obj == null) {
            return 0;
        }    
        else {
            return inst.getObjectSize(obj);
        }    
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
    
    private static void info(String log) {
    	if (logEnabled) {
    		System.out.println(log);
    	}
    }
    
    private static void error(String msg, Throwable t) {
    	if (logEnabled) {
    		System.err.println(msg);
    		t.printStackTrace();
    	}
    }

}
