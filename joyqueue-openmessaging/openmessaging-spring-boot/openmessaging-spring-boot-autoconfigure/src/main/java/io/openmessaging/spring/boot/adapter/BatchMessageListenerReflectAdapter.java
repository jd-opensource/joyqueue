package io.openmessaging.spring.boot.adapter;

import io.openmessaging.consumer.BatchMessageListener;
import io.openmessaging.exception.OMSRuntimeException;
import io.openmessaging.message.Message;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Adapter for the BatchMessageListener.
 *
 * @version OMS 1.0.0
 * @since OMS 1.0.0
 */
public class BatchMessageListenerReflectAdapter implements BatchMessageListener {

    private Object instance;
    private Method method;

    public BatchMessageListenerReflectAdapter(Object instance, Method method) {
        this.instance = instance;
        this.method = method;
        this.method.setAccessible(true);
    }

    @Override
    public void onReceived(List<Message> batchMessage, Context context) {
        try {
            method.invoke(instance, batchMessage, context);
        } catch (Exception e) {
            throw new OMSRuntimeException(-1, e.getMessage(), e);
        }
    }
}