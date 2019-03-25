package com.jd.journalq.handler.binder;

import com.jd.journalq.handler.binder.annotation.Path;
import com.jd.laf.binding.binder.Binder;
import com.jd.laf.binding.reflect.Reflect;
import com.jd.laf.binding.reflect.exception.ReflectionException;
import io.vertx.ext.web.RoutingContext;

/**
 * @author wylixiaobin
 * Date: 2018/10/16
 */
public class PathBinder implements Binder {
    @Override
    public boolean bind(Context context) throws ReflectionException {
        if (context == null) {
            return false;
        }
        Object obj = context.getSource();
        if (!(obj instanceof RoutingContext)) {
            return false;
        }
        RoutingContext ctx = (RoutingContext) obj;
        return Reflect.set(context.getTarget(), context.getField(), ctx.normalisedPath(), null, context.getFactory());
    }

    @Override
    public Class<?> annotation() {
        return Path.class;
    }
}
