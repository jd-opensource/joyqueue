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
package com.jd.journalq.client.internal.producer.exception;

import com.jd.journalq.client.internal.exception.ClientException;

/**
 * ProducerException
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/20
 */
public class ProducerException extends ClientException {

    public ProducerException() {
    }

    public ProducerException(String message) {
        super(message);
    }

    public ProducerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProducerException(Throwable cause) {
        super(cause);
    }

    public ProducerException(String error, int code) {
        super(error, code);
    }

    public ProducerException(String error, int code, Throwable cause) {
        super(error, code, cause);
    }
}