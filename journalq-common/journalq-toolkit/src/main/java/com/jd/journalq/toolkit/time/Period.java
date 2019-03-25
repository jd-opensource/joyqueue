package com.jd.journalq.toolkit.time;

import java.util.concurrent.TimeUnit;

/**
 * 时间片段
 * Created by hexiaofeng on 16-7-16.
 */
public interface Period {

    /**
     * 开始
     */
    void begin();

    /**
     * 结束
     */
    void end();

    /**
     * 时间
     *
     * @return 时间
     */
    long time();

    /**
     * 获取时间单位
     *
     * @return 时间单位
     */
    TimeUnit getTimeUnit();

    /**
     * 清理
     */
    void clear();
}
