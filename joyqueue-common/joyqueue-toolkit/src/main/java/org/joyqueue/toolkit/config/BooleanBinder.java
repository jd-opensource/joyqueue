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

import org.joyqueue.toolkit.config.annotation.BooleanBinding;
import org.joyqueue.toolkit.reflect.Reflect;
import org.joyqueue.toolkit.reflect.ReflectException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * 布尔值绑定
 * Created by hexiaofeng on 16-8-29.
 */
public class BooleanBinder implements Binder {
    public static final BooleanBinder INSTANCE = new BooleanBinder();

    @Override
    public void bind(final Field field, final Annotation annotation, final Object target, final Context context) throws
            ReflectException {
        if (field == null || annotation == null || target == null || context == null || !(annotation instanceof
                BooleanBinding)) {
            return;
        }
        BooleanBinding binding = (BooleanBinding) annotation;
        Class<?> type = field.getType();
        if (type.isAssignableFrom(boolean.class) || type.isAssignableFrom(Boolean.class)) {
            boolean value = context.getBoolean(binding.key(), binding.defaultValue());
            Reflect.set(field, target, value);
        }
    }
}
