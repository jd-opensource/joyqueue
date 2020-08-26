package org.joyqueue.broker.store;

import org.joyqueue.broker.config.PartitionGroupConfig;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.toolkit.config.PropertySupplier;

import java.util.HashMap;
import java.util.Map;

public class StoreUtils {

    /**
     * Create properties supplier from group
     **/
    public static PropertySupplier partitionGroupExtendProperties(PartitionGroup group){
        Map<String,Object> properties=new HashMap();
        properties.put(PartitionGroupConfig.PartitionGroupConfigKey.ELECTION_TYPE.getName(),group.getElectType().type());
        properties.put(PartitionGroupConfig.PartitionGroupConfigKey.FIX_LEADER.getName(),group.getLeader());
        properties.put(PartitionGroupConfig.PartitionGroupConfigKey.REC_LEADER.getName(),group.getRecLeader());
        return new PropertySupplier.MapSupplier(properties);
    }
}
