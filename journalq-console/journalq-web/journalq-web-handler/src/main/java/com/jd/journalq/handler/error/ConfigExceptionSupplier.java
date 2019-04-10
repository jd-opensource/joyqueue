package com.jd.journalq.handler.error;

import com.jd.laf.web.vertx.response.ErrorSupplier;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;

/**
 * Config exception conversion
 * Created by chenyanying3 on 18-11-16.
 */
public class ConfigExceptionSupplier implements ErrorSupplier {
    @Override
    public Response error(final Throwable throwable) {
        ConfigException exception = (ConfigException) throwable;
        return Responses.error(exception.getCode(), exception.getStatus(), exception.getMessage());
    }

    @Override
    public Class<? extends Throwable> type() {
        return ConfigException.class;
    }
}
