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
package org.joyqueue.network.transport.command.handler.filter;

import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * CommandHandlerContext
 *
 * author: gaohaoxiang
 * date: 2018/8/24
 */
public class CommandHandlerContext {

    private Map<Object, Object> context;

    public <T> T set(Object key, Object value) {
        return (T) getOrNewContext().put(key, value);
    }

    public <T> T get(Object key) {
        if (context == null) {
            return null;
        }
        return (T) context.get(key);
    }

    public <T> T remove(Object key) {
        if (context == null) {
            return null;
        }
        return (T) context.remove(key);
    }

    public Set<Object> keys() {
        if (context == null) {
            return Collections.emptySet();
        }
        return context.keySet();
    }

    protected Map<Object, Object> getOrNewContext() {
        if (context == null) {
            context = Maps.newHashMap();
        }
        return context;
    }
}