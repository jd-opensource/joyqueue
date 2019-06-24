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
package com.jd.joyqueue.broker.index.handler;

import com.jd.joyqueue.broker.BrokerContext;
import com.jd.joyqueue.broker.consumer.Consume;
import com.jd.joyqueue.broker.index.command.ConsumeIndexStoreRequest;
import com.jd.joyqueue.broker.index.command.ConsumeIndexStoreResponse;
import com.jd.joyqueue.broker.index.model.IndexAndMetadata;
import com.jd.joyqueue.domain.QosLevel;
import com.jd.joyqueue.domain.TopicName;
import com.jd.joyqueue.exception.JournalqCode;
import com.jd.joyqueue.exception.JournalqException;
import com.jd.joyqueue.network.command.CommandType;
import com.jd.joyqueue.network.session.Consumer;
import com.jd.joyqueue.network.transport.Transport;
import com.jd.joyqueue.network.transport.codec.JournalqHeader;
import com.jd.joyqueue.network.transport.command.Command;
import com.jd.joyqueue.network.transport.command.Direction;
import com.jd.joyqueue.network.transport.command.Type;
import com.jd.joyqueue.network.transport.command.handler.CommandHandler;
import com.jd.joyqueue.network.transport.exception.TransportException;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by zhuduohui on 2018/9/7.
 */
public class ConsumeIndexStoreHandler implements CommandHandler, Type {
    private final Logger logger = LoggerFactory.getLogger(ConsumeIndexStoreHandler.class);

    private BrokerContext brokerContext;
    private Consume consume;

    public ConsumeIndexStoreHandler(BrokerContext brokerContext) {
        this.brokerContext = brokerContext;
        this.consume = brokerContext.getConsume();
    }

    @Override
    public Command handle(Transport transport, Command command) throws TransportException {
        ConsumeIndexStoreRequest request = (ConsumeIndexStoreRequest) command.getPayload();
        if (request == null) return null;

        // offset meta data to store
        // group by topic -> partition -> offset metadata
        Map<String, Map<Integer, IndexAndMetadata>> indexMetadata = request.getIndexMetadata();

        logger.debug("ConsumeIndexStoreRequest info:[{}]", indexMetadata.toString());

        // offset meta data store status
        // group by topic -> partition -> return code
        Map<String, Map<Integer, Short>> indexStoreStatus = new HashedMap();

        String app = request.getApp();
        for (String topic : indexMetadata.keySet()) {
            // offset meta data of partition
            // partition -> offset meta data
            Map<Integer, IndexAndMetadata> partitionIndexes = indexMetadata.get(topic);
            Map<Integer, Short> partitionIndexStoreStatus = new HashedMap();

            for (int partition : partitionIndexes.keySet()) {
                // set consume index
                int retCode = JournalqCode.SUCCESS.getCode();
                IndexAndMetadata indexAndMetadata = partitionIndexes.get(partition);
                try {
                    setConsumeIndex(topic, (short) partition, app, indexAndMetadata.getIndex(), indexAndMetadata.getIndexCommitTime());
                } catch (JournalqException je) {
                    retCode = je.getCode();
                }
                partitionIndexStoreStatus.put(partition, (short)retCode);
            }
            indexStoreStatus.put(topic, partitionIndexStoreStatus);
        }

        JournalqHeader header = new JournalqHeader(Direction.RESPONSE, QosLevel.ONE_WAY, CommandType.CONSUME_INDEX_STORE_RESPONSE);
        ConsumeIndexStoreResponse offsetStoreResponse = new ConsumeIndexStoreResponse(indexStoreStatus);
        return new Command(header, offsetStoreResponse);
    }

    private void setConsumeIndex(String topic, short partition, String app, long offset, long commitTime) throws JournalqException {
        Consumer consumer = new Consumer(topic, app);
        if (consume.getLastAckTimeByPartition(TopicName.parse(topic), app, partition) > commitTime) {
            return;
        }
        consume.setAckIndex(consumer, partition, offset);
        consume.setStartAckIndex(consumer, partition, -1);
    }


    @Override
    public int type() {
        return CommandType.CONSUME_INDEX_STORE_REQUEST;
    }
}
