package org.joyqueue.broker.consumer.position;

import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.consumer.Consume;
import org.joyqueue.broker.consumer.ConsumeConfig;
import org.joyqueue.broker.consumer.command.ReplicateConsumePosRequest;
import org.joyqueue.broker.consumer.command.ReplicateConsumePosResponse;
import org.joyqueue.broker.consumer.model.ConsumePartition;
import org.joyqueue.broker.consumer.position.model.Position;
import org.joyqueue.broker.network.session.BrokerTransportManager;
import org.joyqueue.broker.network.session.BrokerTransportSession;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.TopicName;
import org.joyqueue.network.command.CommandType;
import org.joyqueue.network.transport.codec.JoyQueueHeader;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.CommandCallback;
import org.joyqueue.network.transport.command.Direction;
import org.joyqueue.store.PartitionGroupStore;
import org.joyqueue.store.StoreService;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * Sync consume position from the leader to followers
 *
 * @author LiYue
 * Date: 2019/12/12
 */
class ConsumePositionReplicator {
    private static final Logger logger = LoggerFactory.getLogger(ConsumePositionReplicator.class);
    private final StoreService storeService;
    private final Consume consume;
    private final BrokerTransportManager brokerTransportManager;
    private final ClusterManager clusterManager;
    private final ConsumeConfig consumeConfig;
    private final ConsumePositionReplicationManager consumePositionReplicationManager;
    ConsumePositionReplicator(StoreService storeService, Consume consume, BrokerTransportManager brokerTransportManager, ClusterManager clusterManager, ConsumeConfig consumeConfig) {
        this.storeService = storeService;
        this.consume = consume;
        this.brokerTransportManager = brokerTransportManager;
        this.clusterManager = clusterManager;
        this.consumeConfig=consumeConfig;
        this.consumePositionReplicationManager=new DefaultConsumePositionReplicationManager(consumeConfig.getBroadcastIndexResetInterval());
    }

    /**
     *  向Followers 复制消费位置
     **/
    void replicateConsumePosition() {
        consumePositionReplicationManager.onReplicateRoundStart();
        storeService.getAllStores().stream()
                .filter(PartitionGroupStore::writable)
                .forEach(store -> {
                    try {
                        TopicName topic = TopicName.parse(store.getTopic());
                        int group = store.getPartitionGroup();
                        int localReplicaId = clusterManager.getBrokerId();
                        Map<ConsumePartition, Position> consumePositions = consume.getConsumePositionByGroup(TopicName.parse(store.getTopic()), store.getPartitionGroup());
                        if (consumePositions == null||consumePositions.size()==0) {
                            logger.debug("Partition group {}/node {} consume partition info empty",store.getTopic(), clusterManager.getBrokerId());
                            return;
                        }
                        // clone position
                        Map<ConsumePartition, Position>  snapshot=consumePositionSnapshot(consumePositions);
                        ReplicateConsumePosRequest request = new ReplicateConsumePosRequest(snapshot);
                        JoyQueueHeader header = new JoyQueueHeader(Direction.REQUEST, CommandType.REPLICATE_CONSUME_POS_REQUEST);
                        Command command = new Command(header, request);

                        PartitionGroup partitionGroup = clusterManager.getPartitionGroup(topic, group);
                        if(partitionGroup == null) {
                            logger.warn("Partition group not found in cluster manager! Topic: {}, group: {}.", topic.getFullName(), group);
                            return;
                        }

                        partitionGroup.getReplicas().stream()
                                .filter(r -> r != localReplicaId&&consumePositionReplicationManager.needReplicate(topic.getFullName(),group,snapshot,r))
                                .map(clusterManager::getBrokerById)
                                .forEach(broker -> {
                                    try {
                                        BrokerTransportSession session = brokerTransportManager.getOrCreateSession(broker);
                                        session.async(command, new ReplicateConsumePosRequestCallback(topic.getFullName(), group,snapshot,
                                                consumePositionReplicationManager, localReplicaId, broker.getId()));
                                    } catch (Throwable t) {
                                        logger.warn("Partition group {}-{}/node {} send replicate consume pos message fail",
                                                topic.getFullName(), group, localReplicaId, t);

                                    }
                                });
                    } catch (Throwable e) {
                        logger.warn("Partition group {}-{} send replicate consume pos message fail",
                                store.getTopic(), store.getPartitionGroup(), e);
                    }

                });
        consumePositionReplicationManager.onReplicateRoundEnd();
    }

    /**
     * Consume position snapshot
     **/
    public  Map<ConsumePartition, Position> consumePositionSnapshot(Map<ConsumePartition, Position> consumePositions) throws CloneNotSupportedException{
        Map<ConsumePartition, Position> consumePositionsNew=new HashMap<>();
        for(Map.Entry<ConsumePartition,Position> e:consumePositions.entrySet()){
            consumePositionsNew.put(e.getKey(),e.getValue().clone());
        }
        return consumePositionsNew;
    }

    public interface ConsumePositionReplicationManager {

        /**
         * 复制前判断是否需要复制
         *
         **/
        boolean needReplicate(String topic,int partitionGroup,Map<ConsumePartition, Position> positionMap,int replicaId);

        /**
         * 一轮复制开始
         **/
        void  onReplicateRoundStart();
        /**
         * 一轮复制结束
         **/
        void  onReplicateRoundEnd();
        /**
         * 复制完成回调
         **/
        void onReplicateComplete(String topic,int partitionGroup,Map<ConsumePartition, Position> positionMap,int replicaId);
    }


    /**
     *  每隔一段时间，需要开启强制复制消费位置
     *  非强制复制消费位置状态下，只有消费位置发生变化才复制
     *
     **/
    public class DefaultConsumePositionReplicationManager implements  ConsumePositionReplicationManager{

        private static final long DEFAULT_LAST_REPLICATE_TIME_MS=-1;
        private static final int  DEFAULT_MAX_FORCE_REPLICATE_ROUND=3;
        /**
         * 强制复制的间隔
         **/
        private long forceIntervalMs;
        private long lastForceReplicateTimeMs=DEFAULT_LAST_REPLICATE_TIME_MS;
        /**
         * 强制复制开关
         **/
        private boolean forceReplicate=false;
        private int forceReplicateRound=0;

        private Map<String/*topic-partition group-replicaId*/,Map<ConsumePartition, Position>> lastReplicatedConsumePosition=new ConcurrentHashMap<>();
        public DefaultConsumePositionReplicationManager(long forceIntervalMs){
            this.forceIntervalMs=forceIntervalMs;
        }
        @Override
        public boolean needReplicate(String topic,int partitionGroup,Map<ConsumePartition, Position> positionMap, int replicaId) {
            if(!forceReplicate){
               String key= newKey(topic,partitionGroup,replicaId);
               Map<ConsumePartition,Position> last=lastReplicatedConsumePosition.get(key);
               // 有上次复制的缓存，且partition数一致
               if(last!=null&&last.size()==positionMap.size()&&!mayUpdate(last,positionMap)){
                   logger.info("{} Partition group {} consume position not change",topic,partitionGroup);
                   return false;
               }
            }
            return true;
        }

        /**
         *
         * 如果新的消费位置信息和上次复制的一样，则不需要更新，否则需要更新
         *
         **/
        public boolean mayUpdate(Map<ConsumePartition,Position> old,Map<ConsumePartition,Position> ne){
            for(Map.Entry<ConsumePartition,Position> e: ne.entrySet()){
                 Position oldPos=old.get(e.getKey());
                 if(oldPos==null||!oldPos.equals(e.getValue())){
                     return true;
                 }
            }
            return false;
        }

        @Override
        public void onReplicateComplete(String topic,int partitionGroup,Map<ConsumePartition, Position> positionMap, int replicaId) {
            String key= newKey(topic,partitionGroup,replicaId);
            lastReplicatedConsumePosition.put(key,positionMap);
        }

        public String newKey(String key,int partitionGroup,int replicaId){
            return key+"-"+partitionGroup+"-"+replicaId;
        }

        @Override
        public void onReplicateRoundStart() {
            if(lastForceReplicateTimeMs==DEFAULT_LAST_REPLICATE_TIME_MS){
                lastForceReplicateTimeMs= SystemClock.now();
                forceReplicate=true;
            }
            if(!forceReplicate&&SystemClock.now()-lastForceReplicateTimeMs>forceIntervalMs){
                forceReplicate=true;
                lastForceReplicateTimeMs=SystemClock.now();
            }
            if(forceReplicate){
                // 清理下
                lastReplicatedConsumePosition.clear();
                logger.debug("On force replicate consume offsets state");
            }
        }

        @Override
        public void onReplicateRoundEnd() {
             if(forceReplicate){
                 forceReplicateRound++;
                 if(forceReplicateRound>=DEFAULT_MAX_FORCE_REPLICATE_ROUND){
                     // 关闭强制复制
                     forceReplicate=false;
                     forceReplicateRound=0;
                     logger.debug("On close force replicate consume offsets state");
                 }
             }
        }
    }

    /**
     * Callback of replicate consume pos request command
     */
    private static class ReplicateConsumePosRequestCallback implements CommandCallback {
        private final int remoteBrokerId;
        private final String topic;
        private final int group;
        private final int localBrokerId;
        private final Map<ConsumePartition,Position> positionMap;
        private final ConsumePositionReplicationManager consumePositionReplicationManager;
        ReplicateConsumePosRequestCallback(String topic, int group,Map<ConsumePartition,Position> positionMap,
                                           ConsumePositionReplicationManager consumePositionReplicationManager, int localBrokerId, int remoteBrokerId) {
            this.topic = topic;
            this.group = group;
            this.positionMap=positionMap;
            this.consumePositionReplicationManager=consumePositionReplicationManager;
            this.localBrokerId = localBrokerId;
            this.remoteBrokerId = remoteBrokerId;
        }

        @Override
        public void onSuccess(Command request, Command responseCommand) {
            if (!(responseCommand.getPayload() instanceof ReplicateConsumePosResponse)) {
                return;
            }
            ReplicateConsumePosResponse response = (ReplicateConsumePosResponse)responseCommand.getPayload();
            if (!response.isSuccess()) {
                logger.info("Partition group {}-{}/node {} replicate consume pos to {} fail",
                        topic, group, localBrokerId, remoteBrokerId);
            }else{
              consumePositionReplicationManager.onReplicateComplete(topic,group,positionMap,remoteBrokerId);
            }
        }

        @Override
        public void onException(Command request, Throwable cause) {
            logger.info("Partition group {}-{}/node {} replicate consume pos to {} fail",
                    topic, group, localBrokerId, remoteBrokerId, cause);
        }
    }

}
