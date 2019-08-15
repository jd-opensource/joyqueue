package io.chubao.joyqueue.broker.protocol.command;

import io.chubao.joyqueue.broker.network.traffic.ProduceTrafficPayload;
import io.chubao.joyqueue.broker.network.traffic.Traffic;

/**
 * ProduceMessageResponse
 *
 * author: gaohaoxiang
 * date: 2019/5/16
 */
public class ProduceMessageResponse extends io.chubao.joyqueue.network.command.ProduceMessageResponse implements ProduceTrafficPayload {

    private Traffic traffic;

    public void setTraffic(Traffic traffic) {
        this.traffic = traffic;
    }

    @Override
    public Traffic getTraffic() {
        return traffic;
    }
}