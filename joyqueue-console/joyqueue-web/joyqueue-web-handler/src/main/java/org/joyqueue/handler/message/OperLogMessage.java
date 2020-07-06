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
package org.joyqueue.handler.message;

import org.joyqueue.model.domain.Identity;
import org.joyqueue.model.domain.OperLog;

import java.util.Date;

/**
 * 操作日志消息
 * Created by wangxiaofei1 on 2018/12/4.
 */
public class OperLogMessage extends OperLog{

    public OperLogMessage() {
        super();
    }

    public OperLogMessage(Integer operType, Integer type, String identity, String target, Long userId) {
        super.setOperType(operType);
        super.setType(type);
        super.setIdentity(identity);
        super.setTarget(target);
        super.setCreateBy(new Identity(userId));
        super.setCreateTime(new Date());
    }
}
