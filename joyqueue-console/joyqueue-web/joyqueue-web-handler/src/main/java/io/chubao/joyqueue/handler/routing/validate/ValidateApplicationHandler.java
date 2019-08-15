package io.chubao.joyqueue.handler.routing.validate;

import io.chubao.joyqueue.handler.error.ConfigException;
import io.chubao.joyqueue.handler.error.ErrorCode;
import io.chubao.joyqueue.model.domain.Application;
import io.chubao.joyqueue.model.domain.Identifier;
import io.chubao.joyqueue.service.ApplicationService;
import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.parameter.Parameter;
import com.jd.laf.web.vertx.parameter.Parameters;
import io.vertx.ext.web.RoutingContext;

import static io.chubao.joyqueue.handler.Constants.APP_ID;
import static io.chubao.joyqueue.handler.Constants.APP_CODE;
import static io.chubao.joyqueue.handler.Constants.ID;
import static io.chubao.joyqueue.handler.Constants.APPLICATION;

/**
 * 判断请求的应用是否和认证的应用身份一致，避免越权查看其它应用信息
 * Created by yangyang115 on 18-8-3.
 */
public class ValidateApplicationHandler extends ValidateHandler {

    @Value
    protected ApplicationService applicationService;

    @Override
    public String type() {
        return "validateApplication";
    }

    @Override
    protected void validate(final RoutingContext context, final Parameters.RequestParameter parameter) {
        Parameter query = parameter.query();
        String value = query.getString(APP_ID);
        value = value == null ? query.getString(APP_CODE) : value;
        value = value == null ? query.getString(ID) : value;
        Application application = context.get(APPLICATION);
        if (application != null) {
            //有认证的上下文
            if (application.getCode().equals(value)
                    || String.valueOf(application.getId()).equals(value)) {
                //和上下文一致
                return;
            }
            //越权访问
            throw new ConfigException(ErrorCode.NoPrivilege);
        }
        //没有认证上下文场景，通过ID或Code加载应用
        if (Character.isDigit(value.charAt(0))) {
            //可能是ID
            try {
                application = applicationService.findById(Long.parseLong(value));
            } catch (NumberFormatException e) {
            }
        }
        if (application == null) {
            if (Identifier.isIdentifier(value)) {
                //根据code查询
                application = applicationService.findByCode(value);
                if (application == null) {
                    throw new ConfigException(ErrorCode.ApplicationNotExists);
                }
            } else {
                throw new ConfigException(ErrorCode.ApplicationNotExists);
            }
        }
        context.put(APPLICATION, application);
    }
}
