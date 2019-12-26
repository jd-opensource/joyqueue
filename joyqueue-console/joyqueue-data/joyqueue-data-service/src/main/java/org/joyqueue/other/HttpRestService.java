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
package org.joyqueue.other;

import org.joyqueue.monitor.RestResponse;

public interface HttpRestService {

      /**
       * @param pathKey  key in monitorUrl mapping
       * @param dataClass  data model
       * @param isList    data is list Object
       * @param args  request params
       **/
     <T> RestResponse<T> get(String pathKey,Class dataClass,boolean isList,String... args);

    /**
     * @param pathKey  key in monitorUrl mapping
     * @param dataClass  data model
     * @param isList    data is list Object
     * @param args  request params
     **/
    <T> RestResponse<T> put(String pathKey,Class dataClass,boolean isList,String content,String... args);


    /**
     * @param pathKey  key in monitorUrl mapping
     * @param dataClass  data model
     * @param isList    data is list Object
     * @param args  request params
     **/
    <T> RestResponse<T> delete(String pathKey,Class dataClass,boolean isList,String... args);


}
