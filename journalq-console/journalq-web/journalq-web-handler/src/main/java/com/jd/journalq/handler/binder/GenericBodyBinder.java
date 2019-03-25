package com.jd.journalq.handler.binder;

import com.jd.journalq.handler.Constants;
import com.jd.journalq.handler.JMQBinding;
import com.jd.journalq.handler.binder.annotation.GenericBody;
import com.jd.laf.binding.binder.Binder;
import com.jd.laf.binding.marshaller.JsonProviders;
import com.jd.laf.binding.marshaller.XmlProviders;
import com.jd.laf.binding.reflect.exception.ReflectionException;
import io.vertx.ext.web.RoutingContext;

import java.io.ByteArrayInputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Properties;

/**
 * 泛型Body 绑定
 * @author wylixiaobin
 * Date: 2018/10/17
 * Since jdk 1.8
 */
public class GenericBodyBinder implements Binder {
    @Override
    public boolean bind(final Context context) throws ReflectionException {
        GenericBody annotation = (GenericBody) context.getAnnotation();
        Object source = context.getSource();
        Object target = context.getTarget();
        if (!(source instanceof RoutingContext)) {
            return false;
        }
        RoutingContext ctx = (RoutingContext) source;
        GenericBody.BodyType type = annotation.type();
        JMQBinding.BindingMethod bindingMethod = (JMQBinding.BindingMethod) context.getTarget();
        int paramIndex = bindingMethod.getParamIndex();
        int typeIndex = annotation.typeindex();
        Object[] args = ctx.get(Constants.args);
        //获取字段类型
        Class classType = null;
        //获取父类类型，例如：SupperCommand<M,S,Q>
        Type superClass = bindingMethod.getTagert().getClass().getGenericSuperclass();
        if (superClass instanceof ParameterizedType) {//父类泛型，并且字段泛型
            //获取该泛型字段子类传入父类的实体类
            classType = (Class) ((ParameterizedType) superClass).getActualTypeArguments()[typeIndex];
        }
        if (type ==  GenericBody.BodyType.DETECT) {
            String contentType = ctx.getAcceptableContentType();
            if (contentType != null) {
                contentType = contentType.toLowerCase();
                if (contentType.indexOf("json") >= 0) {
                    type =  GenericBody.BodyType.JSON;
                } else if (contentType.indexOf("xml") >= 0) {
                    type =  GenericBody.BodyType.XML;
                } else if (contentType.indexOf("properties") >= 0) {
                    type =  GenericBody.BodyType.PROPERTIES;
                } else {
                    type =  GenericBody.BodyType.TEXT;
                }
            } else {
                type =  GenericBody.BodyType.JSON;
            }
        }

        try {
            switch (type) {
                case JSON:
                    args[paramIndex] = JsonProviders.getPlugin().getUnmarshaller().unmarshall(
                            ctx.getBodyAsString(), classType, null);
                    break;
                case XML:
                    args[paramIndex] = XmlProviders.getPlugin().getUnmarshaller().unmarshall(
                            ctx.getBodyAsString(), classType, null);
                    break;
                case PROPERTIES:
                    byte[] data = ctx.getBody().getBytes();
                    Properties properties = new Properties();
                    properties.load(new ByteArrayInputStream(data));
                    args[paramIndex] = properties;
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
        return GenericBody.class;
    }
}
