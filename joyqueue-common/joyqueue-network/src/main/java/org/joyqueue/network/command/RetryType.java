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
package org.joyqueue.network.command;

/**
 * RetryType
 *
 * author: gaohaoxiang
 * date: 2018/12/12
 */
public enum RetryType {

    NONE(0),

    TIMEOUT(1),

    EXCEPTION(2),

    OTHER(3)

    ;

    private byte type;

    RetryType(int type) {
        this.type = (byte) type;
    }

    public byte getType() {
        return type;
    }

    public static RetryType valueOf(int type) {
        for (RetryType retryType : RetryType.values()) {
            if (retryType.getType() == type) {
                return retryType;
            }
        }
        return null;
    }
}