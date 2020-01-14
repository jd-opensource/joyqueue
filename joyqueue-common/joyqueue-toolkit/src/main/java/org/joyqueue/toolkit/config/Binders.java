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

import org.joyqueue.toolkit.config.annotation.Binding;
import org.joyqueue.toolkit.config.annotation.BooleanBinding;
import org.joyqueue.toolkit.config.annotation.DateBinding;
import org.joyqueue.toolkit.config.annotation.DoubleBinding;
import org.joyqueue.toolkit.config.annotation.NumberBinding;
import org.joyqueue.toolkit.config.annotation.ObjectBinding;
import org.joyqueue.toolkit.config.annotation.StringBinding;
import org.joyqueue.toolkit.reflect.ReflectException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * 绑定器
 * Created by hexiaofeng on 16-8-29.
 */
public class Binders {
    /**
     * 绑定上下文
     *
     * @param context 上下文
     * @param target  对象
     * @throws ReflectException
     */
    public static void bind(final Context context, final Object target) throws ReflectException {
        if (context == null || target == null) {
            return;
        }
        Class<?> clazz = target.getClass();
        Field[] fields;
        Annotation[] annotations;
        Binder binder;
        while (clazz != null && clazz != Object.class) {
            fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                annotations = field.getDeclaredAnnotations();
                if (annotations != null) {
                    for (Annotation annotation : annotations) {
                        binder = getBinder(annotation);
                        if (binder != null) {
                            binder.bind(field, annotation, target, context);
                        }
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    /**
     * 获取验证器
     *
     * @param annotation 声明
     * @return 验证器
     */
    protected static Binder getBinder(final Annotation annotation) {
        Binder binder = null;
        if (annotation instanceof BooleanBinding) {
            binder = BooleanBinder.INSTANCE;
        } else if (annotation instanceof Binding) {
            binder = BindingBinder.INSTANCE;
        } else if (annotation instanceof DateBinding) {
            binder = DateBinder.INSTANCE;
        } else if (annotation instanceof DoubleBinding) {
            binder = DoubleBinder.INSTANCE;
        } else if (annotation instanceof NumberBinding) {
            binder = NumberBinder.INSTANCE;
        } else if (annotation instanceof ObjectBinding) {
            binder = ObjectBinder.INSTANCE;
        } else if (annotation instanceof StringBinding) {
            binder = StringBinder.INSTANCE;
        }
        return binder;
    }

}
