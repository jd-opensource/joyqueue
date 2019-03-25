package com.jd.journalq.handler.binder;

import com.jd.journalq.handler.Constants;
import com.jd.journalq.handler.JMQBinding;
import com.jd.journalq.handler.binder.annotation.Body;
import com.jd.laf.binding.binder.Binder;
import com.jd.laf.binding.marshaller.JsonProviders;
import com.jd.laf.binding.reflect.exception.ReflectionException;
import io.vertx.ext.web.RoutingContext;

import java.lang.reflect.Method;

/**
 * 泛型Body 绑定
 * @author wylixiaobin
 * Date: 2018/10/17
 * Since jdk 1.8
 */
public class BodyBinder implements Binder {
    @Override
    public boolean bind(final Context context) throws ReflectionException {
        Body annotation = (Body) context.getAnnotation();
        Object source = context.getSource();
        if (!(source instanceof RoutingContext)) {
            return false;
        }
        RoutingContext ctx = (RoutingContext) source;
        BodyType type = annotation.type();
        JMQBinding.BindingMethod bindingMethod = (JMQBinding.BindingMethod) context.getTarget();
        int paramIndex = bindingMethod.getParamIndex();
        Object[] args = ctx.get(Constants.args);
        //获取字段类型
        Method m=bindingMethod.getMethod();
        Class classType =m.getParameterTypes()[paramIndex];
        if (type == BodyType.DETECT) {
            String contentType = ctx.getAcceptableContentType();
            if (contentType != null) {
                contentType = contentType.toLowerCase();
                if (contentType.indexOf("json") >= 0) {
                    type = BodyType.JSON;
                }
            } else {
                type = BodyType.JSON;
            }
        }
        try {
            switch (type) {
                case JSON:
                    args[paramIndex] = JsonProviders.getPlugin().getUnmarshaller().unmarshall(
                            ctx.getBodyAsString(), classType, null);
                    break;
                default:
                    args[paramIndex] = ctx.getBody();
                    break;
            }
        } catch (ReflectionException e) {
            throw e;
        } catch (Exception e) {
            throw new ReflectionException(e.getMessage(), e);
        }
        return true;
    }

    @Override
    public Class<?> annotation() {
        return Body.class;
    }
}
