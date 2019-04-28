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
package com.jd.journalq.broker.test.kafka.common;


import com.jd.journalq.toolkit.network.IpUtil;

/**
 * Created by zhuduohui on 2018/12/17.
 */
public interface KafkaConfigs {

    String GROUP_ID = "group1";
    String TOPIC = "default.topic1";
    String BOOTSTRAP = IpUtil.getLocalIp() + ":50088";
    String PRODUCE_CLIENT_ID = "group1";
    String CONSUME_CLIENT_ID = "group1";

}