package io.chubao.joyqueue.toolkit.time;

import java.util.concurrent.TimeUnit;

/**
 * 时间片段(微妙)
 * Created by hexiaofeng on 16-7-16.
 */
public class MicroPeriod implements Period {
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
        return TimeUnit.NANOSECONDS.toMicros(endTime - startTime);
    }

    @Override
    public void clear() {
        startTime = 0;
        endTime = 0;
    }

    @Override
    public TimeUnit getTimeUnit() {
        return TimeUnit.MICROSECONDS;
    }
}
