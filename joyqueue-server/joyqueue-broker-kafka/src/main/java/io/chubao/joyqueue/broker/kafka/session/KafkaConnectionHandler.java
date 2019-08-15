package io.chubao.joyqueue.broker.kafka.session;

import io.chubao.joyqueue.broker.kafka.command.FetchRequest;
import io.chubao.joyqueue.broker.kafka.command.FindCoordinatorRequest;
import io.chubao.joyqueue.broker.kafka.command.ProduceRequest;
import io.chubao.joyqueue.network.transport.ChannelTransport;
import io.chubao.joyqueue.network.transport.TransportHelper;
import io.chubao.joyqueue.network.transport.command.Command;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * kafka连接处理
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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
            this.connectionStatistic(ctx, (Command) msg);
        }
        super.channelRead(ctx, msg);
    }

    protected void connectionStatistic(ChannelHandlerContext ctx, Command command) {
        Channel channel = ctx.channel();
        Object payload = command.getPayload();
        ChannelTransport transport = TransportHelper.getTransport(channel);

        if (payload instanceof FetchRequest) {
            FetchRequest fetchRequest = (FetchRequest) payload;
            kafkaConnectionManager.addConnection(transport, fetchRequest.getClientId(), String.valueOf(fetchRequest.getVersion()));
            for (Map.Entry<String, List<FetchRequest.PartitionRequest>> entry : fetchRequest.getPartitionRequests().entrySet()) {
                kafkaConnectionManager.addConsumer(transport, entry.getKey());
            }
        } else if (payload instanceof ProduceRequest) {
            ProduceRequest produceRequest = (ProduceRequest) payload;
            kafkaConnectionManager.addConnection(transport, produceRequest.getClientId(), String.valueOf(produceRequest.getVersion()));
            for (Map.Entry<String, List<ProduceRequest.PartitionRequest>> entry : produceRequest.getPartitionRequests().entrySet()) {
                kafkaConnectionManager.addProducer(transport, entry.getKey());
            }
        } else if (payload instanceof FindCoordinatorRequest) {
            FindCoordinatorRequest findCoordinatorRequest = (FindCoordinatorRequest) payload;
            kafkaConnectionManager.addGroup(transport, findCoordinatorRequest.getCoordinatorKey());
        }
    }
}