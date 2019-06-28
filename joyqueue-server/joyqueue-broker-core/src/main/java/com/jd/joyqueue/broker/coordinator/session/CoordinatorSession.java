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
package com.jd.joyqueue.broker.coordinator.session;

import com.jd.joyqueue.broker.coordinator.config.CoordinatorConfig;
import com.jd.joyqueue.network.transport.Transport;
import com.jd.joyqueue.network.transport.command.Command;
import com.jd.joyqueue.network.transport.command.CommandCallback;
import com.jd.joyqueue.network.transport.exception.TransportException;
import com.jd.joyqueue.toolkit.service.Service;

/**
 * CoordinatorSession
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/12
 */
public class CoordinatorSession extends Service {

    private int brokerId;
    private String brokerHost;
    private int brokerPort;
    private CoordinatorConfig config;
    private Transport transport;

    public CoordinatorSession() {

    }

    public CoordinatorSession(int brokerId, String brokerHost, int brokerPort, CoordinatorConfig config, Transport transport) {
        this.brokerId = brokerId;
        this.brokerHost = brokerHost;
        this.brokerPort = brokerPort;
        this.config = config;
        this.transport = transport;
    }

    @Override
    protected void doStop() {
        transport.stop();
    }

    public void oneway(Command command) throws TransportException {
        oneway(command, config.getSessionTimeout());
    }

    public void oneway(Command command, long timeout) throws TransportException {
        transport.oneway(command, timeout);
    }

    public Command sync(Command command) throws TransportException {
        return sync(command, config.getSessionTimeout());
    }

    public Command sync(Command command, int timeout) throws TransportException {
        return transport.sync(command, timeout);
    }

    public void async(Command command, CommandCallback callback) throws TransportException {
        async(command, config.getSessionTimeout(), callback);
    }

    void async(Command command, long timeout, CommandCallback callback) throws TransportException {
        transport.async(command, timeout, callback);
    }

    public int getBrokerId() {
        return brokerId;
    }

    public String getBrokerHost() {
        return brokerHost;
    }

    public int getBrokerPort() {
        return brokerPort;
    }
}