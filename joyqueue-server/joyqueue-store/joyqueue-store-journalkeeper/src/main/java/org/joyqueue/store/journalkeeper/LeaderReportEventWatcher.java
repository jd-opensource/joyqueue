package org.joyqueue.store.journalkeeper;

import io.journalkeeper.rpc.URIParser;
import io.journalkeeper.utils.event.Event;
import io.journalkeeper.utils.event.EventType;
import io.journalkeeper.utils.event.EventWatcher;
import io.journalkeeper.utils.spi.ServiceSupport;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.domain.TopicName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**s
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
    private JournalKeeperStore store;
    public LeaderReportEventWatcher(String topic, int group,JournalKeeperStore store ,ClusterManager clusterManager) {
        this.topic = topic;
        this.group = group;
        this.clusterManager = clusterManager;
        this.store=store;
    }

    @Override
    public void onEvent(Event event) {
        Map<String, String> eventData = event.getEventData();
        switch(event.getEventType()) {
            case EventType.ON_LEADER_CHANGE:
                String leaderUri = eventData.get("leader");
                int term = Integer.parseInt(eventData.get("term"));
                logger.info("Leader changed: {}, term: {}, topic: {}, group: {}.", leaderUri, term, topic, group);
                int brokerId = joyQueueUriParser.getBrokerId(URI.create(leaderUri));
                clusterManager.leaderReport(new TopicName(topic), group, brokerId, new HashSet<>(), term);
                break;
            case EventType.ON_VOTERS_CHANGE:
                //Map<String, String> eventData = event.getEventData();
                String voters = eventData.get("voters");
                String currentVoter = eventData.get("currentVoter");
                logger.info("On voters change {},{}",currentVoter,voters);
                if(voters!=null&&currentVoter!=null) {
                    store.onVoteConfigChange(URI.create(currentVoter), parseVoters(voters));
                }
                break;
            default:
//                StringBuilder sb=new StringBuilder();
//                for(Map.Entry<String,String> e:eventData.entrySet()){
//                    sb.append(e.getKey()).append(":").append(e.getValue()).append("\n");
//                }
//                logger.info("On event {}",sb.toString());
                break;
        }
    }


    /**
     * Parse uri from string
     **/
    public List<URI> parseVoters(String voters){
        voters=voters.replace("[","");
        voters=voters.replace("]","");
        String[] uriArray=voters.split(",");
        List<URI> uris=new ArrayList<>();
        for(String u:uriArray) {
            uris.add(URI.create(u.trim()));
        }
        return uris;
    }
}
