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
package com.jd.joyqueue.stat;


import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TP性能统计，记录每次调用时间
 */
public class TPStatBuffer implements Serializable {

    private ConcurrentMap<Integer, AtomicInteger> tpTimes = new ConcurrentHashMap<Integer, AtomicInteger>();

    // 成功处理的记录条数
    protected AtomicLong totalCount = new AtomicLong(0);
    // 成功调用次数
    protected AtomicLong totalSuccess = new AtomicLong(0);
    // 失败调用次数
    protected AtomicLong totalError = new AtomicLong(0);
    // 数据大小
    protected AtomicLong totalSize = new AtomicLong(0);
    // 总时间
    protected AtomicLong totalTime = new AtomicLong(0);

    /**
     * 清理
     */
    public void clear() {
        tpTimes.clear();
        totalSuccess.set(0);
        totalError.set(0);
        totalSize.set(0);
        totalTime.set(0);
    }

    /**
     * 成功调用，增加TP计数
     *
     * @param count 处理的记录条数
     * @param size  数据包大小
     * @param time  时间(毫秒)
     */
    public void success(final int count, final long size, final int time) {
        totalSuccess.incrementAndGet();
        if (count > 0) {
            totalCount.addAndGet(count);
        }
        if (size > 0) {
            totalSize.addAndGet(size);
        }
        if (time > 0) {
            totalTime.addAndGet(time);
        }

        updateTPTimes(count, time);
    }

    private void updateTPTimes(int count, int time) {
        Integer tpKey = time;
        if (count > 0) {
            tpKey = time / count;
        }

        AtomicInteger tpCount = tpTimes.get(tpKey);
        if (tpCount == null) {
            tpCount = new AtomicInteger(0);
        }
        AtomicInteger old = tpTimes.putIfAbsent(tpKey, tpCount);
        if (old != null) {
            tpCount = old;
        }

        tpCount.addAndGet(count);
    }

    /**
     * 出错，增加TP计数
     */
    public void error() {
        totalError.incrementAndGet();
    }

    /**
     * 获取性能统计
     *
     * @return 性能统计
     */
    public ClientTpOriginals getTPStat() {
        ClientTpOriginals stat = new ClientTpOriginals();
        stat.setSuccess(totalSuccess.get());
        stat.setError(totalError.get());
        stat.setCount(totalCount.get());
        stat.setTime(totalTime.get());
        stat.setSize(totalSize.get());

        if (stat.getSuccess() <= 0) {
            return stat;
        }

        stat.setTpTimes(tpTimes);

        return stat;
    }

}
