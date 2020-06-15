package com.jd.joyqueue.broker.jmq2.command;

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