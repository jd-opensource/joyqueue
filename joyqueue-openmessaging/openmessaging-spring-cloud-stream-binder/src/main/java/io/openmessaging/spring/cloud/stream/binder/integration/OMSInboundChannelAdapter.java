/**
 * Copyright 2019 The JoyQueue Authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.openmessaging.spring.cloud.stream.binder.integration;

import io.openmessaging.consumer.BatchMessageListener;
import io.openmessaging.consumer.MessageListener;
import io.openmessaging.message.Message;
import io.openmessaging.spring.cloud.stream.binder.consuming.OMSListenerBindingContainer;
import io.openmessaging.spring.cloud.stream.binder.properties.OMSConsumerProperties;
import io.openmessaging.spring.cloud.stream.binder.utils.MessageUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessagingException;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;

import java.util.List;

/**
 * OMS Inbound Channel Adapter
 */
public class OMSInboundChannelAdapter extends MessageProducerSupport {

    private static final Log log = LogFactory.getLog(OMSInboundChannelAdapter.class);

    private RetryTemplate retryTemplate;

    private RecoveryCallback<? extends Object> recoveryCallback;

    private final OMSListenerBindingContainer omsListenerContainer;

    private final ExtendedConsumerProperties<OMSConsumerProperties> consumerProperties;

    public OMSInboundChannelAdapter(OMSListenerBindingContainer omsListenerContainer,
                                    ExtendedConsumerProperties<OMSConsumerProperties> consumerProperties) {
        this.omsListenerContainer = omsListenerContainer;
        this.consumerProperties = consumerProperties;
    }

    @Override
    protected void onInit() {
        if (consumerProperties == null || !consumerProperties.getExtension().getEnable()) {
            return;
        }
        super.onInit();
        if (this.retryTemplate != null) {
            Assert.state(getErrorChannel() == null,
                    "Cannot have an 'errorChannel' property when a 'RetryTemplate' is "
                            + "provided; use an 'ErrorMessageSendingRecover' in the 'recoveryCallback' property to "
                            + "send an error message when retries are exhausted");
        }
        BindingOMSListener listener = new BindingOMSListener();
        BindingOMSBatchListener batchListener = new BindingOMSBatchListener();
        omsListenerContainer.setMessageListener(listener);
        omsListenerContainer.setBatchMessageListener(batchListener);
        if (retryTemplate != null) {
            this.retryTemplate.registerListener(listener);
            this.retryTemplate.registerListener(batchListener);
        }
        try {
            omsListenerContainer.afterPropertiesSet();
        } catch (Exception e) {
            throw new IllegalArgumentException("omsListenerContainer init error: " + e.getMessage(), e);
        }
    }

    @Override
    protected void doStart() {
        if (consumerProperties == null || !consumerProperties.getExtension().getEnable()) {
            return;
        }
        try {
            omsListenerContainer.start();
        } catch (Exception e) {
            log.error("jmqListenerContainer startup failed, Caused by " + e.getMessage());
            throw new MessagingException(MessageBuilder.withPayload("jmqListenerContainer startup failed, Caused by " + e.getMessage()).build(), e);
        }
    }

    @Override
    protected void doStop() {
        omsListenerContainer.stop();
    }

    public RetryTemplate getRetryTemplate() {
        return retryTemplate;
    }

    public void setRetryTemplate(RetryTemplate retryTemplate) {
        this.retryTemplate = retryTemplate;
    }

    public RecoveryCallback<? extends Object> getRecoveryCallback() {
        return recoveryCallback;
    }

    public void setRecoveryCallback(RecoveryCallback<? extends Object> recoveryCallback) {
        this.recoveryCallback = recoveryCallback;
    }

    /**
     * Binding listener
     */
    protected class BindingOMSListener implements MessageListener, RetryListener {

        @Override
        public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
            return true;
        }

        @Override
        public <T, E extends Throwable> void close(RetryContext context,
                                                   RetryCallback<T, E> callback, Throwable throwable) {
        }

        @Override
        public <T, E extends Throwable> void onError(RetryContext context,
                                                     RetryCallback<T, E> callback, Throwable throwable) {

        }

        @Override
        public void onReceived(Message message, Context context) {
            boolean enableRetry = OMSInboundChannelAdapter.this.retryTemplate != null;
            if (enableRetry) {
                OMSInboundChannelAdapter.this.retryTemplate.execute(retryContext -> {
                    OMSInboundChannelAdapter.this.sendMessage(MessageUtil.convert2SpringMessage(message));
                    return null;
                }, (RecoveryCallback<Object>) OMSInboundChannelAdapter.this.recoveryCallback);
            } else {
                OMSInboundChannelAdapter.this.sendMessage(MessageUtil.convert2SpringMessage(message));
            }
        }
    }

    /**
     * Binding batch listener
     */
    protected class BindingOMSBatchListener implements BatchMessageListener, RetryListener {

        @Override
        public void onReceived(List<Message> list, Context context) {
            boolean enableRetry = OMSInboundChannelAdapter.this.retryTemplate != null;
            if (enableRetry) {
                OMSInboundChannelAdapter.this.retryTemplate.execute(retryContext -> {
                    list.forEach(message -> {
                        OMSInboundChannelAdapter.this.sendMessage(MessageUtil.convert2SpringMessage(message));
                    });
                    return null;
                }, (RecoveryCallback<Object>) OMSInboundChannelAdapter.this.recoveryCallback);
            } else {
                list.forEach(message -> {
                    OMSInboundChannelAdapter.this.sendMessage(MessageUtil.convert2SpringMessage(message));
                });
            }
        }

        @Override
        public <T, E extends Throwable> boolean open(RetryContext retryContext, RetryCallback<T, E> retryCallback) {
            return true;
        }

        @Override
        public <T, E extends Throwable> void close(RetryContext retryContext, RetryCallback<T, E> retryCallback, Throwable throwable) {

        }

        @Override
        public <T, E extends Throwable> void onError(RetryContext retryContext, RetryCallback<T, E> retryCallback, Throwable throwable) {

        }
    }

}
