package io.openmessaging.journalq.consumer.extension;

import com.jd.journalq.client.internal.consumer.MessageConsumer;
import com.jd.journalq.client.internal.metadata.domain.TopicMetadata;
import io.openmessaging.journalq.extension.AbstractExtensionAdapter;

/**
 * ExtensionAdapter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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