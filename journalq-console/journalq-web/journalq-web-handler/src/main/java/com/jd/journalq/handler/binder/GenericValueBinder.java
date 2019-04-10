package com.jd.journalq.handler.binder;

import com.jd.journalq.handler.annotation.GenericValue;
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
