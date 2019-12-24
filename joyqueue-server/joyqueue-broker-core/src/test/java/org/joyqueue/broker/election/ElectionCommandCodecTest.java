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

import org.joyqueue.broker.consumer.model.ConsumePartition;
import org.joyqueue.broker.consumer.position.model.Position;
import org.joyqueue.broker.election.TopicPartitionGroup;
import org.joyqueue.broker.election.command.AppendEntriesRequest;
import org.joyqueue.broker.election.command.AppendEntriesResponse;
import org.joyqueue.broker.election.command.ReplicateConsumePosRequest;
import org.joyqueue.broker.election.command.ReplicateConsumePosResponse;
import org.joyqueue.broker.election.command.TimeoutNowRequest;
import org.joyqueue.broker.election.command.TimeoutNowResponse;
import org.joyqueue.broker.election.command.VoteRequest;
import org.joyqueue.broker.election.command.VoteResponse;
import org.joyqueue.broker.election.network.codec.AppendEntriesRequestDecoder;
import org.joyqueue.broker.election.network.codec.AppendEntriesRequestEncoder;
import org.joyqueue.broker.election.network.codec.AppendEntriesResponseDecoder;
import org.joyqueue.broker.election.network.codec.AppendEntriesResponseEncoder;
import org.joyqueue.broker.election.network.codec.ReplicateConsumePosRequestDecoder;
import org.joyqueue.broker.election.network.codec.ReplicateConsumePosRequestEncoder;
import org.joyqueue.broker.election.network.codec.ReplicateConsumePosResponseDecoder;
import org.joyqueue.broker.election.network.codec.ReplicateConsumePosResponseEncoder;
import org.joyqueue.broker.election.network.codec.TimeoutNowRequestDecoder;
import org.joyqueue.broker.election.network.codec.TimeoutNowRequestEncoder;
import org.joyqueue.broker.election.network.codec.TimeoutNowResponseDecoder;
import org.joyqueue.broker.election.network.codec.TimeoutNowResponseEncoder;
import org.joyqueue.broker.election.network.codec.VoteRequestDecoder;
import org.joyqueue.broker.election.network.codec.VoteRequestEncoder;
import org.joyqueue.broker.election.network.codec.VoteResponseDecoder;
import org.joyqueue.broker.election.network.codec.VoteResponseEncoder;
import org.joyqueue.network.transport.codec.JoyQueueHeader;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class ElectionCommandCodecTest {

    @Test
    public void testAppendEntriesCodec() throws Exception {
        final long commitPosition = 110L;
        final long startPosition = 15L;
        final long leftPosition = 14L;
        final long prevPosition = 13L;

        final int entriesTerm = 3;
        final int prevTerm = 2;
        final int term = 4;

        final int leaderId = 30;

        final boolean match = true;

        final TopicPartitionGroup topicPartitionGroup = new TopicPartitionGroup("test", 1);

        ByteBuffer entries = ByteBuffer.allocate(100);
        entries.putInt(1);
        entries.putInt(2);
        entries.putInt(3);
        entries.flip();
        final int entriesLength = entries.remaining();

        AppendEntriesRequest request = AppendEntriesRequest.Build.create()
                .commitPosition(commitPosition)
                .entries(entries)
                .entriesTerm(entriesTerm)
                .leader(leaderId)
                .leftPosition(leftPosition)
                .match(match)
                .partitionGroup(topicPartitionGroup)
                .prevPosition(prevPosition)
                .prevTerm(prevTerm)
                .startPosition(startPosition)
                .term(term)
                .build();
        request.setHeader(new JoyQueueHeader());

        AppendEntriesRequestEncoder encoder = new AppendEntriesRequestEncoder();
        ByteBuf byteBuf = Unpooled.buffer(512);
        encoder.encode(request, byteBuf);

        AppendEntriesRequestDecoder decoder = new AppendEntriesRequestDecoder();
        JoyQueueHeader header = new JoyQueueHeader(request.type());
        AppendEntriesRequest decodeRequest = (AppendEntriesRequest)decoder.decode(header, byteBuf);

        Assert.assertEquals(decodeRequest.getCommitPosition(), commitPosition);
        Assert.assertEquals(decodeRequest.getLeftPosition(), leftPosition);
        Assert.assertEquals(decodeRequest.getStartPosition(), startPosition);
        Assert.assertEquals(decodeRequest.getPrevPosition(), prevPosition);
        Assert.assertEquals(decodeRequest.getEntriesLength(), entriesLength);
        //Assert.assertEquals(decodeRequest.getEntriesTerm(), entriesTerm);
        Assert.assertEquals(decodeRequest.getLeaderId(), leaderId);
        Assert.assertEquals(decodeRequest.getPrevTerm(), prevTerm);
        Assert.assertEquals(decodeRequest.getTerm(), term);
        Assert.assertEquals(decodeRequest.getTopic(), topicPartitionGroup.getTopic());
        Assert.assertEquals(decodeRequest.getPartitionGroup(), topicPartitionGroup.getPartitionGroupId());

        ByteBuffer decodeEntires = decodeRequest.getEntries();
        Assert.assertEquals(decodeEntires.remaining(), entriesLength);
        Assert.assertEquals(1, decodeEntires.getInt());
        Assert.assertEquals(2, decodeEntires.getInt());
        Assert.assertEquals(3, decodeEntires.getInt());
    }

    @Test
    public void testAppendEntriesResponseCodec() throws Exception {
        final int entriesTerm = 1;
        final int replicaId = 8;
        final int term = 10;
        final long writePosition = 100L;
        final long nextPosition = 110L;
        final boolean success = true;

        final TopicPartitionGroup topicPartitionGroup = new TopicPartitionGroup("test", 1);

        AppendEntriesResponse response = AppendEntriesResponse.Build.create()
                .entriesTerm(entriesTerm)
                .nextPosition(nextPosition)
                .replicaId(replicaId)
                .success(success)
                .term(term)
                .topicPartitionGroup(topicPartitionGroup)
                .writePosition(writePosition)
                .build();
        AppendEntriesResponseEncoder encoder = new AppendEntriesResponseEncoder();
        ByteBuf byteBuf = Unpooled.buffer(512);
        encoder.encode(response, byteBuf);

        AppendEntriesResponseDecoder decoder = new AppendEntriesResponseDecoder();
        JoyQueueHeader header = new JoyQueueHeader(response.type());
        AppendEntriesResponse decodeResponse = (AppendEntriesResponse)decoder.decode(header, byteBuf);

        //Assert.assertEquals(decodeResponse.getEntriesTerm(), entriesTerm);
        Assert.assertEquals(decodeResponse.getReplicaId(), replicaId);
        Assert.assertEquals(decodeResponse.getTerm(), term);
        Assert.assertEquals(decodeResponse.isSuccess(), success);
        Assert.assertEquals(decodeResponse.getWritePosition(), writePosition);
        Assert.assertEquals(decodeResponse.getNextPosition(), nextPosition);
        //Assert.assertEquals(decodeResponse.getTopicPartitionGroup().getTopic(), topicPartitionGroup.getTopic());
        //Assert.assertEquals(decodeResponse.getTopicPartitionGroup().getPartitionGroupId(), topicPartitionGroup.getPartitionGroupId());
    }

    @Test
    public void testReplicateConsumePosRequestCodec() throws Exception {
        Map<ConsumePartition, Position> consumePositions = new HashMap<>();
        consumePositions.put(new ConsumePartition("logbook18-HT", "logbookApi.lgbk18", Short.valueOf("0")), new Position(-1,-1,-1,-1));
        ReplicateConsumePosRequest request = new ReplicateConsumePosRequest(consumePositions);
        request.setHeader(new JoyQueueHeader());

        ReplicateConsumePosRequestEncoder encoder = new ReplicateConsumePosRequestEncoder();
        ByteBuf byteBuf = Unpooled.buffer(512);
        encoder.encode(request, byteBuf);

        ReplicateConsumePosRequestDecoder decoder = new ReplicateConsumePosRequestDecoder();
        JoyQueueHeader header = new JoyQueueHeader(request.type());
        ReplicateConsumePosRequest decodeRequest = (ReplicateConsumePosRequest)decoder.decode(header, byteBuf);

        Assert.assertEquals(decodeRequest.getConsumePositions().size(), consumePositions.size());
    }

    @Test
    public void testReplicateConsumePosResponseCodec() {
        final boolean success = true;
        ReplicateConsumePosResponse request = new ReplicateConsumePosResponse(success);
        request.setHeader(new JoyQueueHeader());

        ReplicateConsumePosResponseEncoder encoder = new ReplicateConsumePosResponseEncoder();
        ByteBuf byteBuf = Unpooled.buffer(512);
        encoder.encode(request, byteBuf);

        ReplicateConsumePosResponseDecoder decoder = new ReplicateConsumePosResponseDecoder();
        JoyQueueHeader header = new JoyQueueHeader(request.type());
        ReplicateConsumePosResponse decodeRequest = (ReplicateConsumePosResponse)decoder.decode(header, byteBuf);

        Assert.assertEquals(decodeRequest.isSuccess(), success);
    }

    @Test
    public void testTimeoutNowRequestCodec() throws Exception {
        final int term = 1;
        final TopicPartitionGroup topicPartitionGroup = new TopicPartitionGroup("test", 1);

        TimeoutNowRequest request = new TimeoutNowRequest(topicPartitionGroup, term);
        request.setHeader(new JoyQueueHeader());

        TimeoutNowRequestEncoder encoder = new TimeoutNowRequestEncoder();
        ByteBuf byteBuf = Unpooled.buffer(512);
        encoder.encode(request, byteBuf);

        TimeoutNowRequestDecoder decoder = new TimeoutNowRequestDecoder();
        JoyQueueHeader header = new JoyQueueHeader(request.type());
        TimeoutNowRequest decodeRequest = (TimeoutNowRequest)decoder.decode(header, byteBuf);

        Assert.assertEquals(decodeRequest.getTerm(), term);
        Assert.assertEquals(decodeRequest.getTopic(), topicPartitionGroup.getTopic());
        Assert.assertEquals(decodeRequest.getPartitionGroup(), topicPartitionGroup.getPartitionGroupId());
    }

    @Test
    public void testTimeoutResponseCodec() throws Exception {
        final boolean success = true;
        final int term = 1;
        TimeoutNowResponse response = new TimeoutNowResponse(success, term);

        TimeoutNowResponseEncoder encoder = new TimeoutNowResponseEncoder();
        ByteBuf byteBuf = Unpooled.buffer(512);
        encoder.encode(response, byteBuf);

        TimeoutNowResponseDecoder decoder = new TimeoutNowResponseDecoder();
        JoyQueueHeader header = new JoyQueueHeader(response.type());
        TimeoutNowResponse decodeResponse = (TimeoutNowResponse)decoder.decode(header, byteBuf);

        Assert.assertEquals(decodeResponse.getTerm(), term);
        Assert.assertEquals(decodeResponse.isSuccess(), success);
    }

    @Test
    public void testVoteRequestCodec() throws Exception {
        final TopicPartitionGroup topicPartitionGroup = new TopicPartitionGroup("test", 1);

        final int term = 10;
        final int candidateId = 101;
        final long lastLogPos = 2000;
        final int lastLogTerm = 9;
        final boolean preVote = true;

        VoteRequest request = new VoteRequest(topicPartitionGroup, term, candidateId, lastLogTerm, lastLogPos, preVote);
        request.setHeader(new JoyQueueHeader());

        VoteRequestEncoder encoder = new VoteRequestEncoder();
        ByteBuf byteBuf = Unpooled.buffer(512);
        encoder.encode(request, byteBuf);

        VoteRequestDecoder decoder = new VoteRequestDecoder();
        JoyQueueHeader header = new JoyQueueHeader(request.type());
        VoteRequest decodeRequest = (VoteRequest)decoder.decode(header, byteBuf);

        Assert.assertEquals(decodeRequest.getTopic(), topicPartitionGroup.getTopic());
        Assert.assertEquals(decodeRequest.getPartitionGroup(), topicPartitionGroup.getPartitionGroupId());
        Assert.assertEquals(decodeRequest.getTerm(), term);
        Assert.assertEquals(decodeRequest.getCandidateId(), candidateId);
        Assert.assertEquals(decodeRequest.getLastLogPos(), lastLogPos);
        Assert.assertEquals(decodeRequest.getLastLogTerm(), lastLogTerm);
        Assert.assertEquals(decodeRequest.isPreVote(), preVote);
    }

    @Test
    public void testVoteResponseCodec() {
        final int term = 1;
        final int candidateId = 101;
        final int voteNodeId = 102;
        final boolean voteGranted = true;

        VoteResponse response = new VoteResponse(term, candidateId, voteNodeId, voteGranted);

        VoteResponseEncoder encoder = new VoteResponseEncoder();
        ByteBuf byteBuf = Unpooled.buffer(512);
        encoder.encode(response, byteBuf);

        VoteResponseDecoder decoder = new VoteResponseDecoder();
        JoyQueueHeader header = new JoyQueueHeader(response.type());
        VoteResponse decodeResponse = (VoteResponse)decoder.decode(header, byteBuf);

        Assert.assertEquals(decodeResponse.getTerm(), term);
        Assert.assertEquals(decodeResponse.getCandidateId(), candidateId);
        Assert.assertEquals(decodeResponse.getVoteNodeId(), voteNodeId);
        Assert.assertEquals(decodeResponse.isVoteGranted(), voteGranted);
    }

}
