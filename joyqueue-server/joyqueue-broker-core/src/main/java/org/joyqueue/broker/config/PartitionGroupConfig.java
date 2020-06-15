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
package org.joyqueue.broker.config;

import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.toolkit.config.PropertyDef;
import org.joyqueue.toolkit.config.PropertySupplier;

public class PartitionGroupConfig {
    private static final  int DEFAULT_ELECTION_TYPE= PartitionGroup.ElectType.fix.type();
    private static final  int DEFAULT_FIX_LEADER=-1;
    private static final  int DEFAULT_RECOMMEND_LEADER=-1;
    private PropertySupplier propertySupplier;
    public enum PartitionGroupConfigKey implements PropertyDef {
        ELECTION_TYPE("election.type", DEFAULT_ELECTION_TYPE, Type.INT),
        REC_LEADER("recommend.leader", DEFAULT_RECOMMEND_LEADER, Type.INT),
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

    /**
     * Recommend leader
     **/
    public int recommendLeader(){
        return PropertySupplier.getValue(propertySupplier, PartitionGroupConfigKey.REC_LEADER);
    }


}
