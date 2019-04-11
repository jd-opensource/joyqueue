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
package com.jd.journalq.toolkit.service;

import com.jd.journalq.toolkit.config.Context;

/**
 * 调度计划
 * Created by hexiaofeng on 16-5-11.
 */
public interface WorkerSchedule {

    /**
     * 获取间隔时间
     *
     * @param context 上下文
     * @return 间隔时间
     * @throws Exception
     */
    long getInterval(Context context) throws Exception;

    /**
     * 初始延迟时间
     *
     * @return 初始延迟时间
     * <li>0 立即执行</li>
     * <li>>0 延迟执行</li>
     * <li><0 需要初始化后才执行</li>
     */
    long getInitialDelay();
}
