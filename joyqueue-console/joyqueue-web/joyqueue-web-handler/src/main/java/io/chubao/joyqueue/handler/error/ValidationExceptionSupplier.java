package io.chubao.joyqueue.handler.error;

import io.chubao.joyqueue.exception.ValidationException;
import com.jd.laf.web.vertx.response.ErrorSupplier;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;

/**
 * Form validation exception conversion
 * Created by chenyanying3 on 18-11-16.
 */
public class ValidationExceptionSupplier implements ErrorSupplier {
    @Override
    public Response error(final Throwable throwable) {
        ValidationException exception = (ValidationException) throwable;
        return Responses.error(ErrorCode.ValidationError.getCode(), exception.getStatus(), exception.getMessage());
    }

    @Override
    public Class<? extends Throwable> type() {
        return ValidationException.class;
    }
}
