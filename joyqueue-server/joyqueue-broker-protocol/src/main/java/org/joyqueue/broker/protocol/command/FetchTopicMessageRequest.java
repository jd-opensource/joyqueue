package org.joyqueue.broker.protocol.command;

import org.joyqueue.broker.network.traffic.FetchRequestTrafficPayload;
import org.joyqueue.broker.network.traffic.Traffic;
import org.joyqueue.network.command.FetchTopicMessageData;

import java.util.Map;

/**
 * FetchTopicMessageRequest
 * author: gaohaoxiang
 * date: 2020/4/7
 */
public class FetchTopicMessageRequest extends org.joyqueue.network.command.FetchTopicMessageRequest implements FetchRequestTrafficPayload {

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
    public void setTopics(Map<String, FetchTopicMessageData> topics) {
        super.setTopics(topics);
        for (Map.Entry<String, FetchTopicMessageData> entry : topics.entrySet()) {
            traffic.record(entry.getKey(), 1, 1);
        }
    }
}