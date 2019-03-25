package com.jd.journalq.handler.routing.validate;

import com.jd.journalq.exception.ServiceException;
import com.jd.journalq.handler.error.ConfigException;
import com.jd.journalq.handler.error.ErrorCode;
import com.jd.journalq.handler.Constants;
import com.jd.journalq.model.domain.Application;
import com.jd.journalq.model.domain.ApplicationToken;
import com.jd.journalq.service.ApplicationTokenService;
import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.parameter.Parameters.RequestParameter;
import io.vertx.ext.web.RoutingContext;

/**
 * 验证是指定应用的令牌
 */
public class ValidateTokenOfApplicationHandler extends ValidateHandler {

    @Value
    protected ApplicationTokenService applicationTokenService;

    @Override
    protected void validate(final RoutingContext context, final RequestParameter parameter) {
        Application app = context.get(Constants.APPLICATION);
        Long appTokenId = parameter.query().getLong(Constants.ID);
        ApplicationToken token = null;
        try {
            token = appTokenId == null ? null : applicationTokenService.findById(appTokenId);
        } catch (Exception e) {
            throw new ServiceException(ServiceException.IGNITE_RPC_ERROR,e.getMessage());
        }
        if (app == null) {
            throw new ConfigException(ErrorCode.ApplicationNotExists);
        } else if (token == null) {
            throw new ConfigException(ErrorCode.AppTokenNotExists);
        } else if (!token.getApplication().getCode().equals(app.getCode()) ) {
            throw new ConfigException(ErrorCode.NoPrivilege);
        }
        context.put(Constants.APP_TOKEN, token);

    }

    @Override
    public String type() {
        return "validateTokenOfApplication";
    }
}
