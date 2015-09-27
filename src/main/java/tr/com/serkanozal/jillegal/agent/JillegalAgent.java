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
package tr.com.serkanozal.jillegal.agent;

import java.io.File;
import java.io.PrintStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.JarFile;

import sun.management.VMManagement;
import tr.com.serkanozal.jillegal.agent.JillegalAgentClassTransformer.ClassDataProcessorHandler;
import tr.com.serkanozal.jillegal.agent.util.ClassLoaderUtil;
import tr.com.serkanozal.jillegal.agent.util.InstanceUtil;
import tr.com.serkanozal.jillegal.agent.util.LogUtil;
import tr.com.serkanozal.jillegal.agent.util.OsUtil;

import com.sun.tools.attach.VirtualMachine;

/**
 * Java agent implementation for <tt>jillegal-agent</tt> framework.
 * 
 * @author Serkan OZAL
 */
@SuppressWarnings("restriction")
public final class JillegalAgent {   

	public static String VERSION = "2.0";
	
	final static public String INSTR_JAR_PREFIX = "jillegal-agent";
	final static public String NATIVE_METHOD_PREFIX = "jillegal_agent";
	
	private static volatile Instrumentation inst;
	private static volatile boolean initialized;
	
	static {
	    JillegalAgentClassTransformer.init();
	}
	
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

            processArguments(arguments, i);
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
    
    private synchronized static void initInstrumentationIfNeeded() {
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
        initInternal(null);
    }
    
    public static void init(String arguments) {
        initInternal(arguments);
    }
    
    private synchronized static void initInternal(String arguments) {
        if (initialized) {
            LogUtil.warn("Agent has been already initialized");
            return;
        }
        
        try {
            LogUtil.intro();
            
            loadAgent(arguments);
        }
        catch (Throwable t) {
            LogUtil.error("Error at JillegalAgent.initInternal(String arguments)", t);
            throw new RuntimeException(t);
        }
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
        	LogUtil.error("Error at JillegalAgent.retransformClass(Class<?> clazz)", e);
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
    
    private static void processArguments(String arguments, Instrumentation i) throws Exception {
        if (arguments == null) {
            return;
        }
        arguments = arguments.trim();
        if (arguments.length() == 0) {
            return;
        }
        String args[] = arguments.split("\\s+");
        String command = args[0];
        if ("-h".equals(command) || "-help".equals(command)) {
            printUsage(false);
        } else if ("-p".equals(command)) {
            handleClassDataProcessorArguments(args, i);
        } else {
            invalidUsage();
            throw new InvalidParameterException();
        }
    }
    
    private static void handleClassDataProcessorArguments(String args[], Instrumentation i) throws Exception {
        Collection<ClassDataProcessorHandler> handlers = createHandlersFromArguments(args);
        if (handlers != null && !handlers.isEmpty()) {
            JillegalAgentClassTransformer transformer = new JillegalAgentClassTransformer(handlers);
            inst.addTransformer(transformer);
            inst.setNativeMethodPrefix(transformer, NATIVE_METHOD_PREFIX);
        }    
    }
    
    private static Collection<ClassDataProcessorHandler> createHandlersFromArguments(String args[]) 
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (args.length < 2) {
            invalidUsage();
            throw new InvalidParameterException();
        }
        List<ClassDataProcessorHandler> handlers = new ArrayList<ClassDataProcessorHandler>();
        for (int i = 1; i < args.length; i++) {
            String argParts[] = args[i].split("=");
            if (argParts.length == 1) {
                String classDataProcessorName = argParts[0];
                ClassDataProcessor processor = InstanceUtil.createInstance(classDataProcessorName);
                if (processor instanceof TargetAwareClassDataProcessor) {
                    handlers.add(
                            JillegalAgentClassTransformer.createHandlerAsTargetAware(
                                    (TargetAwareClassDataProcessor) processor));
                } else {
                    handlers.add(JillegalAgentClassTransformer.createHandlerAsInterestedInWithAll(processor));
                }    
            } else if (argParts.length == 2) {
                String classDataProcessorName = argParts[0];
                ClassDataProcessor processor = InstanceUtil.createInstance(classDataProcessorName);
                if (processor instanceof TargetAwareClassDataProcessor) {
                    handlers.add(
                            JillegalAgentClassTransformer.createHandlerAsTargetAware(
                                    (TargetAwareClassDataProcessor) processor));
                } 
                String targets[] = argParts[1].split(",");
                for (String target : targets) {
                    handlers.add(JillegalAgentClassTransformer.createHandlerAsDefined(processor, target));
                }
            } else {
                invalidUsage();
                throw new InvalidParameterException();
            }
        }

        return handlers;
    }
    
    private static void invalidUsage() {
        System.err.println("Invalid usage!");
        printUsage(true);
    }
    
    private static void printUsage(boolean error) {
        @SuppressWarnings("resource")
        PrintStream ps = error ? System.err : System.out;
        ps.println(
                "-javaagent:<path_to_agent_jar>/jillegal-agent-" + VERSION + ".jar" + 
                    "[" + 
                        "=" + 
                        "-p " + 
                        "[" + 
                            "<class_data_processor>[=<target>[,<target>]*]" + 
                        "]+" + 
                    "]");
    }

}
