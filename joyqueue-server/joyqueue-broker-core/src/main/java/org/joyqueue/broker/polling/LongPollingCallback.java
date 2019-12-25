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
package org.joyqueue.broker.polling;

import org.joyqueue.broker.consumer.model.PullResult;
import org.joyqueue.network.session.Consumer;
import org.joyqueue.network.transport.exception.TransportException;

/**
 * 长轮询回调
 *
 * Created by chengzhiliang on 2018/9/5.
 */
public interface LongPollingCallback {

    void onSuccess(Consumer consumer, PullResult pullResult) throws TransportException;

    void onExpire(Consumer consumer) throws TransportException;

    void onException(Consumer consumer, Throwable throwable) throws TransportException;
}
