package org.joyqueue.msg.filter.support;

import org.joyqueue.msg.filter.Plugins;
import org.joyqueue.msg.filter.TopicMsgFilterMatcher;
import org.joyqueue.msg.filter.TopicMsgFilterOutput;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jiangnan53
 * @date 2020/4/3
 **/
public class TopicMessageFilterSupport {

    List<TopicMsgFilterOutput> filterOutputs;

    List<TopicMsgFilterMatcher> filterMatchers;

    public TopicMessageFilterSupport() {
        this.filterMatchers=loadFilterMatchers();
        this.filterOutputs=loadFilterOutputs();
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


    public void output(String input) {
        filterOutputs.forEach(output ->{
            output.output(input);
        });
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
