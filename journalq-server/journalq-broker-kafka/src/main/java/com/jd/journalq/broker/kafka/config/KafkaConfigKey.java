package com.jd.journalq.broker.kafka.config;

import com.jd.journalq.domain.QosLevel;
import com.jd.journalq.toolkit.config.PropertyDef;

/**
 * KafkaConfigKey
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/5
 */
public enum KafkaConfigKey implements PropertyDef {

    // 每秒处理的消息条数，防止对象太多导致的GC问题，后续需要在对象分配上进行调优
    PERMITSPER_SECOND("kafka.permitsper.second", 1000 * 60 * 5, Type.INT),
    // 消息等待写入队列流控超时时间,单位ms
    ACQUIRE_PERMITS_TIMEOUT("kafka.acquire.permits.timeout", 20, Type.INT),

    // 加入组最小会话超时
    SESSION_MIN_TIMEOUT("kafka.session.min.timeout.ms", 1000 * 6, Type.INT),
    // 加入组最大会话超时
    SESSION_MAX_TIMEOUT("kafka.session.max.timeout.ms", 1000 * 60 * 5, Type.INT),

    // group创建时rebalance延时时间，单位ms
    REBALANCE_INITIAL_DELAY("kafka.rebalance.initial.delay", 1000 * 3, Type.INT),
    // rebalance超时时间
    REBALANCE_TIMEOUT("kafka.rebalance.timeout", 1000 * 60, Type.INT),

    // offset同步超时
    OFFSET_SYNC_TIMEOUT("kafka.offset.sync.timeout", 1000 * 3, Type.INT),
    // offset缓存过期时间
    OFFSET_CACHE_EXPIRE_TIME("kafka.offset.expire.time", 1000 * 60 * 10, Type.INT),

    // 事务同步超时
    TRANSACTION_SYNC_TIMEOUT("kafka.offset.sync.timeout", 1000 * 3, Type.INT),
    // 事务超时
    TRANSACTION_TIMEOUT("kafka.transaction.timeout", 1000 * 60 * 30, Type.INT),
    // 事务日志写入超时
    TRANSACTION_LOG_WRITE_TIMEOUT("kafka.transaction.log.write.timeout", 1000 * 1, Type.INT),
    // 事务日志写入级别
    TRANSACTION_LOG_WRITE_QOSLEVEL("kafka.transaction.log.write.qosLevel", QosLevel.REPLICATION.value(), Type.INT),
    // 事务重试次数
    TRANSACTION_LOG_RETRIES("kafka.transaction.log.reties", 3, Type.INT),
    // 事务间隔
    // TODO 间隔时间
    TRANSACTION_LOG_INTERVAL("kafka.transaction.log.interval", 1000 * 30, Type.INT),
    // 事务扫描大小
    TRANSACTION_LOG_SCAN_SIZE("kafka.transaction.log.scan.size", 1000, Type.INT),
    // 事务日志app
    TRANSACTION_LOG_APP("coordinator.transaction.log.app", "__transaction_log", PropertyDef.Type.STRING),

    // 拉取批量大小
    FETCH_BATCH_SIZE("kafka.fetch.batch.size", 10, Type.INT),

    // 是否启用限速
    RATE_LIMIT_ENABLE("kafka.rate.limit.enable", false, Type.BOOLEAN),
    // 限速延迟
    RATE_LIMIT_DELAY("kafka.rate.limit.delay", 100 * 1, Type.INT),
    // 限速次数
    RATE_LIMIT_TIMES("kafka.rate.limit.times", 100, Type.INT),
    // 限速窗口时间
    RATE_LIMIT_TIME_WINDOW_SIZE("kafka.rate.limit.time.window.size", 1000 * 1, Type.INT),

    ;

    private String name;
    private Object value;
    private Type type;

    KafkaConfigKey(String name, Object value, Type type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public Type getType() {
        return type;
    }
}