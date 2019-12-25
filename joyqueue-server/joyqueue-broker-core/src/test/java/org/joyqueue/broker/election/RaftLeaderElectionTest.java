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
package org.joyqueue.broker.election;

import org.joyqueue.broker.config.Configuration;
import org.joyqueue.domain.Broker;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.TopicName;
import org.joyqueue.store.*;
import org.joyqueue.store.replication.ReplicableStore;
import org.joyqueue.toolkit.concurrent.EventListener;
import org.joyqueue.toolkit.io.Files;
import org.joyqueue.toolkit.network.IpUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;


/**
 * Created by zhuduohui on 2018/8/27.
 */
@Ignore
public class RaftLeaderElectionTest {
    private static Logger logger = LoggerFactory.getLogger(RaftLeaderElectionTest.class);

    private final int RAFT_ELECTION_NUM = 3;
    private final int NODE_NUM = 3;
    private final int TOPIC_NUM = 5;

    private ElectionManagerStub[] electionManager = new ElectionManagerStub[RAFT_ELECTION_NUM];
    private LeaderElection[] leaderElections = new RaftLeaderElection[RAFT_ELECTION_NUM];
    private LeaderElection[][] multiLeaderElections = new RaftLeaderElection[RAFT_ELECTION_NUM][TOPIC_NUM];

    private Broker[] brokers = new Broker[NODE_NUM];

    private Store[] storeServices = new Store[RAFT_ELECTION_NUM];

    private TopicName topic1 = TopicName.parse("test");
    private int partitionGroup1 = 1;
    private String[] topics = new String[TOPIC_NUM];


    private ProduceTask produceTask;
    private ConsumeTask consumeTask;

    private short[] partitions = new short[]{0, 1, 2, 3, 4};

    private String getStoreDir() {
        String property = "java.io.tmpdir";
        return System.getProperty(property)  + File.separator + "store";
    }

    private String getElectionDir() {
        String property = "java.io.tmpdir";
        return System.getProperty(property)  + File.separator + "election";
    }

    @Before
    public void setUp() throws Exception {
        String localIp = IpUtil.getLocalIp();

        for (int i = 0; i < RAFT_ELECTION_NUM; i++) {
            Configuration conf = new Configuration();
            StoreConfig storeConfig = new StoreConfig(conf);
            storeConfig.setPath(getStoreDir() + i);
            storeServices[i] = new Store(storeConfig);
            storeServices[i].start();

            ElectionConfig electionConfig = new ElectionConfig(conf);
            electionConfig.setElectionMetaPath(getElectionDir() + i);
            electionConfig.setListenPort("1800" + (i + 1));

            electionManager[i] = new ElectionManagerStub(electionConfig, storeServices[i], new ConsumeStub());
            electionManager[i].start();
        }

        for (int i = 0; i < NODE_NUM; i++) {
            brokers[i] = new Broker();
            brokers[i].setId(i + 1);
            brokers[i].setIp(localIp);
            brokers[i].setPort(18000 + i);
        }

        for (int i = 0; i < TOPIC_NUM; i++) {
            topics[i] = "test" + i;
        }

        //PartitionGroupStoreManger mock = PowerMockito.mock(PartitionGroupStoreManger.class);
        //PowerMockito.when(mock.getReplicationStatus()).thenReturn(null);
    }

    @After
    public void tearDown() {
        for (int i = 0; i < RAFT_ELECTION_NUM; i++) {
            if (storeServices[i] != null) {
                storeServices[i].removePartitionGroup(topic1.getFullName(), partitionGroup1);
                storeServices[i].stop();
            }

            if (electionManager[i] != null) {
                leaderElections[i] = electionManager[i].getLeaderElection(topic1, partitionGroup1);
                if (leaderElections[i] != null) leaderElections[i].stop();
                electionManager[i].onPartitionGroupRemove(topic1, partitionGroup1);
                electionManager[i].stop();
            }

            Files.deleteDirectory(new File(getStoreDir() + i));
            Files.deleteDirectory(new File(getElectionDir() + i));
        }

        if (produceTask != null) {
            produceTask.stop(true);
            produceTask = null;
        }
        if (consumeTask != null) {
            consumeTask.stop(true);
            consumeTask = null;
        }

    }

    private void createElectionManager(List<Broker> allNodes) throws Exception {
        for (int i = 0; i < RAFT_ELECTION_NUM; i++) {
            storeServices[i].createPartitionGroup(topic1.getFullName(), partitionGroup1, partitions);
            electionManager[i].onPartitionGroupCreate(PartitionGroup.ElectType.raft,
                    topic1, partitionGroup1, allNodes, new TreeSet<>(), brokers[i].getId(), -1);
            leaderElections[i] = electionManager[i].getLeaderElection(topic1, partitionGroup1);
            electionManager[i].addListener(new ElectionEventListener());
        }
    }

    private int nextNode(int nodeId) {
        if (nodeId == RAFT_ELECTION_NUM) return 1;
        else return nodeId + 1;
    }

    private int getLeader(LeaderElection leaderElection, int waitTimes) throws InterruptedException{
        Thread.sleep(5000);
        int times = 0;
        int leaderId = leaderElection.getLeaderId();
        while (leaderId == -1 && times < waitTimes) {
            Thread.sleep(1000);
            leaderId = leaderElection.getLeaderId();
        }
        return leaderId;
    }


    @Test
    public void testElection() throws Exception{

        List<Broker> allNodes = new LinkedList<>();
        for (int i = 0; i < NODE_NUM; i++) {
            allNodes.add(brokers[i]);
        }

        for (int i = 0; i < RAFT_ELECTION_NUM; i++) {
            storeServices[i].createPartitionGroup(topic1.getFullName(), partitionGroup1, partitions);
            electionManager[i].onPartitionGroupCreate(PartitionGroup.ElectType.raft,
                    topic1, partitionGroup1, allNodes, new TreeSet<Integer>(), brokers[i].getId(), -1);
            leaderElections[i] = electionManager[i].getLeaderElection(topic1, partitionGroup1);
        }

        int leaderId = getLeader(leaderElections[0], 10);
        Assert.assertNotEquals(leaderId, -1);
        logger.info("Leader id is " + leaderId);
        Assert.assertEquals(leaderId, leaderElections[1].getLeaderId());
        Assert.assertEquals(leaderId, leaderElections[2].getLeaderId());

        for (int i = 0; i < 1; i++) {
            electionManager[leaderId - 1].stop();
            logger.info("Node " + leaderId + " stop");

            Thread.sleep(10000);

            int leaderIdNew = getLeader(leaderElections[nextNode(leaderId) - 1], 10);
            Assert.assertNotEquals(leaderIdNew, -1);
            logger.info("leaderIdNew id is " + leaderIdNew);

            Thread.sleep(1000);

            for (int j = 0; j < NODE_NUM; j++) {
                if (j != leaderId - 1) {
                    logger.info("leader id of leader election {} is {}", j + 1, leaderElections[j].getLeaderId());
                    Assert.assertEquals(leaderIdNew, leaderElections[j].getLeaderId());
                }
            }

            Thread.sleep(5000);
            electionManager[leaderId - 1].start();
            electionManager[leaderId - 1].onPartitionGroupCreate(PartitionGroup.ElectType.raft,
                    topic1, partitionGroup1, allNodes, new TreeSet<Integer>(), brokers[leaderId - 1].getId(), -1);
            leaderElections[leaderId - 1] = electionManager[leaderId - 1].getLeaderElection(topic1, partitionGroup1);
            logger.info("Node " + leaderId + " start");
            Thread.sleep(3000);
            Assert.assertEquals(leaderIdNew, leaderElections[leaderId - 1].getLeaderId());

            Thread.sleep(5000);

            leaderId = leaderIdNew;
        }

    }


    @Test
    public void testOneNode() throws Exception {
        List<Broker> allNodes = new LinkedList<>();
        allNodes.add(brokers[0]);
        storeServices[0].createPartitionGroup(topic1.getFullName(), partitionGroup1, partitions);
        electionManager[0].onPartitionGroupCreate(PartitionGroup.ElectType.raft,
                topic1, partitionGroup1, allNodes, new TreeSet<>(), brokers[0].getId(), -1);
        leaderElections[0] = electionManager[0].getLeaderElection(topic1, partitionGroup1);
        electionManager[0].addListener(new ElectionEventListener());

        int leaderId = getLeader(leaderElections[0], 10);
        Assert.assertEquals(leaderId, 1);

        produceTask = new ProduceTask(storeServices[leaderId - 1], topic1, partitionGroup1);
        produceTask.start();
        consumeTask = new ConsumeTask(storeServices[leaderId - 1], topic1, partitionGroup1);
        consumeTask.start();

        Thread.sleep(3000);

        produceTask.stop(true);
        produceTask = null;
        consumeTask.stop(true);
        consumeTask = null;

    }

    @Test
    public void testReplication() throws Exception{
        final int maxMessageLength = 1024 * 1024;

        List<Broker> allNodes = new LinkedList<>();
        for (int i = 0; i < NODE_NUM; i++) {
            allNodes.add(brokers[i]);
        }

        createElectionManager(allNodes);

        int leaderId = getLeader(leaderElections[0], 10);
        Assert.assertNotEquals(leaderId, -1);
        logger.info("Leader id is " + leaderId);

        Thread.sleep(2000);

        Assert.assertEquals(leaderId, leaderElections[1].getLeaderId());
        Assert.assertEquals(leaderId, leaderElections[2].getLeaderId());

        produceTask = new ProduceTask(storeServices[leaderId - 1], topic1, partitionGroup1);
        produceTask.start();
        consumeTask = new ConsumeTask(storeServices[leaderId - 1], topic1, partitionGroup1);
        consumeTask.start();

        for (int i = 0; i < 1; i++) {
            Thread.sleep(5000);

            electionManager[leaderId - 1].removeLeaderElection(topic1.getFullName(), partitionGroup1);
            electionManager[leaderId - 1].stop();
            logger.info("Node " + leaderId + " stop");

            int leaderId1 = getLeader(leaderElections[nextNode(leaderId) - 1], 10);
            Assert.assertNotEquals(leaderId1, -1);
            logger.info("Leader1 id is " + leaderId1);

            produceTask.setStoreService(storeServices[leaderId1 - 1]);
            consumeTask.setStoreService(storeServices[leaderId1 - 1]);

            Thread.sleep(5000);

            electionManager[leaderId - 1].start();
            electionManager[leaderId - 1].onPartitionGroupCreate(PartitionGroup.ElectType.raft,
                    topic1, partitionGroup1, allNodes, new TreeSet<Integer>(), brokers[leaderId - 1].getId(), -1);
            leaderElections[leaderId - 1] = electionManager[leaderId - 1].getLeaderElection(topic1, partitionGroup1);
            electionManager[leaderId - 1].addListener(new ElectionEventListener());

            Thread.sleep(5000);
            leaderId = leaderId1;
        }

        produceTask.stop(true);
        produceTask = null;
        consumeTask.stop(true);
        consumeTask = null;

        System.out.println("Produce task and consume task interrupted.");

        Thread.sleep(5000);

        long[] messageLength = new long[RAFT_ELECTION_NUM];
        for (int i = 0; i < RAFT_ELECTION_NUM; i++) {
            ReplicableStore rStore = storeServices[i].getReplicableStore(topic1.getFullName(), partitionGroup1);
            ByteBuffer messages = rStore.readEntryBuffer(0, maxMessageLength);
            messageLength[i] += messages.remaining();
            long position = 0;
            while (messages.remaining() > 0) {
                position += messages.remaining();
                if (position >= rStore.rightPosition()) break;
                messages = rStore.readEntryBuffer(position, maxMessageLength);
                messageLength[i] += messages.remaining();
            }

            logger.info("Store {} message length is {}", i, messageLength[i]);

            if (i > 0) Assert.assertEquals(messageLength[i], messageLength[i - 1]);
        }

        Thread.sleep(1000);

        //Assert.assertEquals(messages.size(), 10);

    }

    @Test
    public void testMultiTopic() throws Exception{

        List<Broker> allNodes = new LinkedList<>();
        for (int i = 0; i < NODE_NUM; i++) {
            allNodes.add(brokers[i]);
        }

        for (int i = 0; i < RAFT_ELECTION_NUM; i++) {
            for (int j = 0; j < TOPIC_NUM; j++) {
                storeServices[i].createPartitionGroup(topics[j], partitionGroup1, partitions);
                electionManager[i].onPartitionGroupCreate(PartitionGroup.ElectType.raft,
                        TopicName.parse(topics[j]), partitionGroup1, allNodes, new TreeSet<Integer>(), brokers[i].getId(), -1);
                multiLeaderElections[i][j] = electionManager[i].getLeaderElection(TopicName.parse(topics[j]), partitionGroup1);
            }
            electionManager[i].addListener(new ElectionEventListener());
        }

        Thread.sleep(5000);

        int[] leaders = new int[TOPIC_NUM];
        for (int i = 0; i < TOPIC_NUM; i++) {
            int leaderId = getLeader(multiLeaderElections[0][i], 10);
            Assert.assertNotEquals(leaderId, -1);
            leaders[i] = leaderId;
            logger.info("Leader of topic {} is {}", topics[i], leaderId);
        }

        Thread.sleep(5000);

        for (int i = 0; i < TOPIC_NUM; i++) {
            logger.info("Leader of topic {}, 1 is {}", topics[i], multiLeaderElections[1][i].getLeaderId());
            logger.info("Leader of topic {}, 2 is {}", topics[i], multiLeaderElections[2][i].getLeaderId());
            Assert.assertEquals(leaders[i], multiLeaderElections[1][i].getLeaderId());
            Assert.assertEquals(leaders[i], multiLeaderElections[2][i].getLeaderId());
        }

        ProduceTask produceTasks[] = new ProduceTask[TOPIC_NUM];
        for (int i = 0; i < produceTasks.length; i++) {
            produceTasks[i] = new ProduceTask(storeServices[leaders[i] - 1], TopicName.parse(topics[i]), partitionGroup1);
            produceTasks[i].start();
        }
        ConsumeTask consumeTasks[] = new ConsumeTask[TOPIC_NUM];
        for (int i = 0; i < produceTasks.length; i++) {
            consumeTasks[i] = new ConsumeTask(storeServices[leaders[i] - 1], TopicName.parse(topics[i]), partitionGroup1);
            consumeTasks[i].start();
        }

        Thread.sleep(5000);

        for (int i = 0; i < produceTasks.length; i++) {
            produceTasks[i].stop(true);
            produceTasks[i] = null;
        }
        for (int i = 0; i < produceTasks.length; i++) {
            consumeTasks[i].stop(true);
            consumeTasks[i] = null;
        }

        System.out.println("Produce task and consume task interrupted.");

        Thread.sleep(3000);

        for (int j = 0; j < TOPIC_NUM; j++) {
            for (int i = 0; i < RAFT_ELECTION_NUM; i++) {
                ReplicableStore rStore = storeServices[i].getReplicableStore(topics[j], partitionGroup1);
                System.out.println("Topic " + topics[j] +" / store " + i +  "'s left is " + rStore.leftPosition()
                        + ", flush position is " + rStore.rightPosition()
                        + ", commit position is " + rStore.commitPosition()
                        + ", term is " + rStore.term());
            }
        }
        Thread.sleep(1000);

        //Assert.assertEquals(messages.size(), 10);

    }

    @Test
    public void testStopReplica() throws Exception{

        List<Broker> allNodes = new LinkedList<>();
        for (int i = 0; i < NODE_NUM; i++) {
            allNodes.add(brokers[i]);
        }

        createElectionManager(allNodes);

        int leaderId = getLeader(leaderElections[0], 10);
        Assert.assertNotEquals(leaderId, -1);
        logger.info("Leader id is " + leaderId);
        Assert.assertEquals(leaderId, leaderElections[1].getLeaderId());
        Assert.assertEquals(leaderId, leaderElections[2].getLeaderId());

        produceTask = new ProduceTask(storeServices[leaderId - 1], topic1, partitionGroup1);
        produceTask.start();
        consumeTask = new ConsumeTask(storeServices[leaderId - 1], topic1, partitionGroup1);
        consumeTask.start();

        Thread.sleep(5000);

        electionManager[nextNode(leaderId) - 1].removeLeaderElection(topic1.getFullName(), partitionGroup1);
        electionManager[nextNode(leaderId) - 1].stop();
        logger.info("Node " + leaderId + " stop");

        int leaderId1 = getLeader(leaderElections[nextNode(leaderId) - 1], 10);
        Assert.assertNotEquals(leaderId1, -1);
        logger.info("Leader1 id is " + leaderId1);

        produceTask.stop(true);
        produceTask = null;
        consumeTask.stop(true);
        consumeTask = null;

        for (int i = 0; i < RAFT_ELECTION_NUM; i++) {
            ReplicableStore rStore = storeServices[i].getReplicableStore(topic1.getFullName(), partitionGroup1);
            System.out.println("Store " + i + "'s left is " + rStore.leftPosition()
                    + ", write position is " + rStore.rightPosition()
                    + ", commit position is " + rStore.commitPosition()
                    + ", term is " + rStore.term());
        }
        Thread.sleep(1000);

        //Assert.assertEquals(messages.size(), 10);

    }


    @Test
    public void testChangeNode() throws Exception {
        List<Broker> allNodes = new LinkedList<>();
        for (int i = 0; i < NODE_NUM; i++) {
            allNodes.add(brokers[i]);
        }

        for (int i = 0; i < RAFT_ELECTION_NUM; i++) {
            storeServices[i].createPartitionGroup(topic1.getFullName(), partitionGroup1, partitions);
            electionManager[i].onPartitionGroupCreate(PartitionGroup.ElectType.raft,
                    topic1, partitionGroup1, allNodes, new TreeSet<>(), brokers[i].getId(), -1);
            leaderElections[i] = electionManager[i].getLeaderElection(topic1, partitionGroup1);
        }

        int leaderId = getLeader(leaderElections[0], 10);
        Assert.assertNotEquals(leaderId, -1);
        logger.info("Leader id is " + leaderId);
        Assert.assertEquals(leaderId, leaderElections[1].getLeaderId());
        Assert.assertEquals(leaderId, leaderElections[2].getLeaderId());

        //删掉leader
        logger.info("Remove leader node {}", leaderId);
        for (int i = 0; i < RAFT_ELECTION_NUM; i++) {
            if (leaderId == i + 1) {
                electionManager[i].onPartitionGroupRemove(topic1, partitionGroup1);
            } else {
                electionManager[i].onNodeRemove(topic1, partitionGroup1, leaderId, brokers[i].getId());
            }
        }

        Thread.sleep(10000);

        int leaderId1 = getLeader(leaderElections[nextNode(leaderId) - 1], 10);
        logger.info("Leader1 id is " + leaderId1);
        Assert.assertNotEquals(leaderId1, -1);
        Collection<DefaultElectionNode> allNodes1 = leaderElections[leaderId1 - 1].getAllNodes();
        Assert.assertEquals(allNodes1.size(), 2);

        //删除leader
        logger.info("Remove leader node {}", leaderId1);
        for (int i = 0; i < RAFT_ELECTION_NUM; i++) {
            if (i + 1 == leaderId) continue;
            if (leaderId1 == i + 1) {
                electionManager[i].onPartitionGroupRemove(topic1, partitionGroup1);
            } else {
                electionManager[i].onNodeRemove(topic1, partitionGroup1, leaderId1, brokers[i].getId());
            }
        }

        Thread.sleep(5000);

        int i;
        for (i = 0; i < RAFT_ELECTION_NUM; i++) {
            if (leaderId != i + 1 && leaderId1 != i + 1) break;
        }

        logger.info("Remain node is {}", i);
        int leaderId2 = getLeader(leaderElections[i], 10);
        logger.info("Leader2 id is " + leaderId2);
        Assert.assertNotEquals(leaderId2, -1);
        Collection<DefaultElectionNode> allNodes2 = leaderElections[leaderId2 - 1].getAllNodes();
        Assert.assertEquals(allNodes2.size(), 1);

        List<Broker> allNodesAdd = new LinkedList<>();
        allNodesAdd.add(brokers[leaderId2 - 1]);
        allNodesAdd.add(brokers[leaderId1 - 1]);


        //Addd node
        logger.info("Add node {}", leaderId1);
        electionManager[leaderId2 - 1].onNodeAdd(topic1, partitionGroup1, PartitionGroup.ElectType.raft,
                allNodesAdd, new TreeSet<>(), brokers[leaderId1 - 1], leaderId1, -1);

        electionManager[leaderId1 - 1].onPartitionGroupCreate(PartitionGroup.ElectType.raft,
                topic1, partitionGroup1, allNodesAdd, new TreeSet<>(), brokers[leaderId1 - 1].getId(), -1);
        leaderElections[leaderId1 - 1] = electionManager[i].getLeaderElection(topic1, partitionGroup1);

        int leaderId3 = getLeader(leaderElections[leaderId2 - 1], 10);
        logger.info("Leader3 id is " + leaderId3);
        Assert.assertNotEquals(leaderId3, -1);
        Collection<DefaultElectionNode> allNodes3 = leaderElections[leaderId2 - 1].getAllNodes();
        Assert.assertEquals(allNodes3.size(), 2);
        Collection<DefaultElectionNode> allNodes4 = leaderElections[leaderId1 - 1].getAllNodes();
        Assert.assertEquals(allNodes4.size(), 2);

        Thread.sleep(1000);

    }



    @Test
    public void testTransferLeader() throws Exception {
        System.out.println("Start test transfer leader");

        List<Broker> allNodes = new LinkedList<>();
        for (int i = 0; i < NODE_NUM; i++) {
            allNodes.add(brokers[i]);
        }

        createElectionManager(allNodes);

        Thread.sleep(10000);
        int leaderId = leaderElections[0].getLeaderId();
        Assert.assertNotEquals(leaderId, -1);
        logger.info("Leader id is " + leaderId);
        Assert.assertEquals(leaderId, leaderElections[1].getLeaderId());
        Assert.assertEquals(leaderId, leaderElections[2].getLeaderId());

        produceTask = new ProduceTask(storeServices[leaderId - 1], topic1, partitionGroup1);
        produceTask.start();
        consumeTask = new ConsumeTask(storeServices[leaderId - 1], topic1, partitionGroup1);
        consumeTask.start();

        try {
            electionManager[leaderId - 1].onLeaderChange(topic1, partitionGroup1, nextNode(leaderId));

            Thread.sleep(10000);
            int leaderId1 = leaderElections[leaderId - 1].getLeaderId();
            logger.info("Leader1 id is " + leaderId1);
            System.out.println("Leader1 is " + leaderId1);
            Assert.assertEquals(leaderId1, nextNode(leaderId));

            for (int i = 0; i < RAFT_ELECTION_NUM; i++) {
                Assert.assertEquals(leaderId1, leaderElections[i].getLeaderId());
            }

            {
                electionManager[nextNode(leaderId1) - 1].removeLeaderElection(topic1.getFullName(), partitionGroup1);
                electionManager[nextNode(leaderId1) - 1].stop();
                logger.info("Node " + nextNode(leaderId1) + " stop");

                System.out.println("Node " + nextNode(leaderId1) + " stop");

                Thread.sleep(3000);

                electionManager[nextNode(leaderId1) - 1].start();
                electionManager[nextNode(leaderId1) - 1].onPartitionGroupCreate(PartitionGroup.ElectType.raft,
                        topic1, partitionGroup1, allNodes, new TreeSet<>(), brokers[nextNode(leaderId1) - 1].getId(), -1);
                leaderElections[nextNode(leaderId1) - 1] = electionManager[nextNode(leaderId1) - 1].getLeaderElection(topic1, partitionGroup1);
                electionManager[nextNode(leaderId1) - 1].addListener(new ElectionEventListener());
                Thread.sleep(10000);
            }

            System.out.println("Change node from " + leaderId1 + " to " + nextNode(leaderId1));

            electionManager[leaderId1 - 1].onLeaderChange(topic1, partitionGroup1, nextNode(leaderId1));
            Thread.sleep(10000);

            int leaderId2 = leaderElections[leaderId1 - 1].getLeaderId();
            logger.info("Leader2 id is " + leaderId2);
            System.out.println("Leader2 is " + leaderId2);
            Assert.assertEquals(leaderId2, nextNode(leaderId1));

            for (int i = 0; i < RAFT_ELECTION_NUM; i++) {
                Assert.assertEquals(leaderId2, leaderElections[i].getLeaderId());
            }

            Thread.sleep(10000);
        } finally {
            produceTask.stop(true);
            consumeTask.stop(true);
        }
    }

    private class ElectionEventListener implements EventListener<ElectionEvent> {
        @Override
        public void onEvent(ElectionEvent event) {
            logger.info("Election event listener, type is {}, leader id is {}",
                    event.getEventType(), event.getLeaderId());
            if (event.getEventType() == ElectionEvent.Type.LEADER_FOUND) {
                produceTask.setStoreService(storeServices[event.getLeaderId() - 1]);
                consumeTask.setStoreService(storeServices[event.getLeaderId() - 1]);
            }
        }
    }

}
