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
package com.jd.journalq.toolkit.ref;

/**
 * 引用管理器
 */
public interface ReferenceManager<T> {

    /**
     * 释放引用，改方法应该在引用对象不再被其他引用的时候调用，这样允许放回池中被重新利用
     *
     * @param reference 待释放引用
     */
    void release(T reference);

}
