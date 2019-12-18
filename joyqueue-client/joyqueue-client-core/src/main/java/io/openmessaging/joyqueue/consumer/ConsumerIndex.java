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
package io.openmessaging.joyqueue.consumer;

/**
 * ConsumerIndex
 * author: gaohaoxiang
 * date: 2019/11/12
 */
public class ConsumerIndex {

    private long index;
    private long leftIndex;
    private long rightIndex;

    public ConsumerIndex(long index, long leftIndex, long rightIndex) {
        this.index = index;
        this.leftIndex = leftIndex;
        this.rightIndex = rightIndex;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public long getLeftIndex() {
        return leftIndex;
    }

    public void setLeftIndex(long leftIndex) {
        this.leftIndex = leftIndex;
    }

    public long getRightIndex() {
        return rightIndex;
    }

    public void setRightIndex(long rightIndex) {
        this.rightIndex = rightIndex;
    }
}