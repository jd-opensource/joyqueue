package org.joyqueue.broker.protocol.command;

import com.google.common.collect.Table;
import org.joyqueue.broker.network.traffic.FetchRequestTrafficPayload;
import org.joyqueue.broker.network.traffic.Traffic;
import org.joyqueue.network.command.FetchPartitionMessageData;

import java.util.Map;

/**
 * FetchPartitionMessageRequest
 * author: gaohaoxiang
 * date: 2020/4/7
 */
public class FetchPartitionMessageRequest extends org.joyqueue.network.command.FetchPartitionMessageRequest implements FetchRequestTrafficPayload {

    private Traffic traffic = new Traffic();

    @Override
    public Traffic getTraffic() {
        return traffic;
    }

    @Override
    public void setApp(String app) {
        super.setApp(app);
        traffic.setApp(app);
    }

    @Override
    public void setPartitions(Table<String, Short, FetchPartitionMessageData> partitions) {
        super.setPartitions(partitions);
        for (Map.Entry<String, Map<Short, FetchPartitionMessageData>> entry : partitions.rowMap().entrySet()) {
            traffic.record(entry.getKey(),1, 1);
        }
    }
}