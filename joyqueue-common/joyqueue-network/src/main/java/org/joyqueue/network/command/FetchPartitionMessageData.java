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
 * FetchPartitionData
 *
 * author: gaohaoxiang
 * date: 2018/12/7
 */
public class FetchPartitionMessageData {

    private int count;
    private long index;

    public FetchPartitionMessageData() {

    }

    public FetchPartitionMessageData(int count, long index) {
        this.count = count;
        this.index = index;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public long getIndex() {
        return index;
    }
}