package com.jd.journalq.toolkit.time;

import org.junit.Test;

/**
 * Created by hexiaofeng on 16-6-29.
 */
public class SystemClockTest {

    @Test
    public void testTime() {
        long time = SystemClock.now();
        for (int i = 0; i < 100000000; i++) {
            SystemClock.now();
        }
        time = SystemClock.now() - time;
        System.out.println(time);
    }
}
