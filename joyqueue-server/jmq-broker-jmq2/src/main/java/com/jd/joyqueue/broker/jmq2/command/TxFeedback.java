/**
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
package com.jd.joyqueue.broker.jmq2.command;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.network.JMQ2Payload;
import org.joyqueue.network.session.ProducerId;

/**
 * TxFeedback
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/8
 */
public class TxFeedback extends JMQ2Payload {

    private String app;
    private ProducerId producerId;
    private int longPull = 0;

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public ProducerId getProducerId() {
        return producerId;
    }

    public void setProducerId(ProducerId producerId) {
        this.producerId = producerId;
    }

    public int getLongPull() {
        return longPull;
    }

    public void setLongPull(int longPull) {
        this.longPull = longPull;
    }

    @Override
    public int type() {
        return JMQ2CommandType.TX_FEEDBACK.getCode();
    }
}