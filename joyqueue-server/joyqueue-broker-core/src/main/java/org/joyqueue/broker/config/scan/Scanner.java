package org.joyqueue.broker.config.scan;

import java.util.Set;
import java.util.function.Predicate;

public interface Scanner {

    String CLASS_SUFFIX = ".class";

    Set<Class<?>> search(String packageName, Predicate<Class<?>> predicate);

    default Set<Class<?>> search(String packageName){
        return search(packageName,null);
    }

}