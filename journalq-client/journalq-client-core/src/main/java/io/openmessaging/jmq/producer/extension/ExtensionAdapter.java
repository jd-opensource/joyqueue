package io.openmessaging.jmq.producer.extension;

import com.jd.journalq.client.internal.metadata.domain.TopicMetadata;
import com.jd.journalq.client.internal.producer.MessageProducer;
import io.openmessaging.jmq.extension.AbstractExtensionAdapter;

/**
 * ExtensionAdapter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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