package io.chubao.joyqueue.broker.protocol.command;

import io.chubao.joyqueue.broker.network.traffic.FetchTrafficPayload;
import io.chubao.joyqueue.broker.network.traffic.Traffic;

/**
 * FetchTopicMessageResponse
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/5/16
 */
public class FetchTopicMessageResponse extends io.chubao.joyqueue.network.command.FetchTopicMessageResponse implements FetchTrafficPayload {

    private Traffic traffic;

    public void setTraffic(Traffic traffic) {
        this.traffic = traffic;
    }

    @Override
    public Traffic getTraffic() {
        return traffic;
    }
}
