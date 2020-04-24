package org.joyqueue.broker.cluster;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.joyqueue.broker.cluster.config.ClusterConfig;
import org.joyqueue.broker.cluster.config.ClusterConfigKey;
import org.joyqueue.domain.TopicConfig;
import org.joyqueue.domain.TopicName;
import org.joyqueue.event.EventType;
import org.joyqueue.event.NameServerEvent;
import org.joyqueue.nsr.NameService;
import org.joyqueue.nsr.event.AddConfigEvent;
import org.joyqueue.nsr.event.RemoveConfigEvent;
import org.joyqueue.nsr.event.UpdateConfigEvent;
import org.joyqueue.toolkit.concurrent.EventListener;
import org.joyqueue.toolkit.service.Service;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * ClusterNameServiceCache
 * author: gaohaoxiang
 * date: 2020/3/30
 */
public class ClusterNameServiceCache extends Service {

    private ClusterConfig config;
    private NameService nameService;

    private volatile Cache<String, TopicConfig> topicConfigCache;

    public ClusterNameServiceCache(ClusterConfig config, NameService nameService) {
        this.config = config;
        this.nameService = nameService;
    }

    @Override
    protected void validate() throws Exception {
        this.nameService.addListener(new EventListener<NameServerEvent>() {
            @Override
            public void onEvent(NameServerEvent event) {
                if (event.getEventType().equals(EventType.UPDATE_CONFIG)) {
                    UpdateConfigEvent updateConfigEvent = (UpdateConfigEvent) event.getMetaEvent();
                    if (updateConfigEvent.getNewConfig().getKey().equals(ClusterConfigKey.GET_TOPIC_DYNAMIC_METADATA_CACHE_EXPIRE_TIME.getName())) {
                        rebuildTopicConfigCache();
                    }
                } else if (event.getEventType().equals(EventType.ADD_CONFIG)) {
                    AddConfigEvent addConfigEvent = (AddConfigEvent) event.getMetaEvent();
                    if (addConfigEvent.getConfig().getKey().equals(ClusterConfigKey.GET_TOPIC_DYNAMIC_METADATA_CACHE_EXPIRE_TIME.getName())) {
                        rebuildTopicConfigCache();
                    }
                } else if (event.getEventType().equals(EventType.REMOVE_CONFIG)) {
                    RemoveConfigEvent removeConfigEvent = (RemoveConfigEvent) event.getMetaEvent();
                    if (removeConfigEvent.getConfig().getKey().equals(ClusterConfigKey.GET_TOPIC_DYNAMIC_METADATA_CACHE_EXPIRE_TIME.getName())) {
                        rebuildTopicConfigCache();
                    }
                }
            }
        });
        rebuildTopicConfigCache();
    }

    protected void rebuildTopicConfigCache() {
        Cache<String, TopicConfig> oldTopicConfigCache = this.topicConfigCache;
        this.topicConfigCache = CacheBuilder.newBuilder()
                .expireAfterWrite(config.getTopicDynamicMetadataCacheExpireTime(), TimeUnit.MILLISECONDS)
                .build();
        if (oldTopicConfigCache != null) {
            oldTopicConfigCache.cleanUp();
        }
    }

    public TopicConfig getTopicConfig(TopicName topicName, Callable<TopicConfig> callable) throws ExecutionException {
        return topicConfigCache.get(topicName.getFullName(), callable);
    }
}