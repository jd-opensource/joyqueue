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

package org.joyqueue.broker.config.scan;

import java.util.Set;
import java.util.function.Predicate;

/**
 * @author jiangnan53
 * refer to https://blog.csdn.net/a729913162/article/details/81698109
 */
public class ScannerExecutor implements Scanner {

    private static volatile ScannerExecutor instance;

    @Override
    public Set<Class<?>> search(String packageName, Predicate<Class<?>> predicate) {
        Scanner fileSc = new FileScanner();
        Set<Class<?>> fileSearch = fileSc.search(packageName, predicate);
        Scanner jarScanner = new JarScanner();
        Set<Class<?>> jarSearch = jarScanner.search(packageName,predicate);
        fileSearch.addAll(jarSearch);
        return fileSearch;
    }

    private ScannerExecutor(){}

    public static ScannerExecutor getInstance(){
        if(instance == null){
            synchronized (ScannerExecutor.class){
                if(instance == null){
                    instance = new ScannerExecutor();
                }
            }
        }
        return instance;
    }

}