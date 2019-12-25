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

import org.joyqueue.domain.TopicName;
import org.joyqueue.store.StoreService;
import org.joyqueue.store.replication.ReplicableStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class ConsumeTask extends Thread {
    private static Logger logger = LoggerFactory.getLogger(ProduceTask.class);

    private StoreService storeService;
    private boolean stop = false;
    private TopicName topicName;
    private int partitionGroup;

    private long position = 0;

    private int maxMessageLength = 1024 * 1024;

    public ConsumeTask(StoreService storeService, TopicName topicName, int partitionGroup) {
        this.storeService = storeService;
        this.topicName = topicName;
        this.partitionGroup = partitionGroup;
    }

    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }

    public void stop(boolean b) {
        this.stop = true;
    }

    @Override
    public void run() {
        while(true) {
            try {
                ByteBuffer messages = consumeMessage(topicName.getFullName(), position, maxMessageLength);
                if (messages == null) continue;

                position += messages.remaining();
                //logger.info("Consume {} messages from {}", messages.remaining(), leaderId);

                Thread.sleep(100);

            } catch (Exception e) {
                logger.info("Consume message fail", e);

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {}
            }

            if (stop) break;
        }
    }


    private ByteBuffer consumeMessage(String topic, long position, int maxMessageLength) throws Exception {
        ReplicableStore replicableStore = storeService.getReplicableStore(topic, partitionGroup);
        if (position >= replicableStore.rightPosition()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}
            if (position >= replicableStore.rightPosition()) {
                return null;
            }
        }
        return replicableStore.readEntryBuffer(position, maxMessageLength);
    }
}
