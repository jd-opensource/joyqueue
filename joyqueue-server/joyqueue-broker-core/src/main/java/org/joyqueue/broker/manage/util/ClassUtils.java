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
package org.joyqueue.broker.manage.util;

import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * ClassUtils
 *
 * author: gaohaoxiang
 * date: 2018/12/21
 */
public class ClassUtils {

    public static Class<?> getParameterizedType(Class<?> source, Class<?> target) {
        return getParameterizedType(source, target, 0);
    }

    public static Class<?> getParameterizedType(Class<?> source, Class<?> target, int index) {
        Type[] genericInterfaces = source.getGenericInterfaces();

        if (ArrayUtils.isEmpty(genericInterfaces)) {
            return null;
        }

        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericInterface;

                if (parameterizedType.getRawType().getTypeName().equals(target.getName())) {
                    return (Class<?>) parameterizedType.getActualTypeArguments()[index];
                }
            }
        }

        return null;
    }
}