package io.openmessaging.jmq.support;

import com.jd.journalq.common.exception.JMQCode;
import com.jd.journalq.toolkit.service.Service;
import io.openmessaging.ServiceLifeState;
import io.openmessaging.ServiceLifecycle;
import io.openmessaging.exception.OMSRuntimeException;
import io.openmessaging.jmq.config.ExceptionConverter;

/**
 * AbstractServiceLifecycle
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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
                throw new OMSRuntimeException(JMQCode.CN_UNKNOWN_ERROR.getCode(),
                        String.format("service state error, current: %s", serviceState));
        }
    }
}