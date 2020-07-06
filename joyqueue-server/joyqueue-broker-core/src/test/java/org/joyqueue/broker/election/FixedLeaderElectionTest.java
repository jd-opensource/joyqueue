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
import org.joyqueue.store.StoreService;
import org.joyqueue.store.replication.ReplicableStore;
import org.joyqueue.toolkit.concurrent.EventListener;
import org.joyqueue.toolkit.io.Files;
import org.joyqueue.toolkit.network.IpUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

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
    private String localIp = IpUtil.getLocalIp();
    private int leaderId = 1;

    private StoreService[] storeServices = new StoreService[FIX_ELECTION_NUM];

    private TopicName topic1 = TopicName.parse("test");
    private int partitionGroup1 = 1;

    private ProduceTask produceTask = new ProduceTask(storeServices[leaderId], topic1, partitionGroup1);
    private ConsumeTask consumeTask = new ConsumeTask(storeServices[leaderId], topic1, partitionGroup1);

    private short[] partitions = new short[]{0, 1, 2, 3, 4};

    private String getStoreDir() {
        String property = "java.io.tmpdir";
        return System.getProperty(property) + File.separator + "store";
    }

    private String getElectionDir() {
        String property = "java.io.tmpdir";
        return System.getProperty(property) + File.separator + "election";
    }

    @Before
    public void setUp() throws Exception {

        for (int i = 0; i < FIX_ELECTION_NUM; i++) {
            Configuration conf = new Configuration();
            StoreConfig storeConfig = new StoreConfig(conf);
            storeConfig.setPath(getStoreDir() + i);
            storeServices[i] = new Store(storeConfig);
            ((Store)storeServices[i]).start();

            ElectionConfig electionConfig = new ElectionConfig(conf);
            electionConfig.setElectionMetaPath(getElectionDir() + i);
            electionConfig.setListenPort("1800" + (i + 1));

            electionManager[i] = new ElectionManagerStub(electionConfig, storeServices[i], new ConsumeStub());
            electionManager[i].start();
        }

        for (int i = 0; i < FIX_NODE_NUM; i++) {
            brokers[i] = new Broker();
            brokers[i].setId(i + 1);
            brokers[i].setIp(localIp);
            brokers[i].setPort(18000 + i);
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

        for (int i = 0; i < FIX_ELECTION_NUM; i++) {
            if (storeServices[i] != null) {
                storeServices[i].removePartitionGroup(topic1.getFullName(), partitionGroup1);
                ((Store)storeServices[i]).stop();
            }

            if (electionManager[i] != null) {
                leaderElections[i] = electionManager[i].getLeaderElection(topic1, partitionGroup1);
                if (leaderElections[i] != null) leaderElections[i].stop();
                electionManager[i].onPartitionGroupRemove(topic1, partitionGroup1);
                electionManager[i].stop();
            }
        }

        Files.deleteDirectory(new File(getStoreDir()));
        Files.deleteDirectory(new File(getElectionDir()));
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
            storeServices[i].createPartitionGroup(topic1.getFullName(), partitionGroup1, partitions);
            electionManager[i].onPartitionGroupCreate(PartitionGroup.ElectType.fix,
                    topic1, partitionGroup1, allNodes, new TreeSet<>(), brokers[i].getId(), leaderId);
            leaderElections[i] = electionManager[i].getLeaderElection(topic1, partitionGroup1);
            electionManager[i].addListener(new ElectionEventListener());
        }

        Thread.sleep(5000);
        int leaderId = leaderElections[0].getLeaderId();
        Assert.assertNotEquals(leaderId, -1);
        logger.info("Leader id is " + leaderId);
        Assert.assertEquals(leaderId, leaderElections[nextNode(leaderId) - 1].getLeaderId());

        produceTask.setStoreService(storeServices[leaderId - 1]);
        consumeTask.setStoreService(storeServices[leaderId - 1]);

        produceTask.start();
        consumeTask.start();

        Thread.sleep(3000);

        produceTask.stop(true);
        consumeTask.stop(true);

        produceTask.interrupt();
        consumeTask.interrupt();

        System.out.println("Produce task and consume task interrupted.");

        Thread.sleep(3000);

        for (int i = 0; i < FIX_ELECTION_NUM; i++) {
            ReplicableStore rStore = storeServices[i].getReplicableStore(topic1.getFullName(), partitionGroup1);
            System.out.println("Store " + i + "'s left is " + rStore.leftPosition()
                    + ", write position is " + rStore.rightPosition()
                    + ", commit position is " + rStore.commitPosition()
                    + ", term is " + rStore.term());
        }
        Thread.sleep(1000);

        //Assert.assertEquals(messages.size(), 10);

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

