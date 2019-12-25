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
package org.joyqueue.nsr.network.command;

import org.joyqueue.domain.Subscription;
import org.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class HasSubscribe extends JoyQueuePayload {

    private String app;
    private Subscription.Type subscribe;

    public HasSubscribe app(String app){
        this.app = app;
        return this;
    }
    public HasSubscribe subscribe(Subscription.Type subscribe){
        this.subscribe = subscribe;
        return this;
    }

    public String getApp() {
        return app;
    }

    public Subscription.Type getSubscribe() {
        return subscribe;
    }

    @Override
    public int type() {
        return NsrCommandType.HAS_SUBSCRIBE;
    }

    @Override
    public String toString() {
        return "HasSubscribe{" +
                "app='" + app + '\'' +
                ", subscribe=" + subscribe +
                '}';
    }
}
