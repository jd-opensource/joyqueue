/**
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
package com.jd.journalq.broker.kafka.command;

import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.model.ProducePartitionStatus;
import com.jd.journalq.broker.network.traffic.ProduceTrafficPayload;
import com.jd.journalq.broker.network.traffic.Traffic;

import java.util.List;
import java.util.Map;

/**
 * Created by zhangkepeng on 16-8-1.
 */
public class ProduceResponse extends KafkaRequestOrResponse implements ProduceTrafficPayload {

    private Traffic traffic;
    private Map<String, List<ProducePartitionStatus>> producerResponseStatuss;

    public ProduceResponse() {

    }

    public ProduceResponse(Traffic traffic, Map<String, List<ProducePartitionStatus>> producerResponseStatuss) {
        this.traffic = traffic;
        this.producerResponseStatuss = producerResponseStatuss;
    }

    public Map<String, List<ProducePartitionStatus>> getProducerResponseStatuss() {
        return producerResponseStatuss;
    }

    public void setProducerResponseStatuss(Map<String, List<ProducePartitionStatus>> producerResponseStatuss) {
        this.producerResponseStatuss = producerResponseStatuss;
    }

    @Override
    public Traffic getTraffic() {
        return traffic;
    }

    @Override
    public int type() {
        return KafkaCommandType.PRODUCE.getCode();
    }
}