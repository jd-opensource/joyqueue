package com.jd.journalq.broker.network.traffic;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Traffic
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/5/16
 */
public class Traffic {

    private Map<String, Integer> topicTraffic;
    private String app;

    public Traffic() {

    }

    public Traffic(String app) {
        this.app = app;
    }

    public Traffic(Map<String, Integer> topicTraffic, String app) {
        this.topicTraffic = topicTraffic;
        this.app = app;
    }

    public void record(String topic, int traffic) {
        if (topicTraffic == null) {
            topicTraffic = Maps.newHashMap();
        }
        traffic = ObjectUtils.defaultIfNull(topicTraffic.get(topic), 0) + traffic;
        topicTraffic.put(topic, traffic);
    }

    public List<String> getTopics() {
        if (topicTraffic == null) {
            return Collections.emptyList();
        }
        return Lists.newArrayList(topicTraffic.keySet());
    }

    public String getApp() {
        return app;
    }

    public int getTraffic(String topic) {
        if (topicTraffic == null) {
            return 0;
        }
        return topicTraffic.get(topic);
    }
}