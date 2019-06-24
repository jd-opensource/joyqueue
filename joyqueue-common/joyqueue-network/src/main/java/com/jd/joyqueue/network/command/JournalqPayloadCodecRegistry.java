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
package com.jd.joyqueue.network.command;

import com.jd.joyqueue.network.codec.AddConnectionResponseCodec;
import com.jd.joyqueue.network.codec.AddConnectionRequestCodec;
import com.jd.joyqueue.network.codec.AddConsumerResponseCodec;
import com.jd.joyqueue.network.codec.AddConsumerRequestCodec;
import com.jd.joyqueue.network.codec.AddProducerRequestCodec;
import com.jd.joyqueue.network.codec.AddProducerResponseCodec;
import com.jd.joyqueue.network.codec.AuthorizationCodec;
import com.jd.joyqueue.network.codec.BooleanAckCodec;
import com.jd.joyqueue.network.codec.CommitAckResponseCodec;
import com.jd.joyqueue.network.codec.CommitAckRequestCodec;
import com.jd.joyqueue.network.codec.FetchAssignedPartitionResponseCodec;
import com.jd.joyqueue.network.codec.FetchAssignedPartitionRequestCodec;
import com.jd.joyqueue.network.codec.FetchClusterResponseCodec;
import com.jd.joyqueue.network.codec.FetchClusterRequestCodec;
import com.jd.joyqueue.network.codec.FetchHealthResponseCodec;
import com.jd.joyqueue.network.codec.FetchHealthRequestCodec;
import com.jd.joyqueue.network.codec.FetchIndexResponseCodec;
import com.jd.joyqueue.network.codec.FetchIndexRequestCodec;
import com.jd.joyqueue.network.codec.FetchPartitionMessageRequestCodec;
import com.jd.joyqueue.network.codec.FetchPartitionMessageResponseCodec;
import com.jd.joyqueue.network.codec.FetchProduceFeedbackRequestCodec;
import com.jd.joyqueue.network.codec.FetchProduceFeedbackResponseCodec;
import com.jd.joyqueue.network.codec.FetchTopicMessageResponseCodec;
import com.jd.joyqueue.network.codec.FetchTopicMessageRequestCodec;
import com.jd.joyqueue.network.codec.FindCoordinatorResponseCodec;
import com.jd.joyqueue.network.codec.FindCoordinatorRequestCodec;
import com.jd.joyqueue.network.codec.GetTopicsAckCodec;
import com.jd.joyqueue.network.codec.GetTopicsCodec;
import com.jd.joyqueue.network.codec.HeartbeatRequestCodec;
import com.jd.joyqueue.network.codec.ProduceMessageCommitRequestCodec;
import com.jd.joyqueue.network.codec.ProduceMessagePrepareRequestCodec;
import com.jd.joyqueue.network.codec.ProduceMessageRequestCodec;
import com.jd.joyqueue.network.codec.ProduceMessageResponseCodec;
import com.jd.joyqueue.network.codec.ProduceMessageCommitResponseCodec;
import com.jd.joyqueue.network.codec.ProduceMessagePrepareResponseCodec;
import com.jd.joyqueue.network.codec.ProduceMessageRollbackRequestCodec;
import com.jd.joyqueue.network.codec.ProduceMessageRollbackResponseCodec;
import com.jd.joyqueue.network.codec.RemoveConnectionRequestCodec;
import com.jd.joyqueue.network.codec.RemoveConsumerRequestCodec;
import com.jd.joyqueue.network.codec.RemoveProducerRequestCodec;
import com.jd.joyqueue.network.codec.SubscribeAckCodec;
import com.jd.joyqueue.network.codec.SubscribeCodec;
import com.jd.joyqueue.network.codec.UnSubscribeCodec;
import com.jd.joyqueue.network.transport.codec.PayloadCodecFactory;

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