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

import org.joyqueue.toolkit.validate.annotation.NotNull;

import java.lang.annotation.Annotation;

/**
 * 验证不能为Null
 * Created by hexiaofeng on 16-5-10.
 */
public class NotNullValidator implements Validator {

    public static final NotNullValidator INSTANCE = new NotNullValidator();

    @Override
    public void validate(final Object target, final Annotation annotation, final Value value) throws ValidateException {
        NotNull notNull = (NotNull) annotation;
        if (value.value == null) {
            if (notNull.message() == null || notNull.message().isEmpty()) {
                throw new ValidateException(value.name + " can not be null.");
            }
            throw new ValidateException(String.format(notNull.message(), value.name));
        }
    }
}
