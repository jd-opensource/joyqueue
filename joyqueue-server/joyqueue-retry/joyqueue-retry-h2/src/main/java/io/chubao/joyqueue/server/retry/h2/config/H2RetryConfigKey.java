package io.chubao.joyqueue.server.retry.h2.config;

import io.chubao.joyqueue.toolkit.config.PropertyDef;

/**
 * @author liyue25
 * Date: 2019-07-05
 */
public enum H2RetryConfigKey implements PropertyDef {

    WRITE_URL("retry.h2.url.write", "", Type.STRING),
    WRITE_USER_NAME("retry.h2.username.write", "", Type.STRING),
    WRITE_PASSWORD("retry.h2.password.write", "", Type.STRING),
    DRIVER("retry.h2.driver", "com.h2.jdbc.Driver", Type.STRING),
    RETRY_DELAY("retry.delay", 1000, Type.INT),
    MAX_RETRY_TIMES("retry.max.retry.times", 3, Type.INT);

    private String name;
    private Object value;
    private Type type;

    H2RetryConfigKey(String name, Object value, Type type) {
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
