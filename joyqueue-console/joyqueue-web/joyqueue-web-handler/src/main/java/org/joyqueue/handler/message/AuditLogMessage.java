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

import java.util.Date;

/**
 * Audit log message
 * Created by chenyanying3 on 19-3-3.
 */
public class AuditLogMessage {
    //用户
    protected String user;
    //时间
    protected Date time;
    //目标对象
    protected String target;
    //操作类型
    protected ActionType type;
    //消息
    protected String message;

    public AuditLogMessage() {
    }

    public AuditLogMessage(String user, String target, ActionType type, String message) {
        this(user, new Date(), target, type, message);
    }

    public AuditLogMessage(String user, Date time, String target, ActionType type, String message) {
        this.user = user;
        this.time = time;
        this.target = target;
        this.type = type;
        this.message = message;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public ActionType getType() {
        return type;
    }

    public void setType(ActionType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 配置变更事件类型
     */
    public enum ActionType {

        ADD("添加"),

        UPDATE("修改"),

        DELETE("删除");

        private String description;

        ActionType(String description) {
            this.description = description;
        }

        public String description() {
            return description;
        }
    }
}
