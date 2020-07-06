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
public class PositionOverflowException extends ReadException {
    private final long position;
    private final long right;

    public PositionOverflowException(long position, long right) {
        super(String.format("Read position %d should be less than store right position %d.", position, right));
        this.position = position;
        this.right = right;
    }

    public long getPosition() {
        return position;
    }

    public long getRight() {
        return right;
    }
}
