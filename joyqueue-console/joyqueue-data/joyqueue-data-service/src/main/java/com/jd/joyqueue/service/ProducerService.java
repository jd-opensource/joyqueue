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
package com.jd.joyqueue.service;

import com.jd.joyqueue.model.domain.Producer;
import com.jd.joyqueue.model.query.QProducer;
import com.jd.joyqueue.nsr.NsrService;

public interface ProducerService extends NsrService<Producer, QProducer,String> {

    Producer findByTopicAppGroup(String namespace,String topic,String app);

}
