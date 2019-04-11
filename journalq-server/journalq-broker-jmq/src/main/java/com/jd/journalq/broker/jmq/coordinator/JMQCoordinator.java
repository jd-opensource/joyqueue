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
package com.jd.journalq.broker.jmq.coordinator;

import com.jd.journalq.broker.coordinator.Coordinator;
import com.jd.journalq.domain.Broker;

/**
 * Coordinator
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/4
 */
public class JMQCoordinator {

    private Coordinator coordinator;

    public JMQCoordinator(Coordinator coordinator) {
        this.coordinator = coordinator;
    }

    public boolean isCurrentCoordinator(String app) {
        return coordinator.isCurrentCoordinator(app);
    }

    public Broker findCoordinator(String app) {
        return coordinator.findCoordinator(app);
    }
}