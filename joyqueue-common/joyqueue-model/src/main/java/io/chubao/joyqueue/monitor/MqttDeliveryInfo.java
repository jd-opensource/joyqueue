package io.chubao.joyqueue.monitor;

import java.util.Map;

/**
 * @author majun8
 */
public class MqttDeliveryInfo extends BaseMonitorInfo {
    private Map<String, MqttSessionInfo> deliveryInfos;

    public Map<String, MqttSessionInfo> getDeliveryInfos() {
        return deliveryInfos;
    }

    public void setDeliveryInfos(Map<String, MqttSessionInfo> deliveryInfos) {
        this.deliveryInfos = deliveryInfos;
    }
}
