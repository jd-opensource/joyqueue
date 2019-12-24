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
package org.joyqueue.server.retry;

import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.server.retry.api.MessageRetry;
import org.joyqueue.server.retry.api.RetryPolicyProvider;
import org.joyqueue.server.retry.model.RetryMessageModel;
import org.joyqueue.toolkit.config.PropertySupplier;

import java.util.List;

/**
 * Created by chengzhiliang on 2019/4/11.
 */
public class NullMessageRetry implements MessageRetry<Long> {
    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isStarted() {
        return false;
    }

    @Override
    public void addRetry(List<RetryMessageModel> retryMessageModelList) throws JoyQueueException {

    }

    @Override
    public void retrySuccess(String topic, String app, Long[] messageIds) throws JoyQueueException {

    }

    @Override
    public void retryError(String topic, String app, Long[] messageIds) throws JoyQueueException {

    }

    @Override
    public void retryExpire(String topic, String app, Long[] messageIds) throws JoyQueueException {

    }

    @Override
    public List<RetryMessageModel> getRetry(String topic, String app, short count, long startIndex) throws JoyQueueException {
        return null;
    }

    @Override
    public int countRetry(String topic, String app) throws JoyQueueException {
        return 0;
    }

    @Override
    public void setRetryPolicyProvider(RetryPolicyProvider retryPolicyProvider) {

    }

    @Override
    public void setSupplier(PropertySupplier supplier) {

    }
}
