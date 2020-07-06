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
package org.joyqueue.broker.kafka.session;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.joyqueue.broker.kafka.command.ApiVersionsRequest;
import org.joyqueue.broker.kafka.command.FetchRequest;
import org.joyqueue.broker.kafka.command.FindCoordinatorRequest;
import org.joyqueue.broker.kafka.command.ProduceRequest;
import org.joyqueue.network.session.Language;
import org.joyqueue.network.transport.ChannelTransport;
import org.joyqueue.network.transport.TransportHelper;
import org.joyqueue.network.transport.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * kafka连接处理
 *
 * author: gaohaoxiang
 * date: 2018/7/3
 */
@ChannelHandler.Sharable
public class KafkaConnectionHandler extends ChannelDuplexHandler {

    protected static final Logger logger = LoggerFactory.getLogger(KafkaConnectionHandler.class);

    private KafkaConnectionManager kafkaConnectionManager;

    public KafkaConnectionHandler(KafkaConnectionManager kafkaConnectionManager) {
        this.kafkaConnectionManager = kafkaConnectionManager;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Command) {
            boolean handleConnection = this.handleConnection(ctx, (Command) msg);
            if (!handleConnection) {
                ctx.channel().close();
                return;
            }
        }
        super.channelRead(ctx, msg);
    }

    protected boolean handleConnection(ChannelHandlerContext ctx, Command command) {
        Channel channel = ctx.channel();
        Object payload = command.getPayload();
        ChannelTransport transport = TransportHelper.getTransport(channel);

        if (payload instanceof FetchRequest) {
            FetchRequest fetchRequest = (FetchRequest) payload;
            if (!kafkaConnectionManager.addConnection(transport, fetchRequest.getClientId(), String.valueOf(fetchRequest.getVersion()))) {
                return false;
            }
            for (Map.Entry<String, List<FetchRequest.PartitionRequest>> entry : fetchRequest.getPartitionRequests().entrySet()) {
                kafkaConnectionManager.addConsumer(transport, entry.getKey());
            }
        } else if (payload instanceof ProduceRequest) {
            ProduceRequest produceRequest = (ProduceRequest) payload;
            if (!kafkaConnectionManager.addConnection(transport, produceRequest.getClientId(), String.valueOf(produceRequest.getVersion()))) {
                return false;
            }
            for (Map.Entry<String, List<ProduceRequest.PartitionRequest>> entry : produceRequest.getPartitionRequests().entrySet()) {
                kafkaConnectionManager.addProducer(transport, entry.getKey());
            }
        } else if (payload instanceof FindCoordinatorRequest) {
//            FindCoordinatorRequest findCoordinatorRequest = (FindCoordinatorRequest) payload;
//            kafkaConnectionManager.addGroup(transport, findCoordinatorRequest.getCoordinatorKey());
        } else if (payload instanceof ApiVersionsRequest) {
            ApiVersionsRequest apiVersionsRequest = (ApiVersionsRequest) payload;
            if (StringUtils.isBlank(apiVersionsRequest.getClientSoftwareVersion())) {
                return true;
            }
            String language = StringUtils.replace(apiVersionsRequest.getClientSoftwareName(), "apache-kafka-", "");
            String version = apiVersionsRequest.getClientSoftwareVersion();
            if (Language.parse(language).equals(Language.OTHER)) {
                version = apiVersionsRequest.getClientSoftwareName() + "-" + apiVersionsRequest.getClientSoftwareVersion();
            }
            if (!kafkaConnectionManager.addConnection(transport, apiVersionsRequest.getClientId(), version, Language.parse(language))) {
                return false;
            }
        }
        return true;
    }
}