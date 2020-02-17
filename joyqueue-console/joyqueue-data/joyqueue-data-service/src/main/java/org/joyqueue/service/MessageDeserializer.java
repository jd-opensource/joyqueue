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
package org.joyqueue.service;

import java.util.List;
import java.util.stream.Collectors;
import org.joyqueue.exception.MessageDeserializeException;
/**
 * 消息内容反序列化器，用于把序列化的二进制消息转为可读的文本，用于消息预览
 * @author LiYue
 * Date: 2020/2/8
 */
public interface MessageDeserializer {
    /**
     * 返回此反序列化器支持的序列化格式的名称，用于显示在界面上。
     * @return 消息序列化格式名称
     */
    String getSerializeTypeName();

    /**
     * 反序列化二进制消息，转换为可读的文本，用于消息预览。
     * @param binaryMessage 二进制消息
     * @return 可读额消息文本
     * @throws NullPointerException binaryMessage 为空时抛出此异常
     * @throws MessageDeserializeException deserialize exception
     */
    String deserialize(byte [] binaryMessage);

    /**
     * 批量反序列化二进制消息
     * @param binaryMessages 二进制消息列表
     * @return 反序列化后的消息列表
     * @throws NullPointerException 当参数 binaryMessages 为空，或者binaryMessages的任何一个元素为空时抛出此异常
     */
    default List<String> deserialize(List<byte []> binaryMessages) {
        return binaryMessages.stream().map(this::deserialize).collect(Collectors.toList());
    }
}
