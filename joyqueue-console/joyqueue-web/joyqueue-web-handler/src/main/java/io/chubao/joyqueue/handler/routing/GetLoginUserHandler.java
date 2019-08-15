package io.chubao.joyqueue.handler.routing;

import io.chubao.joyqueue.handler.Constants;
import io.chubao.joyqueue.util.LocalSession;
import com.jd.laf.web.vertx.Command;
import com.jd.laf.web.vertx.RoutingHandler;
import com.jd.laf.web.vertx.response.Responses;
import io.vertx.ext.web.RoutingContext;

/**
 * 获取当前登录用户
 * Created by yangyang36 on 2018/9/18.
 */
public class GetLoginUserHandler implements RoutingHandler {

    @Override
    public void handle(final RoutingContext context) {
        context.put(Command.RESULT, Responses.success(context.get(Constants.USER_KEY)));
        LocalSession.getSession().setUser(context.get(Constants.USER_KEY));
        context.next();
    }

    @Override
    public String type() {
        return "getLoginUser";
    }
}
