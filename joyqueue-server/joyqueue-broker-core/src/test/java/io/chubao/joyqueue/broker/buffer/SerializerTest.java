package io.chubao.joyqueue.broker.buffer;

import io.chubao.joyqueue.message.BrokerMessage;
import io.chubao.joyqueue.toolkit.network.IpUtil;
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
