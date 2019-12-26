/**
 * Copyright 2019 The JoyQueue Authors.
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
package org.joyqueue.toolkit.doc.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class PackageParser {

    private static final String classSuffix=".class";
    private static final String javaSuffix=".java";
    private static final String targetClass="target/classes";
    private static final String srcJavaPath="src/main/java";
    private static final String dot=".";
    private static final String slash="/";
    /**
     *  All classes of package and subpackages from context class loader.
     *
     * @param basePackage The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     *
     */
    public static Class[] scanClass(String basePackage) throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = basePackage.replace(dot, slash);
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> classFileBaseDir = new ArrayList();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            classFileBaseDir.add(new File(resource.getFile()));
        }
        List<Class> classes = new ArrayList();
        for (File directory : classFileBaseDir) {
            classes.addAll(findClasses(directory,basePackage));
        }
        return classes.toArray(new Class[classes.size()]);
    }

    /**
     * @param clazzName  with .class suffix
     * @return  with .java suffix and it src path
     *
     **/
    public static String getSrcPath(String clazzName){
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = clazzName.replace(dot, slash);
        if(!path.endsWith(classSuffix)){
            path+=classSuffix;
        }
        String srcPath= classLoader.getResource(path).getPath();
        srcPath=srcPath.replace(targetClass,srcJavaPath);
        int end=srcPath.lastIndexOf(dot);
        return srcPath.substring(0,end)+javaSuffix;
    }


    /**
     * Find all classes in a given directory and all subdir recursively.
     *
     * @param directory   The base directory
     * @param basePackage base package for all classes of the directory
     * @return The classes
     * @throws ClassNotFoundException
     *
     */
    private static List<Class<?>> findClasses(File directory, String basePackage) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList();
        if (!directory.exists()) {
            return classes;
        }
        File[] classFiles = directory.listFiles();
        for (File file : classFiles) {
            if (file.isDirectory()) {
                classes.addAll(findClasses(file, basePackage + dot + file.getName()));
            } else if (file.getName().endsWith(classSuffix)) {
                classes.add(Class.forName(basePackage +dot + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }




}
