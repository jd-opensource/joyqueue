package io.openmessaging.jmq.config;

import com.jd.journalq.client.internal.exception.ClientException;
import com.jd.journalq.common.exception.JMQCode;
import com.jd.journalq.common.network.transport.exception.TransportException;
import io.openmessaging.exception.OMSDestinationException;
import io.openmessaging.exception.OMSMessageFormatException;
import io.openmessaging.exception.OMSRuntimeException;
import io.openmessaging.exception.OMSSecurityException;
import io.openmessaging.exception.OMSTimeOutException;

/**
 * ClientExceptionConverter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/19
 */
public class ExceptionConverter {

    public static OMSRuntimeException convertRuntimeException(Throwable cause) {
        if (cause instanceof OMSRuntimeException) {
            throw (OMSRuntimeException) cause;
        } else if (cause instanceof IllegalArgumentException) {
            return new OMSRuntimeException(JMQCode.CN_PARAM_ERROR.getCode(), cause.getMessage(), cause);
        } else if (cause instanceof ClientException) {
            ClientException clientException = (ClientException) cause;
            if (clientException.getCause() instanceof TransportException.RequestTimeoutException) {
                throw new OMSTimeOutException(JMQCode.CN_REQUEST_TIMEOUT.getCode(), clientException.getMessage(), cause);
            }
            JMQCode jmqCode = JMQCode.valueOf(clientException.getCode());
            if (jmqCode.equals(JMQCode.FW_TOPIC_NOT_EXIST) || jmqCode.equals(JMQCode.FW_PRODUCER_NOT_EXISTS)) {
                throw new OMSSecurityException(jmqCode.getCode(), clientException.getMessage(), cause);
            }
            return new OMSRuntimeException(((ClientException) cause).getCode(), cause.getMessage(), cause);
        } else {
            return new OMSRuntimeException(JMQCode.CN_UNKNOWN_ERROR.getCode(), JMQCode.CN_UNKNOWN_ERROR.getMessage(), cause);
        }
    }

    public static OMSRuntimeException convertProduceException(Throwable cause) {
        if (cause instanceof ClientException) {
            ClientException clientException = (ClientException) cause;
            if (clientException.getCause() instanceof TransportException.RequestTimeoutException) {
                throw new OMSTimeOutException(JMQCode.CN_REQUEST_TIMEOUT.getCode(), clientException.getMessage(), cause);
            }
            JMQCode jmqCode = JMQCode.valueOf(clientException.getCode());
            if (jmqCode.equals(JMQCode.FW_TOPIC_NOT_EXIST) || jmqCode.equals(JMQCode.FW_PRODUCER_NOT_EXISTS)) {
                throw new OMSSecurityException(jmqCode.getCode(), clientException.getMessage(), cause);
            }
            if (jmqCode.equals(JMQCode.CN_PARAM_ERROR)) {
                throw new OMSMessageFormatException(jmqCode.getCode(), clientException.getMessage(), cause);
            }
            if (jmqCode.equals(JMQCode.CN_SERVICE_NOT_AVAILABLE)) {
                throw new OMSDestinationException(jmqCode.getCode(), clientException.getMessage(), cause);
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
                throw new OMSTimeOutException(JMQCode.CN_REQUEST_TIMEOUT.getCode(), clientException.getMessage(), cause);
            }
            JMQCode jmqCode = JMQCode.valueOf(clientException.getCode());
            if (jmqCode.equals(JMQCode.FW_TOPIC_NOT_EXIST) || jmqCode.equals(JMQCode.FW_CONSUMER_NOT_EXISTS)) {
                throw new OMSSecurityException(jmqCode.getCode(), clientException.getMessage(), cause);
            }
            if (jmqCode.equals(JMQCode.CN_SERVICE_NOT_AVAILABLE)) {
                throw new OMSDestinationException(jmqCode.getCode(), clientException.getMessage(), cause);
            }
            return new OMSRuntimeException(((ClientException) cause).getCode(), cause.getMessage(), cause);
        } else {
            return convertRuntimeException(cause);
        }
    }
}