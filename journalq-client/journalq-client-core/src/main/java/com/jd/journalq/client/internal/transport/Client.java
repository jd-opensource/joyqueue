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
package com.jd.journalq.client.internal.transport;

import com.jd.journalq.client.internal.exception.ClientException;
import com.jd.journalq.client.internal.nameserver.NameServerConfig;
import com.jd.journalq.client.internal.transport.config.TransportConfig;
import com.jd.journalq.exception.JournalqException;
import com.jd.journalq.network.command.Heartbeat;
import com.jd.journalq.network.domain.BrokerNode;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.TransportAttribute;
import com.jd.journalq.network.transport.TransportClient;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.CommandCallback;
import com.jd.journalq.network.transport.command.JMQCommand;
import com.jd.journalq.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Future;

/**
 * Client
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/28
 */
public class Client extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(Client.class);

    private BrokerNode node;
    private TransportConfig transportConfig;
    private TransportClient transportClient;
    private NameServerConfig nameServerConfig;
    private Transport transport;

    private ClientConnectionState connectionState;

    public Client(BrokerNode node, TransportConfig transportConfig, TransportClient transportClient, NameServerConfig nameServerConfig) {
        this.node = node;
        this.transportConfig = transportConfig;
        this.transportClient = transportClient;
        this.nameServerConfig = nameServerConfig;
        this.connectionState = new ClientConnectionState(nameServerConfig, this);
    }

    public void heartbeat(long timeout) {
        sync(new JMQCommand(new Heartbeat()), timeout);
    }

    public Future<Command> async(Command request, long timeout) {
        try {
            connectionState.updateUseTime();
            return (Future<Command>) transport.async(request, timeout);
        } catch (Exception e) {
            throw new ClientException(e);
        }
    }

    public Future<Command> async(Command request) {
        return async(request, transportConfig.getSendTimeout());
    }

    public void async(Command request, long timeout, final CommandCallback callback) {
        try {
            connectionState.updateUseTime();
            transport.async(request, timeout, new CommandCallback() {
                @Override
                public void onSuccess(Command request, Command response) {
                    if (response.isSuccess()) {
                        callback.onSuccess(request, response);
                    } else {
                        callback.onException(request, new JournalqException(response.getHeader().getError(), response.getHeader().getStatus()));
                    }
                }

                @Override
                public void onException(Command request, Throwable cause) {
                    callback.onException(request, cause);
                }
            });
        } catch (Exception e) {
            throw new ClientException(e);
        }
    }

    public void async(Command request, CommandCallback callback) {
        async(request, transportConfig.getSendTimeout(), callback);
    }

    public void oneway(Command request, long timeout) {
        try {
            connectionState.updateUseTime();
            transport.oneway(request, timeout);
        } catch (Exception e) {
            throw new ClientException(e);
        }
    }

    public Command sync(Command request, long timeout) {
        Command response = null;
        try {
            connectionState.updateUseTime();
            response = transport.sync(request, timeout);
        } catch (Exception e) {
            throw new ClientException(e);
        }

        if (!response.isSuccess()) {
            throw new ClientException(response.getHeader().getError(), response.getHeader().getStatus());
        }
        return response;
    }

    public Command sync(Command request) {
        return sync(request, transportConfig.getSendTimeout());
    }

    public void handleAddConnection() {
        connectionState.handleAddConnection();
    }

    public void handleDisconnection() {
        connectionState.handleDisconnection();
    }

    public ClientState getState() {
        switch (transport.state()) {
            case CONNECTED: {
                return ClientState.CONNECTED;
            }
            case DISCONNECTED: {
                return ClientState.DISCONNECTED;
            }
            default: {
                throw new IllegalArgumentException(String.format("unknown state, %s", transport.state()));
            }
        }
    }

    public ClientConnectionInfo getConnectionInfo() {
        return connectionState.getConnectionInfo();
    }

    public TransportAttribute getAttribute() {
        return transport.attr();
    }

    public Transport getTransport() {
        return transport;
    }

    public long getLastUseTime() {
        return connectionState.getLastUseTime();
    }

    @Override
    protected void doStart() throws Exception {
        transport = transportClient.createTransport(new InetSocketAddress(node.getHost(), node.getPort()), transportConfig.getSendTimeout());
    }

    @Override
    protected void doStop() {
        if (transport != null) {
            transport.stop();
        }
    }
}