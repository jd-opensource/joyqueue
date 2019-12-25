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
package org.joyqueue.handler.util;

import java.lang.reflect.Field;
import java.lang.reflect.TypeVariable;

/**
 * 泛型
 * Created by chenyanying3 on 2018-10-17.
 */
public class GenericUtil {

    /**
     * 根据字段查找字段在泛型类参数中序号
     * @param field
     * @return
     * @since jdk 1.8
     */
    public static int getFieldIndexInClassParam(Field field) {
        //获取泛型字段名，例如：M/S/Q
        String fieldTypeName = field.getGenericType().getTypeName();
        //获取父类泛型参数，例如：{M,S,Q}
        TypeVariable[] typeVariables = ((TypeVariable) field.getGenericType()).getGenericDeclaration().getTypeParameters();
        if (typeVariables == null || typeVariables.length < 1) {
            return -1;
        }
        //定位字段在父类参数的序号
        int index = -1;
        for (int i = 0; i < typeVariables.length; i++) {
            if (fieldTypeName.equals(typeVariables[i].getName())) {
                index = i;
                break;
            }
        }
        return index;
    }
}
