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

import java.io.IOException;
import java.util.*;

/**
 * @author jiangnan53
 * if you want to print the configuration in the console, you should follow the steps:
 * 1. create directory named 'scanner' in classpath  -> resources/scanner
 * 2. create the file named 'cfg.txt' in scanner -> resources/scanner/cfg.txt
 * 3. add absolute name of the class (implement {@link org.joyqueue.toolkit.config.PropertyDef}) which you want to print it constant configuration in the console
 */
public class ClassScanner {

    public static Set<Class<?>> search(String packageName) throws ClassNotFoundException, IOException {
        return ScannerExecutor.getInstance().search(packageName);

    }
}