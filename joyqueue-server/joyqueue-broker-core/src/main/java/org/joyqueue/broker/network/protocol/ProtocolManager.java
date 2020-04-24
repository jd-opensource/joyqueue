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
package org.joyqueue.broker.network.protocol;

import com.google.common.collect.Lists;
import com.jd.laf.extension.ExtensionManager;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.BrokerContextAware;
import org.joyqueue.broker.network.protocol.support.ProtocolServerWrapper;
import org.joyqueue.broker.network.protocol.support.ProtocolServiceWrapper;
import org.joyqueue.network.protocol.Protocol;
import org.joyqueue.network.protocol.ProtocolException;
import org.joyqueue.network.protocol.ProtocolServer;
import org.joyqueue.network.protocol.ProtocolService;
import org.joyqueue.toolkit.concurrent.NamedThreadFactory;
import org.joyqueue.toolkit.lang.LifeCycle;
import org.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ProtocolManager
 *
 * author: gaohaoxiang
 * date: 2018/8/13
 */
public class ProtocolManager extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(ProtocolManager.class);

    private BrokerContext brokerContext;
    private ExecutorService commonThreadPool;
    private ExecutorService fetchThreadPool;
    private ExecutorService produceThreadPool;

    private List<Protocol> protocols = Lists.newLinkedList();
    private List<ProtocolService> protocolServices = Lists.newLinkedList();
    private List<ProtocolServer> protocolServers = Lists.newLinkedList();

    public ProtocolManager(BrokerContext brokerContext) {
        this.brokerContext = brokerContext;
        this.commonThreadPool = new ThreadPoolExecutor(brokerContext.getBrokerConfig().getServerCommonThreads(), brokerContext.getBrokerConfig().getServerCommonThreads(),
                brokerContext.getBrokerConfig().getServerCommonThreadKeepalive(), TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(brokerContext.getBrokerConfig().getServerCommonThreadQueueSize()),
                new NamedThreadFactory("joyqueue-frontend-common-threads"), new ProtocolRejectedExecutionHandler("common"));
        this.fetchThreadPool = new ThreadPoolExecutor(brokerContext.getBrokerConfig().getServerFetchThreads(), brokerContext.getBrokerConfig().getServerFetchThreads(),
                brokerContext.getBrokerConfig().getServerFetchThreadKeepalive(), TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(brokerContext.getBrokerConfig().getServerFetchThreadQueueSize()),
                new NamedThreadFactory("joyqueue-frontend-fetch-threads"), new ProtocolRejectedExecutionHandler("fetch"));
        this.produceThreadPool = new ThreadPoolExecutor(brokerContext.getBrokerConfig().getServerProduceThreads(), brokerContext.getBrokerConfig().getServerProduceThreads(),
                brokerContext.getBrokerConfig().getServerProduceThreadKeepalive(), TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(brokerContext.getBrokerConfig().getServerProduceThreadQueueSize()),
                new NamedThreadFactory("joyqueue-frontend-produce-threads"), new ProtocolRejectedExecutionHandler("produce"));
        loadProtocols();
    }

    public List<ProtocolService> getProtocolServices() {
        return protocolServices;
    }

    public List<ProtocolServer> getProtocolServers() {
        return protocolServers;
    }

    public void register(Protocol protocol) {
        protocols.add(protocol);
        if (protocol instanceof ProtocolService) {
            protocolServices.add((ProtocolService) protocol);
        } else if (protocol instanceof ProtocolServer) {
            protocolServers.add((ProtocolServer) protocol);
        }
    }

    @Override
    protected void doStart() throws Exception {
        for (Protocol protocol : protocols) {
            try {
                initProtocol(protocol);
                logger.info("protocol {} is init", protocol.type());
            } catch (Exception e) {
                throw new ProtocolException(String.format("protocol %s init failed", protocol.type()), e);
            }
        }
    }

    @Override
    protected void doStop() {
        for (Protocol protocol : protocols) {
            try {
                stopProtocol(protocol);
            } catch (Exception e) {
                throw new ProtocolException(String.format("protocol %s stop failed", protocol.type()), e);
            }
        }
        commonThreadPool.shutdown();
        fetchThreadPool.shutdown();
        produceThreadPool.shutdown();
    }

    protected List<Protocol> loadProtocols() {
        List<Protocol> result = Lists.newLinkedList();
        List<ProtocolService> protocolServices = doGetProtocolServices();
        List<ProtocolServer> protocolServers = doGetProtocolServers();

        for (ProtocolService protocolService : protocolServices) {
            if (!brokerContext.getBrokerConfig().getServerShardedThreads()) {
                protocolService = new ProtocolServiceWrapper(protocolService, commonThreadPool, fetchThreadPool, produceThreadPool);
            }
            register(protocolService);
            result.add(protocolService);
        }
        for (ProtocolServer protocolServer : protocolServers) {
            if (!brokerContext.getBrokerConfig().getServerShardedThreads()) {
                protocolServer = new ProtocolServerWrapper(protocolServer, commonThreadPool, fetchThreadPool, produceThreadPool);
            }
            register(protocolServer);
            result.add(protocolServer);
        }
        return result;
    }

    protected void initProtocol(Protocol protocol) throws Exception {
        if (protocol instanceof BrokerContextAware) {
            ((BrokerContextAware) protocol).setBrokerContext(brokerContext);
        }
        if (protocol instanceof LifeCycle) {
            ((LifeCycle) protocol).start();
        }
    }

    protected void stopProtocol(Protocol protocol) throws Exception {
        if (protocol instanceof LifeCycle) {
            ((LifeCycle) protocol).stop();
        }
    }

    protected List<ProtocolService> doGetProtocolServices() {
        return Lists.newArrayList(ExtensionManager.getOrLoadExtensions(ProtocolService.class));
    }

    protected List<ProtocolServer> doGetProtocolServers() {
        return Lists.newArrayList(ExtensionManager.getOrLoadExtensions(ProtocolServer.class));
    }
}