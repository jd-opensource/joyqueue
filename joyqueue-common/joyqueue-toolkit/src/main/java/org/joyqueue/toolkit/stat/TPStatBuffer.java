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
package org.joyqueue.toolkit.stat;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * TP性能统计缓冲器，用于计算
 */
public class TPStatBuffer implements Serializable {
    // 默认矩阵长度，2的指数，便于取余数
    protected static final int LENGTH = 256;
    // 矩阵，最多存放length*length-1
    protected AtomicReferenceArray<AtomicLongArray> timer;
    // 超过maxTime的数据存储在这俩
    protected AtomicReference<ConcurrentMap<Integer, AtomicLong>> outstrip;
    // 成功处理的记录条数
    protected AtomicLong recordTotal = new AtomicLong(0);
    // 成功调用次数
    protected AtomicLong successTotal = new AtomicLong(0);
    // 失败调用次数
    protected AtomicLong errorTotal = new AtomicLong(0);
    // 数据大小
    protected AtomicLong sizeTotal = new AtomicLong(0);
    // 总时间
    protected AtomicLong timeTotal = new AtomicLong(0);
    // 最大时间
    protected int maxTime;
    // 矩阵的长度
    protected int length;
    // 2的指数
    protected int exponent;

    public TPStatBuffer() {
        this(LENGTH);
    }

    public TPStatBuffer(int length) {
        if (length < 1) {
            throw new IllegalArgumentException("length must be greater than 0");
        }

        // 容量是2的指数
        int cap = 1;
        int exponent = 0;
        while (length > cap) {
            cap <<= 1;
            exponent++;
        }
        this.length = cap;
        this.exponent = exponent;
        this.timer = new AtomicReferenceArray<AtomicLongArray>(cap);
        this.outstrip = new AtomicReference<ConcurrentMap<Integer, AtomicLong>>();
        this.maxTime = cap * cap - 1;
    }

    /**
     * 清理
     */
    public void clear() {
        timer = new AtomicReferenceArray<AtomicLongArray>(length);
        successTotal.set(0);
        errorTotal.set(0);
        sizeTotal.set(0);
        timeTotal.set(0);
        // 如果有数据超过了maxTime，则保留map数据，避免创建对象开销
        ConcurrentMap<Integer, AtomicLong> exceeds = outstrip.get();
        if (exceeds != null) {
            exceeds.clear();
        }
    }

    /**
     * 成功调用，批量增加统计信息，每次调用时间一样.
     *
     * @param time    单次调用时间
     * @param count   调用次数
     * @param records 总共记录条数
     * @param size    总共数据包大小
     */
    public void success(final int time, final int count, final int records, final long size) {
        if (time < 0 || count <= 0) {
            // 做性能统计时间不可能为负数
            return;
        }
        successTotal.addAndGet(count);
        if (records > 0) {
            recordTotal.addAndGet(records);
        }
        if (size > 0) {
            sizeTotal.addAndGet(size);
        }
        if (time > 0) {
            timeTotal.addAndGet(time * count);
        }
        int maxIndex = length - 1;

        if (time > maxTime) {
            // 超过最大时间，矩阵不能存储，采用MAP存储
            ConcurrentMap<Integer, AtomicLong> exceeds = outstrip.get();
            if (exceeds == null) {
                // 按时间排序
                exceeds = new ConcurrentSkipListMap<Integer, AtomicLong>();
                if (!outstrip.compareAndSet(null, exceeds)) {
                    exceeds = outstrip.get();
                }
            }
            AtomicLong counts = exceeds.get(time);
            if (counts == null) {
                counts = new AtomicLong();
                AtomicLong old = exceeds.putIfAbsent(time, counts);
                if (old != null) {
                    counts = old;
                }
            }
            counts.addAndGet(count);
        } else {
            int i = time >> exponent;
            int j = time & maxIndex;
            AtomicLongArray v = timer.get(i);
            if (v == null) {
                v = new AtomicLongArray(length);
                if (!timer.compareAndSet(i, null, v)) {
                    v = timer.get(i);
                }
            }
            v.addAndGet(j, count);
        }
    }

    /**
     * 单词调用成功
     *
     * @param records 记录条数
     * @param size    数据包大小
     * @param time    调用时间
     */
    public void success(final int records, final long size, final int time) {
        success(time, 1, records, size);
    }

    /**
     * 出错，增加TP计数
     */
    public void error() {
        errorTotal.incrementAndGet();
    }

    /**
     * 出错，增加TP计数
     *
     * @param count 调用次数
     */
    public void error(final int count) {
        errorTotal.addAndGet(count);
    }

    /**
     * 获取性能统计
     *
     * @return 性能统计
     */
    public TPStat getTPStat() {
        TPStat stat = new TPStat();
        stat.setSuccess(successTotal.get());
        stat.setError(errorTotal.get());
        stat.setCount(recordTotal.get());
        stat.setTime(timeTotal.get());
        stat.setSize(sizeTotal.get());

        if (stat.getSuccess() <= 0) {
            return stat;
        }

        int min = -1;
        int max = -1;
        // 计算排序位置
        int tp999 = (int) Math.floor(stat.getSuccess() * 99.9 / 100);
        int tp99 = (int) Math.floor(stat.getSuccess() * 99.0 / 100);
        int tp90 = (int) Math.floor(stat.getSuccess() * 90.0 / 100);
        int tp50 = (int) Math.floor(stat.getSuccess() * 50.0 / 100);

        long count;
        long prev = 0;
        long pos = 0;
        int time;
        AtomicLongArray v;
        // 递增遍历数组
        for (int i = 0; i < length; i++) {
            v = timer.get(i);
            if (v != null) {
                for (int j = 0; j < length; j++) {
                    // 获取该时间的数量
                    count = v.get(j);
                    if (count > 0) {
                        time = i * length + j;
                        // 当前排序位置
                        pos = prev + count;
                        if (min == -1) {
                            min = time;
                        }
                        if (max == -1 || time > max) {
                            max = time;
                        }
                        if (prev < tp50 && pos >= tp50) {
                            stat.setTp50(time);
                        }
                        if (prev < tp90 && pos >= tp90) {
                            stat.setTp90(time);
                        }
                        if (prev < tp99 && pos >= tp99) {
                            stat.setTp99(time);
                        }
                        if (prev < tp999 && pos >= tp999) {
                            stat.setTp999(time);
                        }
                        prev = pos;
                    }
                }
            }
        }
        // 遍历超过最大时间的数据
        ConcurrentMap<Integer, AtomicLong> exceeds = outstrip.get();
        if (exceeds != null) {
            for (Map.Entry<Integer, AtomicLong> entry : exceeds.entrySet()) {
                time = entry.getKey();
                pos = prev + entry.getValue().get();
                if (min == -1) {
                    min = time;
                }
                if (max == -1 || time > max) {
                    max = time;
                }
                if (prev < tp50 && pos >= tp50) {
                    stat.setTp50(time);
                }
                if (prev < tp90 && pos >= tp90) {
                    stat.setTp90(time);
                }
                if (prev < tp99 && pos >= tp99) {
                    stat.setTp99(time);
                }
                if (prev < tp999 && pos >= tp999) {
                    stat.setTp999(time);
                }
                prev = pos;
            }
        }

        stat.setMin(min);
        stat.setMax(max);
        return stat;
    }

}
