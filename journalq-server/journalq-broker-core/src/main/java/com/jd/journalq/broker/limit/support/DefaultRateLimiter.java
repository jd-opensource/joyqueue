/**
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
package com.jd.journalq.broker.limit.support;

import com.jd.journalq.broker.limit.RateLimiter;

/**
 * RateLimiter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/5/16
 */
public class DefaultRateLimiter implements RateLimiter {

    private com.google.common.util.concurrent.RateLimiter tpsRateLimiter;
    private com.google.common.util.concurrent.RateLimiter trafficRateLimiter;

    public DefaultRateLimiter(int tps, int traffic) {
        this.tpsRateLimiter = com.google.common.util.concurrent.RateLimiter.create(tps);
        this.trafficRateLimiter = com.google.common.util.concurrent.RateLimiter.create(traffic);
    }

    @Override
    public boolean tryAcquireTps() {
        return tryAcquireTps(1);
    }

    @Override
    public boolean tryAcquireTps(int tps) {
        return tpsRateLimiter.tryAcquire(tps);
    }

    @Override
    public boolean tryAcquireTraffic(int traffic) {
        if (traffic == 0) {
            return true;
        }
        return trafficRateLimiter.tryAcquire(traffic);
    }
}