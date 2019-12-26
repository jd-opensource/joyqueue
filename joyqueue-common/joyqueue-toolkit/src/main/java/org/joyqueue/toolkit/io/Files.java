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
package org.joyqueue.toolkit.io;

import org.joyqueue.toolkit.lang.Close;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * 文件工具类
 */
public abstract class Files {

    /**
     * 从文件流里面拷贝数据
     *
     * @param in     输入流
     * @param out    输出流
     * @param start  输入位置
     * @param length 长度
     * @throws IOException
     */
    protected static void copy(final FileInputStream in, final FileOutputStream out, final long start,
            final long length) throws IOException {
        if (in == null || out == null) {
            return;
        }
        FileChannel fci = in.getChannel();
        FileChannel fco = out.getChannel();
        try {
            fci.transferTo(start, length, fco);
            fco.force(false);
        } finally {
            Close.close(fci).close(fco);
        }
    }

    /**
     * 从文件流里面拷贝数据
     *
     * @param in       输入流
     * @param out      输出流
     * @param start    输入位置
     * @param length   长度
     * @param position 目标位置
     * @throws IOException
     */
    protected static void copy(final FileInputStream in, final RandomAccessFile out, final long start,
            final long length, final long position) throws IOException {
        if (in == null || out == null) {
            return;
        }
        // 定位到目标位置
        out.seek(position);
        FileChannel fci = in.getChannel();
        FileChannel fco = out.getChannel();
        try {
            fci.transferTo(start, length, fco);
            fco.force(false);
        } finally {
            Close.close(fci).close(fco);
        }
    }

    /**
     * 从通道里面拷贝数据
     *
     * @param in     输入通道
     * @param out    输出通道
     * @param start  输入位置
     * @param length 长度
     * @throws IOException
     */
    protected static void copy(final FileChannel in, final FileChannel out, final long start, final long length) throws
            IOException {
        if (in == null || out == null) {
            return;
        }
        in.transferTo(start, length, out);
        out.force(false);
    }

    /**
     * 追加拷贝文件，并刷盘
     *
     * @param source 文件源
     * @param target 目标文件
     * @param start  开始
     * @param length 长度
     * @throws IOException
     */
    public static void copy(final File source, final File target, final long start, final long length) throws
            IOException {
        if (source == null || target == null) {
            return;
        }
        if (!target.exists()) {
            target.createNewFile();
        }
        if (length <= 0) {
            return;
        }
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(source);
            fos = new FileOutputStream(target, true);
            copy(fis, fos, start < 0 ? 0 : start, length);
        } finally {
            Close.close(fis).close(fos);
        }
    }

    /**
     * 拷贝文件数据到目标文件指定位置，并刷盘
     *
     * @param source   源文件
     * @param target   目标文件
     * @param start    源文件开始位置
     * @param length   长度
     * @param position 目标文件位置
     * @throws IOException
     */
    public static void copy(final File source, final File target, final long start, final long length,
            final long position) throws IOException {
        if (source == null || target == null) {
            return;
        }
        if (!target.exists()) {
            target.createNewFile();
        }
        if (length <= 0) {
            return;
        }
        if (position < 0 || position == target.length()) {
            // 追加模式
            copy(source, target, start, length);
            return;
        }

        FileInputStream fis = null;
        RandomAccessFile raf = null;
        try {
            fis = new FileInputStream(source);
            raf = new RandomAccessFile(target, "rw");
            copy(fis, raf, start < 0 ? 0 : start, length, position);
        } finally {
            Close.close(fis).close(raf);
        }
    }

    /**
     * 追加拷贝文件，并刷盘
     *
     * @param source 文件源
     * @param target 目标文件
     * @param length 长度
     * @throws IOException
     */
    public static void copy(final File source, final File target, final long length) throws IOException {
        copy(source, target, 0, length);
    }

    /**
     * 拷贝覆盖文件，并刷盘
     *
     * @param source 文件源
     * @param target 目标文件
     * @throws IOException
     */
    public static void copy(final File source, final File target) throws IOException {
        if (source == null || target == null) {
            return;
        }
        if (!target.exists()) {
            target.createNewFile();
        }

        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(source);
            fos = new FileOutputStream(target);
            copy(fis, fos, 0, source.length());
        } finally {
            Close.close(fis).close(fos);
        }
    }

    /**
     * 拷贝数据流到文件指定位置，并刷盘
     *
     * @param is       输入
     * @param target   目标文件
     * @param length   数据长度
     * @param position 目标文件起始位置
     * @throws IOException
     */
    public static void copy(final InputStream is, final File target, final long length, final long position) throws
            IOException {
        if (is == null || target == null) {
            return;
        }
        if (!target.exists()) {
            target.createNewFile();
        }
        if (length <= 0) {
            return;
        }
        FileOutputStream fos = null;
        RandomAccessFile raf = null;
        try {
            if (is.getClass() == FileInputStream.class) {
                // 采用zeroCopy技术拷贝
                if (target.length() == position) {
                    // 追加模式
                    fos = new FileOutputStream(target, true);
                    copy((FileInputStream) is, fos, 0, length);
                } else {
                    // 随机拷贝
                    raf = new RandomAccessFile(target, "rw");
                    copy((FileInputStream) is, raf, 0, length, position);
                }
            } else {
                // 使用缓冲器拷贝
                raf = new RandomAccessFile(target, "rw");
                raf.seek(position);
                copy(is, raf, length);
                // 同步到硬盘
                raf.getFD().sync();
            }
        } finally {
            Close.close(fos).close(raf);
        }
    }

    /**
     * 流拷贝，并刷盘
     *
     * @param is     输入
     * @param os     输出
     * @param length 长度
     * @throws IOException
     */
    public static void copy(final InputStream is, final OutputStream os, final long length) throws IOException {
        copy(is, os, 0, length);
    }

    /**
     * 流拷贝，并刷盘
     *
     * @param is     输入
     * @param os     输出
     * @param start  源起始位置
     * @param length 长度
     * @throws IOException
     */
    public static void copy(final InputStream is, final OutputStream os, final long start, final long length) throws
            IOException {
        if (is == null || os == null || length == 0) {
            return;
        }
        if (is.getClass() == FileInputStream.class && os.getClass() == FileOutputStream.class) {
            // 采用zeroCopy技术拷贝
            copy((FileInputStream) is, (FileOutputStream) os, start, length);
        } else {
            long bytes = 0;
            if (start > 0) {
                bytes = is.skip(start);
                if (bytes < start) {
                    return;
                }
            }
            byte buffer[] = new byte[1024 * 4];
            int c = 0;
            bytes = 0;
            while (bytes < length && ((c = is.read(buffer, 0, (int) Math.min(buffer.length, length - bytes))) >= 0)) {
                os.write(buffer, 0, c);
                bytes += c;
            }
        }

    }

    /**
     * 流拷贝
     *
     * @param is     输入
     * @param os     输出
     * @param length 长度
     * @throws IOException
     */
    public static void copy(final InputStream is, final DataOutput os, final long length) throws IOException {
        if (is == null || os == null) {
            return;
        }
        byte buffer[] = new byte[1024 * 4];
        int c = 0;
        long pos = 0;
        while (pos < length && ((c = is.read(buffer, 0, (int) Math.min(buffer.length, length - pos))) >= 0)) {
            os.write(buffer, 0, c);
            pos += c;
        }
    }

    /**
     * Reads <i>length</i> bytes from <i>source</i> into <i>dest</i> starting at <i>offset</i>.
     * The only case where the <i>length</i> <tt>byte</tt>s will not be read is if <i>source</i> returns EOF.
     *
     * @param source The source of bytes to read from. Must not be <code>null</code>.
     * @param dest   The <tt>byte[]</tt> to read bytes into. Must not be <code>null</code>.
     * @param offset The index in <i>dest</i> to start filling.
     * @param length The number of bytes to read.
     * @return Total number of bytes actually read.
     * @throws IndexOutOfBoundsException if <i>offset</i> or <i>length</i> are invalid.
     */
    public static int readBytes(final InputStream source, final byte[] dest, final int offset, final int length) throws
            IOException {
        if (source == null || dest == null) {
            return 0;
        }

        // how many bytes were read.
        int lastRead = source.read(dest, offset, length);

        int totalRead = lastRead;

        // if we did not read as many bytes as we had hoped, try reading again.
        if (lastRead < length) {
            // as long the buffer is not full (remaining() == 0) and we have not reached EOF (lastRead == -1) keep
            // reading.
            while (totalRead < length && lastRead != -1) {
                lastRead = source.read(dest, offset + totalRead, length - totalRead);

                // if we got EOF, do not add to total read.
                if (lastRead != -1) {
                    totalRead += lastRead;
                }
            }
        }

        return totalRead;
    }

    /**
     * 跳过指定字节数
     *
     * @param source 输入缓冲器
     * @param skip   字节数
     * @return 实际跳过的字节数
     * @throws IOException
     */
    public static int skip(final InputStream source, final int skip) throws IOException {
        // optimization also avoids potential for error with some implementation of
        // InputStream.skip() which throw exceptions with negative numbers (ie. ZipInputStream).
        if (source == null || skip <= 0) {
            return 0;
        }

        int toSkip = skip - (int) source.skip(skip);

        boolean more = true;
        while (toSkip > 0 && more) {
            // check to see if we reached EOF
            int read = source.read();
            if (read == -1) {
                more = false;
            } else {
                --toSkip;
                toSkip -= source.skip(toSkip);
            }
        }

        int skipped = skip - toSkip;

        return skipped;
    }

    /**
     * move文件
     *
     * @param from 源文件
     * @param to   目标文件
     * @return 成功标示
     * @throws IOException
     */
    public static boolean move(final File from, final File to) throws IOException {
        if (from == null || to == null || !from.exists()) {
            return false;
        }

        // If a simple rename/mv does not work..
        // 删除目标文件
        to.delete();
        // 重命名
        if (!from.renameTo(to)) {
            // 不成功则采用流复制
            copy(from, to);
            // 删除
            from.delete();
        }
        return true;
    }

    /**
     * 递归删除目录和文件
     *
     * @param root
     */
    public static void deleteDirectory(final File root) {
        if (root == null || !root.exists()) {
            return;
        }
        if (root.isFile()) {
            root.delete();
        } else if (root.isDirectory()) {
            File[] children = root.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (child.isFile()) {
                        child.delete();
                    } else if (child.isDirectory()) {
                        deleteDirectory(child);
                    }
                }
            }
            root.delete();
        }
    }

    /**
     * 创建目录
     *
     * @param directory
     * @return
     */
    public static boolean createDirectory(final File directory) {
        if (directory == null) {
            return false;
        }
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                return directory.exists();
            }
        }
        return true;
    }

    /**
     * 创建文件
     *
     * @param file 文件
     * @return 成功标示
     */
    public static boolean createFile(final File file) {
        if (file == null) {
            return false;
        }
        if (file.exists()) {
            return true;
        }
        try {
            if (file.createNewFile()) {
                return true;
            }
            return file.exists();
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 写文件
     *
     * @param file    文件
     * @param content 消息
     * @throws IOException
     */
    public static void write(final File file, final String content) throws IOException {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(content);
            writer.newLine();
        } finally {
            Close.close(writer);
        }
    }

    /**
     * 读文件内容
     *
     * @param file 文件
     * @return 文件内容
     */
    public static String read(final File file) throws IOException {
        if (file == null || !file.exists() || file.length() < 1) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = null;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                if (count > 0) {
                    builder.append("\n");
                }
                builder.append(line);
                count++;
            }
        } catch (FileNotFoundException e) {
            //忽略文件不存在
        } finally {
            Close.close(reader);
        }
        return builder.toString();
    }

    /**
     * 获取文件名后缀，不包括"."
     *
     * @param name 文件名
     * @return 文件名后缀
     */
    public static String suffix(String name) {
        if (name == null) {
            return null;
        }
        name = name.trim();
        if (name.isEmpty()) {
            return null;
        } else if (name.charAt(0) == '.') {
            if (name.length() == 1) {
                return null;
            }
            name = name.substring(1);
        }
        return name;
    }

    /**
     * 构造目录
     *
     * @param files 文件路径数组
     * @return 全路径
     */
    public static File path(final String... files) {
        if (files == null || files.length == 0) {
            return null;
        }
        File result = null;
        for (String file : files) {
            if (result == null) {
                result = new File(file);
            } else {
                result = new File(result, file);
            }
        }
        return result;
    }

    /**
     *  tree directory
     *
     **/
    public static void  tree(String path,boolean recursive,Directory directory){
        File file=new File(path);
        if(file.exists()) {
            directory.setName(file.getName());
            directory.setPath(path);
            directory.setDirectory(file.isDirectory());
            if(file.isDirectory()) {
                File[] files = file.listFiles();
                if (files.length > 0) {
                    directory.setChildren(new ArrayList<>());
                }
                if (recursive) {
                    sortByName(files);
                    for (File f : files) {
                        Directory child = new Directory();
                        if (f.isDirectory()) {
                            tree(f.getPath(), true, child);
                        } else {
                            child.setName(f.getName());
                            child.setPath(f.getPath());
                        }
                        directory.getChildren().add(child);
                    }
                }
            }
        }
    }

    /**
     * 根据文件名是字符还是数字采用不同的排序
     **/
    public static void sortByName(File[] files){
        boolean allNumbers=true;
        for(File file:files){
         if(!NumberUtils.isNumber(file.getName())){
             allNumbers=false;
             break;
         }
        }
        if(allNumbers){
            Arrays.sort(files,(a,b)->{
               return (int)(Long.valueOf(a.getName())-Long.valueOf(b.getName()));
            });
        }else{
            Arrays.sort(files, Comparator.comparing(File::getName));
        }

    }

}
