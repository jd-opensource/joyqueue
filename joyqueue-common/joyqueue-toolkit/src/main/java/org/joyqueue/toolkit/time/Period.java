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
package org.joyqueue.toolkit.time;

import java.util.concurrent.TimeUnit;

/**
 * 时间片段
 * Created by hexiaofeng on 16-7-16.
 */
public interface Period {

    /**
     * 开始
     */
    void begin();

    /**
     * 结束
     */
    void end();

    /**
     * 时间
     *
     * @return 时间
     */
    long time();

    /**
     * 获取时间单位
     *
     * @return 时间单位
     */
    TimeUnit getTimeUnit();

    /**
     * 清理
     */
    void clear();
}
