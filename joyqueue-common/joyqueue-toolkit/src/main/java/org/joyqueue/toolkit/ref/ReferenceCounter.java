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
package org.joyqueue.toolkit.ref;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 默认计数器
 * Created by hexiaofeng on 16-7-22.
 */
public class ReferenceCounter implements Reference {

    private AtomicLong counter = new AtomicLong(0);

    @Override
    public void acquire() {
        counter.incrementAndGet();
    }

    @Override
    public boolean release() {
        return counter.decrementAndGet() == 0;
    }

    @Override
    public long references() {
        return counter.get();
    }
}
