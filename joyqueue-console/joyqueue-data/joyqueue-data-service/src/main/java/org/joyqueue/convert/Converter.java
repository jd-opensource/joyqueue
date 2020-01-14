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
package org.joyqueue.convert;

/**
 * 模型对象转换器
 *
 * @param <S> 原始对象
 * @param <T> 目标对象
 */
public abstract class Converter<S, T> {

    private boolean checkNull = true;

    Converter() {
    }

    Converter(final boolean checkNull) {
        this.checkNull = checkNull;
    }

    /**
     * Forward converter, S->T
     * @param s
     * @return
     */
    protected abstract T forward(S s);

    /**
     * Backward converter, T->S
     * @param t
     * @return
     */
    protected abstract S backward(T t);

    /**
     * Convert S->T
     *
     * @param s 原始对象
     * @return 目标对象
     */
    public T convert(S s) {
        if (checkNull) {
            return null == s ? null : forward(s);
        } else {
            return forward(s);
        }
    }

    /**
     * Revert T->S
     *
     * @param t 目标对象
     * @return 原始对象
     */
    public S revert(T t){
        if (checkNull) {
            return null == t ? null : backward(t);
        } else {
            return backward(t);
        }
    }

}
