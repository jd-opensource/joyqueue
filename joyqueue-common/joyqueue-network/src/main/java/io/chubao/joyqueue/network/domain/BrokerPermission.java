package io.chubao.joyqueue.network.domain;

/**
 * BrokerPermission
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/6/27
 */
public class BrokerPermission {

    private static final int READABLE_MARK = 1 << 0;
    private static final int WRITABLE_MARK = 1 << 1;

    public static int setReadable(int permission, boolean readable) {
        if (readable) {
            return permission | READABLE_MARK;
        } else {
            return permission & ~READABLE_MARK;
        }
    }

    public static int setWritable(int permission, boolean writable) {
        if (writable) {
            return permission | WRITABLE_MARK;
        } else {
            return permission & ~WRITABLE_MARK;
        }
    }

    public static boolean isReadable(int permission) {
        return (permission & READABLE_MARK) != 0;
    }

    public static boolean isWritable(int permission) {
        return (permission & WRITABLE_MARK) != 0;
    }
}