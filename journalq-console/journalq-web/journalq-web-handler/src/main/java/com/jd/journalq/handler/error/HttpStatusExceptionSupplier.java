package com.jd.journalq.handler.error;

import com.jd.laf.web.vertx.response.ErrorSupplier;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import io.vertx.ext.web.handler.impl.HttpStatusException;

import static com.jd.journalq.handler.error.ErrorCode.RuntimeError;

/**
 * Http status异常转换器
 */
public class HttpStatusExceptionSupplier implements ErrorSupplier {
    @Override
    public Response error(final Throwable throwable) {
        HttpStatusException exception = (HttpStatusException) throwable;
        return Responses.error(RuntimeError.getCode(), exception.getStatusCode(), exception.getPayload());
    }

    @Override
    public Class<? extends Throwable> type() {
        return HttpStatusException.class;
    }
}
