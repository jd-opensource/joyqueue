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

/**
 *
 * Broker config key
 *
 **/
public enum ConfigDef {

    APPLICATION_DATA_PATH(null,"application.data.path","message store root path "),
    TRANSPORT_SERVER_PORT("broker.frontend-server.","transport.server.port","broker port"),
    NAME_SERVICE_MESSAGE_PORT("nameservice.","messenger.port", "name service message port"),
    NAME_SERVICE_CACHE_PATH("nameservice.","allmetadata.cache.file","name service cache path"),
    NAME_SERVER_JOURNAL_KEEPER_PORT("nameserver.","journalkeeper.port","bookeeper name server port "),
    NAME_SERVER_JOURNAL_KEEPER_WORKING_DIR("nameserver.","journalkeeper.working.dir"," journalkeeper working dir "),
    NAME_SERVER_JOURNAL_KEEPER_NODES("nameserver.","journalkeeper.nodes","journal keeper cluster config"),
    STORE_ENGINE("store.","engine","storage engine name");
    private  String region;
    private String name;
    private String desc;

    ConfigDef(String region, String name, String desc){
        this.region=region;
        this.name= name;
        this.desc=desc;
    }

    /**
     * property key
     *
     **/
    public String key(){
        return region==null?name:region+name;
    }
}
