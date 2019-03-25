package com.jd.journalq.broker.profile;

import com.jd.journalq.toolkit.stat.TPStatBuffer;
import com.jd.journalq.toolkit.stat.TPStatDoubleBuffer;
import com.jd.journalq.toolkit.stat.TPStatSlice;
import com.jd.journalq.toolkit.time.MilliPeriod;
import com.jd.journalq.toolkit.time.Period;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 消费和发送性能统计
 *
 * @author lindeqiang
 * @since 2016/9/8 18:10
 */
public class PubSubStat extends TPStatDoubleBuffer<PubSubStat.PubSubStatSlice> {
    public static final long ONE_MINUTE = 1000 * 60;

    public PubSubStat() {
        super(new PubSubStatSlice(), new PubSubStatSlice(), ONE_MINUTE);
    }

    public void success(String topic, String app, StatType type, int count, long size, int time) {
        TPStatBuffer buffer = writeStat.getAndCreateTPBuffer(String.format("%s:%s", topic, app), type);
        buffer.success(count, size, time);
    }

    public void error(String app, String topic, StatType type) {
        TPStatBuffer buffer = writeStat.getAndCreateTPBuffer(String.format("%s:%s", topic, app), type);
        buffer.error();
    }

    public static class PubSubStatSlice implements TPStatSlice {
        private ConcurrentMap<String, TPStatBuffer> produceStat = new ConcurrentHashMap<>();
        private ConcurrentMap<String, TPStatBuffer> consumeStat = new ConcurrentHashMap<>();
        private MilliPeriod period = new MilliPeriod();

        @Override
        public Period getPeriod() {
            return period;
        }

        public TPStatBuffer getStat(String topic, String app, StatType type) {
            if (type == StatType.produce) {
                return produceStat.get(String.format("%s:%s", topic, app));
            } else if (type == StatType.consume) {
                return consumeStat.get(String.format("%s:%s", topic, app));
            }
            return null;
        }

        public ConcurrentMap<String, TPStatBuffer> getStats(StatType type) {
            if (StatType.consume == type) {
                return consumeStat;
            } else if (StatType.produce == type) {
                return produceStat;
            }

            return null;
        }

        protected TPStatBuffer getAndCreateTPBuffer(String key, StatType type) {
            TPStatBuffer buffer = null;
            if (type == StatType.consume) {
                buffer = consumeStat.get(key);
                if (buffer == null) {
                    buffer = new TPStatBuffer();
                    TPStatBuffer old = consumeStat.putIfAbsent(key, buffer);
                    if (old != null) {
                        buffer = old;
                    }
                }
                return buffer;
            } else if (type == StatType.produce) {
                buffer = produceStat.get(key);
                if (buffer == null) {
                    buffer = new TPStatBuffer();
                    TPStatBuffer old = produceStat.putIfAbsent(key, buffer);
                    if (old != null) {
                        buffer = old;
                    }
                }
                return buffer;
            }
            return buffer;
        }

        @Override
        public void clear() {
            produceStat.clear();
            consumeStat.clear();
        }
    }

    //订阅类型
    public enum StatType {
        consume,
        produce
    }
}
