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
package org.joyqueue.toolkit.reflect;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by hexiaofeng on 16-5-9.
 */
public class Reflect {

    private static ConcurrentMap<Field, FieldAccessor> accessors = new ConcurrentHashMap<Field, FieldAccessor>();

    /**
     * 获取自动访问器
     *
     * @param field 字段
     * @return 访问器
     */
    public static FieldAccessor getAccessor(Field field) {
        if (field == null) {
            return null;
        }
        FieldAccessor accessor = accessors.get(field);
        if (accessor == null) {
            accessor = new FieldAccessor(field);
            FieldAccessor exists = accessors.putIfAbsent(field, accessor);
            if (exists != null) {
                accessor = exists;
            }
        }
        return accessor;
    }

    /**
     * 设置字段值
     *
     * @param field  字段
     * @param target 目标对象
     * @param value  值
     * @throws ReflectException
     */
    public static void set(final Field field, final Object target, final Object value) throws ReflectException {
        if (field == null || target == null) {
            return;
        }
        FieldAccessor accessor = getAccessor(field);
        if (accessor != null) {
            accessor.set(target, value);
        }
    }

    /**
     * 获取值
     *
     * @param field  字段
     * @param target 对象
     * @return 字段值
     * @throws ReflectException
     */
    public static Object get(final Field field, final Object target) throws ReflectException {
        if (field == null || target == null) {
            return null;
        }
        FieldAccessor accessor = getAccessor(field);
        if (accessor != null) {
            return accessor.get(target);
        }
        return null;
    }

    /**
     * 获取对象的大小，支持字符序列，集合，散列，数组
     *
     * @param type 类型
     * @param value 字段值
     * @return 大小
     */
    public static int size(final Class<?> type, final Object value) {
        if (type == null || value == null) {
            return 0;
        }
        int size = 0;
        if (type.isArray()) {
            size = Array.getLength(value);
        } else if (CharSequence.class.isAssignableFrom(type)) {
            size = ((CharSequence) value).length();
        } else if (Collection.class.isAssignableFrom(type)) {
            size = ((Collection) value).size();
        } else if (Map.class.isAssignableFrom(type)) {
            size = ((Map) value).size();
        }
        return size;
    }

    /**
     * 字段访问器
     */
    public static class FieldAccessor {
        // 字段
        private Field field;
        // 获取方法
        private Method getter;
        // 设置方法
        private Method setter;

        public FieldAccessor(Field field) {
            if (field == null) {
                throw new IllegalArgumentException("field can not be null.");
            }
            this.field = field;
            Class<?> clazz = field.getDeclaringClass();
            if (clazz != null) {
                String name = field.getName();
                char[] data = name.toCharArray();
                data[0] = Character.toUpperCase(data[0]);
                name = new String(data);
                String getName = "get" + name;
                String getBoolName = "is" + name;
                String setName = "set" + name;
                Class<?>[] types;
                // 获取GETTER方法
                Method[] methods = clazz.getDeclaredMethods();
                for (Method method : methods) {
                    if (Modifier.isPublic(method.getModifiers())) {
                        // 方法的名称会放入JVM的常量池里面
                        name = method.getName();
                        if (name.equals(getName) || name.equals(getBoolName)) {
                            types = method.getParameterTypes();
                            if (types == null || types.length == 0) {
                                getter = method;
                            }
                        } else if (name.equals(setName)) {
                            types = method.getParameterTypes();
                            if (types != null && types.length == 1 && types[0] == field.getType()) {
                                setter = method;
                            }
                        }

                    }
                }
            }
        }

        public Field getField() {
            return field;
        }

        public Method getGetter() {
            return getter;
        }

        public Method getSetter() {
            return setter;
        }

        /**
         * 获取字段值
         *
         * @param target 对象
         * @return 字段值
         * @throws ReflectException
         */
        public Object get(final Object target) throws ReflectException {
            try {
                if (target == null) {
                    return null;
                } else if (getter != null) {
                    return getter.invoke(target);
                } else if (field.isAccessible()) {
                    return field.get(target);
                } else {
                    field.setAccessible(true);
                    try {
                        return field.get(target);
                    } finally {
                        field.setAccessible(false);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new ReflectException(e.getMessage(), e);
            } catch (InvocationTargetException e) {
                throw new ReflectException(e.getMessage(), e.getCause() != null ? e.getCause() : e);
            }
        }

        /**
         * 设置字段值
         *
         * @param target 对象
         * @param value  字段值
         */
        public void set(final Object target, final Object value) throws ReflectException {
            try {
                if (target == null) {
                    return;
                } else if (setter != null) {
                    setter.invoke(target, value);
                } else if (field.isAccessible()) {
                    field.set(target, value);
                } else {
                    field.setAccessible(true);
                    try {
                        field.set(target, value);
                    } finally {
                        field.setAccessible(false);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new ReflectException(e.getMessage(), e);
            } catch (InvocationTargetException e) {
                throw new ReflectException(e.getMessage(), e.getCause() != null ? e.getCause() : e);
            }
        }
    }
}
