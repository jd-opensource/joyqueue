/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jd.journalq.toolkit.ref;

import com.jd.journalq.toolkit.lang.Preconditions;

import java.io.Closeable;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 引用池
 */
public class ReferencePool<T extends Reference> implements ReferenceManager<T>, Closeable {
    private final ReferenceFactory<T> factory;
    private final Queue<T> pool = new ConcurrentLinkedQueue<T>();
    private volatile boolean closed;

    public ReferencePool(final ReferenceFactory<T> factory) {
        Preconditions.checkArgument(factory != null, "factory cannot be null");
        this.factory = factory;
    }

    /**
     * 获取引用
     *
     * @return 获取引用对象
     */
    public T acquire() {
        Preconditions.checkState(closed, "pool is closed.");
        T reference = pool.poll();
        if (reference == null) {
            reference = factory.create(this);
        }
        reference.acquire();
        return reference;
    }

    @Override
    public void release(final T reference) {
        if (reference == null) {
            return;
        }
        if (!closed) {
            pool.add(reference);
        }
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;
        for (T reference : pool) {
            if (reference instanceof Closeable) {
                try {
                    ((Closeable) reference).close();
                } catch (IOException ignored) {
                }
            }
        }
        pool.clear();
    }

}
