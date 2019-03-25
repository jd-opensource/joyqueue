package com.jd.journalq.handler.binder;

import com.jd.journalq.handler.Constants;
import com.jd.journalq.handler.JMQBinding;
import com.jd.journalq.handler.binder.annotation.ParamterValue;
import com.jd.laf.binding.binder.Binder;
import com.jd.laf.binding.reflect.FieldAccessorFactory;
import com.jd.laf.binding.reflect.exception.ReflectionException;
import io.vertx.core.MultiMap;
import io.vertx.ext.web.RoutingContext;

/**
 * 值绑定
 */
public class ParamterValueBinder implements Binder {

    @Override
    public boolean bind(final Context context) throws ReflectionException {
        if (context == null) {
            return false;
        }
        ParamterValue value = (ParamterValue) context.getAnnotation();
        FieldAccessorFactory factory = context.getFactory();
        Object target = context.getTarget();
        Object source = context.getSource();
        if (!(source instanceof RoutingContext)) {
            return false;
        }
        RoutingContext routingContext = (RoutingContext)source;
        JMQBinding.BindingMethod bindingMethod = (JMQBinding.BindingMethod) context.getTarget();
        int paramIndex = bindingMethod.getParamIndex();
        Object[] args = routingContext.get(Constants.args);
        //字段名
        String name = value.value();
        //获取属性值
        MultiMap params = ((RoutingContext) source).request().params();
        args[paramIndex] = params.get(name);
        return true;
    }

    @Override
    public Class<?> annotation() {
        return ParamterValue.class;
    }
}
