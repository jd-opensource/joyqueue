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
package org.joyqueue.broker.buffer;

import org.joyqueue.broker.buffer.Serializer;
import org.joyqueue.message.BrokerMessage;
import org.joyqueue.toolkit.network.IpUtil;
import org.junit.Assert;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * @author lining11
 * Date: 2018/9/10
 */
public class SerializerTest {

    private BrokerMessage message = new BrokerMessage();

    @Test
    public void test() throws Exception {
        message.setClientIp(IpUtil.toByte(new InetSocketAddress(50088)));
        message.setText("text");
        message.setAttribute("Test","TestTest");
        message.setExtension("Test".getBytes());

        int size = Serializer.sizeOf(message);


        ByteBuffer buffer = ByteBuffer.allocate(size);
        Serializer.write(message, buffer ,size);

//        buffer.position(0);
        BrokerMessage bm = Serializer.readBrokerMessage(buffer);

        Assert.assertEquals(message.getText(),bm.getText());
        Assert.assertEquals(message.getAttribute("Test"),bm.getAttribute("Test"));
        Assert.assertArrayEquals(message.getExtension(),bm.getExtension());
    }

    @Test
    public void readFlag() throws Exception {
        message.setFlag((short) 10);
        message.setClientIp(IpUtil.toByte(new InetSocketAddress(50088)));
        message.setText("text");
        message.setAttribute("Test","TestTest");
        message.setExtension("Test".getBytes());

        int size = Serializer.sizeOf(message);


        ByteBuffer buffer = ByteBuffer.allocate(size);
        Serializer.write(message, buffer ,size);

        short flag = Serializer.readFlag(buffer);

        Assert.assertEquals(flag, 10);
    }

    @Test
    public void readPartition() throws Exception {
        message.setPartition((short) 1);
        message.setClientIp(IpUtil.toByte(new InetSocketAddress(50088)));
        message.setText("text");
        message.setAttribute("Test","TestTest");
        message.setExtension("Test".getBytes());

        int size = Serializer.sizeOf(message);


        ByteBuffer buffer = ByteBuffer.allocate(size);
        Serializer.write(message, buffer ,size);

        short partition = Serializer.readPartition(buffer);

        Assert.assertEquals(partition, 1);
    }

    @Test
    public void readIndex() throws Exception {
        message.setMsgIndexNo((short) 1000);
        message.setClientIp(IpUtil.toByte(new InetSocketAddress(50088)));
        message.setText("text");
        message.setAttribute("Test","TestTest");
        message.setExtension("Test".getBytes());

        int size = Serializer.sizeOf(message);


        ByteBuffer buffer = ByteBuffer.allocate(size);
        Serializer.write(message, buffer ,size);

        long index = Serializer.readIndex(buffer);

        Assert.assertEquals(index, 1000);
    }

    @Test
    public void readSendTime() throws Exception {
        message.setStartTime(100l);
        message.setClientIp(IpUtil.toByte(new InetSocketAddress(50088)));
        message.setText("text");
        message.setAttribute("Test","TestTest");
        message.setExtension("Test".getBytes());

        int size = Serializer.sizeOf(message);

        ByteBuffer buffer = ByteBuffer.allocate(size);
        Serializer.write(message, buffer ,size);

        long sendTime = Serializer.readSendTime(buffer);
        Assert.assertEquals(sendTime, 100l);
    }

}
