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
package io.openmessaging.joyqueue.domain;

import io.openmessaging.OMSBuiltinKeys;

/**
 * JoyQueueConsumerBuiltinKeys
 *
 * author: gaohaoxiang
 * date: 2019/2/19
 */
public interface JoyQueueConsumerBuiltinKeys extends OMSBuiltinKeys {

    String GROUP = "CONSUMER_GROUP";

    String BATCH_SIZE = "CONSUMER_BATCH_SIZE";

    String ACK_TIMEOUT = "CONSUMER_ACK_TIMEOUT";

    String TIMEOUT = "CONSUMER_TIMEOUT";

    String POLL_TIMEOUT = "CONSUMER_POLL_TIMEOUT";

    String LONGPOLL_TIMEOUT = "CONSUMER_LONGPOLL_TIMEOUT";

    String INTERVAL = "CONSUMER_INTERVAL";

    String IDLE_INTERVAL = "CONSUMER_IDLE_INTERVAL";

    String SESSION_TIMEOUT = "CONSUMER_SESSION_TIMEOUT";

    String THREAD = "CONSUMER_THREAD";

    String FAILOVER = "CONSUMER_FAILOVER";

    String FORCE_ACK = "CONSUMER_FORCE_ACK";

    String LOADBALANCE = "CONSUMER_LOADBALANCE";

    String LOADBALANCE_TYPE = "CONSUMER_LOADBALANCE_TYPE";

    String BROADCAST_GROUP = "CONSUMER_BROADCAST_GROUP";

    String BROADCAST_LOCAL_PATH = "CONSUMER_BROADCAST_LOCAL_PATH";

    String BROADCAST_PERSIST_INTERVAL = "CONSUMER_BROADCAST_PERSIST_INTERVAL";

    String BROADCAST_INDEX_EXPIRE_TIME = "CONSUMER_BROADCAST_INDEX_EXPIRE_TIME";

    String BROADCAST_INDEX_AUTO_RESET = "CONSUMER_BROADCAST_INDEX_AUTO_RESET";
}