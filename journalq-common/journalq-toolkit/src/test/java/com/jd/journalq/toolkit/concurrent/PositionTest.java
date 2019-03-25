package com.jd.journalq.toolkit.concurrent;

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
