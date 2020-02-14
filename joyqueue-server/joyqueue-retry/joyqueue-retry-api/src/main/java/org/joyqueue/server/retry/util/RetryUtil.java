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
package org.joyqueue.server.retry.util;

/**
 * 重试工具类
 *
 * Created by chengzhiliang on 2019/2/10.
 */
public class RetryUtil {

    /**
     * 生成消息Id
     *
     * @param topic     消息主题
     * @param partition 分区
     * @param index     消息序号
     * @return 消息ID
     */
    public static String generateMessageId(String topic, short partition, long index,long sendTime) {
        StringBuilder sb = new StringBuilder();
        sb.append(topic);
        sb.append('-').append(partition);
        sb.append('-').append(index);
        sb.append('-').append(sendTime);
        return sb.toString();
    }

}
