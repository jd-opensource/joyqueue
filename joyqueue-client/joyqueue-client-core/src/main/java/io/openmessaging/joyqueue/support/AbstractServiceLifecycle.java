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
package io.openmessaging.joyqueue.support;

import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.toolkit.service.Service;
import io.openmessaging.ServiceLifeState;
import io.openmessaging.ServiceLifecycle;
import io.openmessaging.exception.OMSRuntimeException;
import io.openmessaging.joyqueue.config.ExceptionConverter;

/**
 * AbstractServiceLifecycle
 *
 * author: gaohaoxiang
 * date: 2019/2/19
 */
public abstract class AbstractServiceLifecycle extends Service implements ServiceLifecycle {

    @Override
    public void start() {
        try {
            super.start();
        } catch (Exception e) {
            throw ExceptionConverter.convertRuntimeException(e);
        }
    }

    @Override
    public void stop() {
        try {
            super.stop();
        } catch (Exception e) {
            throw ExceptionConverter.convertRuntimeException(e);
        }
    }

    @Override
    public ServiceLifeState currentState() {
        ServiceState serviceState = super.getServiceState();
        switch (serviceState) {
            case WILL_START:
                return ServiceLifeState.INITIALIZED;
            case STARTING:
                return ServiceLifeState.STARTING;
            case STARTED:
            case START_FAILED:
                return ServiceLifeState.STARTED;
            case WILL_STOP:
            case STOPPING:
                return ServiceLifeState.STOPPING;
            case STOPPED:
                return ServiceLifeState.STOPPED;
            default:
                throw new OMSRuntimeException(JoyQueueCode.CN_UNKNOWN_ERROR.getCode(),
                        String.format("service state error, current: %s", serviceState));
        }
    }
}