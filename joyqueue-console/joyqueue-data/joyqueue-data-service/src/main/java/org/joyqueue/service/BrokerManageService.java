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

import org.joyqueue.toolkit.io.Directory;

public interface BrokerManageService {

    /**
     * Broker store tree view
     * @param recursive  recurse child directory if true
     * @return store tree view
     **/
    Directory storeTreeView(int brokerId,boolean recursive);

    /**
     * Delete garbage file on broker, which name start with .d.
     * @return true if delete success
     **/
    boolean deleteGarbageFile(int brokerId,String path,boolean retain);



}
