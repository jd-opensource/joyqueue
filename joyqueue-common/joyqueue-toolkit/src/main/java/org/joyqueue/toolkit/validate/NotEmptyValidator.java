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

import org.joyqueue.toolkit.reflect.Reflect;
import org.joyqueue.toolkit.validate.annotation.NotEmpty;

import java.lang.annotation.Annotation;

/**
 * 验证不能为空，支持字符序列，集合，散列，数组
 * Created by hexiaofeng on 16-5-10.
 */
public class NotEmptyValidator implements Validator {

    public static final NotEmptyValidator INSTANCE = new NotEmptyValidator();

    @Override
    public void validate(final Object target, final Annotation annotation, final Value value) throws ValidateException {
        NotEmpty notEmpty = (NotEmpty) annotation;
        int size = Reflect.size(value.type, value.value);
        if (size == 0) {
            if (notEmpty.message() == null || notEmpty.message().isEmpty()) {
                throw new ValidateException(String.format("%s must not be empty.", value.name));
            }
            throw new ValidateException(String.format(notEmpty.message(), value.name));
        }
    }
}
