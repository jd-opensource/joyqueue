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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 命名线程工厂类
 *
 * @author hexiaofeng
 * @since 2013-12-09
 */
public class NamedThreadFactory implements ThreadFactory {
    private AtomicInteger counter = new AtomicInteger(0);

    private String name;
    private boolean daemon;

    public NamedThreadFactory(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name is empty");
        }
        this.name = name;
    }

    public NamedThreadFactory(String name, boolean daemon) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name is empty");
        }
        this.name = name;
        this.daemon = daemon;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName(name + " - " + counter.incrementAndGet());
        if (daemon) {
            thread.setDaemon(daemon);
        }
        return thread;
    }

}
