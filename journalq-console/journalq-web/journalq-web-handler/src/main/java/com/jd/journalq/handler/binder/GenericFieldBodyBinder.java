package com.jd.journalq.handler.binder;

import com.jd.journalq.handler.binder.annotation.GenericFieldBody;
import com.jd.journalq.handler.util.GenericUtil;
import com.jd.laf.binding.binder.Binder;
import com.jd.laf.binding.marshaller.JsonProviders;
import com.jd.laf.binding.marshaller.XmlProviders;
import com.jd.laf.binding.reflect.exception.ReflectionException;
import io.vertx.ext.web.RoutingContext;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Properties;

/**
 * 泛型Body 绑定
 * Created by chenyanying3 on 2018-10-17.
 * Since jdk 1.8
 */
public class GenericFieldBodyBinder implements Binder {
    @Override
    public boolean bind(final Context context) throws ReflectionException {
        GenericFieldBody annotation = (GenericFieldBody) context.getAnnotation();
        Field field = context.getField();
        Object source = context.getSource();
        Object target = context.getTarget();
        if (!(source instanceof RoutingContext)) {
            return false;
        }
        RoutingContext ctx = (RoutingContext) source;
        GenericFieldBody.BodyType type = annotation.type();

        //获取字段类型
        Class fieldType = field.getType();
        //获取父类类型，例如：SupperCommand<M,S,Q>
        Type superClass = target.getClass().getGenericSuperclass();
        if (superClass instanceof ParameterizedType && field.getGenericType() instanceof TypeVariable) {//父类泛型，并且字段泛型
            //判断字段是否是泛型
            int index = GenericUtil.getFieldIndexInClassParam(field);
            if (index == -1) {
                return false;
            }
            //获取该泛型字段子类传入父类的实体类
            fieldType = (Class) ((ParameterizedType) superClass).getActualTypeArguments()[index];
        }

        if (type == GenericFieldBody.BodyType.DETECT) {
            String contentType = ctx.getAcceptableContentType();
            if (contentType != null) {
                contentType = contentType.toLowerCase();
                if (contentType.indexOf("json") >= 0) {
                    type = GenericFieldBody.BodyType.JSON;
                } else if (contentType.indexOf("xml") >= 0) {
                    type = GenericFieldBody.BodyType.XML;
                } else if (contentType.indexOf("properties") >= 0) {
                    type = GenericFieldBody.BodyType.PROPERTIES;
                } else {
                    type = GenericFieldBody.BodyType.TEXT;
                }
            } else {
                type = GenericFieldBody.BodyType.JSON;
            }
        }

        try {
            switch (type) {
                case JSON:
                    return context.bind(JsonProviders.getPlugin().getUnmarshaller().unmarshall(
                            ctx.getBodyAsString(), fieldType, null));
                case XML:
                    return context.bind(XmlProviders.getPlugin().getUnmarshaller().unmarshall(
                            ctx.getBodyAsString(), fieldType, null));
                case PROPERTIES:
                    byte[] data = ctx.getBody().getBytes();
                    Properties properties = new Properties();
                    properties.load(new ByteArrayInputStream(data));
                    return context.bind(properties);
                default:
                    return context.bind(ctx.getBody());
            }
        } catch (ReflectionException e) {
            throw e;
        } catch (Exception e) {
            throw new ReflectionException(e.getMessage(), e);
        }
    }

    @Override
    public Class<?> annotation() {
        return GenericFieldBody.class;
    }
}
