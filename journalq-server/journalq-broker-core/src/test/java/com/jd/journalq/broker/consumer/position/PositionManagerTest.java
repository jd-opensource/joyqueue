package com.jd.journalq.broker.consumer.position;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.consumer.ConsumeConfig;
import com.jd.journalq.broker.consumer.position.model.Position;
import com.jd.journalq.domain.PartitionGroup;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.exception.JMQException;
import com.jd.journalq.store.PartitionGroupStore;
import com.jd.journalq.store.StoreService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.LinkedList;

/**
 * Created by chengzhiliang on 2019/3/11.
 */
@RunWith(PowerMockRunner.class)
public class PositionManagerTest {

    TopicName topic = new TopicName("topic");
    String app = "app";
    short partition = 0;

    private PositionManager positionManager;

    @Before
    public void setup() throws Exception {
        ClusterManager clusterManager = Mockito.mock(ClusterManager.class);

        LinkedList<PartitionGroup> partitionGroups = new LinkedList<>();
        PartitionGroup partitionGroup = new PartitionGroup();
        partitionGroup.setGroup(1);
        partitionGroup.setPartitions(Sets.newHashSet((short) 0));
        partitionGroups.add(partitionGroup);

        Mockito.when(clusterManager.getPartitionGroup(Mockito.any())).thenReturn(partitionGroups);

        Mockito.when(clusterManager.getMasterPartitionList(Mockito.any())).thenReturn(Lists.newArrayList((short) 0));

        Mockito.when(clusterManager.getPartitionGroupId(Mockito.any(), Mockito.anyShort())).thenReturn(0);


        PartitionGroupStore store = Mockito.mock(PartitionGroupStore.class);
        Mockito.when(store.getRightIndex(Mockito.anyShort())).thenReturn(0l);
        Mockito.when(store.getLeftIndex(Mockito.anyShort())).thenReturn(0l);

        StoreService storeService = Mockito.mock(StoreService.class);
        Mockito.when(storeService.getStore(Mockito.any(), Mockito.anyInt(), Mockito.any())).thenReturn(store);

        ConsumeConfig consumeConfig = Mockito.mock(ConsumeConfig.class);
        Mockito.when(consumeConfig.getConsumePositionPath()).thenReturn("temp/position_store");

        // ClusterManager clusterManager, StoreService storeService,ConsumeConfig consumeConfig
        positionManager = new PositionManager(clusterManager, storeService, consumeConfig);

        positionManager.start();
    }


    @Test
    public void getLastMsgAckIndex() throws JMQException {
        addConsumer();
        long lastMsgAckIndex = positionManager.getLastMsgAckIndex(new TopicName("topic"), "app", partition);
        Assert.assertEquals(0, lastMsgAckIndex);
    }

    @Test
    public void updateLastMsgAckIndex() throws JMQException {
        addConsumer();
        boolean b = positionManager.updateLastMsgAckIndex(topic, app, partition, 10);
        Assert.assertEquals(true, b);

        Position position = positionManager.getPosition(topic, app, partition);
        Assert.assertEquals(10, position.getAckCurIndex());
    }

    @Test
    public void updateStartMsgAckIndex() throws JMQException {
        addConsumer();
        boolean b = positionManager.updateStartMsgAckIndex(topic, app, partition, 10);
        Assert.assertEquals(true, b);

        Position position = positionManager.getPosition(topic, app, partition);
        Assert.assertEquals(10, position.getAckStartIndex());
    }

    @Test
    public void getLastMsgPullIndex() throws JMQException {
        addConsumer();
        long lastMsgAckIndex = positionManager.getLastMsgAckIndex(topic, app, partition);
        Assert.assertEquals(0, lastMsgAckIndex);
    }

    @Test
    public void updateLastMsgPullIndex() throws JMQException {
        addConsumer();
        boolean b = positionManager.updateLastMsgPullIndex(topic, app, partition, 10);
        Assert.assertEquals(true, b);
        Position position = positionManager.getPosition(topic, app, partition);
        Assert.assertEquals(10, position.getPullCurIndex());

    }

    @Test
    public void increaseMsgPullIndex() throws JMQException {
        addConsumer();
        boolean b = positionManager.increaseMsgPullIndex(topic, app, partition, 10);
        Assert.assertEquals(true, b);
        Position position = positionManager.getPosition(topic, app, partition);
        Assert.assertEquals(10, position.getPullCurIndex());
    }

    @Test
    public void addConsumer() {
        positionManager.addConsumer(topic, app);
        Position position = positionManager.getPosition(topic, app, (short) 0);
        Assert.assertEquals(0, position.getAckStartIndex());
        Assert.assertEquals(0, position.getAckCurIndex());
        Assert.assertEquals(0, position.getPullCurIndex());
        Assert.assertEquals(0, position.getPullStartIndex());
    }

    @Test
    public void removeConsumer() {
        addConsumer();

        positionManager.removeConsumer(topic, app);

        Position position = positionManager.getPosition(topic, app, (short) 0);
        Assert.assertEquals(null, position);
    }

    @Test
    public void getPosition() {
        addConsumer();

        Position position = positionManager.getPosition(topic, app, (short) 0);
        Assert.assertEquals(0, position.getAckStartIndex());
        Assert.assertEquals(0, position.getAckCurIndex());
        Assert.assertEquals(0, position.getPullCurIndex());
        Assert.assertEquals(0, position.getPullStartIndex());
    }

    @Test
    public void getConsumePosition() {
        // positionManager.getConsumePosition(new TopicName("topic"), "app", 1);
        // TODO
    }

    @Test
    public void setConsumePosition() {
        //positionManager.setConsumePosition("json-string");
        // TODO
    }
}