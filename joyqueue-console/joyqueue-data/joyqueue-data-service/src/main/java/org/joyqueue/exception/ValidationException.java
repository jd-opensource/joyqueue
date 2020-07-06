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
package org.joyqueue.exception;

import org.joyqueue.model.exception.BusinessException;

/**
 * 验证异常异常
 */
public class ValidationException extends BusinessException {

    public static final int UNIQUE_EXCEPTION_STATUS = 100;
    public static final int NOT_FOUND_EXCEPTION_STATUS = 200;
    public static final int OTHER_EXCEPTION_STATUS = 900;

    protected ValidationException() {
    }

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(int status, String message) {
        super(status, message);
    }

    public ValidationException(String code, String message) {
        super(code, message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidationException(int status, String code, String message, Throwable cause) {
        super(status, code, message, cause);
    }

}