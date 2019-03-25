package com.jd.journalq.broker.election;

import com.jd.journalq.domain.PartitionGroup;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by zhuduohui on 2018/8/27.
 */
public class ElectionMetadataManagerTest {
    private String metadataFile = "./metadata.dat";
    private ElectionMetadataManager electionMetadataManager;
    private int localNodeId = 1;
    private List<DefaultElectionNode> allNodes = new LinkedList<>();
    private Set<Integer> learners = new HashSet<>();
    private int leaderId = 2;
    private TopicPartitionGroup fixTopic = new TopicPartitionGroup("fix", 1);
    private TopicPartitionGroup raftTopic = new TopicPartitionGroup("raft", 2);

    @Before
    public void setUp() throws Exception {
        electionMetadataManager = new ElectionMetadataManager(new File(metadataFile));
        electionMetadataManager.start();

        allNodes.add(new DefaultElectionNode("", 1));
        allNodes.add(new DefaultElectionNode("", 2));

        learners.add(1);
        learners.add(2);
    }

    @After
    public void teardown() {
        electionMetadataManager.stop();
    }

    @Test
    public void testOneMetadata() {

        ElectionMetadata raftMetadata = ElectionMetadata.Build.create().electionType(PartitionGroup.ElectType.raft)
                .allNodes(allNodes).learners(learners)
                .localNode(localNodeId).currentTerm(1).votedFor(2).build();
        electionMetadataManager.updateElectionMetadata(raftTopic, raftMetadata);

        ElectionMetadata fixMetadata = ElectionMetadata.Build.create().electionType(PartitionGroup.ElectType.fix)
                .allNodes(allNodes).learners(learners)
                .localNode(localNodeId).leaderId(leaderId).build();
        electionMetadataManager.updateElectionMetadata(fixTopic, fixMetadata);

        electionMetadataManager.stop();
        try {
            electionMetadataManager.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

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

    }

    @Test
    public void testMultiMetadata() {
        for (int i = 0; i < 100; i++) {
            TopicPartitionGroup partitionGroup = new TopicPartitionGroup("test", i);
            ElectionMetadata metadata = ElectionMetadata.Build.create().allNodes(allNodes).learners(learners)
                    .localNode(localNodeId).currentTerm(i + 1).votedFor(i + 2).build();
            electionMetadataManager.updateElectionMetadata(partitionGroup, metadata);
        }

        for (int i = 0; i < 100; i++) {
            TopicPartitionGroup partitionGroup = new TopicPartitionGroup("test", i);
            ElectionMetadata metadata = electionMetadataManager.getElectionMetadata(partitionGroup);
            Assert.assertEquals(metadata.getCurrentTerm(), i + 1);
            Assert.assertEquals(metadata.getVotedFor(), i + 2);
            Assert.assertEquals(metadata.getAllNodes().size(), allNodes.size());
            Assert.assertEquals(metadata.getLocalNodeId(), localNodeId);
            Assert.assertEquals(metadata.getLearners().size(), learners.size());
        }
    }

}
