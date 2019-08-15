package io.openmessaging.joyqueue.extension;

import io.chubao.joyqueue.client.internal.metadata.domain.TopicMetadata;
import com.google.common.base.Preconditions;
import io.openmessaging.extension.Extension;
import io.openmessaging.extension.QueueMetaData;
import io.openmessaging.joyqueue.config.ExceptionConverter;
import org.apache.commons.lang3.StringUtils;

/**
 * AbstractExtensionAdapter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/3/1
 */
public abstract class AbstractExtensionAdapter implements Extension {

    @Override
    public QueueMetaData getQueueMetaData(String queueName) {
        try {
            Preconditions.checkArgument(StringUtils.isNotBlank(queueName), "queueName can not be null");

            TopicMetadata topicMetadata = getTopicMetadata(queueName);
            if (topicMetadata == null) {
                return null;
            }
            return TopicMetadataConverter.convert(topicMetadata);
        } catch (Throwable cause) {
            throw ExceptionConverter.convertRuntimeException(cause);
        }
    }

    protected abstract TopicMetadata getTopicMetadata(String queueName);
}