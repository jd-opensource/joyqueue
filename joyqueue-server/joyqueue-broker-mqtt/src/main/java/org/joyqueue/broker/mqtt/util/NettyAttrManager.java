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
package org.joyqueue.broker.mqtt.util;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

/**
 * @author majun8
 */
public class NettyAttrManager {

    public static final String CLIENT_ID = "ClientID";//客户端ID
    public static final String CLEAN_SESSION = "cleanSession";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String KEEP_ALIVE = "keepAlive";//心跳包时长
    public static final String APP_CODE = "appCode";//心跳包时长

    private static final AttributeKey<Object> ATTR_KEY_KEEPALIVE = AttributeKey.valueOf(KEEP_ALIVE);
    private static final AttributeKey<Object> ATTR_KEY_CLEANSESSION = AttributeKey.valueOf(CLEAN_SESSION);
    private static final AttributeKey<Object> ATTR_KEY_CLIENTID = AttributeKey.valueOf(CLIENT_ID);
    private static final AttributeKey<Object> ATTR_KEY_APPCODE = AttributeKey.valueOf(APP_CODE);
    private static final AttributeKey<Object> ATTR_KEY_USERNAME = AttributeKey.valueOf(USERNAME);
    private static final AttributeKey<Object> ATTR_KEY_PASSWORD = AttributeKey.valueOf(PASSWORD);

    public static String getAttrClientId(Channel channel) {
        return (String) channel.attr(NettyAttrManager.ATTR_KEY_CLIENTID).get();
    }

    public static void setAttrClientId(Channel channel, String clientID) {
        channel.attr(NettyAttrManager.ATTR_KEY_CLIENTID).set(clientID);
    }

    public static Boolean getAttrCleanSession(Channel channel) {
        return (Boolean) channel.attr(NettyAttrManager.ATTR_KEY_CLEANSESSION).get();
    }

    public static void setAttrCleanSession(Channel channel, Boolean cleansession) {
        channel.attr(NettyAttrManager.ATTR_KEY_CLEANSESSION).set(cleansession);
    }

    public static int getAttrKeepAlive(Channel channel) {
        return (int) channel.attr(NettyAttrManager.ATTR_KEY_KEEPALIVE).get();
    }

    public static void setAttrKeepAlive(Channel channel, int keepAlive) {
        channel.attr(NettyAttrManager.ATTR_KEY_KEEPALIVE).set(keepAlive);
    }

    public static String getAttrAppCode(Channel channel) {
        return (String) channel.attr(NettyAttrManager.ATTR_KEY_APPCODE).get();
    }

    public static void setAttrAppCode(Channel channel, String appCode) {
        channel.attr(NettyAttrManager.ATTR_KEY_APPCODE).set(appCode);
    }

    public static void setUsername(Channel channel, String username) {
        channel.attr(NettyAttrManager.ATTR_KEY_USERNAME).set(username);
    }

    public static String getUsername(Channel channel) {
        return (String) channel.attr(NettyAttrManager.ATTR_KEY_USERNAME).get();
    }

    public static void setPassword(Channel channel, String password) {
        channel.attr(NettyAttrManager.ATTR_KEY_PASSWORD).set(password);
    }

    public static String getPassword(Channel channel) {
        return (String) channel.attr(NettyAttrManager.ATTR_KEY_PASSWORD).get();
    }
}
