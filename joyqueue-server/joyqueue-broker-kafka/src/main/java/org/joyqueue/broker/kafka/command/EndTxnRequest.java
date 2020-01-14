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
package org.joyqueue.broker.kafka.command;

import org.joyqueue.broker.kafka.KafkaCommandType;

import java.util.Objects;

/**
 * EndTxnRequest
 *
 * author: gaohaoxiang
 * date: 2019/4/4
 */
public class EndTxnRequest extends KafkaRequestOrResponse {

    private String transactionId;
    private long producerId;
    private short producerEpoch;

    // false = ABORT, true = COMMIT
    private boolean transactionResult;

    public boolean isCommit() {
        return transactionResult;
    }

    public boolean isAbort() {
        return !transactionResult;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public long getProducerId() {
        return producerId;
    }

    public void setProducerId(long producerId) {
        this.producerId = producerId;
    }

    public short getProducerEpoch() {
        return producerEpoch;
    }

    public void setProducerEpoch(short producerEpoch) {
        this.producerEpoch = producerEpoch;
    }

    public boolean isTransactionResult() {
        return transactionResult;
    }

    public void setTransactionResult(boolean transactionResult) {
        this.transactionResult = transactionResult;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EndTxnRequest that = (EndTxnRequest) o;
        return producerId == that.producerId &&
                producerEpoch == that.producerEpoch &&
                transactionResult == that.transactionResult &&
                Objects.equals(transactionId, that.transactionId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(transactionId, producerId, producerEpoch, transactionResult);
    }

    @Override
    public String toString() {
        return "EndTxnRequest{" +
                "transactionId='" + transactionId + '\'' +
                ", producerId=" + producerId +
                ", producerEpoch=" + producerEpoch +
                ", transactionResult=" + transactionResult +
                '}';
    }

    @Override
    public int type() {
        return KafkaCommandType.END_TXN.getCode();
    }
}
