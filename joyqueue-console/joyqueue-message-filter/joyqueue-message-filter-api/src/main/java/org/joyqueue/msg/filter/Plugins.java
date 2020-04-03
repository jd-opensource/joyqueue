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
