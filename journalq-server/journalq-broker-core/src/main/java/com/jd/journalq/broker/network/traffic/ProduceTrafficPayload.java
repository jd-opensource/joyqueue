package com.jd.journalq.broker.network.traffic;

/**
 * ProduceTrafficPayload
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/5/16
 */
public interface ProduceTrafficPayload extends TrafficPayload, TrafficType {

    @Override
    default String getTrafficType() {
        return "produce";
    }
}