
package org.joyqueue.msg.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jiangnan53
 * @date 2020/4/3
 **/
public class DefaultTopicMsgFilterOutput implements TopicMsgFilterOutput{

    private static final Logger logger = LoggerFactory.getLogger(DefaultTopicMsgFilterOutput.class);

    @Override
    public void output(String path) {
        logger.info("default message filter file path: {}",path);
    }
}
