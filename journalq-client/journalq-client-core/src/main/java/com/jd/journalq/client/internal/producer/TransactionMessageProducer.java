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
package com.jd.journalq.client.internal.producer;

import com.jd.journalq.client.internal.producer.domain.ProduceMessage;
import com.jd.journalq.client.internal.producer.domain.SendResult;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * TransactionMessageProducer
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/10
 */
public interface TransactionMessageProducer {

    void commit();

    void rollback();

    SendResult send(ProduceMessage message);

    SendResult send(ProduceMessage message, long timeout, TimeUnit timeoutUnit);

    List<SendResult> batchSend(List<ProduceMessage> messages);

    List<SendResult> batchSend(List<ProduceMessage> messages, long timeout, TimeUnit timeoutUnit);
}