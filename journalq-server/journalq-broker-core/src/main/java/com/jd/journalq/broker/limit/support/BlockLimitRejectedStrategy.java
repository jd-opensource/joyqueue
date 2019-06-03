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
package com.jd.journalq.broker.limit.support;

import com.jd.journalq.broker.limit.LimitRejectedStrategy;
import com.jd.journalq.broker.limit.domain.LimitContext;
import com.jd.journalq.network.transport.command.Command;

/**
 * BlockLimitRejectedStrategy
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/5/16
 */
public class BlockLimitRejectedStrategy implements LimitRejectedStrategy {

    @Override
    public Command execute(LimitContext context) {
        try {
            synchronized (Thread.currentThread()) {
                Thread.currentThread().sleep(context.getDelay());
            }
        } catch (InterruptedException e) {
        }
        return context.getResponse();
    }

    @Override
    public String type() {
        return "block";
    }
}