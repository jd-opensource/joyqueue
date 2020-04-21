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
package org.joyqueue.msg.filter.cfs;


import org.joyqueue.toolkit.config.PropertyDef;

/**
 * @author jiangnan53
 * @date 2020/4/15
 **/
public enum CfsConfigKey implements PropertyDef {

    CFS_REGION("cfs.region","",Type.STRING),
    CFS_ENDPOINT("cfs.endpoint","",Type.STRING),
    CFS_BUCKET_NAME("cfs.bucket.name","",Type.STRING),
    CFS_ACCESS_KEY("cfs.access.key","",Type.STRING),
    CFS_SECRET_KEY("cfs.secret.key","",Type.STRING),
    ;

    private final String name;
    private final Object value;
    private final Type type;

    CfsConfigKey(String name, Object value, Type type) {
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
