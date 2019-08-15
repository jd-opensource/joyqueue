package io.chubao.joyqueue.nsr.journalkeeper.config;

import io.chubao.joyqueue.toolkit.config.PropertyDef;
import io.chubao.joyqueue.toolkit.network.IpUtil;

/**
 * JournalkeeperConfigKey
 * author: gaohaoxiang
 * date: 2019/8/14
 */
public enum JournalkeeperConfigKey implements PropertyDef {

    PREFIX("nsr.journalkeeper", "VOTER", PropertyDef.Type.STRING),
    ROLE("nsr.journalkeeper.role", "VOTER", PropertyDef.Type.STRING),
    PORT("nsr.journalkeeper.port", 50095, PropertyDef.Type.STRING),
    NODES("nsr.journalkeeper.votes", IpUtil.getLocalIp(), PropertyDef.Type.STRING),
    WORKING_DIR("nsr.journalkeeper.working.dir", null, PropertyDef.Type.STRING),

    SNAPSHOT_STEP("nsr.journalkeeper.snapshot.step", 0, PropertyDef.Type.INT),

    RPC_TIMEOUT("nsr.journalkeeper.rpc.timeout", 1000 * 1, PropertyDef.Type.INT),

    FLUSH_INTERVAL("nsr.journalkeeper.flush.interval", 50, PropertyDef.Type.INT),

    STATE_BATCH_SIZE("nsr.journalkeeper.state.batch.size", 1024 * 1024 * 1, PropertyDef.Type.INT),

    METRIC_ENABLE("nsr.journalkeeper.metric.enable", false, PropertyDef.Type.BOOLEAN),
    METRIC_PRINT_INTERVAL("nsr.journalkeeper.metric.print.interval", 0, PropertyDef.Type.INT),

    ;

    private String name;
    private Object value;
    private PropertyDef.Type type;

    JournalkeeperConfigKey(String name, Object value, Type type) {
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
