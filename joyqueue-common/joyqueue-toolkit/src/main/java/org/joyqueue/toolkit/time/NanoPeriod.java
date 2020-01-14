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
package org.joyqueue.toolkit.time;

import java.util.concurrent.TimeUnit;

/**
 * 时间片段(纳秒)
 * Created by hexiaofeng on 16-7-16.
 */
public class NanoPeriod implements Period {
    // 开始时间
    protected long startTime;
    // 终止时间
    protected long endTime;

    @Override
    public void begin() {
        startTime = System.nanoTime();
    }

    @Override
    public void end() {
        endTime = System.nanoTime();
    }

    @Override
    public long time() {
        return endTime - startTime;
    }

    @Override
    public void clear() {
        startTime = 0;
        endTime = 0;
    }

    @Override
    public TimeUnit getTimeUnit() {
        return TimeUnit.NANOSECONDS;
    }
}
