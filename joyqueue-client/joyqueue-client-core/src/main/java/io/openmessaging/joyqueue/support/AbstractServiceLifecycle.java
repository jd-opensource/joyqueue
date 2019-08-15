package io.openmessaging.joyqueue.support;

import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.toolkit.service.Service;
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