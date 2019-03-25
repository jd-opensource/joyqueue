package com.jd.journalq.toolkit.stat;

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
