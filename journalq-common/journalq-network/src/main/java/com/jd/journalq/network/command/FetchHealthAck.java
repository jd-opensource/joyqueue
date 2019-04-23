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
package com.jd.journalq.network.command;

import com.jd.journalq.network.transport.command.JMQPayload;

/**
 * FetchHealthAck
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/28
 */
public class FetchHealthAck extends JMQPayload {

    private double point;

    public FetchHealthAck() {

    }

    public FetchHealthAck(double point) {
        this.point = point;
    }

    @Override
    public int type() {
        return JournalqCommandType.FETCH_HEALTH_ACK.getCode();
    }

    public void setPoint(double point) {
        this.point = point;
    }

    public double getPoint() {
        return point;
    }
}