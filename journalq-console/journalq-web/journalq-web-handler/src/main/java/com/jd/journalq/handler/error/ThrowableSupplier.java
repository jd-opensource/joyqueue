package com.jd.journalq.handler.error;

import com.jd.journalq.exception.ServiceException;
import com.jd.laf.web.vertx.response.ErrorSupplier;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;

import java.lang.reflect.InvocationTargetException;

import static com.jd.journalq.handler.error.ErrorCode.BadRequest;
import static com.jd.journalq.handler.error.ErrorCode.RuntimeError;
import static com.jd.journalq.handler.error.ErrorCode.ServiceError;

/**
 * 其它异常转换器
 */
public class ThrowableSupplier implements ErrorSupplier {
    @Override
    public Response error(final Throwable throwable) {
        if (throwable instanceof IllegalArgumentException) {
            return Responses.error(BadRequest.getCode(), BadRequest.getStatus(), throwable.getMessage());
        } else if (throwable instanceof DuplicateKeyException) {
            return Responses.error(RuntimeError.getCode(), RuntimeError.getStatus(), "记录已经存在，请刷新重试");
        } else if (throwable instanceof DataIntegrityViolationException) {
            return Responses.error(RuntimeError.getCode(), RuntimeError.getStatus(), "输入记录不符合要求");
        } else if (throwable instanceof InvocationTargetException) {
            Throwable targetException = ((InvocationTargetException) throwable).getTargetException();
            if (targetException instanceof ServiceException) {
                ServiceException serviceException = (ServiceException) targetException;
                return Responses.error(ServiceError.getCode(), serviceException.getStatus(), serviceException.getMessage());
            }
            return Responses.error(RuntimeError.getCode(), RuntimeError.getStatus(), targetException.getMessage());
        }

//        return Responses.error(RuntimeError.getCode(), RuntimeError.getStatus(), StringUtils.isBlank(throwable.getMessage())? throwable.toString() : throwable.getMessage());
        return Responses.error(RuntimeError.getCode(), RuntimeError.getStatus(), StringUtils.isBlank(throwable.getMessage())?RuntimeError.getMessage(): throwable.getMessage());
    }

    @Override
    public Class<? extends Throwable> type() {
        return Throwable.class;
    }
}
