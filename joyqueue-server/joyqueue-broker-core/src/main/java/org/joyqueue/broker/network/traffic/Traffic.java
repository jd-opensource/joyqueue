/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.broker.network.traffic;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Traffic
 *
 * author: gaohaoxiang
 * date: 2019/5/16
 */
public class Traffic {

    private Map<String, Integer> topicTraffic;
    private Map<String, Integer> topicTps;
    private Map<String, Boolean> isLimited;
    private boolean limited = false;
    private String app;

    public Traffic() {

    }

    public Traffic(String app) {
        this.app = app;
    }

    public void record(String topic, int traffic, int tps) {
        recordTps(topic, tps);
        recordTraffic(topic, traffic);
    }

    public void recordTps(String topic, int tps) {
        if (topicTps == null) {
            topicTps = Maps.newHashMap();
        }
        tps = ObjectUtils.defaultIfNull(topicTps.get(topic), 0) + tps;
        topicTps.put(topic, tps);
    }

    public void recordTraffic(String topic, int traffic) {
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

    public void limited(boolean limited) {
        this.limited = limited;
    }

    public boolean isLimited() {
        return limited;
    }

    public void limited(String topic, boolean limited) {
        if (isLimited == null) {
            isLimited = Maps.newHashMap();
        }
        isLimited.put(topic, limited);
    }

    public boolean isLimited(String topic) {
        if (limited) {
            return true;
        }
        if (isLimited == null) {
            return false;
        }
        Boolean result = isLimited.get(topic);
        if (result == null) {
            return false;
        }
        return result;
    }

    public void setApp(String app) {
        this.app = app;
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

    public int getTps(String topic) {
        if (topicTps == null) {
            return 1;
        }
        return topicTps.get(topic);
    }
}