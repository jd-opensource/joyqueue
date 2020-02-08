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
package org.joyqueue.broker.monitor.stat;

import org.joyqueue.broker.monitor.metrics.Metrics;

/**
 * RetryStat
 *
 * author: gaohaoxiang
 * date: 2018/10/11
 */
public class RetryStat {

    private Metrics total = new Metrics();
    private Metrics success = new Metrics();
    private Metrics failure = new Metrics();

    public Metrics getTotal() {
        return total;
    }

    public Metrics getSuccess() {
        return success;
    }

    public Metrics getFailure() {
        return failure;
    }
}