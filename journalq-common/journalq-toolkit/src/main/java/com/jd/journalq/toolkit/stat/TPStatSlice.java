package com.jd.journalq.toolkit.stat;

import com.jd.journalq.toolkit.time.Period;

/**
 * 性能统计切片接口对象
 */
public interface TPStatSlice {

    /**
     * 获取时间片段
     * @return 时间
     */
    Period getPeriod();

    /**
     * 初始化
     */
    void clear();
}
