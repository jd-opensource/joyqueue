package com.jd.journalq.broker.election;

import com.jd.journalq.broker.buffer.Serializer;
import com.jd.journalq.domain.Broker;
import com.jd.journalq.domain.PartitionGroup;
import com.jd.journalq.domain.QosLevel;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.message.BrokerMessage;
import com.jd.journalq.message.SourceType;
import com.jd.journalq.store.PartitionGroupStore;
import com.jd.journalq.store.StoreService;
import com.jd.journalq.store.WriteRequest;
import com.jd.journalq.store.WriteResult;
import com.jd.journalq.store.message.MessageParser;
import com.jd.journalq.store.replication.ReplicableStore;
import com.jd.journalq.toolkit.concurrent.EventListener;
import com.jd.journalq.toolkit.network.IpUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;


/**
 * Created by zhuduohui on 2018/8/27.
 */
public class RaftLeaderElectionTest {
    private static Logger logger = LoggerFactory.getLogger(RaftLeaderElectionTest.class);

    private final int RAFT_ELECTION_NUM = 3;
    private final int NODE_NUM = 3;
    private final int TOPIC_NUM = 20;

    private ElectionManagerStub[] electionManager = new ElectionManagerStub[RAFT_ELECTION_NUM];
    private LeaderElection[] leaderElections = new RaftLeaderElection[RAFT_ELECTION_NUM];
    private LeaderElection[][] multiLeaderElections = new RaftLeaderElection[RAFT_ELECTION_NUM][TOPIC_NUM];

    private Broker[] brokers = new Broker[NODE_NUM];

    private StoreService[] storeServices = null;//new Store[RAFT_ELECTION_NUM];

    private TopicName topic1 = TopicName.parse("test");
    private int partitionGroup1 = 1;
    private String[] topics = new String[TOPIC_NUM];


    private ProduceTask produceTask = new ProduceTask(-1);
    private ConsumeTask consumeTask = new ConsumeTask(-1);

    @Before
    public void setUp() throws Exception {
        String localIp = IpUtil.getLocalIp();

        for (int i = 0; i < RAFT_ELECTION_NUM; i++) {
            // TODO 临时注释掉
          //  StoreConfig storeConfig = new StoreConfig(null);
           // storeConfig.setPath("/Users/zhuduohui/Data/jmq/store" + i);
           // storeServices[i] = new Store(storeConfig);

            ElectionConfig electionConfig = new ElectionConfigStub(null);
            electionConfig.setMetadataFile("/Users/zhuduohui/Data/jmq/raft" + i + ".dat");
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
        if (produceTask.isAlive()) {
            produceTask.interrupt();
        }
        if (consumeTask.isAlive()) {
            consumeTask.interrupt();
        }

        for (int i = 0; i < RAFT_ELECTION_NUM; i++) {
            //storeServices[i].removePartitionGroup(topic1, partitionGroup1);
          //TODO 临时注释掉
            //  if (storeServices[i] != null) storeServices[i].physicalDelete();

            if (electionManager[i] != null) {
                leaderElections[i] = electionManager[i].getLeaderElection(topic1, partitionGroup1);
                if (leaderElections[i] != null) leaderElections[i].stop();
                electionManager[i].onPartitionGroupRemove(topic1, partitionGroup1);
                electionManager[i].stop();
            }
        }

        //brokerService.stop();
    }

    private int nextNode(int nodeId) {
        if (nodeId == RAFT_ELECTION_NUM) return 1;
        else return nodeId + 1;
    }

    @Test
    public void testElection() throws Exception{

        List<Broker> allNodes = new LinkedList<>();
        for (int i = 0; i < NODE_NUM; i++) {
            allNodes.add(brokers[i]);
        }

        for (int i = 0; i < RAFT_ELECTION_NUM; i++) {
            // TODO
           // storeServices[i].createPartitionGroup(topic1.getFullName(), partitionGroup1, new short[]{1}, new int[]{1});
            electionManager[i].onPartitionGroupCreate(PartitionGroup.ElectType.raft,
                    topic1, partitionGroup1, allNodes, new TreeSet<Integer>(), brokers[i].getId(), -1);
            leaderElections[i] = electionManager[i].getLeaderElection(topic1, partitionGroup1);
        }

        Thread.sleep(5000);
        int leaderId = leaderElections[0].getLeaderId();
        while (leaderId == -1) {
            Thread.sleep(1000);
            leaderId = leaderElections[0].getLeaderId();
        }
        Assert.assertNotEquals(leaderId, -1);
        logger.info("Leader id is " + leaderId);
        Assert.assertEquals(leaderId, leaderElections[1].getLeaderId());
        Assert.assertEquals(leaderId, leaderElections[2].getLeaderId());

        for (int i = 0; i < 100; i++) {
            electionManager[leaderId - 1].stop();
            logger.info("Node " + leaderId + " stop");

            Thread.sleep(5000);
            int leaderIdNew = leaderElections[nextNode(leaderId) - 1].getLeaderId();
            while(leaderIdNew == -1) {
                Thread.sleep(1000);
                leaderIdNew = leaderElections[nextNode(leaderId) - 1].getLeaderId();
            }

            Thread.sleep(5000);
            leaderIdNew = leaderElections[nextNode(leaderId) - 1].getLeaderId();
            Assert.assertNotEquals(leaderIdNew, -1);
            logger.info("leaderIdNew id is " + leaderIdNew);

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
        storeServices[0].createPartitionGroup(topic1.getFullName(), partitionGroup1, new short[]{1}, new int[]{1});
        electionManager[0].onPartitionGroupCreate(PartitionGroup.ElectType.raft,
                topic1, partitionGroup1, allNodes, new TreeSet<>(), brokers[0].getId(), -1);
        leaderElections[0] = electionManager[0].getLeaderElection(topic1, partitionGroup1);
        electionManager[0].addListener(new ElectionEventListener());

        Thread.sleep(5000);
        int leaderId = leaderElections[0].getLeaderId();
        while (leaderId == -1) {
            Thread.sleep(1000);
            leaderId = leaderElections[0].getLeaderId();
        }
        Assert.assertEquals(leaderId, 1);

        produceTask.start();
        consumeTask.start();

        Thread.sleep(60000);

        produceTask.stop(true);
        consumeTask.stop(true);

    }

    @Test
    public void testReplication() throws Exception{

        List<Broker> allNodes = new LinkedList<>();
        for (int i = 0; i < NODE_NUM; i++) {
            allNodes.add(brokers[i]);
        }

        for (int i = 0; i < RAFT_ELECTION_NUM; i++) {
            storeServices[i].createPartitionGroup(topic1.getFullName(), partitionGroup1, new short[]{1}, new int[]{1});
            electionManager[i].onPartitionGroupCreate(PartitionGroup.ElectType.raft,
                    topic1, partitionGroup1, allNodes, new TreeSet<>(), brokers[i].getId(), -1);
            leaderElections[i] = electionManager[i].getLeaderElection(topic1, partitionGroup1);
            electionManager[i].addListener(new ElectionEventListener());
        }

        Thread.sleep(5000);
        int leaderId = leaderElections[0].getLeaderId();
        while (leaderId == -1) {
            Thread.sleep(1000);
            leaderId = leaderElections[0].getLeaderId();
        }
        Assert.assertNotEquals(leaderId, -1);
        logger.info("Leader id is " + leaderId);

        Thread.sleep(2000);

        Assert.assertEquals(leaderId, leaderElections[1].getLeaderId());
        Assert.assertEquals(leaderId, leaderElections[2].getLeaderId());

        produceTask.start();
        consumeTask.start();

        for (int i = 0; i < 10; i++) {
            Thread.sleep(240000);

            electionManager[leaderId - 1].removeLeaderElection(topic1.getFullName(), partitionGroup1);
            electionManager[leaderId - 1].stop();
            logger.info("Node " + leaderId + " stop");

            Thread.sleep(5000);
            int leaderId1 = leaderElections[nextNode(leaderId) - 1].getLeaderId();
            while (leaderId1 == -1) {
                Thread.sleep(1000);
                leaderId1 = leaderElections[nextNode(leaderId) - 1].getLeaderId();
            }
            Assert.assertNotEquals(leaderId1, -1);
            logger.info("Leader1 id is " + leaderId1);

            produceTask.setLeaderId(leaderId1);
            consumeTask.setLeaderId(leaderId1);

            Thread.sleep(60000);

            electionManager[leaderId - 1].start();
            electionManager[leaderId - 1].onPartitionGroupCreate(PartitionGroup.ElectType.raft,
                    topic1, partitionGroup1, allNodes, new TreeSet<Integer>(), brokers[leaderId - 1].getId(), -1);
            leaderElections[leaderId - 1] = electionManager[leaderId - 1].getLeaderElection(topic1, partitionGroup1);
            electionManager[leaderId - 1].addListener(new ElectionEventListener());

            Thread.sleep(60000);
            leaderId = leaderId1;
        }

        produceTask.stop(true);
        consumeTask.stop(true);

        System.out.println("Produce task and consume task interrupted.");

        Thread.sleep(10000);

        long[] messageLength = new long[RAFT_ELECTION_NUM];
        for (int i = 0; i < RAFT_ELECTION_NUM; i++) {
            ReplicableStore rStore = storeServices[i].getReplicableStore(topic1.getFullName(), partitionGroup1);
            ByteBuffer messages = rStore.readEntryBuffer(0, Integer.MAX_VALUE);
            messageLength[i] += messages.remaining();
            long position = 0;
            while (messages.remaining() > 0) {
                position += messages.remaining();
                messages = rStore.readEntryBuffer(position, Integer.MAX_VALUE);
                messageLength[i] += messages.remaining();
            }
            messageLength[i] += messages.remaining();

            logger.info("Store {} message length is {}", i, messageLength[i]);

            if (i > 0) Assert.assertEquals(messageLength[i], messageLength[i - 1]);
        }

        Thread.sleep(10000);

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
                storeServices[i].createPartitionGroup(topics[j], partitionGroup1, new short[]{1}, new int[]{1});
                electionManager[i].onPartitionGroupCreate(PartitionGroup.ElectType.raft,
                        TopicName.parse(topics[j]), partitionGroup1, allNodes, new TreeSet<Integer>(), brokers[i].getId(), -1);
                multiLeaderElections[i][j] = electionManager[i].getLeaderElection(TopicName.parse(topics[j]), partitionGroup1);
            }
            electionManager[i].addListener(new ElectionEventListener());
        }

        Thread.sleep(5000);

        int[] leaders = new int[TOPIC_NUM];
        for (int i = 0; i < TOPIC_NUM; i++) {
            int leaderId = multiLeaderElections[0][i].getLeaderId();
            while (leaderId == -1) {
                Thread.sleep(1000);
                leaderId = multiLeaderElections[0][i].getLeaderId();
            }
            Assert.assertNotEquals(leaderId, -1);
            leaders[i] = leaderId;
            logger.info("Leader of topic {} is {}", topics[i], leaderId);
        }

        Thread.sleep(15000);

        for (int i = 0; i < TOPIC_NUM; i++) {
            logger.info("Leader of topic {}, 1 is {}", topics[i], multiLeaderElections[1][i].getLeaderId());
            logger.info("Leader of topic {}, 2 is {}", topics[i], multiLeaderElections[2][i].getLeaderId());
            Assert.assertEquals(leaders[i], multiLeaderElections[1][i].getLeaderId());
            Assert.assertEquals(leaders[i], multiLeaderElections[2][i].getLeaderId());
        }

        MultiProduceTask produceTask = new MultiProduceTask(leaders);
        produceTask.start();

        MultiConsumeTask consumeTask = new MultiConsumeTask(leaders);
        consumeTask.start();

        Thread.sleep(1200000);

        produceTask.stop(true);
        consumeTask.stop(true);

        System.out.println("Produce task and consume task interrupted.");

        Thread.sleep(10000);

        for (int j = 0; j < TOPIC_NUM; j++) {
            for (int i = 0; i < RAFT_ELECTION_NUM; i++) {
                ReplicableStore rStore = storeServices[i].getReplicableStore(topics[j], partitionGroup1);
                System.out.println("Topic " + topics[j] +" / store " + i +  "'s left is " + rStore.leftPosition()
                        + ", flush position is " + rStore.rightPosition()
                        + ", commit position is " + rStore.commitPosition()
                        + ", term is " + rStore.term());
            }
        }
        Thread.sleep(10000);

        //Assert.assertEquals(messages.size(), 10);

    }

    @Test
    public void testStopReplica() throws Exception{

        List<Broker> allNodes = new LinkedList<>();
        for (int i = 0; i < NODE_NUM; i++) {
            allNodes.add(brokers[i]);
        }

        for (int i = 0; i < RAFT_ELECTION_NUM; i++) {
            storeServices[i].createPartitionGroup(topic1.getFullName(), partitionGroup1, new short[]{1}, new int[]{1});
            electionManager[i].onPartitionGroupCreate(PartitionGroup.ElectType.raft,
                    topic1, partitionGroup1, allNodes, new TreeSet<>(), brokers[i].getId(), -1);
            leaderElections[i] = electionManager[i].getLeaderElection(topic1, partitionGroup1);
            electionManager[i].addListener(new ElectionEventListener());
        }

        Thread.sleep(5000);
        int leaderId = leaderElections[0].getLeaderId();
        while (leaderId == -1) {
            Thread.sleep(1000);
            leaderId = leaderElections[0].getLeaderId();
        }
        Assert.assertNotEquals(leaderId, -1);
        logger.info("Leader id is " + leaderId);
        Assert.assertEquals(leaderId, leaderElections[1].getLeaderId());
        Assert.assertEquals(leaderId, leaderElections[2].getLeaderId());

        produceTask.start();
        consumeTask.start();

        Thread.sleep(30000);

        electionManager[nextNode(leaderId) - 1].removeLeaderElection(topic1.getFullName(), partitionGroup1);
        electionManager[nextNode(leaderId) - 1].stop();
        logger.info("Node " + leaderId + " stop");

        Thread.sleep(30000);
        int leaderId1 = leaderElections[nextNode(leaderId) - 1].getLeaderId();
        if (leaderId1 == -1) {
            logger.info("Leader1 id is -1");
            Thread.sleep(30000);
        }
        Assert.assertNotEquals(leaderId1, -1);
        logger.info("Leader1 id is " + leaderId1);

        for (int i = 0; i < RAFT_ELECTION_NUM; i++) {
            ReplicableStore rStore = storeServices[i].getReplicableStore(topic1.getFullName(), partitionGroup1);
            System.out.println("Store " + i + "'s left is " + rStore.leftPosition()
                    + ", write position is " + rStore.rightPosition()
                    + ", commit position is " + rStore.commitPosition()
                    + ", term is " + rStore.term());
        }
        Thread.sleep(10000);

        //Assert.assertEquals(messages.size(), 10);

    }


    @Test
    public void testChangeNode() throws Exception {
        List<Broker> allNodes = new LinkedList<>();
        for (int i = 0; i < NODE_NUM; i++) {
            allNodes.add(brokers[i]);
        }

        for (int i = 0; i < RAFT_ELECTION_NUM; i++) {
            storeServices[i].createPartitionGroup(topic1.getFullName(), partitionGroup1, new short[]{1}, new int[]{1});
            electionManager[i].onPartitionGroupCreate(PartitionGroup.ElectType.raft,
                    topic1, partitionGroup1, allNodes, new TreeSet<>(), brokers[i].getId(), -1);
            leaderElections[i] = electionManager[i].getLeaderElection(topic1, partitionGroup1);
        }

        Thread.sleep(30000);
        int leaderId = leaderElections[0].getLeaderId();
        Assert.assertNotEquals(leaderId, -1);
        logger.info("Leader id is " + leaderId);
        Assert.assertEquals(leaderId, leaderElections[1].getLeaderId());
        Assert.assertEquals(leaderId, leaderElections[2].getLeaderId());

        for (int i = 0; i < RAFT_ELECTION_NUM; i++) {
            electionManager[i].onNodeRemove(topic1, partitionGroup1, leaderId, brokers[i].getId());
        }

        Thread.sleep(30000);
        int leaderId1 = leaderElections[nextNode(leaderId) - 1].getLeaderId();
        logger.info("Leader1 id is " + leaderId1);
        Assert.assertNotEquals(leaderId1, -1);

        List<Broker> newNodes = new LinkedList<>();
        for (int i = 0; i < brokers.length; i++) newNodes.add(brokers[i]);
        for (int i = 0; i < RAFT_ELECTION_NUM; i++) {
            LeaderElection leaderElection = electionManager[i].getLeaderElection(topic1, partitionGroup1);
            if (leaderElection == null) {
                Assert.assertEquals(i, leaderId - 1);
                electionManager[i].onPartitionGroupCreate(PartitionGroup.ElectType.raft, topic1,
                        partitionGroup1, newNodes, new HashSet<Integer>(), i + 1, -1);
                leaderElection = electionManager[i].getLeaderElection(topic1, partitionGroup1);
            }

            if (leaderElection instanceof FixLeaderElection) {
                electionManager[i].onNodeAdd(topic1, partitionGroup1, PartitionGroup.ElectType.fix, newNodes,
                        new HashSet<>(), brokers[leaderId - 1], brokers[i].getId(), leaderId1);
            } else {
                electionManager[i].onNodeAdd(topic1, partitionGroup1, PartitionGroup.ElectType.raft, newNodes,
                        new HashSet<>(), brokers[leaderId - 1], brokers[i].getId(), leaderId1);
            }
        }

        Thread.sleep(10000);

    }

    private void produceMessage(int leaderId, String topic, int partitionGroup, int messageCount) throws Exception {

        PartitionGroupStore pgStore = storeServices[leaderId].getStore(topic, partitionGroup, QosLevel.PERSISTENCE);
        ReplicableStore replicableStores = storeServices[leaderId].getReplicableStore(topic, partitionGroup);
        if (!replicableStores.serviceStatus()) {
            logger.info("Produce message, store status is {}", replicableStores.serviceStatus());
            Thread.sleep(1000);
            return;
        }

        for (int i = 0; i < messageCount; i++) {
            BrokerMessage brokerMessage = new BrokerMessage();
            brokerMessage.setSource(SourceType.JMQ2.getValue());
            brokerMessage.setClientIp("10.1.1.1".getBytes());
            brokerMessage.setBody(("Test_" + i).getBytes());

            int msgSize = Serializer.sizeOf(brokerMessage);
            ByteBuffer buf = ByteBuffer.allocate(msgSize);
            Serializer.write(brokerMessage, buf, msgSize);


            CRC32 crc32 = new CRC32();
            crc32.update(MessageParser.getByteBuffer(buf,MessageParser.BODY));
            MessageParser.setLong(buf,MessageParser.CRC,crc32.getValue());
            //List<RByteBuffer> partitionedBuf = new ArrayList<>();
            //partitionedBuf.add(buf);

            //logger.info("Topic {}, will write message to {}", topic, leaderId);
            Future<WriteResult> writeResultFuture = pgStore.asyncWrite(new WriteRequest((short) 1, buf));
            WriteResult writeResult = writeResultFuture.get(100, TimeUnit.MILLISECONDS);
            if (writeResult != null) {
                logger.info("Topic {}, Write message to {} return code {}, indexies = {}",
                        topic, leaderId, writeResult.getCode(), writeResult.getIndices());
            }
        }
    }

    private ByteBuffer consumeMessage(int nodeId, String topic, int messageCount) throws Exception {
        ReplicableStore replicableStore = storeServices[nodeId].getReplicableStore(topic, partitionGroup1);
        return replicableStore.readEntryBuffer(0, Integer.MAX_VALUE);
    }

    private class ProduceTask extends Thread {
        private int leaderId;
        private boolean stop = false;

        ProduceTask(int leaderId) {
            this.leaderId = leaderId;
        }

        void setLeaderId(int leaderId) {
            this.leaderId = leaderId;
        }

        void stop(boolean stop) {
            this.stop = stop;
        }

        @Override
        public void run() {
            while(true) {
                try {
                    if (leaderId != -1) {
                        produceMessage(leaderId - 1, topic1.getFullName(), partitionGroup1, 10);
                    }
                    Thread.sleep(100);
                } catch (Exception e) {
                    logger.info("Produce message to {} fail, exception is {}", leaderId, e);
                    e.printStackTrace();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        e.printStackTrace();
                    }
                }
                if (stop) break;
            }
        }

    }

    private class ConsumeTask extends Thread {
        private int leaderId = 0;
        private boolean stop = false;

        ConsumeTask(int leaderId) {
            this.leaderId = leaderId;
        }

        void setLeaderId(int leaderId) {
            this.leaderId = leaderId;
        }

        void stop(boolean stop) {
            this.stop = stop;
        }

        @Override
        public void run() {
            while(true) {
                try {
                    ByteBuffer messages = consumeMessage(leaderId - 1, topic1.getFullName(), 10);
                    logger.info("Consume {} messages from {}", messages.remaining(), leaderId);
                    Thread.sleep(100);
                } catch (Exception e) {
                    logger.info("Consume message fail, exception is {}", e);
                    e.printStackTrace();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                        e.printStackTrace();
                    }
                }

                if (stop) break;
            }
        }

    }

    private class ElectionEventListener implements EventListener<ElectionEvent> {
        @Override
        public void onEvent(ElectionEvent event) {
            logger.info("Election event listener, type is {}, leader id is {}",
                    event.getEventType(), event.getLeaderId());
            if (event.getEventType() == ElectionEvent.Type.LEADER_FOUND) {
                produceTask.setLeaderId(event.getLeaderId());
                consumeTask.setLeaderId(event.getLeaderId());
            }
        }
    }


    private class MultiProduceTask extends Thread {
        private int[] leaders;
        private boolean stop = false;

        MultiProduceTask(int[] leaders) {
            this.leaders = leaders;
        }

        void stop(boolean stop) {
            this.stop = stop;
        }

        @Override
        public void run() {
            while(true) {
                for (int i = 0; i < TOPIC_NUM; i++) {
                    try {
                        produceMessage(leaders[i] - 1, topics[i], partitionGroup1, 10);
                        Thread.sleep(100);
                    }catch(Exception e){
                        logger.info("Produce message to {} fail, exception is {}", leaders[i], e);
                        e.printStackTrace();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                            e.printStackTrace();
                        }
                    }
                }
                if (stop) break;
            }
        }

    }

    private class MultiConsumeTask extends Thread {
        private int[] leaders = new int[TOPIC_NUM];
        private boolean stop = false;

        MultiConsumeTask(int[] leaders) {
            this.leaders = leaders;
        }

        void stop(boolean stop) {
            this.stop = stop;
        }

        @Override
        public void run() {
            while(true) {
                for (int i = 0; i < TOPIC_NUM; i++) {
                    try {
                        ByteBuffer messages = consumeMessage(leaders[i] - 1, topics[i], 10);
                        logger.info("Consume {} messages from {}", messages.remaining(), leaders[i]);
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        logger.info("Consume message fail, exception is {}", e);
                        e.printStackTrace();
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ie) {
                            e.printStackTrace();
                        }
                    }
                }
                if (stop) break;
            }
        }

    }
}
