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

/**
 * 配置异常
 * Created by yangyang115 on 18-7-26.
 */
public class ConfigException extends RuntimeException {

    private int code;
    private int status;

    public ConfigException(ErrorCode code) {
        super(code.getMessage());
        this.code = code.getCode();
        this.status = code.getStatus();
    }

    public ConfigException(ErrorCode code, String message) {
        super(code.getMessage() + " " + message);
        this.code = code.getCode();
        this.status = code.getStatus();
    }

    public ConfigException(ErrorCode code, Throwable throwable) {
        super(throwable);
        this.code = code.getCode();
        this.status = code.getStatus();
    }

    public int getCode() {
        return code;
    }

    public int getStatus() {
        return status;
    }
}
