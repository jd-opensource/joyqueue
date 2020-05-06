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
package org.joyqueue.msg.filter.support;

import org.joyqueue.msg.filter.FilterResponse;
import org.joyqueue.msg.filter.Plugins;
import org.joyqueue.msg.filter.TopicMsgFilterMatcher;
import org.joyqueue.msg.filter.TopicMsgFilterOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jiangnan53
 * @date 2020/4/3
 **/
public class TopicMessageFilterSupport {

    private static final Logger logger = LoggerFactory.getLogger(TopicMessageFilterSupport.class);

    List<TopicMsgFilterOutput> filterOutputs;

    List<TopicMsgFilterMatcher> filterMatchers;

    public TopicMessageFilterSupport() {
        this.filterMatchers = loadFilterMatchers();
        this.filterOutputs = loadFilterOutputs();
    }

    protected List<TopicMsgFilterOutput> loadFilterOutputs() {
        List<TopicMsgFilterOutput> filterOutputHandlers = new ArrayList<>();
        for(TopicMsgFilterOutput output: Plugins.TOPIC_MSG_FILTER_OUTPUT.extensions()){
            filterOutputHandlers.add(output);
        }
        return filterOutputHandlers;
    }

    protected List<TopicMsgFilterMatcher> loadFilterMatchers(){
        List<TopicMsgFilterMatcher> filterMatchers = new ArrayList<>();
        for(TopicMsgFilterMatcher matcher:Plugins.TOPIC_MSG_FILTER_MATCHER.extensions()){
            filterMatchers.add(matcher);
        }
        return filterMatchers;
    }


    public List<FilterResponse> output(String input) {
        List<FilterResponse> responses = new ArrayList<>(filterOutputs.size());
        filterOutputs.forEach(output -> {
            try {
                responses.add(output.output(input));
            }catch (Exception e){
                logger.error("Output error, class: {}, error: {}",output.getClass(), e.getMessage());
            }
        });
        return responses;
    }

    public boolean match(String content, String filter) {
        for(TopicMsgFilterMatcher matcher:filterMatchers) {
            if (matcher.match(content,filter)) {
                return true;
            }
        }
        return false;
    }
}
