/**
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
package com.jd.journalq.broker.election;

import com.jd.journalq.broker.consumer.Consume;
import com.jd.journalq.broker.buffer.Serializer;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.domain.Broker;
import com.jd.journalq.domain.PartitionGroup;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.message.BrokerMessage;
import com.jd.journalq.message.SourceType;
import com.jd.journalq.store.PartitionGroupStore;
import com.jd.journalq.store.StoreService;
import com.jd.journalq.store.WriteRequest;
import com.jd.journalq.store.WriteResult;
import com.jd.journalq.store.replication.ReplicableStore;
import com.jd.journalq.toolkit.concurrent.EventListener;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.*;

/**
 * Created by zhuduohui on 2018/8/27.
 */
public class FixedLeaderElectionTest {
    private static Logger logger = LoggerFactory.getLogger(RaftLeaderElectionTest.class);

    private final int FIX_ELECTION_NUM = 2;
    private final int FIX_NODE_NUM = 2;

    private ElectionManagerStub[] electionManager = new ElectionManagerStub[FIX_ELECTION_NUM];
    private LeaderElection[] leaderElections = new FixLeaderElection[FIX_ELECTION_NUM];

    private Broker[] brokers = new Broker[FIX_NODE_NUM];
    private String localIp = "10.12.133.224";

    private StoreService[] storeServices;
    private Consume[] consumes = new Consume[FIX_ELECTION_NUM];
    private ClusterManager[] clusterManagers = new ClusterManager[FIX_ELECTION_NUM];

    private TopicName topic1 = TopicName.parse("test");
    private int partitionGroup1 = 1;

    private ExecutorService electionExecutor;


    private ProduceTask produceTask = new ProduceTask(-1);
    private ConsumeTask consumeTask = new ConsumeTask(-1);

    @Before
    public void setUp() throws Exception {
        //TODO store
        /*electionExecutor = new ThreadPoolExecutor(5, 5, 60, TimeUnit.SECONDS, new LinkedBlockingDeque<>());

        for (int i = 0; i < FIX_ELECTION_NUM; i++) {
            StoreConfig storeConfig = new StoreConfig(null);
            storeConfig.setPath("/export/journalq/store" + i);
            storeServices[i] = new Store(storeConfig);

            ElectionConfig electionConfig = new ElectionConfigStub(null,null);
            electionConfig.setMetadataFile("/export/journalq/raft" + i + ".dat");
            electionConfig.setListenPort("1800" + (i + 1));

            //clusterManagers[i] = new ClusterManager();
            //clusterManagers[i].start();
            //consumes[i] = new ConsumeManager(clusterManagers[i], storeServices[i]);
            electionManager[i] = new ElectionManagerStub(electionConfig, storeServices[i], new ConsumeStub());
            electionManager[i].start();
        }

        for (int i = 0; i < FIX_NODE_NUM; i++) {
            brokers[i] = new Broker();
            brokers[i].setId(i + 1);
            brokers[i].setIp(localIp);
            brokers[i].setPort(18000 + i);
        }
*/
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

        for (int i = 0; i < FIX_ELECTION_NUM; i++) {
            //storeServices[i].removePartitionGroup(topic1, partitionGroup1);

            //TODO 临时注释掉
            //if (storeServices[i] != null) storeServices[i].physicalDelete();

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
        if (nodeId == FIX_ELECTION_NUM) return 1;
        else return nodeId + 1;
    }


    @Test
    public void testFixReplication() throws Exception{

        List<Broker> allNodes = new LinkedList<>();
        for (int i = 0; i < FIX_NODE_NUM; i++) {
            allNodes.add(brokers[i]);
        }

        for (int i = 0; i < FIX_ELECTION_NUM; i++) {
            storeServices[i].createPartitionGroup(topic1.getFullName(), partitionGroup1, new short[]{1});
            electionManager[i].onPartitionGroupCreate(PartitionGroup.ElectType.fix,
                    topic1, partitionGroup1, allNodes, new TreeSet<Integer>(), brokers[i].getId(), 1);
            leaderElections[i] = electionManager[i].getLeaderElection(topic1, partitionGroup1);
            electionManager[i].addListener(new ElectionEventListener());
        }

        Thread.sleep(5000);
        int leaderId = leaderElections[0].getLeaderId();
        Assert.assertNotEquals(leaderId, -1);
        logger.info("Leader id is " + leaderId);
        Assert.assertEquals(leaderId, leaderElections[nextNode(leaderId) - 1].getLeaderId());

        produceTask.setLeaderId(leaderId);
        consumeTask.setLeaderId(leaderId);

        produceTask.start();
        consumeTask.start();

        Thread.sleep(60000);

        produceTask.stop(true);
        consumeTask.stop(true);

        produceTask.interrupt();
        consumeTask.interrupt();

        System.out.println("Produce task and consume task interrupted.");

        Thread.sleep(20000);

        for (int i = 0; i < FIX_ELECTION_NUM; i++) {
            ReplicableStore rStore = storeServices[i].getReplicableStore(topic1.getFullName(), partitionGroup1);
            System.out.println("Store " + i + "'s left is " + rStore.leftPosition()
                    + ", write position is " + rStore.rightPosition()
                    + ", commit position is " + rStore.commitPosition()
                    + ", term is " + rStore.term());
        }
        Thread.sleep(10000);

        //Assert.assertEquals(messages.size(), 10);

    }

    private void produceMessage(int leaderId, int messageCount) throws Exception {

        PartitionGroupStore pgStore = storeServices[leaderId].getStore(topic1.getFullName(), partitionGroup1);
        //logger.info("Produce message, store status is {}", ((QosStore) pgStore).getStatus());

        for (int i = 0; i < messageCount; i++) {
            BrokerMessage brokerMessage = new BrokerMessage();
            brokerMessage.setSource(SourceType.JMQ2.getValue());
            brokerMessage.setClientIp("10.1.1.1".getBytes());
            brokerMessage.setBody(new String("Test_" + i).getBytes());

            int msgSize = Serializer.sizeOf(brokerMessage);
            ByteBuffer buf = ByteBuffer.allocate(msgSize);
            Serializer.write(brokerMessage, buf, msgSize);
            //List<RByteBuffer> partitionedBuf = new ArrayList<>();
            //partitionedBuf.add(buf);

            Future<WriteResult> writeResultFuture = pgStore.asyncWrite(new WriteRequest((short)1, buf));
            WriteResult writeResult = writeResultFuture.get();
            logger.info("Write message to {} return code {}, indexies = {}", leaderId, writeResult.getCode(), writeResult.getIndices());

        }
    }

    ByteBuffer consumeMessage(int nodeId, int messageCount) throws Exception {
        ReplicableStore replicableStore = storeServices[nodeId].getReplicableStore(topic1.getFullName(), partitionGroup1);
        return replicableStore.readEntryBuffer(0, Integer.MAX_VALUE);
    }

    private class ProduceTask extends Thread {
        private int leaderId = 0;
        private boolean stop = false;

        public ProduceTask(int leaderId) {
            this.leaderId = leaderId;
        }

        public void setLeaderId(int leaderId) {
            this.leaderId = leaderId;
        }

        public void stop(boolean stop) {
            this.stop = stop;
        }

        @Override
        public void run() {
            while(true) {
                try {
                    produceMessage(leaderId - 1, 10);
                    Thread.sleep(100);
                } catch (Exception e) {
                    logger.info("Produce message to {} fail, exception is {}", leaderId, e);
                    e.printStackTrace();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {

                    }
                }
                if (stop) break;
            }
        }

    }

    private class ConsumeTask extends Thread {
        private int leaderId = 0;
        private boolean stop = false;

        public ConsumeTask(int leaderId) {
            this.leaderId = leaderId;
        }

        public void setLeaderId(int leaderId) {
            this.leaderId = leaderId;
        }

        public void stop(boolean stop) {
            this.stop = stop;
        }

        @Override
        public void run() {
            while(true) {
                try {
                    ByteBuffer messages = consumeMessage(leaderId - 1, 10);
                    logger.info("Consume {} messages from {}", messages.remaining(), leaderId);

                    Thread.sleep(1000);
                } catch (Exception e) {
                    logger.info("Consume message fail, exception is {}", e);
                    e.printStackTrace();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {

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

}

