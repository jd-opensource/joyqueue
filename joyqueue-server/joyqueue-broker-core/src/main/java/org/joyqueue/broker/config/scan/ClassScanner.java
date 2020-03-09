package org.joyqueue.broker.config.scan;

import org.joyqueue.toolkit.config.PropertyDef;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;

public class ClassScanner {

    public static Set<Class<?>> search(String packageName){
        return search(packageName,null);
    }

    public static Set<Class<?>> search(String packageName, Predicate<Class<?>> predicate){
        return ScannerExecutor.getInstance().search(packageName,predicate);
    }


    public static Map<String, String> getEnumConstantsConfig() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Map<String, String> configMap = new HashMap<>(10);
        Set<Class<?>> classes = ClassScanner.search("org.joyqueue");
        for (Class<?> clazz : classes) {
            List<Class<?>> impls = Arrays.asList(clazz.getInterfaces());
            if (impls.contains(PropertyDef.class) && clazz.isEnum()) {
                Method method = clazz.getMethod("values");
                if (method.getReturnType().isArray()) {
                    Object[] values = (Object[]) method.invoke(null);
                    for (Object obj : values) {
                        if (obj instanceof PropertyDef) {
                            PropertyDef propertyDef = (PropertyDef) obj;
                            configMap.put(propertyDef.getName(), String.valueOf(propertyDef.getValue()));
                        }
                    }
                }
            }
        }
        return configMap;
    }

}