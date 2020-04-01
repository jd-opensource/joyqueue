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
package org.joyqueue.other.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author jiangnan53
 * @date 2020/4/1
 **/
@Service("topicMsgFilterOutput")
public class TopicMsgFilterOutputImpl implements TopicMsgFilterOutput {

    private static final Logger logger = LoggerFactory.getLogger(TopicMsgFilterOutputImpl.class);

    @Override
    public void output(String path) {
        logger.info("message filter file path: {}",path);
    }
}
