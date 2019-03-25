package com.jd.journalq.handler.binder;

import com.jd.journalq.handler.binder.annotation.Operator;
import com.jd.journalq.handler.Constants;
import com.jd.laf.binding.binder.Binder;
import com.jd.laf.binding.reflect.exception.ReflectionException;
import com.jd.journalq.model.domain.Application;
import com.jd.journalq.model.domain.Identity;
import com.jd.journalq.model.domain.User;
import io.vertx.ext.web.RoutingContext;

import static com.jd.laf.binding.reflect.Reflect.set;

/**
 * 操作人员绑定
 */
public class OperatorBinder implements Binder {

    @Override
    public boolean bind(final Context context) throws ReflectionException {
        if (context == null) {
            return false;
        }
        Object obj = context.getSource();
        if (!(obj instanceof RoutingContext)) {
            return false;
        }
        RoutingContext ctx = (RoutingContext) obj;
        Application application = ctx.get(Constants.APPLICATION);
        User session = ctx.get(Constants.USER_KEY);
        Identity identity = session != null ? new Identity(session) : (application != null ? application.getOwner() : null);
        return set(context.getTarget(), context.getField(), identity, null, context.getFactory());
    }

    @Override
    public Class<?> annotation() {
        return Operator.class;
    }
}
