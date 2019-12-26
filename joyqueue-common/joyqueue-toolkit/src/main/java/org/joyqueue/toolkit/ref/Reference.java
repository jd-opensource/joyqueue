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
package org.joyqueue.toolkit.ref;

/**
 * 引用计数
 */
public interface Reference {

    /**
     * 获取引用
     *
     */
    void acquire();

    /**
     * 释放引用.
     *
     * @return 是否都已经释放了.
     */
    boolean release();

    /**
     * 获取引用次数
     *
     * @return 引用次数.
     */
    long references();

}
