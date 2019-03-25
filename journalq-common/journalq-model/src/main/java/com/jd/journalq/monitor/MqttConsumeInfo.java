package com.jd.journalq.monitor;

import java.util.Map;

/**
 * @author majun8
 */
public class MqttConsumeInfo extends BaseMonitorInfo {
    private Map<String, MqttSessionInfo> consumeInfos;

    public Map<String, MqttSessionInfo> getConsumeInfos() {
        return consumeInfos;
    }

    public void setConsumeInfos(Map<String, MqttSessionInfo> consumeInfos) {
        this.consumeInfos = consumeInfos;
    }
}
