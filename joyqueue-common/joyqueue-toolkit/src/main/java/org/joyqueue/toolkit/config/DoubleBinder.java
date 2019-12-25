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

import org.joyqueue.toolkit.config.annotation.DoubleBinding;
import org.joyqueue.toolkit.reflect.Reflect;
import org.joyqueue.toolkit.reflect.ReflectException;
import org.joyqueue.toolkit.validate.annotation.DoubleRange;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * 浮点数绑定
 * Created by hexiaofeng on 16-8-29.
 */
public class DoubleBinder implements Binder {
    public static final DoubleBinder INSTANCE = new DoubleBinder();

    @Override
    public void bind(final Field field, final Annotation annotation, final Object target, final Context context) throws
            ReflectException {
        if (field == null || annotation == null || target == null || context == null || !(annotation instanceof
                DoubleBinding)) {
            return;
        }
        DoubleBinding binding = (DoubleBinding) annotation;
        Class<?> type = field.getType();
        if (type.isAssignableFrom(double.class) || type.isAssignableFrom(Double.class)) {
            double value = context.getDouble(binding.key(), binding.defaultValue());
            DoubleRange range = binding.range();
            if (range != null && (value < range.min() || value > range.max())) {
                value = binding.defaultValue();
            }
            Reflect.set(field, target, value);
        }
    }
}
