package io.chubao.joyqueue.handler.routing.validate;

import com.jd.laf.web.vertx.RoutingHandler;
import com.jd.laf.web.vertx.parameter.Parameters;
import com.jd.laf.web.vertx.parameter.Parameters.RequestParameter;
import io.vertx.ext.web.RoutingContext;

/**
 * 验证基础类
 */
public abstract class ValidateHandler implements RoutingHandler {

    @Override
    public void handle(final RoutingContext context) {
        validate(context, Parameters.get(context.request()));
        context.next();
    }

    /**
     * 验证
     *
     * @param context   上下文
     * @param parameter 请求参数
     */
    protected abstract void validate(RoutingContext context, RequestParameter parameter);
}