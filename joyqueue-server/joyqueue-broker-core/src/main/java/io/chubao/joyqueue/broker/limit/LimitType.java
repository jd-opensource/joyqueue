package io.chubao.joyqueue.broker.limit;

import io.chubao.joyqueue.broker.network.traffic.FetchTrafficPayload;
import io.chubao.joyqueue.broker.network.traffic.ProduceTrafficPayload;

/**
 * LimitType
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/5/17
 */
public enum LimitType {

    FETCH(FetchTrafficPayload.TYPE),

    PRODUCE(ProduceTrafficPayload.TYPE)

    ;

    private String type;

    LimitType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}