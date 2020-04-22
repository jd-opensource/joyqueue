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
package org.joyqueue.msg.filter.s3;


import org.joyqueue.toolkit.config.PropertyDef;

/**
 * @author jiangnan53
 * @date 2020/4/15
 **/
public enum S3ConfigKey implements PropertyDef {

    S3_REGION("s3.region","",Type.STRING),
    S3_ENDPOINT("s3.endpoint","",Type.STRING),
    S3_BUCKET_NAME("s3.bucket.name","",Type.STRING),
    S3_ACCESS_KEY("s3.access.key","",Type.STRING),
    S3_SECRET_KEY("s3.secret.key","",Type.STRING),
    ;

    private final String name;
    private final Object value;
    private final Type type;

    S3ConfigKey(String name, Object value, Type type) {
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
