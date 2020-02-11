package org.joyqueue.broker.retry;

import com.google.common.collect.Maps;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.consumer.ConsumeConfig;
import org.joyqueue.broker.limit.RateLimiter;
import org.joyqueue.broker.limit.support.DefaultRateLimiter;
import org.joyqueue.domain.Config;
import org.joyqueue.domain.Consumer;
import org.joyqueue.domain.TopicName;
import org.joyqueue.event.MetaEvent;
import org.joyqueue.nsr.event.*;
import org.joyqueue.toolkit.concurrent.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * Consumer retry rate limiter manager
 *
 **/
public class BrokerRetryRateLimiterManager implements RetryRateLimiter, EventListener<MetaEvent>{
    protected static final Logger logger = LoggerFactory.getLogger(BrokerRetryRateLimiterManager.class);
    private ClusterManager clusterManager;
    private ConsumeConfig consumeConfig;
    private static final int DEFAULT_CONSUMER_RETRY_RATE=500;
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
            consumerRetryRateLimiter=new DefaultRateLimiter(tps,Integer.MAX_VALUE);
            RateLimiter oldRateLimiter=topicRateLimiters.putIfAbsent(app,consumerRetryRateLimiter);
            if(oldRateLimiter!=null){
                consumerRetryRateLimiter= oldRateLimiter;
            }
        }
        return consumerRetryRateLimiter;
    }


    /**
     *  Mix broker level consume retry rate with consumer level config
     *  priority:
     *    1. broker consumer level
     *    2. broker level
     *    3. consumer config level
     *
     **/
    public int consumerRetryRate(String topic,String app){
        Consumer consumer = clusterManager.tryGetConsumer(TopicName.parse(topic), app);
        int consumerLevelRetryRate=DEFAULT_CONSUMER_RETRY_RATE;
        int retryRate=consumeConfig.getRetryRate(topic,app);
        if(retryRate<0){
            // get broker level retry rate
            retryRate=consumeConfig.getRetryRate();
        }
        if (consumer != null && consumer.getConsumerPolicy()!= null){
            consumerLevelRetryRate=consumer.getConsumerPolicy().getRetryRate();
        }
        return retryRate>0? Math.min(consumeConfig.getRetryRate(),consumerLevelRetryRate):consumerLevelRetryRate;
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
                RemoveConfigEvent removeConsumerEvent = (RemoveConfigEvent) event;
                Config config=removeConsumerEvent.getConfig();
                cleanRateLimiter(config);
                break;
            }
        }
    }

    /**
     * @param config  consumer config
     *
     **/
    public void cleanRateLimiter(Config config){
        String configKey=config.getKey();
        if(configKey!=null){
            String[] keys=configKey.split("_");
            if(keys.length>=3){
                String topic=keys[1];
                String app=keys[2];
                cleanRateLimiter(topic,app);
            }
        }
    }

    /**
     * Clean rate limiter of consumer
     **/
    public void cleanRateLimiter(String topic,String app){
       Map<String,RateLimiter> rateLimiters= retryRateLimiters.get(topic);
       if(rateLimiters!=null){
           rateLimiters.remove(app);
       }
    }
}
