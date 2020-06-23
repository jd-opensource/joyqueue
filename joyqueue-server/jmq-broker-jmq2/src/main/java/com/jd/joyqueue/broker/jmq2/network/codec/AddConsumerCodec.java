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
import com.jd.joyqueue.broker.jmq2.command.AddConsumer;
import com.jd.joyqueue.broker.jmq2.network.JMQ2PayloadCodec;
import com.jd.joyqueue.broker.jmq2.util.Serializer;
import org.joyqueue.domain.TopicName;
import org.joyqueue.network.session.ConsumerId;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Payload;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * 添加消费者解码器
 */
public class AddConsumerCodec implements JMQ2PayloadCodec, Type {

    @Override
    public AddConsumer decode(Header header, final ByteBuf in) throws Exception {
        AddConsumer payload = new AddConsumer();
        // 1字节消费者ID长度
        payload.setConsumerId(new ConsumerId(Serializer.readString(in)));
        // 1字节主题长度
        payload.setTopic(TopicName.parse(Serializer.readString(in)));
        // 2字节选择器长度
        payload.setSelector(Serializer.readString(in, 2));

        return payload;
    }

    @Override
    public void encode(Payload payload, ByteBuf buffer) {

    }

    @Override
    public int type() {
        return JMQ2CommandType.ADD_CONSUMER.getCode();
    }
}