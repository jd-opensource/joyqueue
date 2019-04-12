/**
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
package com.jd.journalq.broker.election;

import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.exception.JMQException;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/9/29
 */
public class ElectionException extends JMQException {
    public ElectionException(String message) {
        super(message, JMQCode.FW_ELECTION_ERROR.getCode());
    }

    public ElectionException(String message, Throwable cause) {
        super(message, cause, JMQCode.FW_ELECTION_ERROR.getCode());
    }
}
