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
package org.joyqueue.monitor;


import com.jd.laf.extension.Type;

public interface PointTracer extends Type {

    /**
     *
     * @param key 要跟踪的key
     * @return end时要带的参数
     */
    TraceStat begin(String key);

    /**
     *
     * @param end begin 方法的返回值，用来结束跟踪
     */
    void end(TraceStat end);

    void error(TraceStat end);
}
