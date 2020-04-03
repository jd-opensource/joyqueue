package org.joyqueue.msg.filter;

import com.jd.laf.extension.ExtensionPoint;
import com.jd.laf.extension.ExtensionPointLazy;
import com.jd.laf.extension.SpiLoader;

/**
 * @author jiangnan53
 * @date 2020/4/3
 **/
public interface Plugins {

    ExtensionPoint<TopicMsgFilterOutput, String> TOPIC_MSG_FILTER_OUTPUT = new ExtensionPointLazy<>(TopicMsgFilterOutput.class, SpiLoader.INSTANCE, null, null);

    ExtensionPoint<TopicMsgFilterMatcher, String> TOPIC_MSG_FILTER_MATCHER = new ExtensionPointLazy<>(TopicMsgFilterMatcher.class, SpiLoader.INSTANCE, null, null);

}
