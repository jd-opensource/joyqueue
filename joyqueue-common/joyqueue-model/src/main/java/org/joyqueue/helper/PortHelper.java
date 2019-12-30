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
    private static final int STORE_PORT_OFFSET = 7;

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

    public static int getStorePortOffset(int basePort) {return basePort + STORE_PORT_OFFSET;}
}