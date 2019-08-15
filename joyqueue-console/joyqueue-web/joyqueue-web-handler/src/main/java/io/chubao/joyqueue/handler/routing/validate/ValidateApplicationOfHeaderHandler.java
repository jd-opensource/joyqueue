package io.chubao.joyqueue.handler.routing.validate;

import io.chubao.joyqueue.handler.error.ConfigException;
import io.chubao.joyqueue.handler.error.ErrorCode;
import io.chubao.joyqueue.model.domain.Application;
import io.chubao.joyqueue.service.ApplicationService;
import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.parameter.Parameter;
import com.jd.laf.web.vertx.parameter.Parameters;
import io.vertx.ext.web.RoutingContext;

import static io.chubao.joyqueue.handler.Constants.APPLICATION;

/**
 * Created by yangyang36 on 2018/9/17.
 */
public class ValidateApplicationOfHeaderHandler extends ValidateHandler {

    @Value
    protected ApplicationService applicationService;

    @Override
    protected void validate(RoutingContext context, Parameters.RequestParameter parameter) {
        Parameter header = parameter.header();
        String appCode = header.getString(APPLICATION);
        if (appCode == null || appCode.isEmpty()) {
            throw new ConfigException(ErrorCode.BadRequest, "请求头没有应用代码");
        }
        Application application = applicationService.findByCode(appCode);
        if (application == null) {
            throw new ConfigException(ErrorCode.ApplicationNotExists);
        }
        context.put(APPLICATION, application);
    }

    @Override
    public String type() {
        return "validateAppOfHeader";
    }
}
