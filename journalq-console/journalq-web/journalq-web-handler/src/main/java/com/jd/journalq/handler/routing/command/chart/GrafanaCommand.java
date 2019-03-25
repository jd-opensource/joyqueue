package com.jd.journalq.handler.routing.command.chart;

import com.jd.journalq.exception.ServiceException;
import com.jd.journalq.handler.Constants;
import com.jd.journalq.handler.binder.BodyType;
import com.jd.journalq.handler.binder.annotation.Body;
import com.jd.journalq.handler.binder.annotation.ParamterValue;
import com.jd.journalq.handler.binder.annotation.Path;
import com.jd.journalq.handler.util.GrafanaUtils;
import com.jd.journalq.model.domain.grafana.GrafanaSearch;
import com.jd.journalq.model.domain.grafana.GrafanaVariable;
import com.jd.journalq.model.domain.grafana.GrafanaVariableParameter;
import com.jd.journalq.model.domain.grafana.GrafanaVariableResult;
import com.jd.journalq.toolkit.lang.Preconditions;
import com.jd.journalq.util.NullUtil;
import com.jd.laf.web.vertx.Command;
import com.jd.laf.web.vertx.annotation.Context;
import com.jd.laf.web.vertx.pool.Poolable;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.jd.journalq.exception.ServiceException.INTERNAL_SERVER_ERROR;
import static com.jd.journalq.handler.util.GrafanaUtils.getResult;

public class GrafanaCommand implements Command<Response>, Poolable {

    private static final Logger logger = LoggerFactory.getLogger(GrafanaCommand.class);

    @Context
    protected RoutingContext context;
    private String module;

    @Path("test")
    public Response test() throws Exception {
        return Responses.success();
    }

    @Path("search")
    public List<String> search(@Body(typeindex = 0, type = BodyType.JSON) GrafanaSearch grafanaSearch) throws Exception {
        //check argument
        Preconditions.checkArgument(grafanaSearch != null && StringUtils.isNotBlank(grafanaSearch.getTarget()),
                "illegal args at grafana search target.");
        // format target and get key
        String target = formatTarget(grafanaSearch.getTarget());
        String[] key = GrafanaUtils.getKey(target);
        // find metrics by key, if exists, return values
        List<String> metrics = GrafanaUtils.getMetrics().get(key[1]);
        if (NullUtil.isNotEmpty(metrics)) {
            return metrics;
        }
        // not metric, then find variables by key
        GrafanaVariable variable = GrafanaUtils.getVariables().get(key[0]);
        if (variable == null) {
            logger.error(String.format("can not get grafana variable config with target %s", target));
            return Collections.emptyList();
        }
        //get query conditions
        List<GrafanaVariableParameter> parameters = variable.getQuery().getParameters();
        String[] args = new String[parameters.size()];
        String[] variables = target.split(GrafanaUtils.getDelimiter(target));
        if (parameters != null) {
            args = parameters.stream().sorted(Comparator.comparing(GrafanaVariableParameter::getArgIndex)).map(p ->
                    variables[p.getTargetIndex()]).toArray(String[]::new);
        }
        //reflect query
        Object result;
        try {
            Object service = context.get(variable.getQuery().getBean());
            Class[] types = new Class[parameters.size()];
            for(int i=0; i<parameters.size(); i++) {
                types[i] = String.class;
            }
            Method method = service.getClass().getMethod(variable.getQuery().getMethod(), types);
            result = method.invoke(service, args);
        } catch (Exception e) {
            throw new ServiceException(INTERNAL_SERVER_ERROR, "grafana query variable error. caused by: "+ e.getCause(), e);
        }

        GrafanaVariableResult resultFormat = variable.getResult();
        return formateResult(result, resultFormat.getFormat(), resultFormat.getType(), resultFormat.getDelimiter());
    }

    @Path("getRedirectUrl")
    public Response getRedirectUrl(@ParamterValue(Constants.UID) Object uid) throws Exception {
        Preconditions.checkArgument(uid != null, "invalid arguments, uid can not be null.");
        return Responses.success(GrafanaUtils.getUrls().get(uid));
    }

    protected String getModule() {
        //module默认值与type相同
        module = this.type();
        return module;
    }

    @Override
    public Response execute() throws Exception {
        return Responses.error(Response.HTTP_NOT_FOUND,Response.HTTP_NOT_FOUND,"Not Found");
    }

    private String formatTarget(String target) {
        return target.replaceAll("\\\\", "");
    }

    private List<String> formateResult(Object result, String format, String type, String delimiter) {
        if (result == null) {
            return null;
        }
        if ("string".equalsIgnoreCase(type)) {
            return Collections.singletonList((String)((ArrayList) result).stream().map(a ->
                    getResult(a, format)).collect(Collectors.joining(delimiter == null ? "" : delimiter)));
        } else {
            return (List<String>) ((ArrayList) result).stream().map(a ->
                    getResult(a, format)).collect(Collectors.toList());
        }
    }

    @Override
    public void clean() {
        context = null;
    }
}
