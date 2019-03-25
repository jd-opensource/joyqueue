package com.jd.journalq.toolkit.service;

import com.jd.journalq.toolkit.config.Context;

/**
 * 调度计划
 * Created by hexiaofeng on 16-5-11.
 */
public interface WorkerSchedule {

    /**
     * 获取间隔时间
     *
     * @param context 上下文
     * @return 间隔时间
     * @throws Exception
     */
    long getInterval(Context context) throws Exception;

    /**
     * 初始延迟时间
     *
     * @return 初始延迟时间
     * <li>0 立即执行</li>
     * <li>>0 延迟执行</li>
     * <li><0 需要初始化后才执行</li>
     */
    long getInitialDelay();
}
