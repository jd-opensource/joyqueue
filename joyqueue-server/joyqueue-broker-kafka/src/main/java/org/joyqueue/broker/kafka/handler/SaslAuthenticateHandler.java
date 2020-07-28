package org.joyqueue.broker.kafka.handler;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.joyqueue.broker.kafka.KafkaCommandType;
import org.joyqueue.broker.kafka.KafkaContext;
import org.joyqueue.broker.kafka.KafkaContextAware;
import org.joyqueue.broker.kafka.KafkaErrorCode;
import org.joyqueue.broker.kafka.command.SaslAuthenticateRequest;
import org.joyqueue.broker.kafka.command.SaslAuthenticateResponse;
import org.joyqueue.broker.kafka.config.KafkaConfig;
import org.joyqueue.broker.kafka.helper.KafkaClientHelper;
import org.joyqueue.broker.kafka.network.helper.KafkaSessionHelper;
import org.joyqueue.broker.monitor.SessionManager;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.response.BooleanResponse;
import org.joyqueue.security.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SaslAuthenticateHandler
 * author: gaohaoxiang
 * date: 2020/4/9
 */
public class SaslAuthenticateHandler extends AbstractKafkaCommandHandler implements KafkaContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(SaslAuthenticateHandler.class);

    private KafkaConfig config;
    private SessionManager sessionManager;
    private Authentication authentication;

    @Override
    public void setKafkaContext(KafkaContext kafkaContext) {
        this.config = kafkaContext.getConfig();
        this.sessionManager = kafkaContext.getBrokerContext().getSessionManager();
        this.authentication = kafkaContext.getBrokerContext().getAuthentication();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        SaslAuthenticateRequest request = (SaslAuthenticateRequest) command.getPayload();
        SaslAuthenticateRequest.SaslAuthenticateData authData = request.getData();
        SaslAuthenticateResponse response = null;

        try {
            String[] clientIds = StringUtils.splitByWholeSeparator(KafkaClientHelper.parseClient(request.getClientId()), ".");
            String clientId = ArrayUtils.isEmpty(clientIds) ? null : clientIds[0];
            if (!StringUtils.equals(clientId, authData.getApp())) {
                logger.error("sasl authentication failed, clientId not equals app, transport: {}, request: {}", transport, request);
                response = new SaslAuthenticateResponse(KafkaErrorCode.SASL_AUTHENTICATION_FAILED.getCode(), "clientId not equals app",
                        request.getAuthBytes(), 0);
            } else {
                BooleanResponse authResponse = authentication.auth(authData.getApp(), authData.getToken());
                if (authResponse.isSuccess()) {
                    KafkaSessionHelper.setIsAuth(transport, true);
                    response = new SaslAuthenticateResponse(KafkaErrorCode.NONE.getCode(), null, request.getAuthBytes(), 0);
                } else {
                    logger.error("sasl authentication failed, transport: {}, request: {}, code: {}", transport, request, authResponse.getJoyQueueCode());
                    response = new SaslAuthenticateResponse(KafkaErrorCode.SASL_AUTHENTICATION_FAILED.getCode(), authResponse.getJoyQueueCode().name(),
                            request.getAuthBytes(), 0);
                }
            }
        } catch (Exception e) {
            logger.error("sasl authentication exception, transport: {}, request: {}", transport, request, e);
            response = new SaslAuthenticateResponse(KafkaErrorCode.SASL_AUTHENTICATION_FAILED.getCode(), "SASL Authentication failed",
                    request.getAuthBytes(), 0);
        }
        return new Command(response);
    }

    @Override
    public int type() {
        return KafkaCommandType.SASL_AUTHENTICATE.getCode();
    }
}