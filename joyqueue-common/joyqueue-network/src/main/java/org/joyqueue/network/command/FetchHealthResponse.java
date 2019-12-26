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
package org.joyqueue.network.command;

import org.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * FetchHealthResponse
 *
 * author: gaohaoxiang
 * date: 2018/12/28
 */
public class FetchHealthResponse extends JoyQueuePayload {

    private double point;

    public FetchHealthResponse() {

    }

    public FetchHealthResponse(double point) {
        this.point = point;
    }

    @Override
    public int type() {
        return JoyQueueCommandType.FETCH_HEALTH_RESPONSE.getCode();
    }

    public void setPoint(double point) {
        this.point = point;
    }

    public double getPoint() {
        return point;
    }
}