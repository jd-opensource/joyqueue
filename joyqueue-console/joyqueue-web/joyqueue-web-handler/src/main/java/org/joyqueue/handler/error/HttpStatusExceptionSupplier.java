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

import com.jd.laf.web.vertx.response.ErrorSupplier;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import io.vertx.ext.web.handler.impl.HttpStatusException;

import static org.joyqueue.handler.error.ErrorCode.RuntimeError;

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
