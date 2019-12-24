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
package org.joyqueue.broker.limit.exception;

import org.joyqueue.network.transport.command.Command;

/**
 * LimitRejectedException
 *
 * author: gaohaoxiang
 * date: 2019/5/16
 */
public class LimitRejectedException extends LimitException {

    private Command request;
    private Command response;

    public LimitRejectedException() {
    }

    public LimitRejectedException(Command request, Command response) {
        this.request = request;
        this.response = response;
    }

    public LimitRejectedException(String message) {
        super(message);
    }

    public LimitRejectedException(String message, Throwable cause) {
        super(message, cause);
    }

    public LimitRejectedException(Throwable cause) {
        super(cause);
    }

    public LimitRejectedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}