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
package org.joyqueue.toolkit.config;

import org.joyqueue.toolkit.config.annotation.NumberBinding;
import org.joyqueue.toolkit.reflect.Reflect;
import org.joyqueue.toolkit.reflect.ReflectException;
import org.joyqueue.toolkit.validate.annotation.Range;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * 数值绑定
 * Created by hexiaofeng on 16-8-29.
 */
public class NumberBinder implements Binder {
    public static final NumberBinder INSTANCE = new NumberBinder();

    @Override
    public void bind(final Field field, final Annotation annotation, final Object target, final Context context) throws
            ReflectException {
        if (field == null || annotation == null || target == null || context == null || !(annotation instanceof
                NumberBinding)) {
            return;
        }
        NumberBinding binding = (NumberBinding) annotation;
        Class<?> type = field.getType();
        Number number = null;
        Range range = binding.range();
        if (type.isAssignableFrom(byte.class) || type.isAssignableFrom(Byte.class)) {
            number = context.getByte(binding.key(), (byte) binding.defaultValue());
            number = range != null && (number.longValue() < range.min() || number.longValue() > range
                    .max()) ? (byte) binding.defaultValue() : number;
            Reflect.set(field, target, number);
        } else if (type.isAssignableFrom(short.class) || type.isAssignableFrom(Short.class)) {
            number = context.getShort(binding.key(), (short) binding.defaultValue());
            number = range != null && (number.longValue() < range.min() || number.longValue() > range
                    .max()) ? (short) binding.defaultValue() : number;
            Reflect.set(field, target, number);
        } else if (type.isAssignableFrom(int.class) || type.isAssignableFrom(Integer.class)) {
            number = context.getInteger(binding.key(), (int) binding.defaultValue());
            number = range != null && (number.longValue() < range.min() || number.longValue() > range
                    .max()) ? (int) binding.defaultValue() : number;
            Reflect.set(field, target, number);
        } else if (type.isAssignableFrom(long.class) || type.isAssignableFrom(Long.class)) {
            number = context.getLong(binding.key(), binding.defaultValue());
            number = range != null && (number.longValue() < range.min() || number.longValue() > range.max()) ? binding
                    .defaultValue() : number;
            Reflect.set(field, target, number);
        }
    }
}
