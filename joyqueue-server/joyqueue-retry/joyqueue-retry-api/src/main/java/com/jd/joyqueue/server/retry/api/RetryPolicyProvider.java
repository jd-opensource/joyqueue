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
package com.jd.joyqueue.server.retry.api;

import com.jd.joyqueue.domain.TopicName;
import com.jd.joyqueue.exception.JournalqException;
import com.jd.joyqueue.toolkit.retry.RetryPolicy;

/**
 * 重试策略类
 *
 * Created by chengzhiliang on 2019/2/22.
 */
public interface RetryPolicyProvider {

    /**
     * 获取重试策略
     *
     * @param topic 主题
     * @param app 应用
     * @return
     * @throws JournalqException
     */
    RetryPolicy getPolicy(TopicName topic, String app) throws JournalqException;
}
