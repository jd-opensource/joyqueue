package com.jd.journalq.broker.kafka.handler;


import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.BrokerContextAware;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.KafkaErrorCode;
import com.jd.journalq.broker.kafka.command.ListOffsetsRequest;
import com.jd.journalq.broker.kafka.command.ListOffsetsResponse;
import com.jd.journalq.broker.kafka.model.PartitionOffsetsResponse;
import com.jd.journalq.domain.PartitionGroup;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.store.PartitionGroupStore;
import com.jd.journalq.store.StoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * ListOffsetsHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/5
 */
public class ListOffsetsHandler extends AbstractKafkaCommandHandler implements BrokerContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(ListOffsetsHandler.class);

    private static final long EARLIEST_TIMESTAMP = -2L;
    private static final long LATEST_TIMESTAMP = -1L;

    private ClusterManager clusterManager;
    private StoreService storeService;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.clusterManager = brokerContext.getClusterManager();
        this.storeService = brokerContext.getStoreService();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        ListOffsetsRequest request = (ListOffsetsRequest) command.getPayload();
        Table<TopicName, Integer, ListOffsetsRequest.PartitionOffsetRequestInfo> offsetRequestTable = request.getOffsetRequestTable();
        Table<String, Integer, PartitionOffsetsResponse> offsetResponseTable = HashBasedTable.create();

        for (TopicName topic : offsetRequestTable.rowKeySet()) {
            Map<Integer, ListOffsetsRequest.PartitionOffsetRequestInfo> partitionOffsetRequestMap = offsetRequestTable.row(topic);
            for (int partition : partitionOffsetRequestMap.keySet()) {
                ListOffsetsRequest.PartitionOffsetRequestInfo partitionOffsetRequestInfo = partitionOffsetRequestMap.get(partition);
                PartitionOffsetsResponse partitionOffsetsResponse = null;

                try {
                    partitionOffsetsResponse = getOffsetByTimestamp(topic, partition, partitionOffsetRequestInfo.getTime());
                } catch (Exception e) {
                    logger.error("list offset exception, topic: {}, partition: {}", topic, partition, e);
                    short errorCode = KafkaErrorCode.exceptionFor(e);
                    partitionOffsetsResponse = new PartitionOffsetsResponse(errorCode, partitionOffsetRequestInfo.getTime(), -1);
                }

                offsetResponseTable.put(topic.getFullName(), partition, partitionOffsetsResponse);
            }
        }

        // TODO 临时日志
        logger.info("list offset, request: {}, response: {}", offsetRequestTable, offsetResponseTable);
        ListOffsetsResponse response = new ListOffsetsResponse(offsetResponseTable);
        return new Command(response);
    }

    private PartitionOffsetsResponse getOffsetByTimestamp(TopicName topic, int partition, long timestamp) throws Exception {
        PartitionGroup partitionGroup = clusterManager.getPartitionGroup(topic, (short) partition);
        if (partitionGroup == null) {
            logger.error("list offset error, partitionGroup not exist, topic: {}, partition: {}", topic, partition);
            return new PartitionOffsetsResponse(KafkaErrorCode.NOT_LEADER_FOR_PARTITION.getCode(), timestamp, -1);
        }

        long offset = 0L;
        PartitionGroupStore partitionGroupStore = storeService.getStore(topic.getFullName(), partitionGroup.getGroup());

        if (timestamp == LATEST_TIMESTAMP) {
            // 得到partition的最大位置
            offset = partitionGroupStore.getRightIndex((short) partition);
        } else if (timestamp == EARLIEST_TIMESTAMP) {
            offset = partitionGroupStore.getLeftIndex((short) partition);
        } else {
            offset = partitionGroupStore.getIndex((short) partition, timestamp);
        }

        return new PartitionOffsetsResponse(KafkaErrorCode.NONE.getCode(), timestamp, offset);
    }

    @Override
    public int type() {
        return KafkaCommandType.LIST_OFFSETS.getCode();
    }
}

