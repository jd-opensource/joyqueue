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
package org.joyqueue.network.transport.command.support;

import org.joyqueue.network.transport.command.Ordered;

import java.util.Comparator;

/**
 * CommandHandlerFilterComparator
 *
 * author: gaohaoxiang
 * date: 2018/8/27
 */
public class CommandHandlerFilterComparator implements Comparator<Object> {

    @Override
    public int compare(Object o1, Object o2) {
        if (!(o1 instanceof Ordered) && !(o2 instanceof Ordered)) {
            return 0;
        }
        if ((o1 instanceof Ordered) && !(o2 instanceof Ordered)) {
            return -1;
        }
        if (!(o1 instanceof Ordered) && (o2 instanceof Ordered)) {
            return 1;
        }

        Ordered order1 = (Ordered) o1;
        Ordered order2 = (Ordered) o2;
        return Integer.compare(order1.getOrder(), order2.getOrder());
    }
}