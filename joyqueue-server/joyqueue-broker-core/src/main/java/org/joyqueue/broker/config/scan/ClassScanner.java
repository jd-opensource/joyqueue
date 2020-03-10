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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author jiangnan53
 * refer to https://gitee.com/luhui123/luhui_library/tree/master/commons_utils/src/main/java/com/luhui/commons/scanclass
 */
public class ClassScanner {

    private static final String SCANPKG_RESOURCE = "joyqueue/scanpkg.txt";

    private static List<String> getConfigPackages() {
        InputStream inputStream = ClassScanner.class.getClassLoader().getResourceAsStream(SCANPKG_RESOURCE);
        if(inputStream!=null) {
            return new BufferedReader(new InputStreamReader(inputStream))
                    .lines().parallel().collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public static Set<Class<?>> search(String packageName) {
        return search(packageName, null);
    }

    public static Set<Class<?>> search(String packageName, Predicate<Class<?>> predicate) {
        return ScannerExecutor.getInstance().search(packageName, predicate);
    }

    public static Set<Class<?>> defaultSearch(){
        Set<Class<?>> clazzSet = new HashSet<>(0);
        for(String pkgName:getConfigPackages()){
            clazzSet.addAll(search(pkgName));
        }
        return clazzSet;
    }
}