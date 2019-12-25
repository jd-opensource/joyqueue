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

import org.joyqueue.toolkit.config.annotation.DateBinding;
import org.joyqueue.toolkit.reflect.Reflect;
import org.joyqueue.toolkit.reflect.ReflectException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期绑定
 * Created by hexiaofeng on 16-8-29.
 */
public class DateBinder implements Binder {
    public static final DateBinder INSTANCE = new DateBinder();

    @Override
    public void bind(final Field field, final Annotation annotation, final Object target, final Context context) throws
            ReflectException {
        if (field == null || annotation == null || target == null || context == null || !(annotation instanceof
                DateBinding)) {
            return;
        }
        DateBinding binding = (DateBinding) annotation;
        Class<?> type = field.getType();
        // 日期类型
        if (type.isAssignableFrom(Date.class)) {
            // 获取值
            Object value = context.getObject(binding.key());
            // 日期类型有值
            if (value != null && type.isInstance(value)) {
                Reflect.set(field, target, value);
            } else if (value != null && value instanceof Number) {
                Reflect.set(field, target, new Date(((Number) value).longValue()));
            } else {
                // 获取默认文本
                String text = binding.defaultValue();
                // 当前值为字符串
                if (value != null && value instanceof String) {
                    text = (String) value;
                    if (text.isEmpty()) {
                        text = binding.defaultValue();
                    }
                }
                // 字符串不为空，并且格式化不为空
                if (text != null && !text.isEmpty() && binding.format() != null && !binding.format().isEmpty()) {
                    // 格式化
                    SimpleDateFormat sdf = new SimpleDateFormat(binding.format());
                    try {
                        value = sdf.parse(text);
                        Reflect.set(field, target, value);
                    } catch (ParseException e) {
                        Reflect.set(field, target, null);
                    }
                } else {
                    Reflect.set(field, target, null);
                }
            }
        }
    }
}
