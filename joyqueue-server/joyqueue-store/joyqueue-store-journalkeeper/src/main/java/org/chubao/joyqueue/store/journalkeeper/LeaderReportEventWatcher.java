package org.chubao.joyqueue.store.journalkeeper;

import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.domain.TopicName;
import io.journalkeeper.rpc.URIParser;
import io.journalkeeper.utils.event.Event;
import io.journalkeeper.utils.event.EventType;
import io.journalkeeper.utils.event.EventWatcher;
import io.journalkeeper.utils.spi.ServiceSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;

/**
 * @author LiYue
 * Date: 2019/10/10
 */
public class LeaderReportEventWatcher implements EventWatcher {
    private static final Logger logger = LoggerFactory.getLogger(LeaderReportEventWatcher.class);
    private final String topic;
    private final int group;
    private final JoyQueueUriParser joyQueueUriParser = ServiceSupport.load(
            URIParser.class,
            JoyQueueUriParser.class.getCanonicalName());
    private final ClusterManager clusterManager;

    public LeaderReportEventWatcher(String topic, int group, ClusterManager clusterManager) {
        this.topic = topic;
        this.group = group;
        this.clusterManager = clusterManager;
    }

    @Override
    public void onEvent(Event event) {
        if (event.getEventType() == EventType.ON_LEADER_CHANGE ) {
            Map<String, String> eventData = event.getEventData();
            String leaderUri = eventData.get("leader");
            int term = Integer.parseInt(eventData.get("term"));
            logger.info("Leader changed: {}, term: {}, topic: {}, group: {}.", leaderUri, term, topic, group);
            int brokerId = joyQueueUriParser.getBrokerId(URI.create(leaderUri));
            clusterManager.leaderReport(new TopicName(topic), group, brokerId, new HashSet<>(), term);
        }
    }
}
