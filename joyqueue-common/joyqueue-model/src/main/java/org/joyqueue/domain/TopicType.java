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
package org.joyqueue.domain;

public enum TopicType {
    /**
     * 主题
     */
    TOPIC((byte)0, "普通主题"),

    /**
     * 广播
     */
    BROADCAST((byte)1, "广播");
//        /**
//         * 顺序队列
//         */
//        SEQUENTIAL((byte)2, "顺序主题");


    private final byte code;
    private final String name;

    TopicType(byte code, String name) {
        this.code = code;
        this.name = name;
    }

    public byte code() {
        return this.code;
    }


    public String getName() {
        return this.name;
    }


    public static TopicType valueOf(final byte value) {
        for (TopicType type : TopicType.values()) {
            if (value == type.code) {
                return type;
            }
        }
        return TOPIC;
    }
}
