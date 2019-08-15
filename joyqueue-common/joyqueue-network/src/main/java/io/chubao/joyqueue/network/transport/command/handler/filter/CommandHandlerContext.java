package io.chubao.joyqueue.network.transport.command.handler.filter;

import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * CommandHandlerContext
 *
 * author: gaohaoxiang
 * date: 2018/8/24
 */
public class CommandHandlerContext {

    private Map<Object, Object> context;

    public <T> T set(Object key, Object value) {
        return (T) getOrNewContext().put(key, value);
    }

    public <T> T get(Object key) {
        if (context == null) {
            return null;
        }
        return (T) context.get(key);
    }

    public <T> T remove(Object key) {
        if (context == null) {
            return null;
        }
        return (T) context.remove(key);
    }

    public Set<Object> keys() {
        if (context == null) {
            return Collections.emptySet();
        }
        return context.keySet();
    }

    protected Map<Object, Object> getOrNewContext() {
        if (context == null) {
            context = Maps.newHashMap();
        }
        return context;
    }
}