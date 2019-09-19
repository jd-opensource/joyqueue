package io.chubao.joyqueue.nsr.journalkeeper.config;

import io.chubao.joyqueue.toolkit.config.PropertyDef;
import io.chubao.joyqueue.toolkit.network.IpUtil;

/**
 * JournalkeeperConfigKey
 * author: gaohaoxiang
 * date: 2019/8/14
 */
public enum JournalkeeperConfigKey implements PropertyDef {

    PREFIX("nameserver.journalkeeper", null, PropertyDef.Type.STRING),
    ROLE("nameserver.journalkeeper.role", "VOTER", PropertyDef.Type.STRING),
    PORT("nameserver.journalkeeper.port", 50095, PropertyDef.Type.INT),
    LOCAL("nameserver.journalkeeper.local", IpUtil.getLocalIp(), PropertyDef.Type.STRING),
    NODES("nameserver.journalkeeper.nodes", null, PropertyDef.Type.STRING),
    WAIT_LEADER_TIMEOUT("nameserver.journalkeeper.waitLeaderTimeout", 1000 * 30, PropertyDef.Type.INT),
    WORKING_DIR("nameserver.journalkeeper.working.dir", null, PropertyDef.Type.STRING),
    INIT_FILE("nameserver.journalkeeper.init.file", "/journalkeeper/nameserver/schema.sql", PropertyDef.Type.STRING),

    SNAPSHOT_STEP("nameserver.journalkeeper.snapshot.step", 0, PropertyDef.Type.INT),

    RPC_TIMEOUT("nameserver.journalkeeper.rpc.timeout", 1000 * 60 * 1, PropertyDef.Type.INT),

    FLUSH_INTERVAL("nameserver.journalkeeper.flush.interval", 50, PropertyDef.Type.INT),

    STATE_BATCH_SIZE("nameserver.journalkeeper.state.batch.size", 1024 * 1024 * 1, PropertyDef.Type.INT),

    METRIC_ENABLE("nameserver.journalkeeper.metric.enable", false, PropertyDef.Type.BOOLEAN),
    METRIC_PRINT_INTERVAL("nameserver.journalkeeper.metric.print.interval", 0, PropertyDef.Type.INT),

    ;

    public static final String NODE_SPLITTER = ",";
    public static final String DEFAULT_WORKING_DIR = "/journalkeeper/nameserver";

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
