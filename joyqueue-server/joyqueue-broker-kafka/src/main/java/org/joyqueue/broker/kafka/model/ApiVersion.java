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
package org.joyqueue.broker.kafka.model;

/**
 * ApiVersion
 *
 * author: gaohaoxiang
 * date: 2018/11/5
 */
public class ApiVersion {

    private short code;
    private short minVersion;
    private short maxVersion;

    public ApiVersion(short code, short minVersion, short maxVersion) {
        this.code = code;
        this.minVersion = minVersion;
        this.maxVersion = maxVersion;
    }

    public short getCode() {
        return code;
    }

    public short getMinVersion() {
        return minVersion;
    }

    public short getMaxVersion() {
        return maxVersion;
    }
}