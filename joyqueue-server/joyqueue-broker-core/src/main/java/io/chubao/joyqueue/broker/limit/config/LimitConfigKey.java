package io.chubao.joyqueue.broker.limit.config;

import io.chubao.joyqueue.toolkit.config.PropertyDef;

/**
 * LimitConfigKey
 *
 * author: gaohaoxiang
 * date: 2019/5/16
 */
public enum LimitConfigKey implements PropertyDef {

    // 是否启用
    ENABLE("limit.enable", true, PropertyDef.Type.BOOLEAN),

    // 限流后延时
    DELAY("limit.delay", LimitConfig.DELAY_DYNAMIC, PropertyDef.Type.INT),

    // 最大延时
    MAX_DELAY("limit.delay.max", 1000, PropertyDef.Type.INT),

    // 拒绝策略
    REJECTED_STRATEGY("limit.rejected.strategy", "delay", PropertyDef.Type.STRING),

    ;

    private String name;
    private Object value;
    private Type type;

    LimitConfigKey(String name, Object value, Type type) {
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