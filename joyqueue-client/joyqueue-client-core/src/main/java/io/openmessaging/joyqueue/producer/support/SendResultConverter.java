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
package io.openmessaging.joyqueue.producer.support;

import com.google.common.collect.Lists;
import io.openmessaging.producer.SendResult;

import java.util.List;

/**
 * SendResultConverter
 *
 * author: gaohaoxiang
 * date: 2019/2/20
 */
public class SendResultConverter {

    public static List<SendResult> convert(List<org.joyqueue.client.internal.producer.domain.SendResult> sendResults) {
        List<SendResult> result = Lists.newArrayListWithCapacity(sendResults.size());
        for (org.joyqueue.client.internal.producer.domain.SendResult sendResult : sendResults) {
            result.add(convert(sendResult));
        }
        return result;
    }

    public static SendResult convert(org.joyqueue.client.internal.producer.domain.SendResult sendResult) {
        return new SendResultAdapter(sendResult);
    }
}