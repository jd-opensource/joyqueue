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
package org.joyqueue.broker.manage.service;

/**
 * ConnectionManageService
 *
 * author: gaohaoxiang
 * date: 2018/10/15
 */
public interface ConnectionManageService {

    /**
     * 关闭主题下应用的所有生产者连接
     *
     * @param topic 主题
     * @param app 应用
     * @return 被关闭的连接数量
     */
    int closeProducer(String topic, String app);

    /**
     * 关闭主题下应用的所有消费者连接
     *
     * @param topic 主题
     * @param app 应用
     * @return 被关闭的连接数量
     */
    int closeConsumer(String topic, String app);
}