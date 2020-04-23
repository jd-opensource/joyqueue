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
package org.joyqueue.broker.consumer;

import com.google.common.collect.Lists;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.consumer.model.ConsumePartition;
import org.joyqueue.broker.monitor.SessionManager;
import org.joyqueue.domain.TopicName;
import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.network.session.Consumer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

/**
 * Created by chengzhiliang on 2019/3/15.
 */
public class PartitionManagerTest {


    private final ClusterManager clusterManager = Mockito.mock(ClusterManager.class);
    private final SessionManager sessionManager = Mockito.mock(SessionManager.class);
    private PartitionManager partitionManager = new CasPartitionManager(clusterManager, sessionManager);
    private final Consumer consumer = new Consumer();
    private final short partition = 0;
    private final long occupyTimeout = 1000l;

    @Before
    public void setup() throws JoyQueueException {
        consumer.setId("" + 1);
        consumer.setTopic("topic");
        consumer.setApp("app");
        consumer.setConnectionId("testClientId");

        org.joyqueue.domain.Consumer.ConsumerPolicy consumerPolicy = new org.joyqueue.domain.Consumer.ConsumerPolicy();
        consumerPolicy.setMaxPartitionNum(3);
        consumerPolicy.setErrTimes(1);
        consumerPolicy.setRetry(true);

        Mockito.when(clusterManager.getConsumerPolicy(Mockito.any(), Mockito.anyString())).thenReturn(consumerPolicy);
        Mockito.when(clusterManager.getPriorityPartitionList(new TopicName("topic"))).thenReturn(Lists.newArrayList((short) 1));
        Mockito.when(clusterManager.getPartitionGroupId(Mockito.any(), Mockito.anyShort())).thenReturn(1);
        Mockito.when(clusterManager.getLocalPartitions(new TopicName("topic"))).thenReturn(Lists.newArrayList((short) 0, (short) 1, (short) 2));
        Mockito.when(clusterManager.getRetryRandomBound(Mockito.anyString(), Mockito.anyString())).thenReturn(1000);
    }

    @Test
    public void tryOccupyPartition() throws InterruptedException {
        // Consumer consumer, short partition, long occupyTimeout
        boolean b = partitionManager.tryOccupyPartition(consumer, partition, occupyTimeout);
        Assert.assertEquals(true, b);

        boolean result = partitionManager.tryOccupyPartition(consumer, partition, occupyTimeout);
        Assert.assertEquals(false, result);

        Thread.sleep(2000);
        boolean result2 = partitionManager.tryOccupyPartition(consumer, partition, occupyTimeout);
        Assert.assertEquals(true, result2);
    }

    @Test
    public void releasePartition() {
        boolean b = partitionManager.tryOccupyPartition(consumer, partition, occupyTimeout);
        Assert.assertEquals(true, b);

        boolean occupyRst = partitionManager.tryOccupyPartition(consumer, partition, occupyTimeout);
        Assert.assertEquals(false, occupyRst);

        boolean result = partitionManager.releasePartition(consumer, partition);
        Assert.assertEquals(true, result);

        boolean occupyRst2 = partitionManager.tryOccupyPartition(consumer, partition, occupyTimeout);
        Assert.assertEquals(true, occupyRst2);
    }

    @Test
    public void releasePartition1() {
        boolean b = partitionManager.tryOccupyPartition(consumer, partition, occupyTimeout);
        Assert.assertEquals(true, b);

        boolean occupyRst = partitionManager.tryOccupyPartition(consumer, partition, occupyTimeout);
        Assert.assertEquals(false, occupyRst);

        new ConsumePartition("topic", "app", partition);

        boolean result = partitionManager.releasePartition(consumer, partition);
        Assert.assertEquals(true, result);

        boolean occupyRst2 = partitionManager.tryOccupyPartition(consumer, partition, occupyTimeout);
        Assert.assertEquals(true, occupyRst2);
    }


    @Test
    public void selectPartitionIndex() {
        int i = partitionManager.selectPartitionIndex(10, -1, 9);
        Assert.assertEquals(9, i);

        int index = partitionManager.selectPartitionIndex(10, 9, 9);
        Assert.assertEquals(9, index);

    }

    @Test
    public void isRetry() throws JoyQueueException {
        int needRetry = 0;
        int noNeedRetry = 0;
        for (int i = 0; i < 100; i++) {
            if (partitionManager.isRetry(consumer)) {
                needRetry++;
            } else {
                noNeedRetry++;
            }
        }

        Assert.assertEquals(true, needRetry < noNeedRetry);

    }

    public void increaseRetryProbability() throws JoyQueueException {
        for (int i = 0; i < 10; i++) {
            partitionManager.increaseRetryProbability(consumer);
        }

        int needRetry = 0;
        for (int i = 0; i < 10000; i++) {
            if (partitionManager.isRetry(consumer)) {
                needRetry++;
            }
        }
        // 概率在200/1000 左右
        Assert.assertTrue(1500 < needRetry && needRetry < 2500);
    }

    @Test
    public void decreaseRetryProbability() throws JoyQueueException {
        increaseRetryProbability();
        for (int i = 0; i < 10; i++) {
            partitionManager.decreaseRetryProbability(consumer);
        }

        int needRetry = 0;
        for (int i = 0; i < 10000; i++) {
            if (partitionManager.isRetry(consumer)) {
                needRetry++;
            }
        }

        // 概率在5/1000 左右
        Assert.assertTrue(0 < needRetry && needRetry < 200);
    }

    @Test
    public void getPriorityPartition() {
        List<Short> topic = partitionManager.getPriorityPartition(new TopicName("topic"));
        Assert.assertEquals(1, topic.size());
        Assert.assertEquals((short) 1, topic.get(0).shortValue());
    }

    @Test
    public void getGroupByPartition() {
        int group = partitionManager.getGroupByPartition(new TopicName("topic"), (short) 1);
        Assert.assertEquals(1, group);

    }

    @Test
    public void hasFreePartition() {
        boolean b = partitionManager.hasFreePartition(consumer);
        Assert.assertEquals(true, b);

        for (int i = 0; i < 3; i++) {
            boolean rst = partitionManager.tryOccupyPartition(consumer, (short)i, occupyTimeout);
            Assert.assertEquals(true, rst);
        }

        boolean b1 = partitionManager.hasFreePartition(consumer);
        Assert.assertEquals(false, b1);
    }
}