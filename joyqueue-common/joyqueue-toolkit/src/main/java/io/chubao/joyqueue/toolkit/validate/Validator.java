package io.chubao.joyqueue.toolkit.validate;

import java.lang.annotation.Annotation;

/**
 * Created by hexiaofeng on 16-5-10.
 */
public interface Validator {

    /**
     * 认证
     *
     * @param target     目标对象
     * @param annotation 验证声明
     * @param value      字段值
     * @throws ValidateException
     */
    void validate(Object target, Annotation annotation, Value value) throws ValidateException;

    class Value {
        // 名称
        public String name;
        // 值类型
        public Class<?> type;
        // 值
        public Object value;

        public Value(final String name, final Class<?> type, final Object value) {
            this.name = name;
            this.type = type;
            this.value = value;
        }

    }
}
