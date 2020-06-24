/**
 * Copyright 2019 The JoyQueue Authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.joyqueue.handler.jwt;


import org.joyqueue.toolkit.config.PropertyDef;

/**
 * @author jiangnan53
 * @date 2020/6/24
 **/
public enum JwtConfigKey implements PropertyDef {

    JWT_ISSUER("jwt.issuer","issuer",Type.STRING),
    JWT_EXPIRATION_TIME_MINUTES_IN_FUTURE("jwt.expire.in.minute",30,Type.DOUBLE),
    JWT_SUBJECT("jwt.subject","subject",Type.STRING),
    JWT_BASE64("jwt.base64","", Type.STRING)

    ;

    private final String name;
    private final Object value;
    private final Type type;

    JwtConfigKey(String name, Object value, Type type) {
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
