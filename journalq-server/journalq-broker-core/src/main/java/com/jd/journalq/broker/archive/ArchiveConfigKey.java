package com.jd.journalq.broker.archive;

import com.jd.journalq.toolkit.config.PropertyDef;

/**
 * 归档配置
 * <p>
 * Created by chengzhiliang on 2018/12/6.
 */
public enum ArchiveConfigKey implements PropertyDef {
    WRITE_BATCH_NUM("archive.write.batch.num", 1000, Type.INT),
    READ_BATCH_NUM("archive.read.batch.num", 1000, Type.INT),
    LOG_QUEUE_SIZE("archive.send.log.queue.size", 10000, Type.INT),
    WRITE_THREAD_NUM("archive.thread.num", 5, Type.INT);

    private String name;
    private Object value;
    private PropertyDef.Type type;

    ArchiveConfigKey(String name, Object value, PropertyDef.Type type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public Type getType() {
        return type;
    }

}
