package io.chubao.joyqueue.toolkit.ref;

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
