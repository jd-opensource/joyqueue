package io.chubao.joyqueue.client.internal.consumer.converter;

import com.google.common.base.Charsets;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import io.chubao.joyqueue.client.internal.consumer.domain.ConsumeMessage;
import io.chubao.joyqueue.client.internal.consumer.domain.FetchMessageData;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.message.BrokerMessage;
import io.chubao.joyqueue.message.SourceType;
import io.chubao.joyqueue.network.command.FetchPartitionMessageAckData;
import io.chubao.joyqueue.network.command.FetchTopicMessageAckData;
import io.chubao.joyqueue.network.serializer.BatchMessageSerializer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * BrokerMessageConverter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/7
 */
public class BrokerMessageConverter {

    private static MessageConvertSupport messageConvertSupport = new MessageConvertSupport();

    public static Table<String, Short, FetchMessageData> convert(String app, Table<String, Short, FetchPartitionMessageAckData> topicMessageTable) {
        Table<String, Short, FetchMessageData> result = HashBasedTable.create();
        if (topicMessageTable == null || topicMessageTable.isEmpty()) {
            return result;
        }
        for (Map.Entry<String, Map<Short, FetchPartitionMessageAckData>> topicEntry : topicMessageTable.rowMap().entrySet()) {
            String topic = topicEntry.getKey();
            Map<Short, FetchPartitionMessageAckData> partitionMap = topicEntry.getValue();
            for (Map.Entry<Short, FetchPartitionMessageAckData> partitionEntry : partitionMap.entrySet()) {
                result.put(topic, partitionEntry.getKey(),
                        new FetchMessageData(convert(topic, app, partitionEntry.getValue().getMessages()), partitionEntry.getValue().getCode()));
            }
        }
        return result;
    }

    public static Map<String, FetchMessageData> convert(String app, Map<String, FetchTopicMessageAckData> topicMessageMap) {
        if (MapUtils.isEmpty(topicMessageMap)) {
            return Collections.emptyMap();
        }
        Map<String, FetchMessageData> result = Maps.newHashMap();
        for (Map.Entry<String, FetchTopicMessageAckData> entry : topicMessageMap.entrySet()) {
            String topic = entry.getKey();
            FetchTopicMessageAckData fetchTopicMessageAckData = entry.getValue();
            FetchMessageData fetchMessageData = new FetchMessageData(convert(topic, app, fetchTopicMessageAckData.getMessages()), fetchTopicMessageAckData.getCode());
            result.put(topic, fetchMessageData);
        }
        return result;
    }

    public static List<ConsumeMessage> convert(String topic, String app, List<BrokerMessage> brokerMessages) {
        if (CollectionUtils.isEmpty(brokerMessages)) {
            return Collections.emptyList();
        }
        List<ConsumeMessage> result = Lists.newLinkedList();
        for (BrokerMessage brokerMessage : brokerMessages) {
            if (brokerMessage.isBatch()) {
                List<BrokerMessage> convertedBrokerMessages = convertBatch(topic, app, brokerMessage);
                if (convertedBrokerMessages != null) {
                    for (BrokerMessage convertedBrokerMessage : convertedBrokerMessages) {
                        result.add(convert(topic, app, convertedBrokerMessage));
                    }
                }
            } else {
                BrokerMessage convertedMessage = messageConvertSupport.convert(brokerMessage);
                if (convertedMessage == null) {
                    convertedMessage = brokerMessage;
                }
                result.add(convert(topic, app, convertedMessage));
            }
        }
        return result;
    }

    public static List<BrokerMessage> convertBatch(String topic, String app, BrokerMessage batchBrokerMessage) {
        if (batchBrokerMessage.getSource() != SourceType.JOYQUEUE.getValue()) {
            return messageConvertSupport.convertBatch(batchBrokerMessage);
        }
        byte[] body = batchBrokerMessage.getDecompressedBody();
        batchBrokerMessage.setBody(body);
        return BatchMessageSerializer.deserialize(batchBrokerMessage);
    }

    public static ConsumeMessage convert(String topic, String app, BrokerMessage brokerMessage) {
        byte[] body = brokerMessage.getDecompressedBody();
        ConsumeMessage consumeMessage = new ConsumeMessage(TopicName.parse(topic), app, brokerMessage.getPartition(), brokerMessage.getMsgIndexNo(),
                brokerMessage.getTxId(), brokerMessage.getBusinessId(), new String(body, Charsets.UTF_8), body, brokerMessage.getFlag(), brokerMessage.getPriority(),
                brokerMessage.getStartTime(), brokerMessage.getSource(), brokerMessage.getAttributes());
        return consumeMessage;
    }
}