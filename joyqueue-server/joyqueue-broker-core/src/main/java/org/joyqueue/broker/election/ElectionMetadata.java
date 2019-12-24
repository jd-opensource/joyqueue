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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.joyqueue.toolkit.io.DoubleCopy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.joyqueue.domain.PartitionGroup.ElectType;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/11/24
 */
public class ElectionMetadata extends DoubleCopy {
    private static Logger logger = LoggerFactory.getLogger(ElectionMetadata.class);

    private int version = 1;

    private ElectType electType;
    private Collection<DefaultElectionNode> allNodes;
    private Set<Integer> learners = new HashSet<>();
    private int localNodeId = ElectionNode.INVALID_NODE_ID;
    private int leaderId = ElectionNode.INVALID_NODE_ID;
    private int votedFor = ElectionNode.INVALID_NODE_ID;
    private int currentTerm = 0;

    private static int MAX_LENGTH = 10 * 1024;

    public ElectionMetadata(File file) throws IOException {
        super(file, MAX_LENGTH);
    }

    @Override
    protected String getName() {
        return "electionMetadata";
    }


    @Override
    protected  byte[] serialize() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(MAX_LENGTH);

        serializeString(byteBuffer, "version:");
        serializeString(byteBuffer, Integer.valueOf(version).toString());

        serializeString(byteBuffer, "electType:");
        serializeString(byteBuffer, String.valueOf(electType.type()));

        serializeString(byteBuffer, JSON.toJSONString(allNodes));

        serializeString(byteBuffer, JSON.toJSONString(learners));

        serializeString(byteBuffer, "localNodeId:");
        serializeString(byteBuffer, Integer.valueOf(localNodeId).toString());

        serializeString(byteBuffer, "leaderId:");
        serializeString(byteBuffer, Integer.valueOf(leaderId).toString());

        serializeString(byteBuffer, "voteFor:");
        serializeString(byteBuffer, Integer.valueOf(votedFor).toString());

        serializeString(byteBuffer, "currentTerm:");
        serializeString(byteBuffer, Integer.valueOf(currentTerm).toString());

        serializeString(byteBuffer, "-end");

        logger.info("Metadata serialize length is {}", byteBuffer.position());

        byte[] bytes = new byte[byteBuffer.position()];
        byteBuffer.flip();
        byteBuffer.get(bytes);

        return bytes;
    }

    private void serializeString(ByteBuffer byteBuffer, String value) {
        byte[] valueBytes = value.getBytes();
        byteBuffer.putInt(valueBytes.length);
        byteBuffer.put(valueBytes);
    }

    @Override
    protected void parse(byte [] data) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);

        parseString(byteBuffer); //version
        version = Integer.valueOf(parseString(byteBuffer));

        parseString(byteBuffer); //electType
        electType = ElectType.valueOf(Integer.valueOf(parseString(byteBuffer)));

        String allNodesStr = parseString(byteBuffer);
        allNodes = JSON.parseObject(allNodesStr,
                new TypeReference<Collection<DefaultElectionNode>>() {
                });

        String learnersStr = parseString(byteBuffer);
        learners = JSON.parseObject(learnersStr,
                new TypeReference<Set<Integer>>() {
                });

        parseString(byteBuffer); //localNodeId
        localNodeId = Integer.valueOf(parseString(byteBuffer));

        parseString(byteBuffer); //leaderId
        leaderId = Integer.valueOf(parseString(byteBuffer));

        parseString(byteBuffer); //voteFor
        votedFor = Integer.valueOf(parseString(byteBuffer));

        parseString(byteBuffer); //currentTerm
        currentTerm = Integer.valueOf(parseString(byteBuffer));
    }

    private String parseString(ByteBuffer byteBuffer) {
        int length = byteBuffer.getInt();
        byte[] bytes = new byte[length];
        byteBuffer.get(bytes);
        return new String(bytes);
    }

    public ElectType getElectType() {
        return electType;
    }

    public void setElectType(ElectType electType) {
        this.electType = electType;
    }

    public Collection<DefaultElectionNode> getAllNodes() {
        return allNodes;
    }

    public void setAllNodes(Collection<DefaultElectionNode> allNodes) {
        this.allNodes = allNodes;
    }

    public Set<Integer> getLearners() {
        return learners;
    }

    public void setLearners(Set<Integer> learners) {
        this.learners = learners;
    }

    public int getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(int leaderId) {
        this.leaderId = leaderId;
    }

    public int getLocalNodeId() {
        return localNodeId;
    }

    public void setLocalNodeId(int localNodeId) {
        this.localNodeId = localNodeId;
    }

    public void setVotedFor(int votedFor) {
        this.votedFor = votedFor;
    }

    public int getVotedFor() {
        return votedFor;
    }

    public int getCurrentTerm() {
        return currentTerm;
    }

    public void setCurrentTerm(int currentTerm) {
        this.currentTerm = currentTerm;
    }

    @Override
    public String toString() {
        return new StringBuffer("ElectionMetadata{")
                .append("electType:").append(electType)
                .append(", allNodes:").append(JSON.toJSONString(allNodes))
                .append(", learners:").append(JSON.toJSONString(learners))
                .append(", localId:").append(localNodeId)
                .append(", currentTerm:").append(currentTerm)
                .append(", votedFor:").append(votedFor)
                .append("}").toString();
    }

    public static class Build {
        private ElectionMetadata metadata;

        private Build(String fileName) throws IOException {
            metadata = new ElectionMetadata(new File(fileName));
        }

        public static Build create(String path, TopicPartitionGroup topicPartitionGroup) throws IOException {
            String fileName = path + File.separator + topicPartitionGroup.getTopic().replace(File.separatorChar, '@') +
                    File.separator + topicPartitionGroup.getPartitionGroupId();
            return new Build(fileName);
        }

        public Build electionType(ElectType electType) {
            metadata.setElectType(electType);
            return this;
        }

        public Build allNodes(Collection<DefaultElectionNode> allNodes) {
            metadata.setAllNodes(allNodes);
            return this;
        }

        public Build learners(Set<Integer> learners) {
            metadata.setLearners(learners);
            return this;
        }

        public Build localNode(int localNode) {
            metadata.setLocalNodeId(localNode);
            return this;
        }

        public Build leaderId(int leaderId) {
            metadata.setLeaderId(leaderId);
            return this;
        }

        public Build currentTerm(int currentTerm) {
            metadata.setCurrentTerm(currentTerm);
            return this;
        }

        public Build votedFor(int votedFor) {
            metadata.setVotedFor(votedFor);
            return this;
        }

        public ElectionMetadata build() {
            return metadata;
        }
    }
}
