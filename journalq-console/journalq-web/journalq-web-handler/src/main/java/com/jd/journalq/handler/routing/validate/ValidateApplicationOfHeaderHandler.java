package com.jd.journalq.handler.routing.validate;

import com.jd.journalq.handler.error.ConfigException;
import com.jd.journalq.handler.error.ErrorCode;
import com.jd.journalq.model.domain.Application;
import com.jd.journalq.service.ApplicationService;
import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.parameter.Parameter;
import com.jd.laf.web.vertx.parameter.Parameters;
import io.vertx.ext.web.RoutingContext;

import static com.jd.journalq.handler.Constants.APPLICATION;

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
