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
package org.joyqueue.broker.limit.support;

import org.joyqueue.broker.limit.RateLimiter;

import java.util.concurrent.Semaphore;

/**
 * RateLimiter
 *
 * author: gaohaoxiang
 * date: 2019/5/16
 */
public class DefaultRateLimiter implements RateLimiter {

    private int tps;
    private int traffic;

    private com.google.common.util.concurrent.RateLimiter tpsRateLimiter;
    private com.google.common.util.concurrent.RateLimiter trafficRateLimiter;
    private Semaphore semaphore = new Semaphore(1);

    public DefaultRateLimiter(int tps) {
        this.tps = tps;
        this.tpsRateLimiter = com.google.common.util.concurrent.RateLimiter.create(tps);
    }

    public DefaultRateLimiter(int tps, int traffic) {
        this.tps = tps;
        this.traffic = traffic;
        this.tpsRateLimiter = com.google.common.util.concurrent.RateLimiter.create(tps);
        this.trafficRateLimiter = com.google.common.util.concurrent.RateLimiter.create(traffic);
    }

    @Override
    public boolean tryAcquireTps() {
        return tryAcquireTps(1);
    }

    @Override
    public boolean tryAcquireTps(int tps) {
        if (tps <= 0) {
            return true;
        }
        return tpsRateLimiter.tryAcquire(Math.min(tps, this.tps));
    }

    @Override
    public boolean tryAcquireTraffic(int traffic) {
        if (traffic <= 0) {
            return true;
        }
        return trafficRateLimiter.tryAcquire(Math.min(traffic, this.traffic));
    }

    @Override
    public boolean tryAcquireRequire() {
        return semaphore.tryAcquire();
    }

    @Override
    public boolean releaseRequire() {
        semaphore.release();
        return true;
    }

    public int getTps() {
        return tps;
    }

    public int getTraffic() {
        return traffic;
    }
}