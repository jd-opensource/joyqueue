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
package org.joyqueue.broker.consumer.position.model;

import java.util.Objects;

/**
 * 位置信息（消费序号/拉取序号）
 * <p>
 * Created by chengzhiliang on 2019/2/28.
 */
public class Position implements Cloneable {
    // 开始应答序号
    private volatile long ackStartIndex;
    // 结束应答序号
    private volatile long ackCurIndex;
    // 开始拉取序号
    private volatile long pullStartIndex;
    // 结束拉取序号
    private volatile long pullCurIndex;
    private volatile boolean ack = false;

    public Position() {

    }

    public Position(long ackStartIndex, long ackEndIndex, long pullStartIndex, long pullEndIndex) {
        this.ackStartIndex = ackStartIndex;
        this.ackCurIndex = ackEndIndex;
        this.pullStartIndex = pullStartIndex;
        this.pullCurIndex = pullEndIndex;
    }

    public long getAckStartIndex() {
        return ackStartIndex;
    }

    public void setAckStartIndex(long ackStartIndex) {
        this.ackStartIndex = ackStartIndex;
    }

    public long getAckCurIndex() {
        return ackCurIndex;
    }

    public void setAckCurIndex(long ackCurIndex) {
        this.ackCurIndex = ackCurIndex;
    }

    public long getPullStartIndex() {
        return pullStartIndex;
    }


    public void setPullCurIndex(long pullCurIndex) {
        this.pullCurIndex = pullCurIndex;
    }

    public void setPullStartIndex(long pullStartIndex) {
        this.pullStartIndex = pullStartIndex;
    }

    public long getPullCurIndex() {
        return pullCurIndex;
    }

    public boolean isAck() {
        return ack;
    }

    public void setAck(boolean ack) {
        this.ack = ack;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return ackStartIndex == position.ackStartIndex &&
                ackCurIndex == position.ackCurIndex;
    }

    @Override
    public int hashCode() {

        return Objects.hash(ackStartIndex, ackCurIndex);
    }

    @Override
    public Position clone() throws CloneNotSupportedException {
        return (Position) super.clone();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Position{");
        sb.append("ackStartIndex='").append(ackStartIndex).append('\'');
        sb.append(", ackCurIndex=").append(ackCurIndex);
        sb.append(", pullStartIndex=").append(pullStartIndex);
        sb.append(", pullCurIndex=").append(pullCurIndex);
        sb.append('}');
        return sb.toString();
    }
}
