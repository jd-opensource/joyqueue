/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.server.retry.db.config;

import org.joyqueue.toolkit.config.PropertyDef;

/**
 * @author liyue25
 * Date: 2019-07-05
 */
public enum DbRetryConfigKey implements PropertyDef {

    WRITE_URL("retry.mysql.url.write", "", Type.STRING),
    WRITE_USER_NAME("retry.mysql.username.write", "", Type.STRING),
    WRITE_PASSWORD("retry.mysql.password.write", "", Type.STRING),
    READ_URL("retry.mysql.url.read", "", Type.STRING),
    READ_USER_NAME("retry.mysql.username.read", "", Type.STRING),
    READ_PASSWORD("retry.mysql.password.read", "", Type.STRING),
    DRIVER("retry.mysql.driver", "com.mysql.jdbc.Driver", Type.STRING),
    RETRY_DELAY("retry.delay", 1000, Type.INT),
    MAX_RETRY_TIMES("retry.max.retry.times", "0", Type.INT);

    private String name;
    private Object value;
    private Type type;

    DbRetryConfigKey(String name, Object value, Type type) {
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
