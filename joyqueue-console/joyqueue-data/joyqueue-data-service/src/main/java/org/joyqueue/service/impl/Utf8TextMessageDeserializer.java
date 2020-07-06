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
package org.joyqueue.service.impl;

import org.joyqueue.service.MessageDeserializer;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * @author LiYue
 * Date: 2020/2/8
 */
@Component(value = "defaultMessageDeserializer")
public class Utf8TextMessageDeserializer implements MessageDeserializer {
    @Override
    public String getSerializeTypeName() {
        return "UTF-8 TEXT";
    }

    @Override
    public String deserialize(byte[] binaryMessage) {
        if(binaryMessage == null) throw new NullPointerException();
        return new String(binaryMessage, StandardCharsets.UTF_8);
    }
}
