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
package com.jd.journalq.broker.jmq.exception;

import com.jd.journalq.exception.JMQCode;

/**
 * JMQException
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/5
 */
public class JMQException extends RuntimeException {

    private int code;

    public JMQException(Throwable cause) {
        super(cause);
    }

    public JMQException(int code) {
        this.code = code;
    }

    public JMQException(String message, int code) {
        super(message);
        this.code = code;
    }

    public JMQException(JMQCode code, Object... args) {
        super(code.getMessage(args));
        this.code = code.getCode();
    }

    public JMQException(JMQCode code, Throwable cause, Object... args) {
        super(code.getMessage(args), cause);
        this.code = code.getCode();
    }

    public JMQException(String message, Throwable cause, int code) {
        super(message, cause);
        this.code = code;
    }

    public JMQException(Throwable cause, int code) {
        super(cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}