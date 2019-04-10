package com.jd.journalq.handler.routing.validate;

import com.jd.journalq.handler.error.ConfigException;
import com.jd.journalq.handler.error.ErrorCode;
import com.jd.journalq.model.domain.Application;
import com.jd.journalq.model.domain.ApplicationToken;
import com.jd.journalq.service.ApplicationService;
import com.jd.journalq.service.ApplicationTokenService;
import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.parameter.Parameter;
import com.jd.laf.web.vertx.parameter.Parameters.RequestParameter;
import io.vertx.ext.web.RoutingContext;

import java.util.Date;

import static com.jd.journalq.handler.Constants.APPLICATION;
import static com.jd.journalq.handler.Constants.TOKEN;

/**
 * 应用身份认证
 * Created by chenyanying3 on 19-3-3.
 */
public class ValidateAppTokenHandler extends ValidateHandler {

    @Value
    protected ApplicationService applicationService;

    @Value
    protected ApplicationTokenService applicationTokenService;

    @Override
    public String type() {
        return "validateAppToken";
    }

    @Override
    protected void validate(final RoutingContext context, final RequestParameter parameter) {

        Parameter header = parameter.header();
        String appCode = header.getString(APPLICATION);
        String token = header.getString(TOKEN);

        if (appCode == null || appCode.isEmpty()) {
            throw new ConfigException(ErrorCode.BadRequest, "请求头没有应用代码");
        } else if (token == null || token.isEmpty()) {
            throw new ConfigException(ErrorCode.BadRequest, "请求头没有令牌");
        }

        Application application = applicationService.findByCode(appCode);
        if (application == null) {
            throw new ConfigException(ErrorCode.ApplicationNotExists);
        }
        context.put(APPLICATION, application);
        ApplicationToken applicationToken = applicationTokenService.findByAppAndToken(String.valueOf(application.getId()), token);
        if (applicationToken == null
                || !applicationToken.isEffective(new Date())) {
            throw new ConfigException(ErrorCode.InvalidToken);
        }

    }

}
