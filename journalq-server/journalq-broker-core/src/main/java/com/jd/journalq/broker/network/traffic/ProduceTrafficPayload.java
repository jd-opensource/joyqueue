package com.jd.journalq.broker.network.traffic;

/**
 * ProduceTrafficPayload
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/5/16
 */
public interface ProduceTrafficPayload extends TrafficPayload, TrafficType {

    public static final String TYPE = "produce";

    @Override
    default String getTrafficType() {
        return TYPE;
    }
}