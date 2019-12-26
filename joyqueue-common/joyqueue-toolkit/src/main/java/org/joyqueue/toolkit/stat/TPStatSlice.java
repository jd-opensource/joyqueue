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
package org.joyqueue.toolkit.stat;

import org.joyqueue.toolkit.time.Period;

/**
 * 性能统计切片接口对象
 */
public interface TPStatSlice {

    /**
     * 获取时间片段
     * @return 时间
     */
    Period getPeriod();

    /**
     * 初始化
     */
    void clear();
}
