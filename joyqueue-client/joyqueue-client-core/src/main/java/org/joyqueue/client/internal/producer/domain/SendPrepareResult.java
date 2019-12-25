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
package org.joyqueue.client.internal.producer.domain;

import org.joyqueue.exception.JoyQueueCode;

import java.io.Serializable;

/**
 * SendPrepareResult
 *
 * author: gaohaoxiang
 * date: 2018/12/10
 */
public class SendPrepareResult implements Serializable {

    private String txId;
    private JoyQueueCode code;

    public SendPrepareResult() {

    }

    public SendPrepareResult(String txId, JoyQueueCode code) {
        this.txId = txId;
        this.code = code;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getTxId() {
        return txId;
    }

    public void setCode(JoyQueueCode code) {
        this.code = code;
    }

    public JoyQueueCode getCode() {
        return code;
    }
}