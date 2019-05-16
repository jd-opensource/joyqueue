package com.jd.journalq.broker.command;

import com.jd.journalq.broker.network.traffic.FetchTrafficPayload;
import com.jd.journalq.broker.network.traffic.Traffic;

/**
 * FetchPartitionMessageAck
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/5/16
 */
public class FetchPartitionMessageAck extends com.jd.journalq.network.command.FetchPartitionMessageAck implements FetchTrafficPayload {

    private Traffic traffic;

    public void setTraffic(Traffic traffic) {
        this.traffic = traffic;
    }

    @Override
    public Traffic getTraffic() {
        return traffic;
    }
}