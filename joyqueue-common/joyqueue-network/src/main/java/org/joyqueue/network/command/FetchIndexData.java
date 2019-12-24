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

import org.joyqueue.exception.JoyQueueCode;

/**
 * FetchIndexData
 *
 * author: gaohaoxiang
 * date: 2018/12/13
 */
public class FetchIndexData {

    private long index;
    private long leftIndex;
    private long rightIndex;
    private JoyQueueCode code;

    public FetchIndexData() {

    }

    public FetchIndexData(long index, long leftIndex, long rightIndex, JoyQueueCode code) {
        this.index = index;
        this.leftIndex = leftIndex;
        this.rightIndex = rightIndex;
        this.code = code;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public void setLeftIndex(long leftIndex) {
        this.leftIndex = leftIndex;
    }

    public long getLeftIndex() {
        return leftIndex;
    }

    public void setRightIndex(long rightIndex) {
        this.rightIndex = rightIndex;
    }

    public long getRightIndex() {
        return rightIndex;
    }

    public JoyQueueCode getCode() {
        return code;
    }

    public void setCode(JoyQueueCode code) {
        this.code = code;
    }
}