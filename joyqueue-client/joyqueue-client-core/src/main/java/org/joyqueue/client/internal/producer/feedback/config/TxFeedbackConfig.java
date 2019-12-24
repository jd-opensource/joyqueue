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
package org.joyqueue.client.internal.producer.feedback.config;

/**
 * TxFeedbackConfig
 *
 * author: gaohaoxiang
 * date: 2019/1/3
 */
public class TxFeedbackConfig {

    private String app;
    private long timeout = 1000 * 10;
    private long longPollTimeout = 1000 * 3;

    private int fetchInterval = 1000 * 5;
    private int fetchSize = 1;

    public TxFeedbackConfig copy() {
        TxFeedbackConfig txFeedbackConfig = new TxFeedbackConfig();
        txFeedbackConfig.setApp(app);
        txFeedbackConfig.setTimeout(timeout);
        txFeedbackConfig.setLongPollTimeout(longPollTimeout);
        txFeedbackConfig.setFetchInterval(fetchInterval);
        txFeedbackConfig.setFetchSize(fetchSize);
        return txFeedbackConfig;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getApp() {
        return app;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setLongPollTimeout(long longPollTimeout) {
        this.longPollTimeout = longPollTimeout;
    }

    public long getLongPollTimeout() {
        return longPollTimeout;
    }

    public int getFetchInterval() {
        return fetchInterval;
    }

    public void setFetchInterval(int fetchInterval) {
        this.fetchInterval = fetchInterval;
    }

    public int getFetchSize() {
        return fetchSize;
    }

    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }
}