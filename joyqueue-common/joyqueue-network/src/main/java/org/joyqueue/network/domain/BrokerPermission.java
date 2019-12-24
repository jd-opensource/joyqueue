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
package org.joyqueue.network.domain;

/**
 * BrokerPermission
 *
 * author: gaohaoxiang
 * date: 2019/6/27
 */
public class BrokerPermission {

    private static final int READABLE_MARK = 1 << 0;
    private static final int WRITABLE_MARK = 1 << 1;

    public static int setReadable(int permission, boolean readable) {
        if (readable) {
            return permission | READABLE_MARK;
        } else {
            return permission & ~READABLE_MARK;
        }
    }

    public static int setWritable(int permission, boolean writable) {
        if (writable) {
            return permission | WRITABLE_MARK;
        } else {
            return permission & ~WRITABLE_MARK;
        }
    }

    public static boolean isReadable(int permission) {
        return (permission & READABLE_MARK) != 0;
    }

    public static boolean isWritable(int permission) {
        return (permission & WRITABLE_MARK) != 0;
    }
}