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
package org.joyqueue.broker.limit.support;

import org.joyqueue.broker.limit.LimitRejectedStrategy;
import org.joyqueue.broker.limit.domain.LimitContext;
import org.joyqueue.broker.limit.exception.LimitRejectedException;
import org.joyqueue.network.transport.command.Command;

/**
 * ExceptionLimitRejectedStrategy
 *
 * author: gaohaoxiang
 * date: 2019/5/16
 */
public class ExceptionLimitRejectedStrategy implements LimitRejectedStrategy {

    @Override
    public Command execute(LimitContext context) {
        throw new LimitRejectedException(context.getRequest(), context.getResponse());
    }

    @Override
    public String type() {
        return "exception";
    }
}