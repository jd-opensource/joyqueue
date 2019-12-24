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
package org.joyqueue.broker.kafka.conf;

import org.joyqueue.toolkit.network.IpUtil;

/**
 * Created by zhuduohui on 2018/9/6.
 */
public interface KafkaConfigs {

    static final String GROUP_ID = "test_app";
    static final String TOPIC = "test_topic_0";
    static final int TOPIC_COUNT = 2;
    static final String BOOTSTRAP = IpUtil.getLocalIp() + ":50088";
    static final String CLIENT_ID = "test_app";
    static final String TRANSACTION_ID = "test_transaction";

//    static final String GROUP_ID = "zhuduohui";
//    static final String TOPIC = "test2";
//    static final int TOPIC_COUNT = 10;
//    static final String BOOTSTRAP = "192.168.112.92:50088";
//    static final String CLIENT_ID = "zhuduohui";
}