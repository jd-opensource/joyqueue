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
package org.joyqueue.toolkit.config;

/**
 * property 定义
 */
public interface PropertyDef {
    /**
     * get property name
     *
     * @return
     */
    String getName();

    /**
     * get property value
     *
     * @return
     */
    Object getValue();

    /**
     * get value type
     *
     * @return
     */
    PropertyDef.Type getType();


    enum Type {
        BOOLEAN, STRING, INT, SHORT, LONG, DOUBLE, OBJECT
    }
}
