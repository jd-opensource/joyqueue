package io.chubao.joyqueue.nsr.message.support;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.event.MetaEvent;
import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.network.command.BooleanAck;
import io.chubao.joyqueue.network.transport.TransportServer;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.CommandCallback;
import io.chubao.joyqueue.network.transport.command.JoyQueueCommand;
import io.chubao.joyqueue.network.transport.exception.TransportException;
import io.chubao.joyqueue.nsr.config.MessengerConfig;
import io.chubao.joyqueue.nsr.exception.MessengerException;
import io.chubao.joyqueue.nsr.message.MessageListener;
import io.chubao.joyqueue.nsr.message.Messenger;
import io.chubao.joyqueue.nsr.message.support.network.command.MessengerPublishRequest;
import io.chubao.joyqueue.nsr.message.support.network.transport.MessengerTransportServerFactory;
import io.chubao.joyqueue.nsr.message.support.session.MessengerSessionManager;
import io.chubao.joyqueue.toolkit.concurrent.EventBus;
import io.chubao.joyqueue.toolkit.config.PropertySupplier;
import io.chubao.joyqueue.toolkit.config.PropertySupplierAware;
import io.chubao.joyqueue.toolkit.service.Service;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * DefaultMessenger
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public class DefaultMessenger extends Service implements Messenger<MetaEvent>, PropertySupplierAware {

    protected static final Logger logger = LoggerFactory.getLogger(DefaultMessenger.class);

    private final EventBus eventBus = new EventBus("joyqueue-messenger-eventBus");

    private MessengerConfig config;
    private MessengerSessionManager messengerSessionManager;
    private TransportServer messengerTransportServer;

    @Override
    protected void validate() throws Exception {
        messengerSessionManager = new MessengerSessionManager(config);
    }

    @Override
    protected void doStart() throws Exception {
        config.getServerConfig().setPort(config.getPort());
        messengerTransportServer = new MessengerTransportServerFactory(eventBus).bind(config.getServerConfig());
        messengerTransportServer.start();
        messengerSessionManager.start();
        eventBus.start();
    }

    @Override
    protected void doStop() {
        messengerSessionManager.stop();
        messengerTransportServer.stop();
        eventBus.stop();
    }

    @Override
    public void publish(MetaEvent event, List<Broker> brokers) {
        if (CollectionUtils.isEmpty(brokers)) {
            return;
        }

        boolean[] success = {true};
        CountDownLatch latch = new CountDownLatch(brokers.size());

        for (Broker broker : brokers) {
            MessengerPublishRequest messengerPublishRequest = new MessengerPublishRequest(event);
            try {
                messengerSessionManager.getOrCreateSession(broker.getId(), broker.getIp(), config.getPort())
                        .async(new JoyQueueCommand(messengerPublishRequest), config.getSessionTimeout(), new CommandCallback() {
                            @Override
                            public void onSuccess(Command request, Command response) {
                                BooleanAck booleanAck = (BooleanAck) response.getPayload();

                                if (booleanAck.getHeader().getStatus() != JoyQueueCode.SUCCESS.getCode()) {
                                    logger.warn("messenger publish error, event: {}, id: {}, ip: {}, port: {}, code: {}",
                                            event, broker.getId(), broker.getIp(), broker.getPort(), booleanAck.type());

                                    success[0] = false;
                                }
                                latch.countDown();
                            }

                            @Override
                            public void onException(Command request, Throwable cause) {
                                logger.warn("messenger publish error, event: {}, id: {}, ip: {}, port: {}",
                                        event, broker.getId(), broker.getIp(), broker.getPort(), cause);

                                boolean isSuccess = false;
                                if (cause instanceof TransportException.RequestErrorException) {
                                    if (config.getPublishIgnoreConnectionError()) {
                                        isSuccess = true;
                                    }
                                }

                                success[0] = isSuccess;
                                latch.countDown();
                            }
                        });
            } catch (Exception e) {
                logger.warn("create session exception, event: {}, brokerId: {}, brokerIp: {}, brokerPort: {}",
                        event, broker.getId(), broker.getIp(), broker.getPort(), e);

                if (config.getPublishIgnoreConnectionError()) {
                    latch.countDown();
                } else {
                    success[0] = false;
                }
            }
        }

        try {
            if (!latch.await(config.getPublishTimeout(), TimeUnit.MILLISECONDS)) {
                logger.warn("messenger publish timeout, event: {}, brokers: {}, timeout: {}",
                        event, brokers, config.getPublishTimeout());

                if (!config.getPublishForce()) {
                    throw new MessengerException("messenger publish timeout");
                }
            }
        } catch (InterruptedException e) {
            throw new MessengerException("messenger publish exception", e);
        }

        if (!config.getPublishForce()) {
            if (!success[0]) {
                logger.warn("messenger publish failed, event: {}, brokers: {}", event, brokers);
                throw new MessengerException("messenger publish failed");
            }
        }
    }

    @Override
    public void publish(MetaEvent event, Broker... brokers) {
        publish(event, Lists.newArrayList(brokers));
    }

    @Override
    public void fastPublish(MetaEvent event, List<Broker> brokers) {
        if (CollectionUtils.isEmpty(brokers)) {
            return;
        }

        for (Broker broker : brokers) {
            try {
                MessengerPublishRequest messengerPublishRequest = new MessengerPublishRequest(event);
                messengerSessionManager.getOrCreateSession(broker.getId(), broker.getIp(), config.getPort())
                        .oneway(new JoyQueueCommand(messengerPublishRequest), config.getSessionTimeout());
            } catch (Exception e) {
                logger.warn("messenger fastPublish failed, event: {}, broker: {}", event, broker);
            }
        }
    }

    @Override
    public void fastPublish(MetaEvent event, Broker... brokers) {
        fastPublish(event, Lists.newArrayList(brokers));
    }

    @Override
    public void addListener(MessageListener listener) {
        eventBus.addListener(listener);
    }

    @Override
    public void setSupplier(PropertySupplier supplier) {
        this.config = new MessengerConfig(supplier);
    }

    @Override
    public String type() {
        return "default";
    }
}