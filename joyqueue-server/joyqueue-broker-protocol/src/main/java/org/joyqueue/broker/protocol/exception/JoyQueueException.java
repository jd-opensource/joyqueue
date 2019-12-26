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
package org.joyqueue.broker.protocol.exception;

import org.joyqueue.exception.JoyQueueCode;

/**
 * JoyQueueException
 *
 * author: gaohaoxiang
 * date: 2018/12/5
 */
public class JoyQueueException extends RuntimeException {

    private int code;

    public JoyQueueException(Throwable cause) {
        super(cause);
    }

    public JoyQueueException(int code) {
        this.code = code;
    }

    public JoyQueueException(String message, int code) {
        super(message);
        this.code = code;
    }

    public JoyQueueException(JoyQueueCode code, Object... args) {
        super(code.getMessage(args));
        this.code = code.getCode();
    }

    public JoyQueueException(JoyQueueCode code, Throwable cause, Object... args) {
        super(code.getMessage(args), cause);
        this.code = code.getCode();
    }

    public JoyQueueException(String message, Throwable cause, int code) {
        super(message, cause);
        this.code = code;
    }

    public JoyQueueException(Throwable cause, int code) {
        super(cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}