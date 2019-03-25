package com.jd.journalq.model.domain;

/**
 * 重试服务类型
 */
public enum RetryType {
    /**
     * 直连数据库
     */
    DB,
    /**
     * 访问远程服务
     */
    REMOTE
}