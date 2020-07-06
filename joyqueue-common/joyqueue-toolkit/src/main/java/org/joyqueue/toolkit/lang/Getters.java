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
package org.joyqueue.toolkit.lang;

import org.joyqueue.toolkit.reflect.Reflect;

import java.lang.reflect.Field;

/**
 * Created by hexiaofeng on 16-8-11.
 */
public class Getters {

    /**
     * 常量获取器
     *
     * @param value 常量
     * @return 常量
     */
    public static Getter constant(final Object value) {
        return new ConstantGetter(value);
    }

    /**
     * 字段值获取器
     *
     * @param field  字段
     * @param target 目标对象
     * @param cache  是否缓存
     * @return 目标对象字段值
     */
    public static Getter field(final Field field, final Object target, final boolean cache) {
        return new FieldGetter(field, target, cache);
    }


    /**
     * 自动值获取器
     * Created by hexiaofeng on 16-8-11.
     */
    protected static class FieldGetter implements Getter {
        // 字段
        protected Field field;
        // 对象
        protected Object target;
        // 是否缓存
        protected boolean cache;
        // 值
        protected Object value;
        // 是否初始化
        protected boolean flag;

        public FieldGetter(final Field field, final Object target, final boolean cache) {
            this.field = field;
            this.target = target;
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
        }

        @Override
        public Object get() {
            if (!cache) {
                return Reflect.get(field, target);
            } else if (!flag) {
                // 获取该字段的值
                value = Reflect.get(field, target);
                flag = true;
            }
            return value;
        }
    }

    /**
     * 常量获取器
     * Created by hexiaofeng on 16-8-11.
     */
    protected static class ConstantGetter implements Getter {

        protected Object value;

        public ConstantGetter(Object value) {
            this.value = value;
        }

        @Override
        public Object get() {
            return value;
        }
    }
}
