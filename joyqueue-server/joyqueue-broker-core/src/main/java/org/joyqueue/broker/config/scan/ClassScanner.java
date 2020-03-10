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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author jiangnan53
 * scan the implement of {@link PropertyDef} and print them in the console
 */
public class ClassScanner {

    private static final Logger logger = LoggerFactory.getLogger(ClassScanner.class);

    private static final String PREFIX = "META-INF/services/";

    public static Map<String, String> defaultScanner(Class<?> clazz) {
        Map<String, String> configMap = new HashMap<>(0);
        InputStream inputStream = ClassScanner.class.getClassLoader().getResourceAsStream(PREFIX + clazz.getName());
        if (inputStream != null) {
            List<String> classNames = new BufferedReader(new InputStreamReader(inputStream))
                    .lines().parallel().collect(Collectors.toList());
            for (String clazzName : classNames) {
                try {
                    Class<?> cls = Class.forName(clazzName);
                    List<Class<?>> impls = Arrays.asList(cls.getInterfaces());
                    if (impls.contains(clazz) && cls.isEnum()) {
                        // Enum::values
                        Method method = cls.getMethod("values");
                        if (method.getReturnType().isArray()) {
                            Object[] values = (Object[]) method.invoke(null);
                            for (Object obj : values) {
                                if (obj instanceof PropertyDef) {
                                    PropertyDef propertyDef = (PropertyDef) obj;
                                    configMap.put(propertyDef.getName(), String.valueOf(propertyDef.getValue()));
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e.getCause());
                }
            }
        }
        return configMap;
    }
}