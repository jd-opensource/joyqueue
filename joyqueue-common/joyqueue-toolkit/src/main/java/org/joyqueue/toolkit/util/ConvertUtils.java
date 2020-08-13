package org.joyqueue.toolkit.util;

/**
 * ConvertUtils
 * author: gaohaoxiang
 * date: 2020/8/13
 */
public class ConvertUtils {

    public static <T> T convert(Object value, Class<T> type) {
        if (Integer.class.equals(type) || int.class.equals(type)) {
            return (T) Integer.valueOf(String.valueOf(value));
        } else if (Short.class.equals(type) || short.class.equals(type)) {
            return (T) Short.valueOf(String.valueOf(value));
        } else if (Byte.class.equals(type) || byte.class.equals(type)) {
            return (T) Byte.valueOf(String.valueOf(value));
        } else if (Float.class.equals(type) || float.class.equals(type)) {
            return (T) Float.valueOf(String.valueOf(value));
        } else if (Double.class.equals(type) || double.class.equals(type)) {
            return (T) Double.valueOf(String.valueOf(value));
        } else if (Long.class.equals(type) || long.class.equals(type)) {
            return (T) Long.valueOf(String.valueOf(value));
        } else if (Boolean.class.equals(type) || boolean.class.equals(type)) {
            return (T) Boolean.valueOf(String.valueOf(value));
        } else if (String.class.equals(type)) {
            return (T) String.valueOf(value);
        }
        throw new UnsupportedOperationException(type.getName());
    }
}