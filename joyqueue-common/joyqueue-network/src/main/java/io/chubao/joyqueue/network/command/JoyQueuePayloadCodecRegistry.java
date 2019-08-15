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
package io.chubao.joyqueue.network.command;

import io.chubao.joyqueue.network.codec.AddConnectionResponseCodec;
import io.chubao.joyqueue.network.codec.AddConnectionRequestCodec;
import io.chubao.joyqueue.network.codec.AddConsumerResponseCodec;
import io.chubao.joyqueue.network.codec.AddConsumerRequestCodec;
import io.chubao.joyqueue.network.codec.AddProducerRequestCodec;
import io.chubao.joyqueue.network.codec.AddProducerResponseCodec;
import io.chubao.joyqueue.network.codec.AuthorizationCodec;
import io.chubao.joyqueue.network.codec.BooleanAckCodec;
import io.chubao.joyqueue.network.codec.CommitAckResponseCodec;
import io.chubao.joyqueue.network.codec.CommitAckRequestCodec;
import io.chubao.joyqueue.network.codec.FetchAssignedPartitionResponseCodec;
import io.chubao.joyqueue.network.codec.FetchAssignedPartitionRequestCodec;
import io.chubao.joyqueue.network.codec.FetchClusterResponseCodec;
import io.chubao.joyqueue.network.codec.FetchClusterRequestCodec;
import io.chubao.joyqueue.network.codec.FetchHealthResponseCodec;
import io.chubao.joyqueue.network.codec.FetchHealthRequestCodec;
import io.chubao.joyqueue.network.codec.FetchIndexResponseCodec;
import io.chubao.joyqueue.network.codec.FetchIndexRequestCodec;
import io.chubao.joyqueue.network.codec.FetchPartitionMessageRequestCodec;
import io.chubao.joyqueue.network.codec.FetchPartitionMessageResponseCodec;
import io.chubao.joyqueue.network.codec.FetchProduceFeedbackRequestCodec;
import io.chubao.joyqueue.network.codec.FetchProduceFeedbackResponseCodec;
import io.chubao.joyqueue.network.codec.FetchTopicMessageResponseCodec;
import io.chubao.joyqueue.network.codec.FetchTopicMessageRequestCodec;
import io.chubao.joyqueue.network.codec.FindCoordinatorResponseCodec;
import io.chubao.joyqueue.network.codec.FindCoordinatorRequestCodec;
import io.chubao.joyqueue.network.codec.GetTopicsAckCodec;
import io.chubao.joyqueue.network.codec.GetTopicsCodec;
import io.chubao.joyqueue.network.codec.HeartbeatRequestCodec;
import io.chubao.joyqueue.network.codec.ProduceMessageCommitRequestCodec;
import io.chubao.joyqueue.network.codec.ProduceMessagePrepareRequestCodec;
import io.chubao.joyqueue.network.codec.ProduceMessageRequestCodec;
import io.chubao.joyqueue.network.codec.ProduceMessageResponseCodec;
import io.chubao.joyqueue.network.codec.ProduceMessageCommitResponseCodec;
import io.chubao.joyqueue.network.codec.ProduceMessagePrepareResponseCodec;
import io.chubao.joyqueue.network.codec.ProduceMessageRollbackRequestCodec;
import io.chubao.joyqueue.network.codec.ProduceMessageRollbackResponseCodec;
import io.chubao.joyqueue.network.codec.RemoveConnectionRequestCodec;
import io.chubao.joyqueue.network.codec.RemoveConsumerRequestCodec;
import io.chubao.joyqueue.network.codec.RemoveProducerRequestCodec;
import io.chubao.joyqueue.network.codec.SubscribeAckCodec;
import io.chubao.joyqueue.network.codec.SubscribeCodec;
import io.chubao.joyqueue.network.codec.UnSubscribeCodec;
import io.chubao.joyqueue.network.transport.codec.PayloadCodecFactory;

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