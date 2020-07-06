/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.convert;

import org.joyqueue.domain.TopicName;
import org.joyqueue.model.domain.AppName;
import org.joyqueue.model.domain.Identity;
import org.joyqueue.model.domain.Namespace;
import org.joyqueue.model.domain.Topic;
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
