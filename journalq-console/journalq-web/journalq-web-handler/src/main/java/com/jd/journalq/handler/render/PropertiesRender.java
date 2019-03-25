package com.jd.journalq.handler.render;

import com.jd.laf.web.vertx.Command;
import com.jd.laf.web.vertx.render.Render;
import com.jd.laf.web.vertx.response.Response;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Properties;

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

/**
 * Properties渲染
 */
public class PropertiesRender implements Render {

    public static final String APPLICATION_PROPERTIES = "application/properties";

    @Override
    public void render(final RoutingContext context) {
        //获取结果
        Response result = context.get(Command.RESULT);
        HttpServerResponse response = context.response();
        response.putHeader(CONTENT_TYPE, APPLICATION_PROPERTIES);
        if (result != null && result.getCode() != Response.HTTP_OK) {
            //异常
            response.setStatusCode(result.getStatus()).end(result.getMessage());
        } else {
            response.end();
        }
    }

    @Override
    public int order() {
        return 0;
    }

    @Override
    public String type() {
        return APPLICATION_PROPERTIES;
    }
}
