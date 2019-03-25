package com.jd.journalq.handler.binder;

import com.jd.journalq.handler.binder.annotation.GenericValue;
import com.jd.journalq.handler.util.GenericUtil;
import com.jd.laf.binding.binder.Binder;
import com.jd.laf.binding.reflect.FieldAccessorFactory;
import com.jd.laf.binding.reflect.exception.ReflectionException;
import org.apache.commons.lang.WordUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import static com.jd.laf.binding.reflect.Reflect.evaluate;
import static com.jd.laf.binding.reflect.Reflect.set;

/**
 * 泛型Value 绑定
 *       泛型字段，根据子类传入父类的泛型类名作为键查找值
 * Created by chenyanying3 on 2018-10-17.
 * Since jdk 1.8
 */
public class GenericValueBinder implements Binder {

    @Override
    public boolean bind(final Context context) throws ReflectionException {
        if (context == null) {
            return false;
        }
        GenericValue service = (GenericValue) context.getAnnotation();
        FieldAccessorFactory factory = context.getFactory();
        Object target = context.getTarget();
        Object source = context.getSource();
        //字段名
        Field field = context.getField();
        String name = service.value();

        if (name == null || name.isEmpty()) {
            //获取父类类型，例如：SupperCommand<M,S,Q>
            Type superClass = target.getClass().getGenericSuperclass();
            if (superClass instanceof ParameterizedType) {//父类泛型
                //判断字段是否是泛型
                if (field.getGenericType() instanceof TypeVariable) {//字段泛型
                    int index = GenericUtil.getFieldIndexInClassParam(field);
                    if (index == -1) {
                        return false;
                    }
                    //获取该泛型字段子类传入父类的实体类
                    Class clazz = (Class) ((ParameterizedType) superClass).getActualTypeArguments()[index];
                    //获取实体类名,首字母小写
                    name = WordUtils.uncapitalize(clazz.getSimpleName());

                } else {//字段非泛型
                    name = WordUtils.uncapitalize(field.getType().getSimpleName());
                }

            } else {//父类非泛型
                name = field.getName();
            }
        }
        //获取属性值
        Object result = evaluate(source, name, factory);
        if (!service.nullable() && result == null) {
            //判断不能为空
            return false;
        }
        return set(target, field, result, null, factory);
    }

    @Override
    public Class<?> annotation() {
        return GenericValue.class;
    }
}
