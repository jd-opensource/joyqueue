package io.chubao.joyqueue.handler.error;

import com.jd.laf.web.vertx.response.ErrorSupplier;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import org.springframework.jdbc.BadSqlGrammarException;

import static io.chubao.joyqueue.handler.error.ErrorCode.SQLError;

/**
 * Sql exception conversion
 * Created by chenyanying3 on 18-11-16.
 */
public class SQLExceptionSupplier implements ErrorSupplier {
    @Override
    public Response error(final Throwable throwable) {
        BadSqlGrammarException exception = (BadSqlGrammarException) throwable;
        return Responses.error(SQLError.getCode(), exception.getSQLException().getErrorCode(), SQLError.getMessage());
    }

    @Override
    public Class<? extends Throwable> type() {
        return BadSqlGrammarException.class;
    }
}
