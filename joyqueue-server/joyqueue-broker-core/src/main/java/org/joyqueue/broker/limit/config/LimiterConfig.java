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
package org.joyqueue.broker.limit.config;

/**
 * RateLimiterConfig
 *
 * author: gaohaoxiang
 * date: 2019/5/17
 */
public class LimiterConfig {

    private int tps;
    private int traffic;

    public LimiterConfig() {

    }

    public LimiterConfig(int tps, int traffic) {
        this.tps = tps;
        this.traffic = traffic;
    }

    public int getTps() {
        return tps;
    }

    public void setTps(int tps) {
        this.tps = tps;
    }

    public int getTraffic() {
        return traffic;
    }

    public void setTraffic(int traffic) {
        this.traffic = traffic;
    }

    @Override
    public String toString() {
        return "LimiterConfig{" +
                "tps=" + tps +
                ", traffic=" + traffic +
                '}';
    }
}