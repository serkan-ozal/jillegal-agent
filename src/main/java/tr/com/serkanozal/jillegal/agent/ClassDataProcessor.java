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

/**
 * Contact point for implementations which take given class data (bytecode) and
 * returns the processed version (no change or modified/instrumented).
 * 
 * @author Serkan OZAL
 */
public interface ClassDataProcessor {

    /**
     * Takes the given class data (bytecode) and 
     * returns the processed version (no change or modified/instrumented).
     * 
     * @param loader    the {@link ClassLoader} which loads the given class data (bytecode)
     * @param className name of the loaded class
     * @param classData bytecode of the class to be processed
     * @return processed version (no change or modified/instrumented) of the given class data
     */
    byte[] process(ClassLoader loader, String className, byte[] classData);
    
}
