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

import java.util.*;
import java.util.function.Predicate;

/**
 * @author jiangnan53
 * refer to https://blog.csdn.net/a729913162/article/details/81698109
 */
public class ClassScanner {

    public static Set<Class<?>> search(String packageName) {
        return search(packageName, null);
    }

    public static Set<Class<?>> search(String packageName, Predicate<Class<?>> predicate) {
        return ScannerExecutor.getInstance().search(packageName, predicate);
    }
}