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
package org.joyqueue.network.command;


/**
 * 系统指令
 */
public class SystemCmd {
    // 启动Broker
    public static final String START_BROKER = "startBroker";
    // 停止Broker
    public static final String STOP_BROKER = "stopBroker";
    //指令类型
    protected String cmd;
    //参数
    protected String url;
    //执行超时时间
    protected int timeout = 3000;

    public SystemCmd cmd(String cmd) {
        setCmd(cmd);
        return this;
    }

    public SystemCmd url(String url) {
        setUrl(url);
        return this;
    }

    public SystemCmd timeout(int timeout) {
        setTimeout(timeout);
        return this;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SystemCmd{");
        sb.append("cmd='").append(cmd).append('\'');
        sb.append(", url='").append(url).append('\'');
        sb.append(", timeout=").append(timeout);
        sb.append('}');
        return sb.toString();
    }
}
