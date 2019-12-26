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

import org.joyqueue.model.domain.PartitionOffset;
import org.joyqueue.model.domain.Subscribe;
import org.joyqueue.monitor.PartitionAckMonitorInfo;
import org.joyqueue.monitor.PartitionLeaderAckMonitorInfo;

import java.util.List;

//todo 待移走
public interface ConsumeOffsetService {

    /**
     *
     * @return all partition low ,upper index bound and current ack index
     **/
    List<PartitionLeaderAckMonitorInfo> offsets(Subscribe subscribe);


    /**
     *
     * @return  partition  offset
     **/
    long offset(Subscribe subscribe,short partition);

    /**
     * @return  partition message  offset  of @code timeMs
     **/
    List<PartitionAckMonitorInfo> timeOffset(Subscribe subscribe, long timeMs);


    /**
     * Reset partition offset of the @Subscribe
     **/
    boolean resetOffset(Subscribe subscribe,short partition,long offset);

    /**
     *  Reset offset of the @Subscribe by time
     **/
    boolean resetOffset(Subscribe subscribe,long timeMs);


    /**
     *  Reset offset of the @Subscribe by offset
     **/
    boolean resetOffset(Subscribe subscribe, List<PartitionOffset> offsets);

}
