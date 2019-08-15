package io.openmessaging.spring.boot.adapter;

import io.openmessaging.consumer.MessageListener;
import io.openmessaging.exception.OMSRuntimeException;
import io.openmessaging.message.Message;

import java.lang.reflect.Method;

/**
 * Adapter for the MessageListener.
 *
 * @version OMS 1.0.0
 * @since OMS 1.0.0
 */
public class MessageListenerReflectAdapter implements MessageListener {

    private Object instance;
    private Method method;

    public MessageListenerReflectAdapter(Object instance, Method method) {
        this.instance = instance;
        this.method = method;
        this.method.setAccessible(true);
    }

    @Override
    public void onReceived(Message message, Context context) {
        try {
            method.invoke(instance, message, context);
        } catch (Exception e) {
            throw new OMSRuntimeException(-1, e.getMessage(), e);
        }
    }
}