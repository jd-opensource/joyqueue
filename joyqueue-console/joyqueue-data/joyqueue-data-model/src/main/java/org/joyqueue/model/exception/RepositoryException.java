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
 * 仓库异常
 */
public class RepositoryException extends DataException {

    protected RepositoryException() {
    }

    public RepositoryException(String message) {
        super(message);
    }

    public RepositoryException(int code, String message) {
        super(message);
        this.status = code;
    }

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public RepositoryException(int code, String message, Throwable cause) {
        super(message, cause);
        this.status = code;
    }

}