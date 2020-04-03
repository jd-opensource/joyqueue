package org.joyqueue.msg.filter;

import org.apache.commons.lang3.StringUtils;

/**
 * @author jiangnan53
 * @date 2020/4/3
 **/
public class DefaultTopicMsgFilterMatcher implements TopicMsgFilterMatcher {
    @Override
    public boolean match(String content, String filter) {
        return StringUtils.containsIgnoreCase(content,filter);
    }
}
