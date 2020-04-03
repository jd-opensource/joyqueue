/**
 * Copyright 2019 The JoyQueue Authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
