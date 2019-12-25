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
package org.joyqueue.broker.kafka.coordinator.transaction.domain;

/**
 * UnCompletedTransactionMetadata
 *
 * author: gaohaoxiang
 * date: 2019/4/19
 */
public class UnCompletedTransactionMetadata extends TransactionMetadata {

    private long startIndex;
    private long endIndex;
    private int reties = 0;

    public void setStartIndex(long startIndex) {
        this.startIndex = startIndex;
    }

    public long getStartIndex() {
        return startIndex;
    }

    public void setEndIndex(long endIndex) {
        this.endIndex = endIndex;
    }

    public long getEndIndex() {
        return endIndex;
    }

    public void incrReties() {
        reties ++;
    }

    public void setReties(int reties) {
        this.reties = reties;
    }

    public int getReties() {
        return reties;
    }
}