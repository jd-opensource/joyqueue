package io.chubao.joyqueue.broker.manage.util;

import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * ClassUtils
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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