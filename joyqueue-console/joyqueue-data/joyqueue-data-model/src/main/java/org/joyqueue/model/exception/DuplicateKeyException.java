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
 * 唯一约束异常
 * Created by hexiaofeng on 15-7-15.
 */
public class DuplicateKeyException extends RepositoryException {

    public DuplicateKeyException() {
    }

    public DuplicateKeyException(String message) {
        super(message);
    }

    public DuplicateKeyException(int status, String message) {
        super(status, message);
    }

    public DuplicateKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateKeyException(int status, String message, Throwable cause) {
        super(status, message, cause);
    }
}
