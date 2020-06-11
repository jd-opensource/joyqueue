package org.joyqueue.broker.config;

import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.toolkit.config.PropertyDef;
import org.joyqueue.toolkit.config.PropertySupplier;

public class PartitionGroupConfig {
    private final static int DEFAULT_ELECTION_TYPE= PartitionGroup.ElectType.fix.type();
    private final static int DEFAULT_FIX_LEADER=-1;
    private PropertySupplier propertySupplier;
    public enum PartitionGroupConfigKey implements PropertyDef {
        ELECTION_TYPE("election.type", DEFAULT_ELECTION_TYPE, Type.INT),
        FIX_LEADER("fix.leader", DEFAULT_FIX_LEADER, Type.INT);
        private String name;
        private Object value;
        private Type type;
        PartitionGroupConfigKey(String name, Object value, Type type) {
            this.name = name;
            this.value = value;
            this.type = type;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public Type getType() {
            return type;
        }
    }

    public PartitionGroupConfig(PropertySupplier supplier){
        this.propertySupplier=supplier;
    }

    /**
     *  Election type
     **/
    public PartitionGroup.ElectType electionType(){
        int electType= PropertySupplier.getValue(propertySupplier, PartitionGroupConfigKey.ELECTION_TYPE);
        return PartitionGroup.ElectType.valueOf(electType);
    }
    /**
     * Fix leader
     **/
    public int fixLeader(){
        return PropertySupplier.getValue(propertySupplier, PartitionGroupConfigKey.FIX_LEADER);
    }


}
