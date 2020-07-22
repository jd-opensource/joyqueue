/**
 * Copyright 2019 The JoyQueue Authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.joyqueue.broker.config.scan;

import org.joyqueue.toolkit.config.PropertyDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author jiangnan53
 */
public class FileScanner implements Scanner {

    private static final Logger logger = LoggerFactory.getLogger(FileScanner.class);

    private String defaultClassPath = FileScanner.class.getResource("/").getPath();

    public String getDefaultClassPath() {
        return defaultClassPath;
    }

    public void setDefaultClassPath(String defaultClassPath) {
        this.defaultClassPath = defaultClassPath;
    }

    public FileScanner() {
    }

    public FileScanner(String defaultClassPath) {
        this.defaultClassPath = defaultClassPath;
    }

    @Override
    public Set<Class<?>> search(String pkgName) throws ClassNotFoundException, IOException {
        String classPath = defaultClassPath;
        String searchPath = classPath + pkgName;
        return new ClassSearcher().doPath(new File(searchPath), pkgName, true);
    }

    private static class ClassSearcher {
        private Set<Class<?>> classPaths = new HashSet<>(0);

        private Set<Class<?>> doPath(File file, String pkgName, boolean flag) throws ClassNotFoundException, IOException {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (!flag) {
                    pkgName = pkgName + "." + file.getName();
                }
                if (files != null) {
                    for (File f : files) {
                        doPath(f, pkgName, false);
                    }
                }
            } else {
                if (file.isAbsolute() && file.getName().equals(PropertyDef.class.getName()) && file.exists()) {
                    List<String> classNames = Files.readAllLines(file.toPath());
                    for (String className : classNames) {
                        try {
                            classPaths.add(Class.forName(className));
                        }catch (ClassNotFoundException e){
                            logger.error(e.getMessage());
                        }
                    }
                }
            }
            return classPaths;
        }
    }
}
