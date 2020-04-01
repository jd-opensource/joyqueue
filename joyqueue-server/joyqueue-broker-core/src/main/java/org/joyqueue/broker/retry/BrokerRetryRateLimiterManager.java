package org.joyqueue.broker.retry;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.consumer.ConsumeConfig;
import org.joyqueue.broker.consumer.ConsumeConfigKey;
import org.joyqueue.broker.limit.RateLimiter;
import org.joyqueue.broker.limit.support.DefaultRateLimiter;
import org.joyqueue.domain.Config;
import org.joyqueue.event.MetaEvent;
import org.joyqueue.nsr.event.RemoveConfigEvent;
import org.joyqueue.nsr.event.RemoveConsumerEvent;
import org.joyqueue.nsr.event.RemoveTopicEvent;
import org.joyqueue.nsr.event.UpdateConfigEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * Consumer retry rate limiter manager
 *
 **/
public class BrokerRetryRateLimiterManager implements RetryRateLimiter{
    protected static final Logger LOG = LoggerFactory.getLogger(BrokerRetryRateLimiterManager.class);
    private ClusterManager clusterManager;
    private ConsumeConfig consumeConfig;
    private ConcurrentMap<String /** topic **/, ConcurrentMap<String /** app **/, RateLimiter>> retryRateLimiters = Maps.newConcurrentMap();

    public BrokerRetryRateLimiterManager(BrokerContext context){
        this.clusterManager = context.getClusterManager();
        this.clusterManager.addListener(this);
        this.consumeConfig=new ConsumeConfig(context != null ? context.getPropertySupplier() : null);
    }
    @Override
    public RateLimiter getOrCreate(String topic, String app) {
        ConcurrentMap<String,RateLimiter> topicRateLimiters=retryRateLimiters.get(topic);
        if(topicRateLimiters==null){
            topicRateLimiters=new ConcurrentHashMap();
            ConcurrentMap<String,RateLimiter>  old=retryRateLimiters.putIfAbsent(topic,topicRateLimiters);
            if(old!=null){
                topicRateLimiters=old;
            }
        }
        RateLimiter consumerRetryRateLimiter=topicRateLimiters.get(app);
        if(consumerRetryRateLimiter==null){
            int tps=consumerRetryRate(topic,app);
            if(tps>0) { // ulimit
                consumerRetryRateLimiter = new DefaultRateLimiter(tps);
                RateLimiter oldRateLimiter = topicRateLimiters.putIfAbsent(app, consumerRetryRateLimiter);
                if (oldRateLimiter != null) {
                    consumerRetryRateLimiter = oldRateLimiter;
                }else{
                    LOG.info("New rate limiter for {},{},{}",topic,app,tps);
                }
            }
        }
        return consumerRetryRateLimiter;
    }


    /**
     *  Mix broker level consume retry rate(default ulimit) with consumer level config
     *  priority:
     *    1. broker consumer level
     *    2. broker level
     *    3. consumer config level(not support)
     *  @return  -1 indicate ulimit if above all not configure
     *
     **/
    public int consumerRetryRate(String topic,String app){
        int retryRate=consumeConfig.getRetryRate(topic,app);
        if(retryRate<=0){
            // get broker level retry rate
            retryRate=consumeConfig.getRetryRate();
        }
        return retryRate;
    }


    /**
     * Clean up current rate limiter if consumer metadata has any changes
     **/
    @Override
    public void onEvent(MetaEvent event) {
        switch (event.getEventType()) {
            case UPDATE_CONFIG: {
                UpdateConfigEvent updateConfigEvent = (UpdateConfigEvent) event;
                Config config = updateConfigEvent.getNewConfig();
                cleanRateLimiter(config);
                break;
            }
            case REMOVE_CONFIG: {
                RemoveConfigEvent removeConfigEvent = (RemoveConfigEvent) event;
                Config config=removeConfigEvent.getConfig();
                cleanRateLimiter(config);
                break;
            }
            case REMOVE_TOPIC:
                RemoveTopicEvent topicEvent = (RemoveTopicEvent) event;
                cleanRateLimiter(topicEvent.getTopic().getName().getFullName(),null);
                break;
            case REMOVE_CONSUMER:
                RemoveConsumerEvent removeConsumerEvent = (RemoveConsumerEvent) event;
                cleanRateLimiter(removeConsumerEvent.getTopic().getFullName(), removeConsumerEvent.getConsumer().getApp());
                break;
        }
    }

    /**
     * @param config  consumer config
     *
     **/
    public void cleanRateLimiter(Config config){
        String configKey=config.getKey();
        if (StringUtils.isBlank(configKey)) {
            return;
        }

        if (configKey.equals(ConsumeConfigKey.RETRY_RATE.getName())) {
            retryRateLimiters.clear();
        } else if (configKey.startsWith(ConsumeConfigKey.RETRY_RATE_PREFIX.getName())) {
            String[] keys=configKey.split("\\.");
            if(keys.length == 4){
                String topic=keys[2];
                String app=keys[3];
                if(topic!=null&&app!=null) {
                    cleanRateLimiter(topic, app);
                }
            }
        }
    }

    /**
     * Clean rate limiter of consumer
     **/
    public void cleanRateLimiter(String topic,String app) {
        if (app == null) {
            retryRateLimiters.remove(topic);
        }else {
            Map<String, RateLimiter> rateLimiters = retryRateLimiters.get(topic);
            if (rateLimiters != null) {
                rateLimiters.remove(app);
            }

        }
    }
}
