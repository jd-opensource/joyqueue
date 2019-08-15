package io.chubao.joyqueue.broker.consumer;

import io.chubao.joyqueue.broker.cluster.ClusterManager;
import io.chubao.joyqueue.broker.consumer.filter.FilterCallback;
import io.chubao.joyqueue.domain.Consumer;
import io.chubao.joyqueue.exception.JoyQueueException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by chengzhiliang on 2019/3/15.
 */
public class FilterMessageSupportTest {

    final Consumer consumer = Mockito.mock(Consumer.class);
    final ClusterManager clusterManager = Mockito.mock(ClusterManager.class);
    final FilterMessageSupport filterMessageSupport = new FilterMessageSupport(clusterManager);

    @Before
    public void setup() {
        Mockito.when(consumer.getId()).thenReturn("1");
        Consumer.ConsumerPolicy consumerPolicy = new Consumer.ConsumerPolicy();
        consumerPolicy.setFilters(Collections.singletonMap("flag", "[1,2]"));
        Mockito.when(consumer.getConsumerPolicy()).thenReturn(consumerPolicy);

    }

    @Test
    public void filter() throws JoyQueueException {
        // Consumer consumer, List<ByteBuffer> byteBuffers, FilterCallback filterCallback
        List<ByteBuffer> byteBufferList = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            ByteBuffer allocate = ByteBuffer.allocate(100);
            allocate.position(59);
            allocate.putShort((short) i);
            allocate.flip();

            byteBufferList.add(allocate);
        }

        List<ByteBuffer> filter = filterMessageSupport.filter(consumer, byteBufferList, new FilterCallback() {
            @Override
            public void callback(List<ByteBuffer> list) throws JoyQueueException {
                // nothing to do;
                Assert.assertEquals(1, list.size());
            }
        });

        Assert.assertEquals(2, filter.size());
    }
}