package org.joyqueue.broker.kafka.handler;

import com.google.common.collect.Lists;
import org.joyqueue.broker.kafka.KafkaCommandType;
import org.joyqueue.broker.kafka.KafkaContext;
import org.joyqueue.broker.kafka.KafkaContextAware;
import org.joyqueue.broker.kafka.KafkaErrorCode;
import org.joyqueue.broker.kafka.command.SaslHandshakeRequest;
import org.joyqueue.broker.kafka.command.SaslHandshakeResponse;
import org.joyqueue.broker.kafka.config.KafkaConfig;
import org.joyqueue.broker.monitor.SessionManager;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.security.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * SaslHandshakeHandler
 * author: gaohaoxiang
 * date: 2020/4/9
 */
public class SaslHandshakeHandler extends AbstractKafkaCommandHandler implements KafkaContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(SaslHandshakeHandler.class);

    private KafkaConfig config;
    private SessionManager sessionManager;
    private Authentication authentication;

    private static List<String> mechanisms = Lists.newArrayList();

    static {
        mechanisms.add("PLAIN");
    }

    @Override
    public void setKafkaContext(KafkaContext kafkaContext) {
        this.config = kafkaContext.getConfig();
        this.sessionManager = kafkaContext.getBrokerContext().getSessionManager();
        this.authentication = kafkaContext.getBrokerContext().getAuthentication();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        SaslHandshakeRequest request = (SaslHandshakeRequest) command.getPayload();
        boolean isMatch = mechanisms.contains(request.getMechanism());
        SaslHandshakeResponse response = null;

        if (isMatch) {
            response = new SaslHandshakeResponse(KafkaErrorCode.NONE.getCode(), mechanisms);
        } else {
            response = new SaslHandshakeResponse(KafkaErrorCode.UNSUPPORTED_SASL_MECHANISM.getCode(), mechanisms);
        }
        return new Command(response);
    }

    @Override
    public int type() {
        return KafkaCommandType.SASL_HANDSHAKE.getCode();
    }
}