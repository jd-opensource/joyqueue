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
package com.jd.journalq.broker;

import com.jd.journalq.broker.buffer.Serializer;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.exception.JMQException;
import com.jd.journalq.message.BrokerMessage;
import com.jd.journalq.store.file.StoreMessageSerializer;
import com.jd.journalq.toolkit.network.IpUtil;

import java.nio.ByteBuffer;
import java.util.zip.CRC32;

public class Test {

    public static void main(String[] args) throws Exception {
        BrokerMessage brokerMessage = new BrokerMessage();
        brokerMessage.setBody("1234567890".getBytes());
        brokerMessage.setApp("app");
        brokerMessage.setClientIp(IpUtil.toByte("127.0.0.1"));
        CRC32 crc32 = new CRC32();
        crc32.update(brokerMessage.getBody().slice());
        brokerMessage.setBodyCRC(crc32.getValue());
        ByteBuffer buffer = convertBrokerMessage2RByteBuffer(brokerMessage);
       // System.out.println(StoreMessageSerializer.checkCRC(buffer));

    }

    public static ByteBuffer convertBrokerMessage2RByteBuffer(BrokerMessage brokerMessage) throws JMQException {
        int msgSize = Serializer.sizeOf(brokerMessage);
        // todo bufferPool有问题，暂时直接创建
        ByteBuffer allocate = ByteBuffer.allocate(msgSize);
        try {
            Serializer.write(brokerMessage, allocate, msgSize);
        } catch (Exception e) {
            throw new JMQException(JMQCode.SE_SERIALIZER_ERROR,e);
        }
        return allocate;
    }
}
