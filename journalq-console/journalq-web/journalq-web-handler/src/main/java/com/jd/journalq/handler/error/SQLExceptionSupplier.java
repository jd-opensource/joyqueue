package com.jd.journalq.handler.error;

import com.jd.laf.web.vertx.response.ErrorSupplier;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import org.springframework.jdbc.BadSqlGrammarException;

/**
 * SQL异常转换器
 */
public class SQLExceptionSupplier implements ErrorSupplier {
    @Override
    public Response error(final Throwable throwable) {
        BadSqlGrammarException exception = (BadSqlGrammarException) throwable;
        return Responses.error(ErrorCode.SQLError.getCode(), exception.getSQLException().getErrorCode(), ErrorCode.SQLError.getMessage());
    }

    @Override
    public Class<? extends Throwable> type() {
        return BadSqlGrammarException.class;
    }
}
