package io.openmessaging.joyqueue.config;

import io.chubao.joyqueue.client.internal.exception.ClientException;
import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.network.transport.exception.TransportException;
import io.openmessaging.exception.OMSDestinationException;
import io.openmessaging.exception.OMSMessageFormatException;
import io.openmessaging.exception.OMSRuntimeException;
import io.openmessaging.exception.OMSSecurityException;
import io.openmessaging.exception.OMSTimeOutException;

/**
 * ClientExceptionConverter
 *
 * author: gaohaoxiang
 * date: 2019/2/19
 */
public class ExceptionConverter {

    public static OMSRuntimeException convertRuntimeException(Throwable cause) {
        if (cause instanceof OMSRuntimeException) {
            throw (OMSRuntimeException) cause;
        } else if (cause instanceof IllegalArgumentException) {
            return new OMSRuntimeException(JoyQueueCode.CN_PARAM_ERROR.getCode(), cause.getMessage(), cause);
        } else if (cause instanceof ClientException) {
            ClientException clientException = (ClientException) cause;
            if (clientException.getCause() instanceof TransportException.RequestTimeoutException) {
                throw new OMSTimeOutException(JoyQueueCode.CN_REQUEST_TIMEOUT.getCode(), clientException.getMessage(), cause);
            }
            JoyQueueCode joyQueueCode = JoyQueueCode.valueOf(clientException.getCode());
            if (joyQueueCode.equals(JoyQueueCode.FW_TOPIC_NOT_EXIST) || joyQueueCode.equals(JoyQueueCode.FW_PRODUCER_NOT_EXISTS)) {
                throw new OMSSecurityException(joyQueueCode.getCode(), clientException.getMessage(), cause);
            }
            return new OMSRuntimeException(((ClientException) cause).getCode(), cause.getMessage(), cause);
        } else {
            return new OMSRuntimeException(JoyQueueCode.CN_UNKNOWN_ERROR.getCode(), JoyQueueCode.CN_UNKNOWN_ERROR.getMessage(), cause);
        }
    }

    public static OMSRuntimeException convertProduceException(Throwable cause) {
        if (cause instanceof ClientException) {
            ClientException clientException = (ClientException) cause;
            if (clientException.getCause() instanceof TransportException.RequestTimeoutException) {
                throw new OMSTimeOutException(JoyQueueCode.CN_REQUEST_TIMEOUT.getCode(), clientException.getMessage(), cause);
            }
            JoyQueueCode joyQueueCode = JoyQueueCode.valueOf(clientException.getCode());
            if (joyQueueCode.equals(JoyQueueCode.FW_TOPIC_NOT_EXIST) || joyQueueCode.equals(JoyQueueCode.FW_PRODUCER_NOT_EXISTS)) {
                throw new OMSSecurityException(joyQueueCode.getCode(), clientException.getMessage(), cause);
            }
            if (joyQueueCode.equals(JoyQueueCode.CN_PARAM_ERROR)) {
                throw new OMSMessageFormatException(joyQueueCode.getCode(), clientException.getMessage(), cause);
            }
            if (joyQueueCode.equals(JoyQueueCode.CN_SERVICE_NOT_AVAILABLE)) {
                throw new OMSDestinationException(joyQueueCode.getCode(), clientException.getMessage(), cause);
            }
            return new OMSRuntimeException(clientException.getCode(), clientException.getMessage(), cause);
        } else {
            return convertRuntimeException(cause);
        }
    }

    public static OMSRuntimeException convertConsumeException(Throwable cause) {
        if (cause instanceof ClientException) {
            ClientException clientException = (ClientException) cause;
            if (clientException.getCause() instanceof TransportException.RequestTimeoutException) {
                throw new OMSTimeOutException(JoyQueueCode.CN_REQUEST_TIMEOUT.getCode(), clientException.getMessage(), cause);
            }
            JoyQueueCode joyQueueCode = JoyQueueCode.valueOf(clientException.getCode());
            if (joyQueueCode.equals(JoyQueueCode.FW_TOPIC_NOT_EXIST) || joyQueueCode.equals(JoyQueueCode.FW_CONSUMER_NOT_EXISTS)) {
                throw new OMSSecurityException(joyQueueCode.getCode(), clientException.getMessage(), cause);
            }
            if (joyQueueCode.equals(JoyQueueCode.CN_SERVICE_NOT_AVAILABLE)) {
                throw new OMSDestinationException(joyQueueCode.getCode(), clientException.getMessage(), cause);
            }
            return new OMSRuntimeException(((ClientException) cause).getCode(), cause.getMessage(), cause);
        } else {
            return convertRuntimeException(cause);
        }
    }
}