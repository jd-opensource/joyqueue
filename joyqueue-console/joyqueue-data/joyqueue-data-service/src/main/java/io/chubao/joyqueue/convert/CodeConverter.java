package io.chubao.joyqueue.convert;

import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.model.domain.AppName;
import io.chubao.joyqueue.model.domain.Identity;
import io.chubao.joyqueue.model.domain.Namespace;
import io.chubao.joyqueue.model.domain.Topic;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

/**
 * @author wylixiaobin
 * Date: 2018/11/29
 */
public class CodeConverter {
    public static TopicName convertTopic(Namespace namespace, Topic topic){
        Preconditions.checkArgument(topic!=null,"topic can't be null");
        return TopicName.parse(topic.getCode(),null==namespace?TopicName.DEFAULT_NAMESPACE:namespace.getCode());
    }

    public static TopicName convertTopicFullName(String fullName) {
        Preconditions.checkArgument(StringUtils.isNotBlank(fullName),"topic full name can't be null");
        return new TopicName(fullName);
    }

    public static String convertApp(Identity app,String subscribeGroup){
        Preconditions.checkArgument(app!=null,"app can't be null");
        return AppName.parse(app.getCode(), subscribeGroup).getFullName();
    }

    public static AppName convertAppFullName(String fullName){
        Preconditions.checkArgument(StringUtils.isNotBlank(fullName),"app full name can't be null");
        return AppName.parse(fullName);
    }

}
