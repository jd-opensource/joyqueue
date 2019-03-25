package com.jd.journalq.convert;

import com.jd.journalq.common.domain.TopicName;
import com.jd.journalq.model.domain.AppName;
import com.jd.journalq.model.domain.Identity;
import com.jd.journalq.model.domain.Namespace;
import com.jd.journalq.model.domain.Topic;
import com.jd.journalq.toolkit.lang.Preconditions;
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
