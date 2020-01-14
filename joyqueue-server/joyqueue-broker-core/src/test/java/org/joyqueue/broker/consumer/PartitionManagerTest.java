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

/**
 * Created by chengzhiliang on 2019/3/15.
 */
// FIXME: 单元测试未通过
public class PartitionManagerTest {


//    private final ClusterManager clusterManager = Mockito.mock(ClusterManager.class);
//    private PartitionManager partitionManager = new PartitionManager(clusterManager);
//    private final Consumer consumer = new Consumer();
//    private final short partition = 0;
//    private final long occupyTimeout = 1000l;
//
//    @Before
//    public void setup() throws JoyQueueException {
//        consumer.setId("" + 1);
//        consumer.setTopic("topic");
//        consumer.setApp("app");
//
//        org.joyqueue.domain.Consumer.ConsumerPolicy consumerPolicy = new org.joyqueue.domain.Consumer.ConsumerPolicy();
//        consumerPolicy.setMaxPartitionNum(3);
//        consumerPolicy.setErrTimes(1);
//
//        Mockito.when(clusterManager.getConsumerPolicy(Mockito.any(), Mockito.anyString())).thenReturn(consumerPolicy);
//        Mockito.when(clusterManager.getPriorityPartitionList(new TopicName("topic"))).thenReturn(Lists.newArrayList((short) 1));
//        Mockito.when(clusterManager.getPartitionGroupId(Mockito.any(), Mockito.anyShort())).thenReturn(1);
//        Mockito.when(clusterManager.getMasterPartitionList(Mockito.any())).thenReturn(Lists.newArrayList((short) 0, (short) 1, (short) 2));
//    }
//
//    @Test
//    public void tryOccupyPartition() throws InterruptedException {
//        // Consumer consumer, short partition, long occupyTimeout
//
//        boolean b = partitionManager.tryOccupyPartition(consumer, partition, occupyTimeout);
//        Assert.assertEquals(true, b);
//
//        boolean result = partitionManager.tryOccupyPartition(consumer, partition, occupyTimeout);
//        Assert.assertEquals(false, result);
//
//        Thread.sleep(1000);
//        boolean result2 = partitionManager.tryOccupyPartition(consumer, partition, occupyTimeout);
//        Assert.assertEquals(true, result2);
//    }
//
//    @Test
//    public void releasePartition() {
//        boolean b = partitionManager.tryOccupyPartition(consumer, partition, occupyTimeout);
//        Assert.assertEquals(true, b);
//
//        boolean occupyRst = partitionManager.tryOccupyPartition(consumer, partition, occupyTimeout);
//        Assert.assertEquals(false, occupyRst);
//
//        boolean result = partitionManager.releasePartition(consumer, partition);
//        Assert.assertEquals(true, result);
//
//        boolean occupyRst2 = partitionManager.tryOccupyPartition(consumer, partition, occupyTimeout);
//        Assert.assertEquals(true, occupyRst2);
//    }
//
//    @Test
//    public void releasePartition1() {
//        boolean b = partitionManager.tryOccupyPartition(consumer, partition, occupyTimeout);
//        Assert.assertEquals(true, b);
//
//        boolean occupyRst = partitionManager.tryOccupyPartition(consumer, partition, occupyTimeout);
//        Assert.assertEquals(false, occupyRst);
//
//        new ConsumePartition("topic", "app", partition);
//
//        boolean result = partitionManager.releasePartition(consumer, partition);
//        Assert.assertEquals(true, result);
//
//        boolean occupyRst2 = partitionManager.tryOccupyPartition(consumer, partition, occupyTimeout);
//        Assert.assertEquals(true, occupyRst2);
//    }
//
//    @Test
//    public void needPause() throws JoyQueueException {
//        boolean b = partitionManager.needPause(consumer);
//        Assert.assertEquals(false, b);
//
//        increaseSerialErr();
//    }
//
//    /**
//     * 过期场景
//     *
//     * @throws JoyQueueException
//     * @throws InterruptedException
//     */
//    @Test
//    public void needPause2() throws JoyQueueException, InterruptedException {
//        OwnerShip ownerShip = new OwnerShip("1", 2 * 1000);
//        partitionManager.increaseSerialErr(ownerShip);
//        boolean b = partitionManager.needPause(consumer);
//        Assert.assertEquals(true, b);
//
//        Thread.sleep(60 * 1000);
//
//        boolean result = partitionManager.needPause(consumer);
//        Assert.assertEquals(false, result);
//    }
//
//    @Test
//    public void increaseSerialErr() throws JoyQueueException {
//        OwnerShip ownerShip = new OwnerShip("1", 10 * 1000);
//        partitionManager.increaseSerialErr(ownerShip);
//
//        boolean b = partitionManager.needPause(consumer);
//        Assert.assertEquals(true, b);
//    }
//
//    @Test
//    public void clearSerialErr() throws JoyQueueException {
//        increaseSerialErr();
//
//        partitionManager.clearSerialErr(consumer);
//
//        boolean b = partitionManager.needPause(consumer);
//        Assert.assertEquals(false, b);
//    }
//
//    @Test
//    public void selectPartitionIndex() {
//        int i = partitionManager.selectPartitionIndex(10, -1, 9);
//        Assert.assertEquals(9, i);
//
//        int index = partitionManager.selectPartitionIndex(10, 9, 9);
//        Assert.assertEquals(9, index);
//
//    }
//
//    @Test
//    public void isRetry() {
//        int needRetry = 0;
//        int noNeedRetry = 0;
//        for (int i = 0; i < 100; i++) {
//            if (partitionManager.isRetry(consumer)) {
//                needRetry++;
//            } else {
//                noNeedRetry++;
//            }
//        }
//
//        Assert.assertEquals(true, needRetry < noNeedRetry);
//
//    }
//
//    @Test
//    public void resetRetryProbability() {
//        // nothing to do;
//    }
//
//    @Test
//    public void increaseRetryProbability() {
//        for (int i = 0; i < 10; i++) {
//            partitionManager.increaseRetryProbability(consumer);
//        }
//
//        int needRetry = 0;
//        int noNeedRetry = 0;
//        for (int i = 0; i < 100; i++) {
//            if (partitionManager.isRetry(consumer)) {
//                needRetry++;
//            } else {
//                noNeedRetry++;
//            }
//        }
//
//        Assert.assertEquals(true, needRetry > noNeedRetry);
//    }
//
//    @Test
//    public void decreaseRetryProbability() {
//        increaseRetryProbability();
//        for (int i = 0; i < 10; i++) {
//            partitionManager.decreaseRetryProbability(consumer);
//        }
//
//        int needRetry = 0;
//        int noNeedRetry = 0;
//        for (int i = 0; i < 100; i++) {
//            if (partitionManager.isRetry(consumer)) {
//                needRetry++;
//            } else {
//                noNeedRetry++;
//            }
//        }
//
//        Assert.assertEquals(true, needRetry < noNeedRetry);
//    }
//
//    @Test
//    public void getPriorityPartition() {
//        List<Short> topic = partitionManager.getPriorityPartition(new TopicName("topic"));
//        Assert.assertEquals(1, topic.size());
//        Assert.assertEquals((short) 1, topic.get(0).shortValue());
//    }
//
//    @Test
//    public void getGroupByPartition() {
//        int group = partitionManager.getGroupByPartition(new TopicName("topic"), (short) 1);
//        Assert.assertEquals(1, group);
//
//    }
//
//    @Test
//    public void hasFreePartition() {
//        boolean b = partitionManager.hasFreePartition(consumer);
//        Assert.assertEquals(true, b);
//
//        for (int i = 0; i < 3; i++) {
//            boolean rst = partitionManager.tryOccupyPartition(consumer, (short)i, occupyTimeout);
//            Assert.assertEquals(true, rst);
//        }
//
//        boolean b1 = partitionManager.hasFreePartition(consumer);
//        Assert.assertEquals(false, b1);
//    }
}