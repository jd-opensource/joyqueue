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
package org.joyqueue.broker.monitor;

import java.util.Map;

public interface PendingStat<K,V> {

   /**
    * 获取当前level的 积压
    **/
   long getPending();
   void setPending(long pending);

   void setPendingStatSubMap(Map<K,V> subMap);

   /**
    *  获取 sub level 的pending stat map
    *
    **/
   Map<K,V> getPendingStatSubMap();


}
