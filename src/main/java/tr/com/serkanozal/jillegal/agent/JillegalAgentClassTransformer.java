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

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.regex.Pattern;

import tr.com.serkanozal.jillegal.agent.util.LogUtil;

/**
 * {@link ClassFileTransformer} implementation transforms loaded class data 
 * with registered {@link ClassDataProcessor}s.
 * 
 * @see ClassFileTransformer
 * 
 * @author Serkan OZAL
 */
class JillegalAgentClassTransformer implements ClassFileTransformer {

    private final ClassDataProcessorHandler[] handlers;
    
    JillegalAgentClassTransformer(Collection<ClassDataProcessorHandler> handlers) {
        this.handlers = 
                handlers != null 
                    ? handlers.toArray(new ClassDataProcessorHandler[handlers.size()])
                    : null;
    }
    
    static void init() {
        LogUtil.debug("Loading " + JillegalAgentClassTransformer.class + " ...");
        LogUtil.debug("Loading " + ClassDataProcessor.class + " ...");
        LogUtil.debug("Loading " + JillegalAgentClassTransformer.ClassDataProcessorHandler.class + " ...");
        LogUtil.debug("Loading " + JillegalAgentClassTransformer.ClassInterestChecker.class + " ...");
        LogUtil.debug("Loading " + JillegalAgentClassTransformer.ClassInterestChecker.INTERESTED_IN_WITH_ALL.getClass() + " ...");
        LogUtil.debug("Loading " + JillegalAgentClassTransformer.DefinedClassInterestChecker.class + " ...");
        LogUtil.debug("Loading " + JillegalAgentClassTransformer.TargetAwareClassInterestChecker.class + " ...");
    }
    
    @Override
    public byte[] transform(ClassLoader loader, String className,
            Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
            byte[] classfileBuffer) throws IllegalClassFormatException {
        if (handlers != null) {
            byte[] classData = classfileBuffer;
            for (ClassDataProcessorHandler handler : handlers) {
                classData = handler.handle(loader, className, classData);
            }
            return classData;
        } else {
            return classfileBuffer;
        }
    }
    
    static ClassDataProcessorHandler createHandlerAsInterestedInWithAll(ClassDataProcessor processor) {
        return new ClassDataProcessorHandler(processor, ClassInterestChecker.INTERESTED_IN_WITH_ALL);
    }
    
    static ClassDataProcessorHandler createHandlerAsDefined(ClassDataProcessor processor, 
                                                            String interestDef) {
        return new ClassDataProcessorHandler(processor, new DefinedClassInterestChecker(interestDef));
    }
    
    static ClassDataProcessorHandler createHandlerAsTargetAware(TargetAwareClassDataProcessor processor) {
        return new ClassDataProcessorHandler(processor, new TargetAwareClassInterestChecker(processor));
    }
    
    private interface ClassInterestChecker {
        
        ClassInterestChecker INTERESTED_IN_WITH_ALL = new ClassInterestChecker() {
            @Override
            public boolean isInterestedInWith(String className) {
                return true;
            }
        };
        
        boolean isInterestedInWith(String className);
        
    }
    
    private static class DefinedClassInterestChecker implements ClassInterestChecker {

        private final Pattern pattern;
        
        private DefinedClassInterestChecker(String interestDef) {
            pattern = Pattern.compile(("\\Q" + interestDef + "\\E").replace("*", "\\E.*\\Q"));
        }
        
        @Override
        public boolean isInterestedInWith(String className) {
            return pattern.matcher(className).matches();
        }
        
    }
    
    private static class TargetAwareClassInterestChecker implements ClassInterestChecker {

        private final TargetAwareClassDataProcessor processor;
        
        private TargetAwareClassInterestChecker(TargetAwareClassDataProcessor processor) {
            this.processor = processor;
        }
        
        @Override
        public boolean isInterestedInWith(String className) {
            return processor.isTarget(className);
        }
        
    }
    
    static class ClassDataProcessorHandler {
        
        private final ClassDataProcessor processor;
        private final ClassInterestChecker checker;
        
        ClassDataProcessorHandler(ClassDataProcessor processor, 
                                  ClassInterestChecker checker) {
            this.processor = processor;
            this.checker = checker;
        }
        
        private byte[] handle(ClassLoader loader, String className, byte[] classData) {
            className = className.replace("/", ".");
            if (checker.isInterestedInWith(className)) {
                return processor.process(loader, className, classData);
            } else {
                return classData;
            }
        }
        
    }
    
}
