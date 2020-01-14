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
package org.joyqueue.broker.producer.transaction.command;

import org.joyqueue.network.command.CommandType;
import org.joyqueue.network.transport.command.JoyQueuePayload;

import java.util.List;

/**
 * TransactionRollbackRequest
 *
 * author: gaohaoxiang
 * date: 2019/4/12
 */
public class TransactionRollbackRequest extends JoyQueuePayload {

    private String topic;
    private String app;
    private List<String> txIds;

    public TransactionRollbackRequest() {

    }

    public TransactionRollbackRequest(String topic, String app, List<String> txIds) {
        this.topic = topic;
        this.app = app;
        this.txIds = txIds;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public void setTxIds(List<String> txIds) {
        this.txIds = txIds;
    }

    public List<String> getTxIds() {
        return txIds;
    }

    @Override
    public int type() {
        return CommandType.TRANSACTION_ROLLBACK_REQUEST;
    }

    @Override
    public String toString() {
        return "TransactionRollbackRequest{" +
                "topic='" + topic + '\'' +
                ", app='" + app + '\'' +
                ", txIds=" + txIds +
                '}';
    }
}