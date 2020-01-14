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
package org.joyqueue.toolkit.validate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * 嵌套验证，支持对象、集合和数组
 * Created by hexiaofeng on 16-5-10.
 */
public class ValidValidator implements Validator {

    public static final ValidValidator INSTANCE = new ValidValidator();

    @Override
    public void validate(final Object target, final Annotation annotation, final Value value) throws ValidateException {
        if (value.value == null) {
            return;
        }
        // 去掉基本类型
        Class type = value.type;
        if (!isSupported(type)) {
            return;
        } else if (type.isArray()) {
            // 数组
            int length = Array.getLength(value.value);
            Object obj;
            for (int i = 0; i < length; i++) {
                obj = Array.get(value.value, i);
                if (obj != null && isSupported(obj.getClass())) {
                    Validators.validate(obj);
                }
            }
        } else if (Collection.class.isAssignableFrom(type)) {
            // 集合
            for (Object obj : (Collection) value.value) {
                if (obj != null && isSupported(obj.getClass())) {
                    Validators.validate(obj);
                }
            }
        } else {
            Validators.validate(value.value);
        }

    }

    /**
     * 是否支持
     *
     * @param type 类型
     * @return 基本类型标示
     */
    protected boolean isSupported(final Class type) {
        if (type == int.class) {
            return false;
        } else if (type == long.class) {
            return false;
        } else if (type == double.class) {
            return false;
        } else if (type == short.class) {
            return false;
        } else if (type == byte.class) {
            return false;
        } else if (type == boolean.class) {
            return false;
        } else if (Number.class.isAssignableFrom(type)) {
            return false;
        } else if (type == Boolean.class) {
            return false;
        } else if (type == String.class) {
            return false;
        } else if (type == Object.class) {
            return false;
        } else if (Date.class.isAssignableFrom(type)) {
            return false;
        } else return !Map.class.isAssignableFrom(type);
    }
}
