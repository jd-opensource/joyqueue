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
package io.openmessaging.joyqueue.producer.interceptor;

import org.joyqueue.client.internal.producer.interceptor.ProduceContext;
import io.openmessaging.KeyValue;
import io.openmessaging.interceptor.Context;

/**
 * ContextAdapter
 *
 * author: gaohaoxiang
 * date: 2019/3/1
 */
public class ContextAdapter implements Context {

    private ProduceContext context;

    private KeyValue attributes;

    public ContextAdapter(ProduceContext consumeContext) {
        this.context = consumeContext;
    }

    @Override
    public KeyValue attributes() {
        if (attributes == null) {
            attributes = new ContextAttributeAdapter(context);
        }
        return attributes;
    }
}