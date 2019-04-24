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
package com.jd.journalq.client.internal.producer.interceptor;

import com.jd.journalq.client.internal.common.interceptor.BaseInterceptor;
import com.jd.journalq.client.internal.producer.domain.SendResult;

import java.util.List;

/**
 * ProducerInterceptor
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/11
 */
public interface ProducerInterceptor extends BaseInterceptor {

    boolean preSend(ProduceContext context);

    void postSend(ProduceContext context, List<SendResult> result);
}