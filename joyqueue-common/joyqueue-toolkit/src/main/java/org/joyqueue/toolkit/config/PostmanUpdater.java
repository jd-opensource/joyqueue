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
package org.joyqueue.toolkit.config;

import org.joyqueue.toolkit.lang.Pair;
import org.joyqueue.toolkit.service.Service;
import org.joyqueue.toolkit.service.ServiceThread;
import org.joyqueue.toolkit.validate.annotation.Size;
import org.joyqueue.toolkit.validate.annotation.Valid;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 定时从服务拉取配置
 * Created by hexiaofeng on 16-8-29.
 */
@Valid
public abstract class PostmanUpdater extends Service implements Postman {
    // 间隔时间
    @Size
    protected int interval;
    // 工作线程
    protected Thread worker;
    // 缓存的上下文
    protected ConcurrentMap<String, Pair<String, Context>> caches =
            new ConcurrentHashMap<String, Pair<String, Context>>();
    // 监听器
    protected ConcurrentMap<String, List<GroupListener>> listeners =
            new ConcurrentHashMap<String, List<GroupListener>>();

    public void setInterval(int interval) {
        this.interval = interval;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        // 初始化一遍上下文
        if (!listeners.isEmpty()) {
            update();
        }
        // 创建定时工作线程
        worker = new Thread(new Worker(this, interval), this.getClass().getSimpleName());
        worker.setDaemon(true);
        worker.start();
    }

    @Override
    protected void doStop() {
        if (worker != null && !worker.isInterrupted()) {
            worker.interrupt();
        }
        caches.clear();
        super.doStop();
    }

    @Override
    public void addListener(final String group, final GroupListener listener) {
        if (group == null || group.isEmpty() || listener == null) {
            return;
        }


        List<GroupListener> grpListeners = listeners.get(group);
        if (grpListeners == null) {
            grpListeners = new CopyOnWriteArrayList<GroupListener>();
            List<GroupListener> old = listeners.putIfAbsent(group, grpListeners);
            if (old != null) {
                grpListeners = old;
            }
        }
        if (isStarted()) {
            // 从缓存取
            Context context = getOrUpdate(group);
            if (context != null) {
                // 广播数据
                publish(group, listener, context.clone());
            }
        }
        grpListeners.add(listener);
    }

    @Override
    public void removeListener(final String group, final GroupListener listener) {
        if (group == null || group.isEmpty() || listener == null) {
            return;
        }
        List<GroupListener> grpListeners = listeners.get(group);
        if (grpListeners != null) {
            grpListeners.remove(listener);
        }
    }

    @Override
    public Context get(final String group) {
        if (!isStarted()) {
            throw new IllegalStateException("service is not started.");
        }
        return getOrUpdate(group);

    }

    /**
     * 获取上下文，不存在测更新
     *
     * @param group 分组
     * @return 上下文
     */
    protected Context getOrUpdate(final String group) {
        // 从缓存取
        Pair<String, Context> pair = caches.get(group);
        if (pair == null) {
            // 加载数据
            Context context = update(group);
            // 缓存
            pair = new Pair<String, Context>(group, context);
            Pair<String, Context> old = caches.putIfAbsent(group, pair);
            if (context == null && old != null && old.getValue() != null) {
                pair = old;
            }
        }
        return pair.getValue();
    }

    /**
     * 更新上下文
     */
    protected void update() {
        Context context;
        Pair<String, Context> pair;
        String group;
        for (Map.Entry<String, List<GroupListener>> entry : listeners.entrySet()) {
            if (!isStarted()) {
                return;
            }
            group = entry.getKey();
            context = update(group);
            pair = caches.get(group);
            // 判断上下文配置是否发生变更
            if (context != null && pair != null && !context.equals(pair.getValue())) {
                caches.put(group, new Pair<String, Context>(group, context));
                for (GroupListener listener : entry.getValue()) {
                    if (!isStarted()) {
                        return;
                    }
                    publish(group, listener, context.clone());
                }
            }
        }
    }

    /**
     * 调用配置服务获取上下文
     *
     * @param group 分组
     * @return 上下文
     */
    protected abstract Context update(String group);

    /**
     * 通知监听器
     *
     * @param group    分组
     * @param listener 监听器
     * @param context  上下文
     */
    protected void publish(final String group, final GroupListener listener, final Context context) {
        synchronized (listener) {
            // 防止并发
            listener.onUpdate(group, context);
        }
    }

    /**
     * 执行器
     */
    protected class Worker extends ServiceThread {

        public Worker(Service parent, long interval) {
            super(parent, interval);
        }

        @Override
        protected void execute() throws Exception {
            update();
        }

        @Override
        public boolean onException(final Throwable e) {
            return true;
        }

    }

}
