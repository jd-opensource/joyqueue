/**
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
package com.jd.journalq.registry.zookeeper.manager;

import com.jd.journalq.toolkit.concurrent.EventListener;
import com.jd.journalq.toolkit.lang.Pair;
import com.jd.journalq.toolkit.time.SystemClock;
import com.jd.journalq.registry.listener.PathEvent;
import com.jd.journalq.registry.listener.PathEvent.PathEventType;
import com.jd.journalq.registry.listener.PathListener;
import com.jd.journalq.registry.zookeeper.ZKClient;
import org.apache.zookeeper.data.Stat;

import java.util.Arrays;

/**
 * 节点监听器
 *
 * @author 何小锋
 */
public class PathManager extends ListenerManager<PathListener, PathEvent> {
    private Pair<Stat, byte[]> cache;

    public PathManager(ZKClient zkClient, String path) {
        super(zkClient, path);
    }

    @Override
    protected void doStop() {
        cache = null;
        super.doStop();
    }

    @Override
    protected void onAddListener(final PathListener listener) {
        if (cache != null) {
            PathEvent event = new PathEvent(PathEventType.UPDATED, path, cache.getValue(), cache.getKey().getVersion());
            events.add(event, listener);
        }
    }

    @Override
    protected void onUpdateEvent() throws Exception {
        Stat stat = new Stat();
        byte[] data = zkClient.getData(path, updateWatcher, stat).getData();
        Pair<Stat, byte[]> pair = new Pair<Stat, byte[]>(stat, data);
        writeLock.lock();
        try {
            if (!isStarted()) {
                return;
            }
            // 数据发送了变化
            if (cache == null || !Arrays.equals(data, cache.getValue())) {
                events.add(new PathEvent(PathEventType.UPDATED, path, data, stat.getVersion()));
                cache = pair;
            } else {
                // 数据没有发送变化，则判断监听器是否心跳感知
                // 应用场景：节点数据只是ID，数据库中的数据已经修改，需要每次重数据库中获取状态
                for (EventListener<PathEvent> listener : events.getListeners()) {
                    if (listener instanceof EventListener.Heartbeat) {
                        // 心跳感知，判断是否要触发
                        if (((EventListener.Heartbeat) listener).trigger(SystemClock.now())) {
                            events.add(new PathEvent(PathEventType.UPDATED, path, data, stat.getVersion()), listener);
                        }
                    }
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

}