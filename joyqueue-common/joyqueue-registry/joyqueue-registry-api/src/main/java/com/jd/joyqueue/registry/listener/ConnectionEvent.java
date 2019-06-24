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
package com.jd.joyqueue.registry.listener;

import com.jd.joyqueue.toolkit.URL;

/**
 * 连接事件
 */
public class ConnectionEvent {

    private ConnectionEventType type;
    private URL url;

    public ConnectionEvent(ConnectionEventType type, URL url) {
        this.type = type;
        this.url = url;
    }

    public ConnectionEventType getType() {
        return type;
    }

    public URL getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "ConnectionEvent [type=" + type + ", url=" + url + "]";
    }

    public enum ConnectionEventType {

        /**
         * 第一次连接或者session过期后重连
         */
        CONNECTED,

        /**
         * 闪断，session未过期
         */
        SUSPENDED,

        /**
         * session未过期，重连成功
         */
        RECONNECTED,

        /**
         * 没连上，并且session失效
         */
        LOST,

        /**
         * 超过最大重试次数(connectionRetryTimes)并且没有重新连接上
         * (如果connectionRetryTimes=0时不会产生此事件)
         */
        FAILED
    }

}
