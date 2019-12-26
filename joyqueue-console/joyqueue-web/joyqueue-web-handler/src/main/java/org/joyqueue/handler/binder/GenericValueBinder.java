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
package org.joyqueue.handler.binder;

import org.joyqueue.handler.annotation.GenericValue;
import com.jd.laf.binding.binder.Binder;
import com.jd.laf.binding.reflect.exception.ReflectionException;
import org.apache.commons.lang.WordUtils;

/**
 * Generic value binder implement
 *      Generic value find by the generic class name passed to super class
 * Created by chenyanying3 on 19-3-13.
 * Since jdk 1.8
 */
public class GenericValueBinder implements Binder {

    @Override
    public boolean bind(final Context context) throws ReflectionException {
        if (context == null) {
            return false;
        }
        GenericValue value = (GenericValue) context.getAnnotation();
        //获取属性值
        Object result = context.evaluate(WordUtils.uncapitalize(context.getScope().getGenericType().getSimpleName()));
        if (!value.nullable() && result == null) {
            //判断不能为空
            return false;
        }
        return context.bind(result);
    }

    @Override
    public Class<?> annotation() {
        return GenericValue.class;
    }
}
