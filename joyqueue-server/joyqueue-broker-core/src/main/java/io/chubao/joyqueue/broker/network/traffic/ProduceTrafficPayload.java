package io.chubao.joyqueue.broker.network.traffic;

/**
 * ProduceTrafficPayload
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/5/16
 */
public interface ProduceTrafficPayload extends TrafficPayload, TrafficType {

    String TYPE = "produce";

    @Override
    default String getTrafficType() {
        return TYPE;
    }
}