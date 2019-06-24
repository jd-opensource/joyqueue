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
package com.jd.journalq.network.command;

import com.jd.journalq.network.codec.AddConnectionResponseCodec;
import com.jd.journalq.network.codec.AddConnectionRequestCodec;
import com.jd.journalq.network.codec.AddConsumerResponseCodec;
import com.jd.journalq.network.codec.AddConsumerRequestCodec;
import com.jd.journalq.network.codec.AddProducerRequestCodec;
import com.jd.journalq.network.codec.AddProducerResponseCodec;
import com.jd.journalq.network.codec.AuthorizationCodec;
import com.jd.journalq.network.codec.BooleanAckCodec;
import com.jd.journalq.network.codec.CommitAckResponseCodec;
import com.jd.journalq.network.codec.CommitAckRequestCodec;
import com.jd.journalq.network.codec.FetchAssignedPartitionResponseCodec;
import com.jd.journalq.network.codec.FetchAssignedPartitionRequestCodec;
import com.jd.journalq.network.codec.FetchClusterResponseCodec;
import com.jd.journalq.network.codec.FetchClusterRequestCodec;
import com.jd.journalq.network.codec.FetchHealthResponseCodec;
import com.jd.journalq.network.codec.FetchHealthRequestCodec;
import com.jd.journalq.network.codec.FetchIndexResponseCodec;
import com.jd.journalq.network.codec.FetchIndexRequestCodec;
import com.jd.journalq.network.codec.FetchPartitionMessageRequestCodec;
import com.jd.journalq.network.codec.FetchPartitionMessageResponseCodec;
import com.jd.journalq.network.codec.FetchProduceFeedbackRequestCodec;
import com.jd.journalq.network.codec.FetchProduceFeedbackResponseCodec;
import com.jd.journalq.network.codec.FetchTopicMessageResponseCodec;
import com.jd.journalq.network.codec.FetchTopicMessageRequestCodec;
import com.jd.journalq.network.codec.FindCoordinatorResponseCodec;
import com.jd.journalq.network.codec.FindCoordinatorRequestCodec;
import com.jd.journalq.network.codec.GetTopicsAckCodec;
import com.jd.journalq.network.codec.GetTopicsCodec;
import com.jd.journalq.network.codec.HeartbeatRequestCodec;
import com.jd.journalq.network.codec.ProduceMessageCommitRequestCodec;
import com.jd.journalq.network.codec.ProduceMessagePrepareRequestCodec;
import com.jd.journalq.network.codec.ProduceMessageRequestCodec;
import com.jd.journalq.network.codec.ProduceMessageResponseCodec;
import com.jd.journalq.network.codec.ProduceMessageCommitResponseCodec;
import com.jd.journalq.network.codec.ProduceMessagePrepareResponseCodec;
import com.jd.journalq.network.codec.ProduceMessageRollbackRequestCodec;
import com.jd.journalq.network.codec.ProduceMessageRollbackResponseCodec;
import com.jd.journalq.network.codec.RemoveConnectionRequestCodec;
import com.jd.journalq.network.codec.RemoveConsumerRequestCodec;
import com.jd.journalq.network.codec.RemoveProducerRequestCodec;
import com.jd.journalq.network.codec.SubscribeAckCodec;
import com.jd.journalq.network.codec.SubscribeCodec;
import com.jd.journalq.network.codec.UnSubscribeCodec;
import com.jd.journalq.network.transport.codec.PayloadCodecFactory;

/**
 * JournalqPayloadCodecRegistry
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/28
 */
public class JournalqPayloadCodecRegistry {

    public static void register(PayloadCodecFactory payloadCodecFactory) {
        payloadCodecFactory.register(new BooleanAckCodec());

        // 连接相关
        payloadCodecFactory.register(new AddConnectionRequestCodec());
        payloadCodecFactory.register(new AddConnectionResponseCodec());
        payloadCodecFactory.register(new RemoveConnectionRequestCodec());
        payloadCodecFactory.register(new AddConsumerRequestCodec());
        payloadCodecFactory.register(new AddConsumerResponseCodec());
        payloadCodecFactory.register(new RemoveConsumerRequestCodec());
        payloadCodecFactory.register(new AddProducerRequestCodec());
        payloadCodecFactory.register(new AddProducerResponseCodec());
        payloadCodecFactory.register(new RemoveProducerRequestCodec());
        payloadCodecFactory.register(new HeartbeatRequestCodec());
        payloadCodecFactory.register(new FetchHealthRequestCodec());
        payloadCodecFactory.register(new FetchHealthResponseCodec());

        // 集群相关
        payloadCodecFactory.register(new FetchClusterRequestCodec());
        payloadCodecFactory.register(new FetchClusterResponseCodec());

        // 协调者相关
        payloadCodecFactory.register(new FindCoordinatorRequestCodec());
        payloadCodecFactory.register(new FindCoordinatorResponseCodec());
        payloadCodecFactory.register(new FetchAssignedPartitionRequestCodec());
        payloadCodecFactory.register(new FetchAssignedPartitionResponseCodec());

        // 消费相关
        payloadCodecFactory.register(new FetchTopicMessageRequestCodec());
        payloadCodecFactory.register(new FetchTopicMessageResponseCodec());
        payloadCodecFactory.register(new FetchPartitionMessageRequestCodec());
        payloadCodecFactory.register(new FetchPartitionMessageResponseCodec());
        payloadCodecFactory.register(new CommitAckRequestCodec());
        payloadCodecFactory.register(new CommitAckResponseCodec());
        payloadCodecFactory.register(new FetchIndexRequestCodec());
        payloadCodecFactory.register(new FetchIndexResponseCodec());

        // 生产相关
        payloadCodecFactory.register(new ProduceMessageRequestCodec());
        payloadCodecFactory.register(new ProduceMessageResponseCodec());
        payloadCodecFactory.register(new ProduceMessagePrepareRequestCodec());
        payloadCodecFactory.register(new ProduceMessagePrepareResponseCodec());
        payloadCodecFactory.register(new ProduceMessageCommitRequestCodec());
        payloadCodecFactory.register(new ProduceMessageCommitResponseCodec());
        payloadCodecFactory.register(new ProduceMessageRollbackRequestCodec());
        payloadCodecFactory.register(new ProduceMessageRollbackResponseCodec());
        payloadCodecFactory.register(new FetchProduceFeedbackRequestCodec());
        payloadCodecFactory.register(new FetchProduceFeedbackResponseCodec());

        // mqtt
        payloadCodecFactory.register(JournalqCommandType.MQTT_SUBSCRIBE.getCode(), new SubscribeCodec());
        payloadCodecFactory.register(JournalqCommandType.MQTT_SUBSCRIBE_ACK.getCode(), new SubscribeAckCodec());
        payloadCodecFactory.register(JournalqCommandType.MQTT_UNSUBSCRIBE.getCode(), new UnSubscribeCodec());
        payloadCodecFactory.register(JournalqCommandType.MQTT_GET_TOPICS.getCode(), new GetTopicsCodec());
        payloadCodecFactory.register(JournalqCommandType.MQTT_GET_TOPICS_ACK.getCode(), new GetTopicsAckCodec());
        payloadCodecFactory.register(JournalqCommandType.MQTT_AUTHORIZATION.getCode(), new AuthorizationCodec());
    }
}