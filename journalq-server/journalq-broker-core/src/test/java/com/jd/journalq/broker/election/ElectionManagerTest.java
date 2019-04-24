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

import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.BrokerService;
import com.jd.journalq.domain.Broker;
import com.jd.journalq.domain.PartitionGroup;
import com.jd.journalq.domain.TopicName;

import com.jd.journalq.store.StoreService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by zhuduohui on 2018/8/27.
 */
public class ElectionManagerTest {
    BrokerService brokerService = new BrokerService();

    private ElectionManager electionManager;
    private StoreService storeService;

    private Broker broker1;
    private Broker broker2;
    private Broker broker3;

    private String topic1 = "test";
    private int partitionGroup1 = 1;
    private String topic2 = "test1";
    private int partitionGroup2 = 2;

    public ElectionManagerTest() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        brokerService.start();

        BrokerContext brokerContext = brokerService.getBrokerContext();

        electionManager = (ElectionManager) brokerContext.getElectionService();
        storeService = brokerContext.getStoreService();

        broker1 = new Broker();
        broker1.setId(1);
        broker1.setIp("1.2.3.4");
        broker1.setPort(1);
        broker2 = new Broker();
        broker2.setId(2);
        broker2.setIp("1.2.3.5");
        broker2.setPort(2);
        broker3 = new Broker();
        broker3.setId(3);
        broker3.setIp("1.2.3.6");
        broker3.setPort(3);
    }

    @After
    public void tearDown() {
        storeService.removePartitionGroup(topic1, partitionGroup1);
        storeService.removePartitionGroup(topic2, partitionGroup2);
        brokerService.stop();
    }

    @Test
    public void testCreateElection() throws Exception{
        //TopicPartitionGroup topicPartitionGroup = new TopicPartitionGroup("test", 1);

        List<Broker> allNodes1 = new LinkedList<>();
        allNodes1.add(broker1);
        allNodes1.add(broker2);
        allNodes1.add(broker3);

        storeService.removePartitionGroup(topic1, partitionGroup1);
        storeService.createPartitionGroup(topic1, partitionGroup1, new short[]{1});

        electionManager.onPartitionGroupCreate(PartitionGroup.ElectType.fix, new TopicName(topic1),
                partitionGroup1, allNodes1, new TreeSet<>(), broker1.getId(), broker1.getId());
        Assert.assertEquals(electionManager.getLeaderElectionCount(), 1);
        LeaderElection election = electionManager.getLeaderElection(topic1, partitionGroup1);
        Assert.assertEquals(election.getLeaderId(), broker1.getId().longValue());

        storeService.removePartitionGroup(topic2, partitionGroup2);
        storeService.createPartitionGroup(topic2, partitionGroup2, new short[]{1});

        List<Broker> allNodes2 = new LinkedList<>();
        allNodes2.add(broker1);
        allNodes2.add(broker2);
        //TopicPartitionGroup topicPartitionGroup1 = new TopicPartitionGroup(topic2, partitionGroup2);
        electionManager.onPartitionGroupCreate(PartitionGroup.ElectType.fix, new TopicName(topic2),
                partitionGroup2, allNodes1, new TreeSet<>(), broker1.getId(), broker2.getId());
        Assert.assertEquals(electionManager.getLeaderElectionCount(), 2);
        election = electionManager.getLeaderElection(topic1, partitionGroup1);
        Assert.assertEquals(election.getLeaderId(), broker1.getId().longValue());
        election = electionManager.getLeaderElection(topic2, partitionGroup2);
        Assert.assertEquals(election.getLeaderId(), broker2.getId().longValue());

        electionManager.removeLeaderElection(topic1, partitionGroup1);
        Assert.assertEquals(electionManager.getLeaderElectionCount(), 1);
        election = electionManager.getLeaderElection(topic1, partitionGroup1);
        Assert.assertNull(election);
        electionManager.removeLeaderElection(topic2, partitionGroup2);
        Assert.assertEquals(electionManager.getLeaderElectionCount(), 0);
        election = electionManager.getLeaderElection(topic2, partitionGroup2);
        Assert.assertNull(election);

    }
}
