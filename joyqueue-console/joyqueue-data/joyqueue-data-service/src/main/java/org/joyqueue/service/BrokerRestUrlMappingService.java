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
package org.joyqueue.service;

import org.joyqueue.model.domain.Broker;

public interface BrokerRestUrlMappingService {

    /**
     *
     * @return  key 对应的path
     *
     **/
    String pathTemplate(String key);

    /**
     *
     * @param key  path key
     * @return  key 对应的path
     * 带有ip:port template前缀
     *
     **/
    String urlTemplate(String key);

    /**
     *
     * @return  http://ip:port
     *
     **/
    String monitorUrl(Broker broker);


    /**
     *
     * @return  http://ip:port
     *
     **/
    String url(String ip,int port);
}
