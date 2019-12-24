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
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.store.Store;
import org.joyqueue.store.StoreConfig;
import org.joyqueue.store.StoreService;
import org.joyqueue.toolkit.io.Files;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by zhuduohui on 2018/8/27.
 */
@Ignore
public class ElectionMetadataManagerTest {
    private String metadataPath = getElectionDir();
    private ElectionMetadataManager electionMetadataManager;
    private int localNodeId = 1;
    private List<DefaultElectionNode> allNodes = new LinkedList<>();
    private Set<Integer> learners = new HashSet<>();
    private int leaderId = 2;
    private TopicPartitionGroup fixTopic = new TopicPartitionGroup("fix", 1);
    private TopicPartitionGroup raftTopic = new TopicPartitionGroup("test/raft", 2);
    private ElectionManager electionManager;
    private StoreService storeService;


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
        allNodes.add(new DefaultElectionNode("192.168.0.1:50089", 1));
        allNodes.add(new DefaultElectionNode("192.168.0.2:50089", 2));

        learners.add(1);
        learners.add(2);

        Configuration conf = new Configuration();
        StoreConfig storeConfig = new StoreConfig(conf);
        storeConfig.setPath(getStoreDir());
        storeService = new Store(storeConfig);
        ((Store) storeService).start();

        ElectionConfig electionConfig = new ElectionConfig(conf);
        electionConfig.setElectionMetaPath(getElectionDir());
        electionConfig.setListenPort("18000");

        electionManager = new ElectionManagerStub(electionConfig, storeService, new ConsumeStub());
        electionManager.start();
    }

    @After
    public void teardown() {
        ((Store)storeService).stop();
        electionManager.stop();

        Files.deleteDirectory(new File(metadataPath));
        Files.deleteDirectory(new File(getStoreDir()));
    }

    @Test
    public void testOneMetadata() {
        try {
            electionMetadataManager = new ElectionMetadataManager(metadataPath);

            ElectionMetadata raftMetadata = ElectionMetadata.Build.create(metadataPath, raftTopic)
                    .electionType(PartitionGroup.ElectType.raft)
                    .allNodes(allNodes).learners(learners)
                    .localNode(localNodeId).currentTerm(1).votedFor(2).build();
            electionMetadataManager.updateElectionMetadata(raftTopic, raftMetadata);

            ElectionMetadata fixMetadata = ElectionMetadata.Build.create(metadataPath, fixTopic)
                    .electionType(PartitionGroup.ElectType.fix)
                    .allNodes(allNodes).learners(learners)
                    .localNode(localNodeId).leaderId(leaderId).build();
            electionMetadataManager.updateElectionMetadata(fixTopic, fixMetadata);
            electionMetadataManager.close();

            electionMetadataManager = new ElectionMetadataManager(metadataPath);
            electionMetadataManager.recover(electionManager);

            ElectionMetadata raftMetadataLoad = electionMetadataManager.getElectionMetadata(raftTopic);
            Assert.assertEquals(raftMetadataLoad.getCurrentTerm(), 1);
            Assert.assertEquals(raftMetadataLoad.getVotedFor(), 2);
            Assert.assertEquals(raftMetadataLoad.getAllNodes().size(), allNodes.size());
            Assert.assertEquals(raftMetadataLoad.getLocalNodeId(), localNodeId);
            Assert.assertEquals(raftMetadataLoad.getLearners().size(), learners.size());

            ElectionMetadata fixMetadataLoad = electionMetadataManager.getElectionMetadata(fixTopic);
            Assert.assertEquals(fixMetadataLoad.getAllNodes().size(), allNodes.size());
            Assert.assertEquals(fixMetadataLoad.getLocalNodeId(), localNodeId);
            Assert.assertEquals(fixMetadataLoad.getLeaderId(), leaderId);

            electionMetadataManager.removeElectionMetadata(raftTopic);
            electionMetadataManager.removeElectionMetadata(fixTopic);

            raftMetadata = electionMetadataManager.getElectionMetadata(raftTopic);
            Assert.assertNull(raftMetadata);
            fixMetadata = electionMetadataManager.getElectionMetadata(fixTopic);
            Assert.assertNull(fixMetadata);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

    }

    @Test
    public void testMultiMetadata() {
        try {
            electionMetadataManager = new ElectionMetadataManager(metadataPath);
            for (int i = 0; i < 100; i++) {
                TopicPartitionGroup partitionGroup = new TopicPartitionGroup("test", i);
                ElectionMetadata metadata = ElectionMetadata.Build.create(metadataPath, partitionGroup)
                        .electionType(PartitionGroup.ElectType.raft)
                        .allNodes(allNodes).learners(learners)
                        .localNode(localNodeId).currentTerm(i + 1).votedFor(i + 2).build();
                electionMetadataManager.updateElectionMetadata(partitionGroup, metadata);
            }
            electionMetadataManager.close();

            electionMetadataManager = new ElectionMetadataManager(metadataPath);
            electionMetadataManager.recover(electionManager);
            for (int i = 0; i < 100; i++) {
                TopicPartitionGroup partitionGroup = new TopicPartitionGroup("test", i);
                ElectionMetadata metadata = electionMetadataManager.getElectionMetadata(partitionGroup);
                Assert.assertEquals(metadata.getCurrentTerm(), i + 1);
                Assert.assertEquals(metadata.getVotedFor(), i + 2);
                Assert.assertEquals(metadata.getAllNodes().size(), allNodes.size());
                Assert.assertEquals(metadata.getLocalNodeId(), localNodeId);
                Assert.assertEquals(metadata.getLearners().size(), learners.size());

                electionMetadataManager.removeElectionMetadata(partitionGroup);
            }
            electionMetadataManager.close();
        } catch (Exception e) {
            Assert.fail();
        }
    }

}
