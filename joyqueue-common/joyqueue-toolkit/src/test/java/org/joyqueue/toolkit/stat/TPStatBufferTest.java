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
package org.joyqueue.toolkit.stat;

import org.joyqueue.toolkit.stat.TPStat;
import org.joyqueue.toolkit.stat.TPStatBuffer;
import org.junit.Assert;
import org.junit.Test;

public class TPStatBufferTest {

    @Test
    public void testTP() {
        // 矩阵足够大
        TPStatBuffer buffer = new TPStatBuffer(64);
        for (int i = 1; i <= 100; i++) {
            buffer.success(i, 1, 1, 0);
        }
        TPStat stat = buffer.getTPStat();
        Assert.assertEquals(stat.getMax(), 100);
        Assert.assertEquals(stat.getMin(), 1);
        Assert.assertEquals(stat.getTp999(), 99);
        Assert.assertEquals(stat.getTp99(), 99);
        Assert.assertEquals(stat.getTp90(), 90);
        Assert.assertEquals(stat.getTp50(), 50);

        buffer.clear();
        for (int i = 1; i <= 90; i++) {
            buffer.success(1, 1, 1, 0);
        }
        buffer.success(2, 1, 1, 0);
        buffer.success(2, 1, 1, 0);
        buffer.success(2, 1, 1, 0);
        buffer.success(2, 1, 1, 0);
        buffer.success(2, 1, 1, 0);
        buffer.success(2, 1, 1, 0);
        buffer.success(2, 1, 1, 0);
        buffer.success(2, 1, 1, 0);
        buffer.success(3, 1, 1, 0);
        buffer.success(3, 1, 1, 0);
        stat = buffer.getTPStat();
        Assert.assertEquals(stat.getMax(), 3);
        Assert.assertEquals(stat.getMin(), 1);
        Assert.assertEquals(stat.getTp999(), 3);
        Assert.assertEquals(stat.getTp99(), 3);
        Assert.assertEquals(stat.getTp90(), 1);
        Assert.assertEquals(stat.getTp50(), 1);

        // 矩阵小
        buffer = new TPStatBuffer(5);
        for (int i = 100; i >= 1; i--) {
            buffer.success(i, 1, 1, 0);
        }
        stat = buffer.getTPStat();
        Assert.assertEquals(stat.getMax(), 100);
        Assert.assertEquals(stat.getMin(), 1);
        Assert.assertEquals(stat.getTp999(), 99);
        Assert.assertEquals(stat.getTp99(), 99);
        Assert.assertEquals(stat.getTp90(), 90);
        Assert.assertEquals(stat.getTp50(), 50);
    }
}
