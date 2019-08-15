package io.chubao.joyqueue.broker.election;

import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.store.StoreService;
import io.chubao.joyqueue.store.replication.ReplicableStore;
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
