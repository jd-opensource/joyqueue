package io.openmessaging.joyqueue.consumer.extension;

import io.chubao.joyqueue.client.internal.consumer.MessageConsumer;
import io.chubao.joyqueue.client.internal.metadata.domain.TopicMetadata;
import io.openmessaging.joyqueue.extension.AbstractExtensionAdapter;

/**
 * ExtensionAdapter
 *
 * author: gaohaoxiang
 * date: 2019/3/1
 */
public class ExtensionAdapter extends AbstractExtensionAdapter {

    private MessageConsumer messageConsumer;

    public ExtensionAdapter(MessageConsumer messageConsumer) {
        this.messageConsumer = messageConsumer;
    }

    @Override
    protected TopicMetadata getTopicMetadata(String queueName) {
        return messageConsumer.getTopicMetadata(queueName);
    }
}