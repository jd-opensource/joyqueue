package org.joyqueue.plugin;

import com.jd.laf.extension.*;

import java.util.Collection;
import java.util.Iterator;

/**
 *
 * None Singleton Extension point regardless Extension interface
 *
 **/
public class DefaultExtensionPoint<T,M> implements ExtensionPoint<T,M> {

    private Class<T> clazz;
    private Collection<Plugin<T>> plugins;
    public DefaultExtensionPoint(Class<T> clazz){
        this.clazz=clazz;
        plugins= SpiLoader.INSTANCE.load(clazz);
    }
    @Override
    public T get(M name) {
        throw new UnsupportedOperationException("");
    }

    @Override
    public T get(M name, M option) {
        throw new UnsupportedOperationException("");
    }

    @Override
    public T getOrDefault(M name) {
        throw new UnsupportedOperationException("");
    }

    @Override
    public T get() {
        return plugins.iterator().hasNext()?instantiate(plugins.iterator().next().getName()):null;
    }

    @Override
    public int size() {
        return plugins.size();
    }

    @Override
    public Iterable<T> extensions() {
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
               Iterator<Plugin<T>> it=plugins.iterator();
                return new Iterator<T>() {
                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }
                    @Override
                    public T next() {
                       return Instantiation.ClazzInstance.INSTANCE.newInstance(it.next().getName());
                    }
                };
            }
        };
    }

    public T instantiate(Name<? extends T,String> name){
        return Instantiation.ClazzInstance.INSTANCE.newInstance(name);
    }

    @Override
    public Iterable<T> reverse() {
        throw  new UnsupportedOperationException("");
    }

    @Override
    public Iterable<ExtensionMeta<T, M>> metas() {
        throw  new UnsupportedOperationException("");
    }

    @Override
    public Iterable<ExtensionMeta<T, M>> metas(M name) {
        throw  new UnsupportedOperationException("");
    }

    @Override
    public ExtensionMeta<T, M> meta(M name) {
        throw  new UnsupportedOperationException("");
    }

    @Override
    public Name<T, String> getName() {
        throw  new UnsupportedOperationException("");
    }
}
