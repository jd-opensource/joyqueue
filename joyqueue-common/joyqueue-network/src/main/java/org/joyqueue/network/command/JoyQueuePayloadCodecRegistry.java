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
package org.joyqueue.network.command;

import org.joyqueue.network.codec.AddConnectionRequestCodec;
import org.joyqueue.network.codec.AddConnectionResponseCodec;
import org.joyqueue.network.codec.AddConsumerRequestCodec;
import org.joyqueue.network.codec.AddConsumerResponseCodec;
import org.joyqueue.network.codec.AddProducerRequestCodec;
import org.joyqueue.network.codec.AddProducerResponseCodec;
import org.joyqueue.network.codec.AuthorizationCodec;
import org.joyqueue.network.codec.BooleanAckCodec;
import org.joyqueue.network.codec.CommitAckRequestCodec;
import org.joyqueue.network.codec.CommitAckResponseCodec;
import org.joyqueue.network.codec.CommitIndexRequestCodec;
import org.joyqueue.network.codec.CommitIndexResponseCodec;
import org.joyqueue.network.codec.FetchAssignedPartitionRequestCodec;
import org.joyqueue.network.codec.FetchAssignedPartitionResponseCodec;
import org.joyqueue.network.codec.FetchClusterRequestCodec;
import org.joyqueue.network.codec.FetchClusterResponseCodec;
import org.joyqueue.network.codec.FetchHealthRequestCodec;
import org.joyqueue.network.codec.FetchHealthResponseCodec;
import org.joyqueue.network.codec.FetchIndexRequestCodec;
import org.joyqueue.network.codec.FetchIndexResponseCodec;
import org.joyqueue.network.codec.FetchPartitionMessageRequestCodec;
import org.joyqueue.network.codec.FetchPartitionMessageResponseCodec;
import org.joyqueue.network.codec.FetchProduceFeedbackRequestCodec;
import org.joyqueue.network.codec.FetchProduceFeedbackResponseCodec;
import org.joyqueue.network.codec.FetchTopicMessageRequestCodec;
import org.joyqueue.network.codec.FetchTopicMessageResponseCodec;
import org.joyqueue.network.codec.FindCoordinatorRequestCodec;
import org.joyqueue.network.codec.FindCoordinatorResponseCodec;
import org.joyqueue.network.codec.GetTopicsAckCodec;
import org.joyqueue.network.codec.GetTopicsCodec;
import org.joyqueue.network.codec.HeartbeatRequestCodec;
import org.joyqueue.network.codec.ProduceMessageCommitRequestCodec;
import org.joyqueue.network.codec.ProduceMessageCommitResponseCodec;
import org.joyqueue.network.codec.ProduceMessagePrepareRequestCodec;
import org.joyqueue.network.codec.ProduceMessagePrepareResponseCodec;
import org.joyqueue.network.codec.ProduceMessageRequestCodec;
import org.joyqueue.network.codec.ProduceMessageResponseCodec;
import org.joyqueue.network.codec.ProduceMessageRollbackRequestCodec;
import org.joyqueue.network.codec.ProduceMessageRollbackResponseCodec;
import org.joyqueue.network.codec.RemoveConnectionRequestCodec;
import org.joyqueue.network.codec.RemoveConsumerRequestCodec;
import org.joyqueue.network.codec.RemoveProducerRequestCodec;
import org.joyqueue.network.codec.SubscribeAckCodec;
import org.joyqueue.network.codec.SubscribeCodec;
import org.joyqueue.network.codec.UnSubscribeCodec;
import org.joyqueue.network.transport.codec.PayloadCodecFactory;

/**
 * JoyQueuePayloadCodecRegistry
 *
 * author: gaohaoxiang
 * date: 2018/11/28
 */
public class JoyQueuePayloadCodecRegistry {

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
        payloadCodecFactory.register(new CommitIndexRequestCodec());
        payloadCodecFactory.register(new CommitIndexResponseCodec());
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
        payloadCodecFactory.register(JoyQueueCommandType.MQTT_SUBSCRIBE.getCode(), new SubscribeCodec());
        payloadCodecFactory.register(JoyQueueCommandType.MQTT_SUBSCRIBE_ACK.getCode(), new SubscribeAckCodec());
        payloadCodecFactory.register(JoyQueueCommandType.MQTT_UNSUBSCRIBE.getCode(), new UnSubscribeCodec());
        payloadCodecFactory.register(JoyQueueCommandType.MQTT_GET_TOPICS.getCode(), new GetTopicsCodec());
        payloadCodecFactory.register(JoyQueueCommandType.MQTT_GET_TOPICS_ACK.getCode(), new GetTopicsAckCodec());
        payloadCodecFactory.register(JoyQueueCommandType.MQTT_AUTHORIZATION.getCode(), new AuthorizationCodec());
    }
}