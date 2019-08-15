package io.chubao.joyqueue.broker.protocol.command;

import io.chubao.joyqueue.broker.network.traffic.FetchTrafficPayload;
import io.chubao.joyqueue.broker.network.traffic.Traffic;

/**
 * FetchPartitionMessageAck
 *
 * author: gaohaoxiang
 * date: 2019/5/16
 */
public class FetchPartitionMessageResponse extends io.chubao.joyqueue.network.command.FetchPartitionMessageResponse implements FetchTrafficPayload {

    private Traffic traffic;

    public void setTraffic(Traffic traffic) {
        this.traffic = traffic;
    }

    @Override
    public Traffic getTraffic() {
        return traffic;
    }
}