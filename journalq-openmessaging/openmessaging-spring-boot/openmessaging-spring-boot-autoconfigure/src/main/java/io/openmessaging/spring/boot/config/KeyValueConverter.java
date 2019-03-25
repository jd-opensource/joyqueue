/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.openmessaging.spring.boot.config;

import io.openmessaging.KeyValue;
import io.openmessaging.OMS;

import java.util.Map;

/**
 * Convert attributes to KeyValue.
 *
 * @version OMS 1.0.0
 * @since OMS 1.0.0
 */
public class KeyValueConverter {

    public static KeyValue convert(Map<String, String> attributes) {
        KeyValue result = OMS.newKeyValue();
        if (attributes != null && !attributes.isEmpty()) {
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
}
