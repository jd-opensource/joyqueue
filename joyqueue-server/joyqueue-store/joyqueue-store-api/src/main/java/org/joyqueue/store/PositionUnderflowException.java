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
 * Date: 2018-12-12
 */
public class PositionUnderflowException extends ReadException {
    private final long position;
    private final long left;

    public PositionUnderflowException(long position, long left) {
        super(String.format("Read position %d should be greater than or equal to store left position %d.", position, left));
        this.position = position;
        this.left = left;
    }

    public long getPosition() {
        return position;
    }

    public long getLeft() {
        return left;
    }
}
