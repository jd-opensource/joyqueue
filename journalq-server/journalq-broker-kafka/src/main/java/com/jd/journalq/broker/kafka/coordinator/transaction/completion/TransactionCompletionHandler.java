package com.jd.journalq.broker.kafka.coordinator.transaction.completion;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jd.journalq.broker.kafka.config.KafkaConfig;
import com.jd.journalq.broker.kafka.coordinator.Coordinator;
import com.jd.journalq.broker.kafka.coordinator.transaction.TransactionMetadataManager;
import com.jd.journalq.broker.kafka.coordinator.transaction.log.TransactionLog;
import com.jd.journalq.broker.kafka.coordinator.transaction.log.TransactionLogSegment;
import com.jd.journalq.broker.kafka.coordinator.transaction.synchronizer.TransactionSynchronizer;
import com.jd.journalq.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * TransactionCompletionHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/19
 */
public class TransactionCompletionHandler extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(TransactionCompletionHandler.class);

    private KafkaConfig config;
    private Coordinator coordinator;
    private TransactionMetadataManager transactionMetadataManager;
    private TransactionLog transactionLog;
    private TransactionSynchronizer transactionSynchronizer;

    private Map<Short, TransactionSegmentCompletionHandler> handlerMap = Maps.newHashMap();

    public TransactionCompletionHandler(KafkaConfig config, Coordinator coordinator, TransactionMetadataManager transactionMetadataManager,
                                        TransactionLog transactionLog, TransactionSynchronizer transactionSynchronizer) {
        this.config = config;
        this.coordinator = coordinator;
        this.transactionMetadataManager = transactionMetadataManager;
        this.transactionLog = transactionLog;
        this.transactionSynchronizer = transactionSynchronizer;
    }

    public void handle() {
        try {
            Set<Short> partitions = Sets.newHashSet(transactionLog.getPartitions());

            Iterator<Map.Entry<Short, TransactionSegmentCompletionHandler>> handlerIterator = handlerMap.entrySet().iterator();
            while (handlerIterator.hasNext()) {
                Map.Entry<Short, TransactionSegmentCompletionHandler> entry = handlerIterator.next();
                if (!partitions.contains(entry.getKey())) {
                    handlerIterator.remove();
                    transactionLog.removeSegment(entry.getKey());
                }
            }

            for (Short partition : partitions) {
                if (handlerMap.containsKey(partition)) {
                    continue;
                }
                TransactionLogSegment transactionLogSegment = transactionLog.getSegment(partition);
                if (transactionLogSegment == null) {
                    continue;
                }
                handlerMap.put(partition, new TransactionSegmentCompletionHandler(config, coordinator, transactionMetadataManager, transactionLogSegment, transactionSynchronizer));
            }

            for (Map.Entry<Short, TransactionSegmentCompletionHandler> entry : handlerMap.entrySet()) {
                entry.getValue().handle();
            }
        } catch (Exception e) {
            logger.error("transaction compensate exception", e);
        }
    }
}