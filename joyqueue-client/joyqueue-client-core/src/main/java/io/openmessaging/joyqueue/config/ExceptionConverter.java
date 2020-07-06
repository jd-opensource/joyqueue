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
package io.openmessaging.joyqueue.config;

import org.joyqueue.client.internal.exception.ClientException;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.transport.exception.TransportException;
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
            return (OMSRuntimeException) cause;
        } else if (cause instanceof IllegalArgumentException) {
            return new OMSRuntimeException(JoyQueueCode.CN_PARAM_ERROR.getCode(), cause.getMessage(), cause);
        } else if (cause instanceof ClientException) {
            ClientException clientException = (ClientException) cause;
            if (clientException.getCause() instanceof TransportException.RequestTimeoutException) {
                return new OMSTimeOutException(JoyQueueCode.CN_REQUEST_TIMEOUT.getCode(), clientException.getMessage(), cause);
            }
            JoyQueueCode joyQueueCode = JoyQueueCode.valueOf(clientException.getCode());
            if (joyQueueCode.equals(JoyQueueCode.FW_TOPIC_NOT_EXIST) || joyQueueCode.equals(JoyQueueCode.FW_PRODUCER_NOT_EXISTS)) {
                return new OMSSecurityException(joyQueueCode.getCode(), clientException.getMessage(), cause);
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
                return new OMSTimeOutException(JoyQueueCode.CN_REQUEST_TIMEOUT.getCode(), clientException.getMessage(), cause);
            }
            JoyQueueCode joyQueueCode = JoyQueueCode.valueOf(clientException.getCode());
            if (joyQueueCode.equals(JoyQueueCode.FW_TOPIC_NOT_EXIST) || joyQueueCode.equals(JoyQueueCode.FW_PRODUCER_NOT_EXISTS)) {
                return new OMSSecurityException(joyQueueCode.getCode(), clientException.getMessage(), cause);
            }
            if (joyQueueCode.equals(JoyQueueCode.CN_PARAM_ERROR)) {
                return new OMSMessageFormatException(joyQueueCode.getCode(), clientException.getMessage(), cause);
            }
            if (joyQueueCode.equals(JoyQueueCode.CN_SERVICE_NOT_AVAILABLE)) {
                return new OMSDestinationException(joyQueueCode.getCode(), clientException.getMessage(), cause);
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
                return new OMSTimeOutException(JoyQueueCode.CN_REQUEST_TIMEOUT.getCode(), clientException.getMessage(), cause);
            }
            JoyQueueCode joyQueueCode = JoyQueueCode.valueOf(clientException.getCode());
            if (joyQueueCode.equals(JoyQueueCode.FW_TOPIC_NOT_EXIST) || joyQueueCode.equals(JoyQueueCode.FW_CONSUMER_NOT_EXISTS)) {
                return new OMSSecurityException(joyQueueCode.getCode(), clientException.getMessage(), cause);
            }
            if (joyQueueCode.equals(JoyQueueCode.CN_SERVICE_NOT_AVAILABLE)) {
                return new OMSDestinationException(joyQueueCode.getCode(), clientException.getMessage(), cause);
            }
            return new OMSRuntimeException(((ClientException) cause).getCode(), cause.getMessage(), cause);
        } else {
            return convertRuntimeException(cause);
        }
    }
}