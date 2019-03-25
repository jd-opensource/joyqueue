package com.jd.journalq.broker.consumer.filter;

import com.jd.journalq.common.exception.JMQException;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by chengzhiliang on 2019/3/15.
 */
public class FlagFilterTest {

    final FlagFilter flagFilter = new FlagFilter();
    final String rule = "[1,2]";

    @Test
    public void setRule() {
        flagFilter.setRule(rule);
    }

    @Test
    public void filter() throws JMQException {
        setRule();

        List<ByteBuffer> byteBufferList = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            ByteBuffer allocate = ByteBuffer.allocate(100);
            allocate.position(59);
            allocate.putShort((short) i);
            allocate.flip();

            byteBufferList.add(allocate);
        }
        // List<ByteBuffer> byteBufferList, FilterCallback filterCallback
        List<ByteBuffer> filter = flagFilter.filter(byteBufferList, new FilterCallback() {
            @Override
            public void callback(List<ByteBuffer> list) throws JMQException {
                Assert.assertEquals(1, list.size());
            }
        });

        Assert.assertEquals(2, filter.size());
    }

    @Test
    public void filter1() throws JMQException {
        flagFilter.setRule("[0,1,2]");

        List<ByteBuffer> byteBufferList = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            ByteBuffer allocate = ByteBuffer.allocate(100);
            allocate.position(59);
            allocate.putShort((short) i);
            allocate.flip();

            byteBufferList.add(allocate);
        }
        // List<ByteBuffer> byteBufferList, FilterCallback filterCallback
        List<ByteBuffer> filter = flagFilter.filter(byteBufferList, new FilterCallback() {
            @Override
            public void callback(List<ByteBuffer> list) throws JMQException {
                Assert.assertEquals(null, list);
            }
        });

        Assert.assertEquals(3, filter.size());
    }

    @Test
    public void filter2() throws JMQException {
        flagFilter.setRule("[9]");

        List<ByteBuffer> byteBufferList = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            ByteBuffer allocate = ByteBuffer.allocate(100);
            allocate.position(59);
            allocate.putShort((short) i);
            allocate.flip();

            byteBufferList.add(allocate);
        }
        // List<ByteBuffer> byteBufferList, FilterCallback filterCallback
        List<ByteBuffer> filter = flagFilter.filter(byteBufferList, new FilterCallback() {
            @Override
            public void callback(List<ByteBuffer> list) throws JMQException {
                Assert.assertEquals(9, list.size());
            }
        });

        Assert.assertEquals(1, filter.size());
    }
}