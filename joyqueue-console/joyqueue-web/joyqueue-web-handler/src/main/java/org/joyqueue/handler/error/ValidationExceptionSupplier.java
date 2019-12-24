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

import org.joyqueue.exception.ValidationException;
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
