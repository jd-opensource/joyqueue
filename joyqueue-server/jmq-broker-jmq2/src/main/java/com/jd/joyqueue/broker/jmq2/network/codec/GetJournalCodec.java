/**
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
package com.jd.joyqueue.broker.jmq2.network.codec;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.command.GetJournal;
import org.joyqueue.network.transport.codec.PayloadDecoder;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * 获取日志解码器
 */
public class GetJournalCodec implements PayloadDecoder, Type {

    @Override
    public Object decode(Header header, final ByteBuf in) throws Exception {
        GetJournal payload = new GetJournal();
        // 8字节偏移量
        payload.setOffset(in.readLong());
        // 8字节最大偏移量
        payload.setMaxOffset(in.readLong());
        // 4字节拉取等待时间
        if (in.isReadable()) {
            payload.setPullTimeout(in.readInt());
        }
        return payload;
    }

    @Override
    public int type() {
        return JMQ2CommandType.GET_JOURNAL.getCode();
    }
}