package io.chubao.joyqueue.broker.consumer;

import io.chubao.joyqueue.toolkit.config.PropertyDef;

/**
 * @author chengzhiliang on 2018/10/22.
 */
public enum ConsumeConfigKey implements PropertyDef {
    ;
    private String name;
    private Object value;
    private PropertyDef.Type type;

    ConsumeConfigKey(String name, Object value, PropertyDef.Type type) {
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
