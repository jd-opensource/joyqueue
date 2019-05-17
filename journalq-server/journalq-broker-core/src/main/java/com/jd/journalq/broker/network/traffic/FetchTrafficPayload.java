package com.jd.journalq.broker.network.traffic;

/**
 * FetchTrafficPayload
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/5/16
 */
public interface FetchTrafficPayload extends TrafficPayload, TrafficType {

    public static final String TYPE = "fetch";

    @Override
    default String getTrafficType() {
        return TYPE;
    }
}