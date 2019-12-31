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
package org.joyqueue.helper;

/**
 * PortHelper
 * author: gaohaoxiang
 * date: 2019/12/6
 */
public class PortHelper {

    private static final int BACKEND_PORT_OFFSET = 1;
    private static final int MONITOR_PORT_OFFSET = 2;
    private static final int NAMESERVER_MONITOR_PORT_OFFSET = 3;
    private static final int NAMESERVER_PORT_OFFSET = 4;
    private static final int MESSENGER_PORT_OFFSET = 5;
    private static final int JOURNALKEEPER_PORT_OFFSET = 6;

    public static int getBackendPort(int basePort) {
        return basePort + BACKEND_PORT_OFFSET;
    }

    public static int getMonitorPort(int basePort) {
        return basePort + MONITOR_PORT_OFFSET;
    }

    public static int getNameServerManagerPort(int basePort) {
        return basePort + NAMESERVER_MONITOR_PORT_OFFSET;
    }

    public static int getNameServerPort(int basePort) {
        return basePort + NAMESERVER_PORT_OFFSET;
    }

    public static int getMessengerPort(int basePort) {
        return basePort + MESSENGER_PORT_OFFSET;
    }

    public static int getJournalkeeperPort(int basePort) {
        return basePort + JOURNALKEEPER_PORT_OFFSET;
    }
}