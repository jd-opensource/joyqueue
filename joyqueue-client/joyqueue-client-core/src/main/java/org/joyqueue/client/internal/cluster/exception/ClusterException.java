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
package org.joyqueue.client.internal.cluster.exception;

import org.joyqueue.client.internal.exception.ClientException;

/**
 * ClusterException
 *
 * author: gaohaoxiang
 * date: 2018/12/3
 */
public class ClusterException extends ClientException {

    public ClusterException() {
    }

    public ClusterException(String message) {
        super(message);
    }

    public ClusterException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClusterException(Throwable cause) {
        super(cause);
    }

    public ClusterException(String error, int code) {
        super(error, code);
    }

    public ClusterException(String error, int code, Throwable cause) {
        super(error, code, cause);
    }
}