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
package org.joyqueue.broker.consumer.position;

/**
 * Created by chengzhiliang on 2019/3/11.
 */
// FIXME: 单元测试未通过
//@RunWith(PowerMockRunner.class)
public class PositionManagerTest {

//    TopicName topic = new TopicName("topic");
//    String app = "app";
//    short partition = 0;
//
//    private PositionManager positionManager;
//
//    @Before
//    public void setup() throws Exception {
//        ClusterManager clusterManager = Mockito.mock(ClusterManager.class);
//
//        LinkedList<PartitionGroup> partitionGroups = new LinkedList<>();
//        PartitionGroup partitionGroup = new PartitionGroup();
//        partitionGroup.setGroup(1);
//        partitionGroup.setPartitions(Sets.newHashSet((short) 0));
//        partitionGroups.add(partitionGroup);
//
//        Mockito.when(clusterManager.getPartitionGroup(Mockito.any())).thenReturn(partitionGroups);
//
//        Mockito.when(clusterManager.getMasterPartitionList(Mockito.any())).thenReturn(Lists.newArrayList((short) 0));
//
//        Mockito.when(clusterManager.getPartitionGroupId(Mockito.any(), Mockito.anyShort())).thenReturn(0);
//
//
//        PartitionGroupStore store = Mockito.mock(PartitionGroupStore.class);
//        Mockito.when(store.getRightIndex(Mockito.anyShort())).thenReturn(0l);
//        Mockito.when(store.getLeftIndex(Mockito.anyShort())).thenReturn(0l);
//
//        StoreService storeService = Mockito.mock(StoreService.class);
//        Mockito.when(storeService.getStore(Mockito.any(), Mockito.anyInt(), Mockito.any())).thenReturn(store);
//
//        ConsumeConfig consumeConfig = Mockito.mock(ConsumeConfig.class);
//        Mockito.when(consumeConfig.getConsumePositionPath()).thenReturn("temp/position_store");
//
//        // ClusterManager clusterManager, StoreService storeService,ConsumeConfig consumeConfig
//        positionManager = new PositionManager(clusterManager, storeService, consumeConfig);
//
//        positionManager.start();
//    }
//
//
//    @Test
//    public void getLastMsgAckIndex() throws JoyQueueException {
//        addConsumer();
//        long lastMsgAckIndex = positionManager.getLastMsgAckIndex(new TopicName("topic"), "app", partition);
//        Assert.assertEquals(0, lastMsgAckIndex);
//    }
//
//    @Test
//    public void updateLastMsgAckIndex() throws JoyQueueException {
//        addConsumer();
//        boolean b = positionManager.updateLastMsgAckIndex(topic, app, partition, 10);
//        Assert.assertEquals(true, b);
//
//        Position position = positionManager.getPosition(topic, app, partition);
//        Assert.assertEquals(10, position.getAckCurIndex());
//    }
//
//    @Test
//    public void updateStartMsgAckIndex() throws JoyQueueException {
//        addConsumer();
//        boolean b = positionManager.updateStartMsgAckIndex(topic, app, partition, 10);
//        Assert.assertEquals(true, b);
//
//        Position position = positionManager.getPosition(topic, app, partition);
//        Assert.assertEquals(10, position.getAckStartIndex());
//    }
//
//    @Test
//    public void getLastMsgPullIndex() throws JoyQueueException {
//        addConsumer();
//        long lastMsgAckIndex = positionManager.getLastMsgAckIndex(topic, app, partition);
//        Assert.assertEquals(0, lastMsgAckIndex);
//    }
//
//    @Test
//    public void updateLastMsgPullIndex() throws JoyQueueException {
//        addConsumer();
//        boolean b = positionManager.updateLastMsgPullIndex(topic, app, partition, 10);
//        Assert.assertEquals(true, b);
//        Position position = positionManager.getPosition(topic, app, partition);
//        Assert.assertEquals(10, position.getPullCurIndex());
//
//    }
//
//    @Test
//    public void increaseMsgPullIndex() throws JoyQueueException {
//        addConsumer();
//        boolean b = positionManager.increaseMsgPullIndex(topic, app, partition, 10);
//        Assert.assertEquals(true, b);
//        Position position = positionManager.getPosition(topic, app, partition);
//        Assert.assertEquals(10, position.getPullCurIndex());
//    }
//
//    @Test
//    public void addConsumer() {
//        positionManager.addConsumer(topic, app);
//        Position position = positionManager.getPosition(topic, app, (short) 0);
//        Assert.assertEquals(0, position.getAckStartIndex());
//        Assert.assertEquals(0, position.getAckCurIndex());
//        Assert.assertEquals(0, position.getPullCurIndex());
//        Assert.assertEquals(0, position.getPullStartIndex());
//    }
//
//    @Test
//    public void removeConsumer() {
//        addConsumer();
//
//        positionManager.removeConsumer(topic, app);
//
//        Position position = positionManager.getPosition(topic, app, (short) 0);
//        Assert.assertEquals(null, position);
//    }
//
//    @Test
//    public void getPosition() {
//        addConsumer();
//
//        Position position = positionManager.getPosition(topic, app, (short) 0);
//        Assert.assertEquals(0, position.getAckStartIndex());
//        Assert.assertEquals(0, position.getAckCurIndex());
//        Assert.assertEquals(0, position.getPullCurIndex());
//        Assert.assertEquals(0, position.getPullStartIndex());
//    }
//
//    @Test
//    public void getConsumePosition() {
//        // positionManager.getConsumePosition(new TopicName("topic"), "app", 1);
//        // TODO
//    }
//
//    @Test
//    public void setConsumePosition() {
//        //positionManager.setConsumePosition("json-string");
//        // TODO
//    }
}