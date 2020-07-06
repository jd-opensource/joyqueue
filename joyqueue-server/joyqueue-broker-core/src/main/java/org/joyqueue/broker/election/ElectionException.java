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
package org.joyqueue.broker.election;

import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.exception.JoyQueueException;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/9/29
 */
public class ElectionException extends JoyQueueException {
    public ElectionException(String message) {
        super(message, JoyQueueCode.FW_ELECTION_ERROR.getCode());
    }

    public ElectionException(String message, Throwable cause) {
        super(message, cause, JoyQueueCode.FW_ELECTION_ERROR.getCode());
    }
}
