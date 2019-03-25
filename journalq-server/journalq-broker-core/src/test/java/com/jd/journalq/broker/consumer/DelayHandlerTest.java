package com.jd.journalq.broker.consumer;

import com.jd.journalq.common.domain.Consumer;
import com.jd.journalq.toolkit.time.SystemClock;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by chengzhiliang on 2019/3/15.
 */
public class DelayHandlerTest {

    private DelayHandler delayHandler = new DelayHandler();

    @Test
    public void handle() {
        // Consumer.ConsumerPolicy consumerPolicy, List<ByteBuffer> byteBufferList
        Consumer.ConsumerPolicy consumerPolicy = new Consumer.ConsumerPolicy();
        consumerPolicy.setDelay(1);

        List<ByteBuffer> byteBufferList = new LinkedList<>();

        ByteBuffer allocate = ByteBuffer.allocate(100);
        allocate.position(39);
        allocate.putLong(SystemClock.now() - 1000);
        allocate.flip();

        ByteBuffer allocate2 = ByteBuffer.allocate(100);
        allocate2.position(39);
        allocate2.putLong(SystemClock.now() + 1000);
        allocate2.flip();


        byteBufferList.add(allocate);
        byteBufferList.add(allocate2);

        List<ByteBuffer> handle = delayHandler.handle(consumerPolicy, byteBufferList);

        Assert.assertEquals(1, handle.size());

    }
}