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
package org.joyqueue.client.internal.producer.converter;

import com.google.common.collect.Lists;
import org.joyqueue.client.internal.producer.domain.FeedbackData;
import org.joyqueue.client.internal.producer.domain.FetchFeedbackData;
import org.joyqueue.client.internal.producer.domain.ProduceMessage;
import org.joyqueue.client.internal.producer.domain.SendBatchResultData;
import org.joyqueue.client.internal.producer.domain.SendResult;
import org.joyqueue.domain.QosLevel;
import org.joyqueue.message.BrokerMessage;
import org.joyqueue.network.command.FetchProduceFeedbackResponse;
import org.joyqueue.network.command.FetchProduceFeedbackAckData;
import org.joyqueue.network.command.ProduceMessageAckData;
import org.joyqueue.network.command.ProduceMessageAckItemData;
import org.joyqueue.network.command.ProduceMessageData;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * MessageSenderConverter
 *
 * author: gaohaoxiang
 * date: 2018/12/28
 */
public class MessageSenderConverter {

    public static FetchFeedbackData convertToFetchFeedbackData(String topic, String app, FetchProduceFeedbackResponse fetchProduceFeedbackResponse) {
        FetchFeedbackData fetchFeedbackData = new FetchFeedbackData();
        if (CollectionUtils.isNotEmpty(fetchProduceFeedbackResponse.getData())) {
            List<FeedbackData> data = Lists.newArrayListWithCapacity(fetchProduceFeedbackResponse.getData().size());
            for (FetchProduceFeedbackAckData ackData : fetchProduceFeedbackResponse.getData()) {
                data.add(new FeedbackData(ackData.getTopic(), ackData.getTxId(), ackData.getTransactionId()));
            }
            fetchFeedbackData.setData(data);
        }
        fetchFeedbackData.setCode(fetchProduceFeedbackResponse.getCode());
        return fetchFeedbackData;
    }

    public static SendBatchResultData convertToBatchResultData(String topic, String app, ProduceMessageAckData produceMessageAckData) {
        SendBatchResultData sendBatchResultData = new SendBatchResultData();

        if (CollectionUtils.isNotEmpty(produceMessageAckData.getItem())) {
            List<SendResult> produceResultList = Lists.newArrayListWithCapacity(produceMessageAckData.getItem().size());
            for (ProduceMessageAckItemData produceMessageAckItemData : produceMessageAckData.getItem()) {
                SendResult produceResult = new SendResult(topic, produceMessageAckItemData.getPartition(), produceMessageAckItemData.getIndex(), produceMessageAckItemData.getStartTime());
                produceResultList.add(produceResult);
            }
            sendBatchResultData.setResult(produceResultList);
        }

        sendBatchResultData.setCode(produceMessageAckData.getCode());
        return sendBatchResultData;
    }

    public static ProduceMessageData convertToProduceMessageData(String topic, String app, String txId, List<ProduceMessage> messages, QosLevel qosLevel, long timeout,
                                                                 boolean compress, int compressThreshold, String compressType, boolean batch) {
        List<BrokerMessage> brokerMessages = ProduceMessageConverter.convertToBrokerMessages(topic, app, messages, compress, compressThreshold, compressType, batch);
        ProduceMessageData produceMessageData = new ProduceMessageData();
        produceMessageData.setQosLevel(qosLevel);
        produceMessageData.setMessages(brokerMessages);
        produceMessageData.setTxId(txId);
        produceMessageData.setTimeout((int) timeout);
        return produceMessageData;
    }
}