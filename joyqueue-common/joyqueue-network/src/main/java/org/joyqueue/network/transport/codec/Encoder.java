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
package org.joyqueue.network.transport.codec;

import org.joyqueue.network.transport.exception.TransportException;
import io.netty.buffer.ByteBuf;

/**
 * 对象编码
 * Created by hexiaofeng on 16-6-23.
 */
public interface Encoder {

    /**
     * 编码
     *
     * @param obj 对象
     * @param buffer 输出流
     * @throws TransportException.CodecException 编解码异常时抛出
     */
    void encode(Object obj, ByteBuf buffer) throws TransportException.CodecException;

}
