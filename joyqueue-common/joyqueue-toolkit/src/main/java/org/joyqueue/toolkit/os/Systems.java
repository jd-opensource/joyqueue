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
package org.joyqueue.toolkit.os;

import sun.misc.Unsafe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.nio.ByteOrder;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 系统信息
 * Created by hexiaofeng on 16-5-16.
 */
public abstract class Systems {
    public static final String USER_HOME = "user.home";
    public static final String OS_NAME = "os.name";
    public static final String JAVA_IO_TMPDIR = "java.io.tmpdir";
    public static final String JDOS_CPU = "JDOS_CPU";

    public static int CACHE_LINE_SIZE = 64;
    public static final Unsafe UNSAFE;
    // 缓存的PID
    protected static Long pid = null;
    protected static Pattern pattern = Pattern.compile("\"Cpuset\":\\s*\"(.*?)\"");
    protected static final String userHome = System.getProperty(USER_HOME);
    protected static final String osName = System.getProperty(OS_NAME);
    protected static final String tmpDir = System.getProperty(JAVA_IO_TMPDIR);
    protected static final Memory memory;
    protected static final long BYTE_ARRAY_OFFSET;
    protected static final long SHORT_ARRAY_OFFSET;
    protected static final long SHORT_ARRAY_STRIDE;
    public static final boolean FAST_ACCESS_SUPPORTED;

    // OSType detection
    public enum OSType {
        LINUX,
        WIN,
        SOLARIS,
        MAC,
        FREEBSD,
        OTHER
    }

    protected static final OSType osType = osName.startsWith("Linux") ? OSType.LINUX : (osName.startsWith("Windows") ? OSType.WIN :
            (osName.contains("SunOS") || osName.contains("Solaris") ? OSType.SOLARIS :
                    (osName.contains("Mac") ? OSType.MAC : (osName.contains("FreeBSD") ? OSType.FREEBSD : OSType.OTHER))));
    // Helper static vars for each platform
    public static final boolean WINDOWS = (osType == OSType.WIN);
    public static final boolean SOLARIS = (osType == OSType.SOLARIS);
    public static final boolean MAC = (osType == OSType.MAC);
    public static final boolean FREEBSD = (osType == OSType.FREEBSD);
    public static final boolean LINUX = (osType == OSType.LINUX);
    public static final boolean OTHER = (osType == OSType.OTHER);
    public static final boolean PPC_64
            = System.getProperties().getProperty("os.arch").contains("ppc64");
    public static CoresDetector[] coresDetectors = new CoresDetector[]{
            new JDOS1CoresDetector(), new JDOS2CoresDetector(), new DefaultCoresDetector()
    };

    static {
        try {
            final PrivilegedExceptionAction<Unsafe> action = new PrivilegedExceptionAction<Unsafe>() {
                public Unsafe run() throws Exception {
                    Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
                    theUnsafe.setAccessible(true);
                    return (Unsafe) theUnsafe.get(null);
                }
            };
            UNSAFE = AccessController.doPrivileged(action);
            BYTE_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);
            SHORT_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(short[].class);
            SHORT_ARRAY_STRIDE = UNSAFE.arrayIndexScale(short[].class);

            // Try to only load one implementation of Memory to assure the call sites are monomorphic (fast)
            Memory target = null;

            // TODO enable UnsafeMemory on big endian machines
            //
            // The current UnsafeMemory code assumes the machine is little endian, and will
            // not work correctly on big endian CPUs.  For now, we will disable UnsafeMemory on
            // big endian machines.  This will make the code significantly slower on big endian.
            // In the future someone should add the necessary flip bytes calls to make this
            // work efficiently on big endian machines.
            if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                try {
                    Memory unsafeMemory = new UnsafeMemory();
                    if (unsafeMemory.getInt(new byte[4], 0) == 0) {
                        target = unsafeMemory;
                    }
                } catch (Throwable ignored) {
                }
            }
            if (target == null) {
                try {
                    Memory slowMemory = new SlowMemory();
                    if (slowMemory.getInt(new byte[4], 0) == 0) {
                        target = slowMemory;
                    } else {
                        throw new AssertionError("SlowMemory class is broken!");
                    }
                } catch (Throwable ignored) {
                    throw new AssertionError("Could not find SlowMemory class");
                }
            }
            memory = target;
            FAST_ACCESS_SUPPORTED = memory.fastAccessSupported();
        } catch (Exception e) {
            throw new RuntimeException("Unable to load unsafe", e);
        }

    }

    /**
     * 获取用户目录
     *
     * @return 用户目录
     */
    public static String getUserHome() {
        return userHome;
    }

    /**
     * 获取操作系统名称
     *
     * @return 操作系统名称
     */
    public static String getOsName() {
        return osName;
    }

    /**
     * 获取操作系统类型
     *
     * @return
     */
    public static OSType getOsType() {
        return osType;
    }

    /**
     * 获取临时目录
     *
     * @return
     */
    public static String getTmpDir() {
        return tmpDir;
    }

    /**
     * 获取进程ID
     *
     * @return 进程ID
     */
    public static long getPid() {
        if (pid != null) {
            return pid;
        }
        String name = ManagementFactory.getRuntimeMXBean().getName();
        int indexOf = name.indexOf('@');
        if (indexOf > 0) {
            name = name.substring(0, indexOf);
        }
        pid = Long.parseLong(name);
        return pid;
    }

    /**
     * 获取CPU核数
     *
     * @return CPU核数,-1没有识别出来
     */
    public static int getCores() {
        int cores;
        for (CoresDetector detector : coresDetectors) {
            cores = detector.getCores();
            if (cores != CoresDetector.UNKNOWN)
                return cores;
        }
        return CoresDetector.UNKNOWN;
    }

    /**
     * Get a handle on the Unsafe instance, used for accessing low-level concurrency
     * and memory constructs.
     *
     * @return The Unsafe
     */
    public static Unsafe getUnsafe() {
        return UNSAFE;
    }

    /**
     * 获取CPU缓存线大小
     *
     * @return
     */
    public static int getCacheLineSize() {
        return CACHE_LINE_SIZE;
    }

    /**
     * 获取长整形的字节数
     *
     * @return 长整形的字节数
     */
    public static int getLongBytes() {
        return 8;
    }

    /**
     * 获取长整形的字节数
     *
     * @return 长整形的字节数
     */
    public static int getIntBytes() {
        return 4;
    }

    /**
     * 读取字节数据
     *
     * @param data  缓冲器
     * @param index 索引
     * @return 字节
     */
    public static int getByte(final byte[] data, final int index) {
        return memory.getByte(data, index);
    }

    /**
     * 读取短整数数据
     *
     * @param data  缓冲器
     * @param index 索引
     * @return 短整数
     */
    public static int getShort(final short[] data, final int index) {
        return memory.getShort(data, index);
    }

    /**
     * 读取整形数据
     *
     * @param data  缓冲器
     * @param index 索引
     * @return 整数
     */
    public static int getInt(final byte[] data, final int index) {
        return memory.getInt(data, index);
    }

    /**
     * 读取长整形数据
     *
     * @param data  缓冲器
     * @param index 索引
     * @return 长整数
     */
    public static long getLong(final byte[] data, final int index) {
        return memory.getLong(data, index);
    }

    /**
     * 拷贝长整数(8字节)
     *
     * @param src       源数据
     * @param srcIndex  源位置
     * @param dest      目标数组
     * @param destIndex 目标位置
     */
    public static void copyLong(final byte[] src, final int srcIndex, final byte[] dest, final int destIndex) {
        memory.copyLong(src, srcIndex, dest, destIndex);
    }

    /**
     * 拷贝数据
     *
     * @param src       源数据
     * @param srcIndex  源位置
     * @param dest      目标数组
     * @param destIndex 目标位置
     * @param length    长度
     */
    public static void copyMemory(final byte[] src, final int srcIndex, final byte[] dest, final int destIndex,
                                  final int length) {
        memory.copyMemory(src, srcIndex, dest, destIndex, length);
    }


    /**
     * 内存操作
     */
    interface Memory {
        /**
         * 是否只是快速操作
         *
         * @return
         */
        boolean fastAccessSupported();

        /**
         * 读取字节数据
         *
         * @param data  缓冲器
         * @param index 索引
         * @return 字节
         */
        int getByte(byte[] data, int index);

        /**
         * 读取短整数数据
         *
         * @param data  缓冲器
         * @param index 索引
         * @return 短整数
         */
        int getShort(short[] data, int index);

        /**
         * 读取整形数据
         *
         * @param data  缓冲器
         * @param index 索引
         * @return 整数
         */
        int getInt(byte[] data, int index);

        /**
         * 读取长整形数据
         *
         * @param data  缓冲器
         * @param index 索引
         * @return 长整数
         */
        long getLong(byte[] data, int index);

        /**
         * 拷贝长整数(8字节)
         *
         * @param src       源数据
         * @param srcIndex  源位置
         * @param dest      目标数组
         * @param destIndex 目标位置
         */
        void copyLong(byte[] src, int srcIndex, byte[] dest, int destIndex);

        /**
         * 拷贝数据
         *
         * @param src       源数据
         * @param srcIndex  源位置
         * @param dest      目标数组
         * @param destIndex 目标位置
         * @param length    长度
         */
        void copyMemory(byte[] src, int srcIndex, byte[] dest, int destIndex, int length);
    }

    static class SlowMemory implements Memory {
        @Override
        public boolean fastAccessSupported() {
            return false;
        }

        @Override
        public int getShort(final short[] data, final int index) {
            return data[index] & 0xFFFF;
        }

        @Override
        public int getByte(final byte[] data, final int index) {
            return data[index] & 0xFF;
        }

        @Override
        public int getInt(final byte[] data, final int index) {
            return (data[index] & 0xff) | (data[index + 1] & 0xff) << 8 | (data[index + 2] & 0xff) << 16 |
                    (data[index + 3] & 0xff) << 24;
        }

        @Override
        public void copyLong(final byte[] src, final int srcIndex, final byte[] dest, final int destIndex) {
            for (int i = 0; i < 8; i++) {
                dest[destIndex + i] = src[srcIndex + i];
            }
        }

        @Override
        public long getLong(final byte[] data, final int index) {
            return (data[index] & 0xffL) | (data[index + 1] & 0xffL) << 8 | (data[index + 2] & 0xffL) << 16 |
                    (data[index + 3] & 0xffL) << 24 | (data[index + 4] & 0xffL) << 32 | (data[index + 5] & 0xffL) <<
                    40 | (data[index + 6] & 0xffL) << 48 | (data[index + 7] & 0xffL) << 56;
        }

        @Override
        public void copyMemory(final byte[] src, final int srcIndex, final byte[] dest, final int destIndex,
                               final int length) {
            System.arraycopy(src, srcIndex, dest, destIndex, length);
        }
    }

    static class UnsafeMemory implements Memory {
        @Override
        public boolean fastAccessSupported() {
            return true;
        }

        @Override
        public int getShort(final short[] data, final int index) {
            assert index >= 0;
            assert index <= data.length;
            return UNSAFE.getShort(data, SHORT_ARRAY_OFFSET + (index * SHORT_ARRAY_STRIDE)) & 0xFFFF;
        }

        @Override
        public int getByte(final byte[] data, final int index) {
            assert index >= 0;
            assert index <= data.length;
            return UNSAFE.getByte(data, BYTE_ARRAY_OFFSET + index) & 0xFF;
        }

        @Override
        public int getInt(final byte[] data, final int index) {
            assert index >= 0;
            assert index + 4 <= data.length;
            return UNSAFE.getInt(data, BYTE_ARRAY_OFFSET + index);
        }

        @Override
        public void copyLong(final byte[] src, final int srcIndex, final byte[] dest, final int destIndex) {
            assert srcIndex >= 0;
            assert srcIndex + 8 <= src.length;
            assert destIndex >= 0;
            assert destIndex + 8 <= dest.length;
            long value = UNSAFE.getLong(src, BYTE_ARRAY_OFFSET + srcIndex);
            UNSAFE.putLong(dest, (BYTE_ARRAY_OFFSET + destIndex), value);
        }

        @Override
        public long getLong(final byte[] data, final int index) {
            assert index > 0;
            assert index + 4 < data.length;
            return UNSAFE.getLong(data, BYTE_ARRAY_OFFSET + index);
        }

        @Override
        public void copyMemory(final byte[] src, final int srcIndex, final byte[] dest, final int destIndex,
                               final int length) {
            assert srcIndex >= 0;
            assert srcIndex + length <= src.length;
            assert destIndex >= 0;
            assert destIndex + length <= dest.length;
            UNSAFE.copyMemory(src, BYTE_ARRAY_OFFSET + srcIndex, dest, BYTE_ARRAY_OFFSET + destIndex, length);
        }
    }

    /**
     * 获取CPU核数
     */
    public interface CoresDetector {
        int UNKNOWN = -1;

        /**
         * 获取CPU核数
         *
         * @return CPU核数,-1表示无法识别
         */
        int getCores();
    }

    public static class JDOS1CoresDetector implements CoresDetector {
        public static String ETC_CONFIG_INFO = "/etc/config_info";

        @Override
        public int getCores() {
            // 兼容jdos1.0
            // cat /etc/config_info
            // {"Config": {"Cpuset": "1,2", "Memory": 4294967296}, "host_ip": "10.8.65.251"}
            File file = new File(ETC_CONFIG_INFO);
            if (file.exists() && file.length() > 0) {
                String text = read(file);
                if (text != null && !text.isEmpty()) {
                    Matcher matcher = pattern.matcher(text);
                    if (matcher.find()) {
                        text = matcher.group(1);
                        return text.split(",").length;
                    }
                }
            }
            return UNKNOWN;
        }

        /**
         * 获取文件内容
         *
         * @param file
         * @return
         */
        protected String read(final File file) {
            String text = null;
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(file));
                String line = null;
                int count = 0;
                while ((line = reader.readLine()) != null) {
                    if (count > 0) {
                        builder.append('\n');
                    }
                    builder.append(line);
                    count++;
                }
                text = builder.toString();
            } catch (FileNotFoundException e) {
                //忽略文件不存在
            } catch (IOException e) {
            } finally {
                if (reader != null)
                    try {
                        reader.close();
                    } catch (IOException e) {
                    }
            }
            return text;
        }
    }

    public static class JDOS2CoresDetector implements CoresDetector {
        @Override
        public int getCores() {
            // 兼容JDOS2.0，在环境变量里面
            String value = System.getenv(JDOS_CPU);
            if (value != null && !value.isEmpty()) {
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException e) {
                }
            }
            return UNKNOWN;
        }

    }

    public static class DefaultCoresDetector implements CoresDetector {
        @Override
        public int getCores() {
            return Runtime.getRuntime().availableProcessors();
        }
    }
}
