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

/**
 * 提供消息预览服务
 * @author LiYue
 * Date: 2020/2/8
 */
public interface MessagePreviewService {

    /**
     * 获取所有支持的消息序列化类型列表
     * @return 消息序列化类型列表
     */
    List<String> getMessageTypeNames();

    /**
     * 预览消息。按照给定的消息序列化类型反序列化消息，并返回可读的消息文本。
     * @param typeName 消息序列化类型
     * @param binaryMessage 二进制消息
     * @return 可读的消息文本。
     * @throws UnsupportedOperationException 不支持给定的typeName时抛出此异常
     * @throws NullPointerException binaryMessage 为空时抛出此异常
     */
    String preview(String typeName, byte [] binaryMessage);

    /**
     * 批量预览消息。按照给定的消息序列化类型反序列化消息，并返回可读的消息文本。
     * @param typeName 消息序列化类型
     * @param binaryMessage 二进制消息列表
     * @return 可读的消息文本列表。
     * @throws UnsupportedOperationException 不支持给定的typeName时抛出此异常
     * @throws NullPointerException binaryMessages 为空，或者列表中任意一个元素为空时抛出此异常
     */
    List<String> preview(String typeName, List<byte []> binaryMessage);
}
