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

import org.joyqueue.toolkit.lang.Getter;
import org.joyqueue.toolkit.lang.Getters;
import org.joyqueue.toolkit.reflect.ReflectException;
import org.joyqueue.toolkit.validate.annotation.NotEmpty;
import org.joyqueue.toolkit.validate.annotation.NotNull;
import org.joyqueue.toolkit.validate.annotation.Range;
import org.joyqueue.toolkit.validate.annotation.Size;
import org.joyqueue.toolkit.validate.annotation.Pattern;
import org.joyqueue.toolkit.validate.annotation.Valid;
import org.joyqueue.toolkit.validate.annotation.DoubleRange;

import javax.xml.bind.ValidationException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 验证
 * Created by hexiaofeng on 15-7-20.
 */
public class Validators {
    // 验证注解
    private static ConcurrentMap<Method, MethodValidation> validations =
            new ConcurrentHashMap<Method, MethodValidation>();

    /**
     * 绑定上下文
     *
     * @param target 对象
     * @throws ReflectException
     */
    public static void validate(final Object target) throws ValidateException {
        if (target == null) {
            return;
        }
        Class<?> clazz = target.getClass();
        Field[] fields;
        Annotation[] annotations;
        Validator validator;
        Getter getter;
        // 遍历类及其父类
        while (clazz != null && clazz != Object.class) {
            // 获取字段
            fields = clazz.getDeclaredFields();
            // 遍历字段
            for (Field field : fields) {
                // 可能存在多个验证声明，只需要获取一次值
                getter = null;
                // 获取声明
                annotations = field.getDeclaredAnnotations();
                if (annotations != null) {
                    // 遍历声明
                    for (Annotation annotation : annotations) {
                        validator = getValidator(annotation);
                        // 判断是否能验证
                        if (validator != null) {
                            // 获取该字段的值
                            if (getter == null) {
                                getter = Getters.field(field, target, true);
                            }
                            // 验证
                            try {
                                validator.validate(target, annotation,
                                        new Validator.Value(field.getName(), field.getType(), getter.get()));
                            } catch (ReflectException e) {
                                throw new ValidateException(e.getMessage(), e);
                            }
                        }
                    }
                }
            }
            // 父类
            clazz = clazz.getSuperclass();
        }
    }

    /**
     * 获取验证器
     *
     * @param annotation 声明
     * @return 验证器
     */
    protected static Validator getValidator(final Annotation annotation) {
        Validator validator = null;
        // 获取验证器
        if (annotation instanceof NotNull) {
            validator = NotNullValidator.INSTANCE;
        } else if (annotation instanceof NotEmpty) {
            validator = NotEmptyValidator.INSTANCE;
        } else if (annotation instanceof Size) {
            validator = SizeValidator.INSTANCE;
        } else if (annotation instanceof Range) {
            validator = RangeValidator.INSTANCE;
        } else if (annotation instanceof DoubleRange) {
            validator = DoubleRangeValidator.INSTANCE;
        } else if (annotation instanceof Pattern) {
            validator = PatternValidator.INSTANCE;
        } else if (annotation instanceof Valid) {
            validator = ValidValidator.INSTANCE;
        }
        return validator;
    }


    /**
     * 认证
     *
     * @param method    方法
     * @param target    目标对象
     * @param arguments 参数
     * @throws ValidationException
     */
    public static void validate(final Method method, final Object target, final Object... arguments) throws
            ValidationException {
        if (method == null || target == null || arguments == null || arguments.length == 0) {
            return;
        }
        // 获取方法验证
        MethodValidation valid = getMethodValidation(method, target);
        if (valid != null && valid.validation != null) {
            Validator validator;
            // 遍历参数
            for (int i = 0; i < valid.types.length; i++) {
                if (valid.annotations[i] != null) {
                    // 遍历声明
                    for (Annotation annotation : valid.annotations[i]) {
                        // 获取验证器
                        validator = getValidator(annotation);
                        // 判断是否能验证
                        if (validator != null) {
                            // 验证
                            validator.validate(target, annotation,
                                    new Validator.Value(valid.names[i], valid.types[i], arguments[i]));
                        }
                    }
                }
            }
        }
    }


    /**
     * 获取方法验证
     *
     * @param method 方法
     * @param target 目标对象
     * @return 方法验证
     */
    protected static MethodValidation getMethodValidation(final Method method, final Object target) {
        MethodValidation cache = validations.get(method);
        if (cache == null) {
            // 找到方法验证配置
            Valid validation = method.getAnnotation(Valid.class);
            // 没有找到方法验证,是抽象方法,尝试从实现类查找方法
            if (validation == null && Modifier.isAbstract(method.getModifiers())) {
                try {
                    Class<?> aClass = target.getClass();
                    Method implMethod = aClass.getMethod(method.getName(), method.getParameterTypes());
                    validation = implMethod.getAnnotation(Valid.class);
                } catch (NoSuchMethodException e) {
                } catch (SecurityException e) {
                }
            }
            cache = new MethodValidation(validation, method.getParameterAnnotations(), method.getParameterTypes());
            validations.putIfAbsent(method, cache);
        }
        return cache;
    }

    /**
     * 方法验证信息
     */
    protected static class MethodValidation {
        protected Valid validation;
        protected Annotation[][] annotations;
        protected Class<?>[] types;
        protected String[] names;

        public MethodValidation(final Valid validation, final Annotation[][] annotations, final Class<?>[] types) {
            this.validation = validation;
            this.annotations = annotations;
            this.types = types;
            this.names = new String[types.length];
            for (int i = 0; i < names.length; i++) {
                names[i] = "arg" + i;
            }
        }

        public MethodValidation(final Valid validation, final Annotation[][] annotations, final Class<?>[] types,
                                final String[] names) {
            this.validation = validation;
            this.annotations = annotations;
            this.types = types;
            this.names = names;
        }
    }

}
