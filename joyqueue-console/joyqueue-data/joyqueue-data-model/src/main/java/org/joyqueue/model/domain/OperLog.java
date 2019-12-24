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
package org.joyqueue.model.domain;

/**
 * oper log model
 *
 * @author liyubo4
 * @create 2017-12-07 16:20
 **/
public class OperLog extends BaseModel{

    /**
     * oper object type,eg:cluster/program/job
     */
    private Integer type;

    /**
     * oper object id,eg:cluster/program/job id
     */
    private String identity;

    /**
     * oper type,eg:create cluster / expand cluster
     */
    private Integer operType;

    /**
     * oper target
     */
    private String target;

    public enum OperType implements EnumItem {
        ADD(1,"增"),
        DELETE(2,"删"),
        UPDATE(3,"更新"),
        QUERY(4,"查询");

        OperType(int value, String description){
            this.value = value;
            this.description = description;
        }
        private int value;
        private String description;

        @Override
        public int value() { return this.value; }
        @Override
        public String description() { return this.description; }
    }

    public enum Type implements EnumItem {
        TOPIC(1,"topic"),
        CONSUMER(2,"consumer"),
        PRODUCER(3,"producer"),
        CONFIG(4,"config"),
        APP_TOKEN(5,"appToken"),
        GROUP(6,"group"),
        BROKER(7,"broker"),
        DATA_CENTER(8,"dataCenter"),
        NAMESPACE(9,"namespace"),
        PARTITION_GROUP(10,"partitionGroup"),
        REPLICA(11,"partitionGroupReplica");

        Type(int value, String description) {
            this.value = value;
            this.description = description;
        }

        private int value;
        private String description;

        @Override
        public int value() {
            return value;
        }

        @Override
        public String description() {
            return description;
        }

        public static Type resolve(String descOrName) {
            for (Type type : Type.values()) {
                if (type.description().equals(descOrName) || type.name().equals(descOrName)) {
                    return type;
                }
            }
            return null;
        }
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public Integer getOperType() {
        return operType;
    }

    public void setOperType(Integer operType) {
        this.operType = operType;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}