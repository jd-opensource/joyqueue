package org.joyqueue.plugin;

import com.jd.laf.extension.*;

import java.lang.reflect.Field;
import java.util.Comparator;

/**
 *
 * Forcing close class singleton
 *
 **/
public class ExtensionPointLazyExt<T,M> extends ExtensionPointLazy<T,M> {


    public ExtensionPointLazyExt(Class<T> extensible){
        this(extensible,null,null,null);
    }
    public ExtensionPointLazyExt(Class<T> extensible, ExtensionLoader loader, Comparator<ExtensionMeta<T, M>> comparator,
                              Classify<T, M> classify) {
        super(extensible,loader,comparator,classify);
    }

    @Override
    protected ExtensionPoint<T, M> getDelegate() {
        ExtensionPoint<T,M> ep= super.getDelegate();
        if(ep instanceof ExtensionSpi){
           ExtensionSpi<T,M> espi=(ExtensionSpi)ep;
           if(forceCloseSingleton()&&SingletonController.forceClosingSingletonClass.contains(extensible.getName())){
              Iterable<ExtensionMeta<T,M>> it=  espi.metas();
              it.forEach(e->{
                   e.setSingleton(false);
              });
               try {
                   Field field = espi.getClass().getDeclaredField("singleton");
                   field.setAccessible(true);
                   field.setBoolean(espi,false);
                   System.out.println(field);
               }catch (Exception e){
                   throw new IllegalStateException("modify singleton field exception");
               }
           }

        }
        return ep;
    }

    /**
     *
     * Force close plugin singleton env variable
     *
     *
     **/
    public boolean forceCloseSingleton(){
       String b= System.getenv("force.close.plugin.singleton");
       if(b!=null){
         return   Boolean.valueOf(b);
       }
       return false;
    }
}
