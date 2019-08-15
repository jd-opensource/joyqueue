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
 * JoyQueueTransportBuiltinKeys
 *
 * author: gaohaoxiang
 * date: 2019/2/19
 */
public interface JoyQueueTransportBuiltinKeys extends OMSBuiltinKeys {

    String CONNECTIONS = "TRANSPORT_CONNECTIONS";

    String SEND_TIMEOUT = "TRANSPORT_SEND_TIMEOUT";

    String IO_THREADS = "TRANSPORT_IO_THREADS";

    String CALLBACK_THREADS = "TRANSPORT_CALLBACK_THREADS";

    String CHANNEL_MAX_IDLE_TIME = "TRANSPORT_CHANNEL_MAX_IDLE_TIME";

    String HEARTBEAT_INTERVAL = "TRANSPORT_HEARTBEAT_INTERVAL";

    String HEARTBEAT_TIMEOUT = "TRANSPORT_HEARTBEAT_TIMEOUT";

    String SO_LINGER = "TRANSPORT_SO_LINGER";

    String TCP_NO_DELAY = "TRANSPORT_TCP_NO_DELAY";

    String KEEPALIVE = "TRANSPORT_KEEPALIVE";

    String SO_TIMEOUT = "TRANSPORT_SO_TIMEOUT";

    String SOCKET_BUFFER_SIZE = "TRANSPORT_SOCKET_BUFFER_SIZE";

    String MAX_ONEWAY = "TRANSPORT_MAX_ONEWAY";

    String MAX_ASYNC = "TRANSPORT_MAX_ASYNC";

    String NONBLOCK_ONEWAY = "TRANSPORT_NONBLOCK_ONEWAY";

    String RETRIES = "TRANSPORT_RETRIES";

}