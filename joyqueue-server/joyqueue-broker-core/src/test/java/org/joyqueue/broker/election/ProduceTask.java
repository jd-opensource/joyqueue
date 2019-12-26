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

import org.joyqueue.broker.buffer.Serializer;
import org.joyqueue.domain.QosLevel;
import org.joyqueue.domain.TopicName;
import org.joyqueue.message.BrokerMessage;
import org.joyqueue.message.SourceType;
import org.joyqueue.store.PartitionGroupStore;
import org.joyqueue.store.StoreService;
import org.joyqueue.store.WriteRequest;
import org.joyqueue.store.WriteResult;
import org.joyqueue.store.message.MessageParser;
import org.joyqueue.store.replication.ReplicableStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;

public class ProduceTask extends Thread {
    private static Logger logger = LoggerFactory.getLogger(ProduceTask.class);

    private StoreService storeService;
    private boolean stop = false;
    private TopicName topicName;
    private int partitionGroup;

    private int PARTITION_NUM = 5;
    private int currentPartition;

    public ProduceTask(StoreService storeService, TopicName topicName, int partitionGroup) {
        this.storeService = storeService;
        this.topicName = topicName;
        this.partitionGroup = partitionGroup;
    }

    private void produceMessage(String topic, int partitionGroup, int messageCount) throws Exception {

        PartitionGroupStore pgStore = storeService.getStore(topic, partitionGroup, QosLevel.RECEIVE);
        ReplicableStore replicableStores = storeService.getReplicableStore(topic, partitionGroup);
        if (!replicableStores.serviceStatus()) {
            logger.info("Produce message, store status is {}", replicableStores.serviceStatus());
            System.out.println("Produce message, store status is " + replicableStores.serviceStatus());
            Thread.sleep(1000);
            return;
        }

        for (int i = 0; i < messageCount; i++) {
            BrokerMessage brokerMessage = new BrokerMessage();
            brokerMessage.setSource(SourceType.JOYQUEUE.getValue());
            brokerMessage.setClientIp("10.1.1.1".getBytes());
            brokerMessage.setBody(("Test_abcdefghigklmnopqrstuvwxyz" + i).getBytes());

            int msgSize = Serializer.sizeOf(brokerMessage);
            ByteBuffer buf = ByteBuffer.allocate(msgSize);
            Serializer.serialize(brokerMessage, buf, msgSize);


            CRC32 crc32 = new CRC32();
            crc32.update(MessageParser.getByteBuffer(buf,MessageParser.BODY));
            MessageParser.setLong(buf,MessageParser.CRC,crc32.getValue());

            //logger.info("Topic {}, will write message to {}", topic, leaderId);
            int partition = currentPartition >= PARTITION_NUM - 1 ? 0 : currentPartition++;
            Future<WriteResult> writeResultFuture = pgStore.asyncWrite(new WriteRequest((short) partition, buf));
            WriteResult writeResult = writeResultFuture.get(100, TimeUnit.MILLISECONDS);
            if (writeResult != null) {
                //logger.info("Topic {}, Write message to {} return code {}, indexies = {}",
                //        topic, leaderId, writeResult.getCode(), writeResult.getIndices());
            }
        }
    }

    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }

    public void stop(boolean b) {
        this.stop = true;
        while (isAlive()) {
            interrupt();
        }
    }

    @Override
    public void run() {
        while(true) {
            try {

                produceMessage(topicName.getFullName(), partitionGroup, 10);

                Thread.sleep(10);
            } catch (Exception e) {
                logger.info("Produce message to {} fail", storeService, e);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
            }
            if (stop) break;
        }
    }
}
