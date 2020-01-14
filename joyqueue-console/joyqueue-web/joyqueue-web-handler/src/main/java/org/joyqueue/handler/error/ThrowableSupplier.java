/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.handler.error;

import org.joyqueue.exception.ServiceException;
import com.jd.laf.web.vertx.response.ErrorSupplier;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;

import java.lang.reflect.InvocationTargetException;

/**
 * Other error or exception conversion
 * Created by chenyanying3 on 18-11-16.
 */
public class ThrowableSupplier implements ErrorSupplier {
    private static final Logger logger = LoggerFactory.getLogger(ThrowableSupplier.class);

    @Override
    public Response error(final Throwable throwable) {
        logger.error("throwable exception",throwable);
        if (throwable instanceof IllegalArgumentException) {
            return Responses.error(ErrorCode.BadRequest.getCode(), ErrorCode.BadRequest.getStatus(), throwable.getMessage());
        } else if (throwable instanceof DuplicateKeyException) {
            return Responses.error(ErrorCode.DuplicateError.getCode(), ErrorCode.DuplicateError.getStatus(), "记录已经存在，请刷新重试");
        } else if (throwable instanceof DataIntegrityViolationException) {
            return Responses.error(ErrorCode.RuntimeError.getCode(), ErrorCode.RuntimeError.getStatus(), "输入记录不符合要求");
        } else if (throwable instanceof InvocationTargetException) {
            Throwable targetException = ((InvocationTargetException) throwable).getTargetException();
            if (targetException instanceof ServiceException) {
                ServiceException serviceException = (ServiceException) targetException;
                return Responses.error(ErrorCode.ServiceError.getCode(), serviceException.getStatus(), serviceException.getMessage());
            }
            return Responses.error(ErrorCode.RuntimeError.getCode(), ErrorCode.RuntimeError.getStatus(), targetException.getMessage());
        } else if(throwable instanceof NullPointerException) {
            return Responses.error(ErrorCode.RuntimeError.getCode(), ErrorCode.RuntimeError.getStatus(), ((NullPointerException) throwable).toString());
        }

        return Responses.error(ErrorCode.RuntimeError.getCode(), ErrorCode.RuntimeError.getStatus(),
                (StringUtils.isNotBlank(throwable.toString()) ? throwable.toString(): ErrorCode.RuntimeError.getMessage()));
    }

    @Override
    public Class<? extends Throwable> type() {
        return Throwable.class;
    }
}
