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
package io.chubao.joyqueue.service.impl;

import io.chubao.joyqueue.model.domain.OperLog;
import io.chubao.joyqueue.model.query.QOperLog;
import io.chubao.joyqueue.repository.OperLogRepository;
import io.chubao.joyqueue.service.OperLogService;
import org.springframework.stereotype.Service;

/**
 * @author liyubo4
 * @create 2017-12-07 18:52
 **/
@Service("operLogService")
public class OperLogServiceImpl extends PageServiceSupport<OperLog, QOperLog,OperLogRepository> implements OperLogService {

}
