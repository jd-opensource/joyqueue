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
package org.joyqueue.client.internal.trace;

/**
 * TraceType
 *
 * author: gaohaoxiang
 * date: 2019/1/3
 */
public enum TraceType {

    PRODUCER_SEND(0),

    CONSUMER_CONSUME(1),

    CONSUMER_FETCH(2),

    ;

    private int type;
    private boolean enable;

    TraceType(int type) {
        this(type, true);
    }

    TraceType(int type, boolean enable) {
        this.type = type;
        this.enable = enable;
    }

    public int getType() {
        return type;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}