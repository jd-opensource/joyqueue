package io.chubao.joyqueue.handler.error;

import com.jd.laf.web.vertx.response.ErrorSupplier;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import io.vertx.ext.web.handler.impl.HttpStatusException;

import static io.chubao.joyqueue.handler.error.ErrorCode.RuntimeError;

/**
 * Http status exception conversion
 * Created by chenyanying3 on 18-11-16.
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
