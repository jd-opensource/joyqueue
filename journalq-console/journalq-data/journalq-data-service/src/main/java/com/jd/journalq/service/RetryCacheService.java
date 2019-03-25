package com.jd.journalq.service;

/**
 * Created by wangxiaofei1 on 2018/12/24.
 */
public interface RetryCacheService {
    void removeMsgBodyFromRedis(String msgBodyKey, String topic, String app, long dbId);
}
