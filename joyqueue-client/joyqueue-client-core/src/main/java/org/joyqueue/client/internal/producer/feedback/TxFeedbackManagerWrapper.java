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
package org.joyqueue.client.internal.producer.feedback;

import org.joyqueue.client.internal.cluster.ClusterManager;
import org.joyqueue.client.internal.producer.MessageSender;
import org.joyqueue.client.internal.producer.TxFeedbackManager;
import org.joyqueue.client.internal.producer.callback.TxFeedbackCallback;
import org.joyqueue.client.internal.producer.transport.ProducerClientManager;
import org.joyqueue.toolkit.service.Service;

/**
 * TxFeedbackManagerWrapper
 *
 * author: gaohaoxiang
 * date: 2019/1/3
 */
public class TxFeedbackManagerWrapper extends Service implements TxFeedbackManager {

    private ClusterManager clusterManager;
    private ProducerClientManager producerClientManager;
    private MessageSender messageSender;
    private TxFeedbackManager delegate;

    public TxFeedbackManagerWrapper(ClusterManager clusterManager, ProducerClientManager producerClientManager, MessageSender messageSender, TxFeedbackManager delegate) {
        this.clusterManager = clusterManager;
        this.producerClientManager = producerClientManager;
        this.messageSender = messageSender;
        this.delegate = delegate;
    }

    @Override
    protected void doStart() throws Exception {
        if (clusterManager != null) {
            clusterManager.start();
        }
        if (producerClientManager != null) {
            producerClientManager.start();
        }
        if (messageSender != null) {
            messageSender.start();
        }
        delegate.start();
    }

    @Override
    protected void doStop() {
        delegate.stop();
        if (producerClientManager != null) {
            producerClientManager.stop();
        }
        if (messageSender != null) {
            messageSender.stop();
        }
        if (clusterManager != null) {
            clusterManager.stop();
        }
    }

    @Override
    public void setTransactionCallback(String topic, TxFeedbackCallback callback) {
        delegate.setTransactionCallback(topic, callback);
    }

    @Override
    public void removeTransactionCallback(String topic) {
        delegate.removeTransactionCallback(topic);
    }
}