package com.jd.journalq.handler.binder;


import com.alibaba.fastjson.JSONObject;
import com.jd.journalq.handler.Constants;
import com.jd.journalq.handler.JMQBinding;
import com.jd.journalq.handler.binder.annotation.Page;
import com.jd.journalq.common.model.Pagination;
import com.jd.journalq.common.model.QPageQuery;
import com.jd.journalq.common.model.Query;
import com.jd.laf.binding.binder.Binder;
import com.jd.laf.binding.marshaller.JsonProviders;
import com.jd.laf.binding.marshaller.Unmarshaller;
import com.jd.laf.binding.reflect.exception.ReflectionException;
import io.vertx.ext.web.RoutingContext;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;



/**
 * 操作人员绑定
 */
public class PageBinder implements Binder {
    private static final String pagination = "pagination";
    private static final String query = "query";
    @Override
    public boolean bind(final Context context) throws ReflectionException {
        if (context == null) {
            return false;
        }
        Page annotation = (Page) context.getAnnotation();
        Object source = context.getSource();
        if (!(source instanceof RoutingContext)) {
            return false;
        }
        RoutingContext routingContext = (RoutingContext)source;
        JMQBinding.BindingMethod bindingMethod = (JMQBinding.BindingMethod) context.getTarget();
        Object[] args = routingContext.get(Constants.args);
        QPageQuery pageQuery = new QPageQuery();
        int typeindex = annotation.typeindex();
        //获取父类类型，例如：SupperCommand<M,S,Q>
        Type superClass = bindingMethod.getTagert().getClass().getGenericSuperclass();
        Class type = null;
        if (superClass instanceof ParameterizedType) {//父类泛型，并且字段泛型
            //获取该泛型字段子类传入父类的实体类
            type = (Class) ((ParameterizedType) superClass).getActualTypeArguments()[typeindex];
        }
        //获取不到子类的泛型
        if (type == null) {
            Type type1 = bindingMethod.getMethod().getGenericParameterTypes()[typeindex];
            //目前只支持获取一个泛型
            type = (Class) ((ParameterizedTypeImpl) type1).getActualTypeArguments()[0];
        }
        try {
            Unmarshaller unmarshaller = JsonProviders.getPlugin().getUnmarshaller();
            JSONObject requestBody = JSONObject.parseObject(routingContext.getBodyAsString());
            if (requestBody == null) {
                pageQuery.setPagination(new Pagination(0,10));
            } else {
                pageQuery.setPagination(unmarshaller.unmarshall(requestBody.getString(pagination), Pagination.class, null));
                pageQuery.setQuery((Query)unmarshaller.unmarshall(
                        requestBody.getString(query), type, null));
            }
            args[bindingMethod.getParamIndex()] = pageQuery;
        } catch (ReflectionException e) {
            throw e;
        } catch (Exception e) {
            throw new ReflectionException(e.getMessage(), e);
        }
        return true;
    }

    @Override
    public Class<?> annotation() {
        return Page.class;
    }
}
