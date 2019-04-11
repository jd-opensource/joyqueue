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
package com.jd.journalq.broker.kafka.session;

import com.jd.journalq.network.transport.Transport;

/**
 * KafkaConnectionHelper
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/9
 */
public class KafkaConnectionHelper {

    private static final String CONNECTION_ATTR_KEY = "_KAFKA_CONNECTION_";

    public static void setConnection(Transport transport, KafkaConnection connection) {
        transport.attr().set(CONNECTION_ATTR_KEY, connection);
    }

    public static KafkaConnection getConnection(Transport transport) {
        return (KafkaConnection) transport.attr().get(CONNECTION_ATTR_KEY);
    }
}