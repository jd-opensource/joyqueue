package com.jd.journalq.handler;


import com.jd.laf.binding.binder.Binder;
import com.jd.laf.binding.binder.Binder.Context;
import com.jd.laf.binding.reflect.FieldAccessorFactory;
import com.jd.laf.binding.reflect.exception.ReflectionException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.jd.laf.binding.binder.Binders.getPlugin;

/**
 * 绑定器
 * Created by hexiaofeng on 16-8-29.
 */
public class JMQBinding {

    // 缓存类的绑定关系
    protected static ConcurrentMap<String, List<BindingMethod>> bindingMethods = new ConcurrentHashMap();

    /**
     * 绑定上下文
     *
     * @param source  上下文
     * @param target  对象
     * @throws ReflectionException
     */
    public static void bindMethod(final Object source, final Object target,final Method method,String path) throws ReflectionException {
        if (source == null || target == null) {
            return;
        }
        // 从缓存中获取
        List<BindingMethod> bindings = bindingMethods.get(target.getClass().getName()+path);
        if (bindings == null) {
            // 没有找到则从注解中查找
            bindings = new ArrayList<BindingMethod>();
            Annotation[][] annotations;
            BindingMethod bindingMethod;
            Binder binder;
            annotations = method.getParameterAnnotations();
            for (int i = 0;i<annotations.length;i++) {
                bindingMethod = null;
                    //是否是绑定注解
                Annotation annotation = annotations[i][0];
                    binder = getPlugin(annotation.annotationType());
                    if (binder != null) {
                        if (bindingMethod == null) {
                            bindingMethod = new BindingMethod(method,i,target);
                        }
                        bindingMethod.add(new BinderAnnotation(annotation, binder));
                    }
                //有绑定注解
                if (bindingMethod != null) {
                    bindings.add(bindingMethod);
                }
            }
            List<BindingMethod> exists = bindingMethods.putIfAbsent(target.getClass().getName()+path, bindings);
            if (exists != null) {
                bindings = exists;
            }
        }
        for (BindingMethod binding : bindings) {
            binding.bind(source, binding, null);
        }
    }
    /**
     * 绑定字段
     */
    public static class BindingMethod{
        //字段
        final protected Method method;
        final int paramIndex;
        final protected Object tagert;
        //绑定实现
        final protected List<BinderAnnotation> annotations = new ArrayList<BinderAnnotation>(2);

        public BindingMethod(Method method,int paramIndex,Object tagert) {
            this.method = method;
            this.paramIndex = paramIndex;
            this.tagert = tagert;
        }

        public Method getMethod() {
            return method;
        }

        public int getParamIndex() {
            return paramIndex;
        }

        public Object getTagert() {
            return tagert;
        }

        public List<BinderAnnotation> getAnnotations() {
            return annotations;
        }

        public void add(final BinderAnnotation annotation) {
            if (annotation != null) {
                annotations.add(annotation);
            }
        }

        /**
         * 绑定
         *
         * @param source
         * @param target
         * @param factory
         * @throws ReflectionException
         */
        public void bind(final Object source, final Object target, final FieldAccessorFactory factory) throws ReflectionException {
            Context context;
            for (BinderAnnotation annotation : annotations) {
                context = new Context(target, null, annotation.annotation, factory, source);
                if (annotation.binder.bind(context)) {
                    return;
                }
            }

        }
    }
    /**
     * 注解绑定器
     */
    protected static class BinderAnnotation {
        final protected Annotation annotation;
        final protected Binder binder;

        public BinderAnnotation(Annotation annotation, Binder binder) {
            this.annotation = annotation;
            this.binder = binder;
        }

        public Annotation getAnnotation() {
            return annotation;
        }

        public Binder getBinder() {
            return binder;
        }
    }

}
