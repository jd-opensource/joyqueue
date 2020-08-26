package org.joyqueue.plugin;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Singleton controller
 **/
public class SingletonController {
    protected static Set<String> forceClosingSingletonClass=new HashSet<>();

    public static  void closeClassSingleton(Class clazz){
        forceClosingSingletonClass.add(clazz.getName());
    }
    public static void forceCloseSingleton() throws Exception{
        Map<String,String> env=System.getenv();
        Field field = env.getClass().getDeclaredField("m");
        field.setAccessible(true);
        ((Map<String, String>) field.get(env)).put("force.close.plugin.singleton","TRUE");
    }
}
