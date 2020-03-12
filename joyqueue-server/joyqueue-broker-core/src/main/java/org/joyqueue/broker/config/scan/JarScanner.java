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

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author jiangnan53
 */
public class JarScanner implements Scanner {

    private static final Logger logger = LoggerFactory.getLogger(JarScanner.class);

    @Override
    public Set<Class<?>> search(String pkgName) {
        Set<Class<?>> clazzSet = new HashSet<>(0);
        try {
            Enumeration<URL> urlEnumeration = Thread.currentThread().getContextClassLoader().getResources(pkgName);
            while (urlEnumeration.hasMoreElements()) {
                URL url = urlEnumeration.nextElement();
                String protocol = url.getProtocol();
                if ("jar".equalsIgnoreCase(protocol)) {
                    JarURLConnection connection = (JarURLConnection) url.openConnection();
                    if (connection != null) {
                        JarFile jarFile = connection.getJarFile();
                        if (jarFile != null) {
                            Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
                            while (jarEntryEnumeration.hasMoreElements()) {
                                JarEntry entry = jarEntryEnumeration.nextElement();
                                String entryName = entry.getName();
                                if (entryName.contains(PropertyDef.class.getName())) {
                                    Properties properties = new Properties();
                                    properties.load(jarFile.getInputStream(entry));
                                    Enumeration<?> enumeration = properties.propertyNames();
                                    while (enumeration.hasMoreElements()) {
                                        String key = (String) enumeration.nextElement();
                                        try {
                                            clazzSet.add(Class.forName(key));
                                        }catch (ClassNotFoundException e){
                                            logger.error(e.getMessage());
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if ("file".equalsIgnoreCase(protocol)) {
                    FileScanner fileScanner = new FileScanner();
                    fileScanner.setDefaultClassPath(url.getPath().replace(pkgName, ""));
                    clazzSet.addAll(fileScanner.search(pkgName));
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            logger.error(e.getMessage());
        }
        return clazzSet;
    }
}
