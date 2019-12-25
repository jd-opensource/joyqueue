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
package org.joyqueue.toolkit.concurrent;

import org.joyqueue.toolkit.concurrent.CAtomicLong;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by hexiaofeng on 16-6-29.
 */
public class PositionTest {

    @Test
    public void testPosition() {
        CAtomicLong p = new CAtomicLong();
        long value = p.incrementAndGet();
        Assert.assertEquals(value, 0);
        Assert.assertEquals(value, p.get());
        p.compareAndSet(value, 3);
        Assert.assertEquals(p.get(), 3);
        p.set(4);
        Assert.assertEquals(p.get(), 4);
        p.setVolatile(5);
        Assert.assertEquals(p.get(), 5);
    }
}
