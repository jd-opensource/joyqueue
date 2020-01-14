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
package org.joyqueue.store;

/**
 * @author liyue25
 * Date: 2018/9/17
 */
public class ReadException extends RuntimeException {
    public ReadException(String message) {
        super(message);
    }
    public ReadException(){
        super();
    }
    public ReadException(String message, Throwable t) {
        super(message, t);
    }

    public ReadException(Throwable t) {
        super(t);
    }
}
