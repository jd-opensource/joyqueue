package io.openmessaging.joyqueue.producer.extension;

import io.chubao.joyqueue.client.internal.metadata.domain.TopicMetadata;
import io.chubao.joyqueue.client.internal.producer.MessageProducer;
import io.openmessaging.joyqueue.extension.AbstractExtensionAdapter;

/**
 * ExtensionAdapter
 *
 * author: gaohaoxiang
 * date: 2019/3/1
 */
public class ExtensionAdapter extends AbstractExtensionAdapter {

    private MessageProducer messageProducer;

    public ExtensionAdapter(MessageProducer messageProducer) {
        this.messageProducer = messageProducer;
    }

    @Override
    protected TopicMetadata getTopicMetadata(String queueName) {
        return messageProducer.getTopicMetadata(queueName);
    }
}