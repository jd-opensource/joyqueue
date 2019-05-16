package com.jd.journalq.broker.command;

import com.jd.journalq.broker.network.traffic.ProduceTrafficPayload;
import com.jd.journalq.broker.network.traffic.Traffic;

/**
 * ProduceMessageAck
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/5/16
 */
public class ProduceMessageAck extends com.jd.journalq.network.command.ProduceMessageAck implements ProduceTrafficPayload {

    private Traffic traffic;

    public void setTraffic(Traffic traffic) {
        this.traffic = traffic;
    }

    @Override
    public Traffic getTraffic() {
        return traffic;
    }
}