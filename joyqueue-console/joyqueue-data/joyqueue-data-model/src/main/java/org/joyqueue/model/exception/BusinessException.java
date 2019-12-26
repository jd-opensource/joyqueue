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
package org.joyqueue.model.exception;

/**
 * 业务异常
 */
public class BusinessException extends DataException {

    // 错误码
    protected String code;

    protected BusinessException() {
    }

    public BusinessException(String message) {
        this(500, "InternalError", message);
    }

    public BusinessException(int status, String message) {
        this(status, "InternalError", message);
    }

    public BusinessException(String code, String message) {
        this(500, code, message);
    }

    public BusinessException(String message, Throwable cause) {
        this(500, "InternalError", message, cause);
    }

    public BusinessException(int status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public BusinessException(int status, String code, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}