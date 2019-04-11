package com.jd.journalq.store.cli;

import com.jd.journalq.store.PartitionGroupStoreManager;
import com.jd.journalq.store.StoreConfig;
import com.jd.journalq.store.file.PositioningStore;
import com.jd.journalq.store.file.StoreMessageSerializer;
import com.jd.journalq.store.index.IndexItem;
import com.jd.journalq.store.index.IndexSerializer;
import com.jd.journalq.store.message.BatchMessageParser;
import com.jd.journalq.store.message.MessageParser;
import com.jd.journalq.store.utils.PreloadBufferPool;
import com.jd.journalq.toolkit.format.Format;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author liyue25
 * Date: 2019-01-30
 */
public class PGCheck {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        if (args.length < 1) {
            showUsage();
            return;
        }

        File base = new File(args[0]);
        if (!base.isDirectory()) {
            System.out.println("Invalid path!");
            return;
        }

        long startPosition = -1;
        if (args.length > 1) {
            startPosition = Long.parseLong(args[1]);
        }

        int[] intPartitions = Arrays.stream(Objects.requireNonNull(new File(base, "index").listFiles((dir, name) -> name.matches("^\\d+$"))))
                .filter(File::isDirectory)
                .map(File::getName)
                .mapToInt(Integer::parseInt).toArray();

        short[] partitions = new short[intPartitions.length];
        for (int i = 0; i < intPartitions.length; i++) {
            partitions[i] = (short) intPartitions[i];
        }
        System.out.println(String.format("Path: %s, partitions: %s.", base.getAbsolutePath(),
                Arrays.stream(intPartitions).mapToObj(String::valueOf).collect(Collectors.joining(","))));

        if (startPosition >= 0) {
            System.out.println("Only files after position : " +
                    Format.formatWithComma(startPosition) +
                    " will be checked.");
        } else {
            System.out.println("All files will be checked.");
        }
        PreloadBufferPool pool = new PreloadBufferPool();
        pool.addPreLoad(StoreConfig.DEFAULT_INDEX_FILE_SIZE, 0, 1);
        pool.addPreLoad(StoreConfig.DEFAULT_MESSAGE_FILE_SIZE, 0, 1);

        checkFiles(base, startPosition);
        PositioningStore<ByteBuffer> logStore =
                new PositioningStore<>(base, new PositioningStore.Config(StoreConfig.DEFAULT_MESSAGE_FILE_SIZE),
                        pool, new StoreMessageSerializer(PartitionGroupStoreManager.Config.DEFAULT_MAX_MESSAGE_LENGTH));
        logStore.recover();

        if (startPosition < 0) startPosition = logStore.left();
        File indexBase = new File(base, "index");
        Map<Short, Partition> partitionMap = new HashMap<>();
        int progress = 0;
        long start = System.currentTimeMillis();
        for (short p : partitions) {
            File partitionBase = new File(indexBase, String.valueOf(p));
            checkFiles(partitionBase, -1);
            PositioningStore<IndexItem> partitionStore =
                    new PositioningStore<>(partitionBase,
                            new PositioningStore.Config(StoreConfig.DEFAULT_INDEX_FILE_SIZE), pool,
                            new IndexSerializer());
            partitionStore.recover();
            Partition partition = new Partition();
            partition.store = partitionStore;
            partitionMap.put(p, partition);
        }

        long position = startPosition;
        while (position < logStore.right()) {
            ByteBuffer logBuffer = logStore.read(position);
            int length = MessageParser.getInt(logBuffer, MessageParser.LENGTH);
            IndexItem logIndex = IndexItem.parseMessage(logBuffer, position);

            Partition partition = partitionMap.get(logIndex.getPartition());

            long indexPosition = logIndex.getIndex() * IndexItem.STORAGE_SIZE;

            if (partition.position >= 0 && partition.position + IndexItem.STORAGE_SIZE != indexPosition) {
                String msg = "Index not continuous:" + "\n" +
                        "Partition: " + logIndex.getPartition() + ", " +
                        "current index in log: " + Format.formatWithComma(logIndex.getIndex()) + ", " +
                        "last index in log: " + Format.formatWithComma(partition.position / IndexItem.STORAGE_SIZE) + ", " +
                        "log position: " + Format.formatWithComma(position) + "\n" +
                        "Log:" + "\n" +
                        MessageParser.getString(logBuffer);
                System.out.println(msg);
                System.out.println("Type \"TRUNCATE\" to rollback store to position " + Format.formatWithComma(position) + ", Type other to quit.");
                String inputStr = scanner.nextLine();

                if ("TRUNCATE".equals(inputStr)) {
                    truncate(base, position);
                }
                System.exit(-1);

            }

            if (indexPosition < partition.store.left()) {
//                System.out.println(String.format("Partition: %d, index: %s less than min index: %s in the index store.",
//                        logIndex.getPartition(),
//                        ThreadSafeFormat.formatWithComma(logIndex.getIndex()),
//                        ThreadSafeFormat.formatWithComma(partition.store.left() / IndexItem.STORAGE_SIZE)));
            } else if (indexPosition >= partition.store.right()) {
//                System.out.println(String.format("Partition: %d, index: %s greater than max index: %s in the index store.",
//                        logIndex.getPartition(),
//                        ThreadSafeFormat.formatWithComma(logIndex.getIndex()),
//                        ThreadSafeFormat.formatWithComma(partition.store.right() / IndexItem.STORAGE_SIZE)));
            } else {

                IndexItem partitionIndex = partition.store.read(indexPosition);
                if (!(logIndex.getOffset() == partitionIndex.getOffset() && length == partitionIndex.getLength())) {
                    String msg = "Incorrect index found:" + "\n" +
                            "Partition: " + logIndex.getPartition() + ", " +
                            "index in log: " + Format.formatWithComma(logIndex.getIndex()) + ", " +
                            "index position in log : " + Format.formatWithComma(indexPosition) + ", " +
                            "next index position of partition: " + Format.formatWithComma(partition.position) + "\n" +
                            "Log position: " + Format.formatWithComma(position) + ", " +
                            "log position from index: " + Format.formatWithComma(partitionIndex.getOffset()) + "\n" +
                            "Log length: " + length + ", " +
                            "log length from index: " + partitionIndex.getLength() + "\n" +
                            "Log:" + "\n" +
                            MessageParser.getString(logBuffer);
                    System.out.println(msg);
                    System.out.println("Type \"TRUNCATE\" to rollback partition index to " + Format.formatWithComma(logIndex.getIndex()) + ", Type other to quit.");
                    String inputStr = scanner.nextLine();

                    if ("TRUNCATE".equals(inputStr)) {
                        partition.store.close();
                        truncate(partition.store.base(), indexPosition);
                        partition.store.recover();
                    }

                } else {
//                    String msg = "Looks good: " +
//                            "partition: " + logIndex.getPartition() + ", " +
//                            "index: " + ThreadSafeFormat.formatWithComma(logIndex.getIndex());
//                    System.out.println(msg);
                }

                if (BatchMessageParser.isBatch(logBuffer)) {
                    short batchSize = BatchMessageParser.getBatchSize(logBuffer);
                    partition.position = indexPosition + (batchSize - 1) * IndexItem.STORAGE_SIZE;
                } else {
                    partition.position = indexPosition;
                }
            }


            int p = (int) (100 * (position - logStore.left()) / (logStore.right() - logStore.left()));
            if (progress < p) {
                progress = p;
                long passed = System.currentTimeMillis() - start;
                long remaining = passed / progress * (100 - progress);
                System.out.println(progress + "%, " + formatDuration(Duration.ofMillis(remaining)));
            }
            position += length;
        }

        System.out.println("Store looks good, do you want to recover the partition group?");
        System.out.println("Input \"RECOVER\" to perform recover, input other to quit. ");
        String inputStr = scanner.nextLine();

        if ("RECOVER".equals(inputStr)) {
            PartitionGroupStoreManager partitionGroupStoreManger = new PartitionGroupStoreManager(
                    "topic", 0, base, new PartitionGroupStoreManager.Config(),
                    new PreloadBufferPool(),
                    new ScheduledThreadPoolExecutor(1));
            partitionGroupStoreManger.recover();
            partitionGroupStoreManger.close();
        }
        System.out.println("Great!");
    }


    private static void showUsage() {
        System.out.println("Usage: PGCheck path-of-partition-group [start position]");
    }

    private static String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        long absSeconds = Math.abs(seconds);
        String positive = String.format(
                "%d:%02d:%02d",
                absSeconds / 3600,
                (absSeconds % 3600) / 60,
                absSeconds % 60);
        return seconds < 0 ? "-" + positive : positive;
    }

    /**
     * 检查文件是否连续，如果不连续，用户可以选择删除断点之后的文件。
     */
    private static void checkFiles(File base, long start) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Checking files " + base.getAbsolutePath() + " ...");
        List<File> files = Arrays.stream(Objects.requireNonNull(base.listFiles(file -> file.isFile() && file.getName().matches("\\d+"))))
                .filter(file -> start < 0 || Long.parseLong(file.getName()) > start)
                .sorted(Comparator.comparing(file -> Long.parseLong(file.getName())))
                .collect(Collectors.toList());
        long lastPos = -1;
        boolean delMark = false;
        for (File file : files) {
            if (delMark) delete(file);
            long filePos = Long.parseLong(file.getName());
            long dataLength = file.length() - PositioningStore.Config.DEFAULT_FILE_HEADER_SIZE;
            if (lastPos < 0) {
                lastPos = filePos + dataLength;
            } else {
                if (lastPos != filePos) {
                    System.out.println(String.format("Files are not continuous! expect: %d, actual file name: %d.", lastPos, filePos));
                    System.out.println("Type \"DELETE\" to rollback store to position " + lastPos + ", Type other to quit.");
                    String inputStr = scanner.nextLine();

                    if ("DELETE".equals(inputStr)) {
                        delete(file);
                        delMark = true;
                    } else {
                        return;
                    }
                } else {
                    lastPos += dataLength;
                }
            }
        }

    }

    private static void delete(File file) {
        if (file.exists()) {
            File delDir = checkDeleteDir(file);
            File destFile = new File(delDir, file.getName());
            System.out.println("Move file " + file.getAbsolutePath() + " to " + destFile.getAbsolutePath());
            file.renameTo(destFile);
        }
    }

    private static void copyToDeleteFolder(File file) throws IOException {
        if (file.exists()) {
            File delDir = checkDeleteDir(file);
            File destFile = new File(delDir, file.getName());
            System.out.println("Copy file " + file.getAbsolutePath() + " to " + destFile.getAbsolutePath());
            Files.copy(file.toPath(), destFile.toPath());
        }
    }

    private static File checkDeleteDir(File file) {
        File delDir = new File(file.getParentFile(), "deleted");
        if (!delDir.exists()) delDir.mkdir();
        return delDir;
    }

    /**
     * 删除store指定位置之后的数据
     */
    private static void truncate(File base, long position) throws IOException {
        NavigableMap<Long, File> fileMap = new TreeMap<>();
        Arrays.stream(Objects.requireNonNull(base.listFiles(file -> file.isFile() && file.getName().matches("\\d+"))))
                .forEach(file -> fileMap.put(Long.parseLong(file.getName()), file));
        Long deleteFrom;
        if (fileMap.containsKey(position)) {
            deleteFrom = position;
        } else {
            Map.Entry<Long, File> entry = fileMap.floorEntry(position);
            if (null != entry) {
                File tf = entry.getValue();
                copyToDeleteFolder(tf);
                long filePos = entry.getKey();
                long truncPos = position - filePos + PositioningStore.Config.DEFAULT_FILE_HEADER_SIZE;
                System.out.println("Truncate file " + tf.getAbsolutePath() + "to size " + truncPos + ".");
                try (FileChannel outChan = new FileOutputStream(tf, true).getChannel()) {
                    outChan.truncate(truncPos);
                }
            }

            deleteFrom = fileMap.ceilingKey(position);
        }

        if (null != deleteFrom) {
            fileMap.tailMap(deleteFrom).values().forEach(PGCheck::delete);
        }

    }

    private static class Partition {
        PositioningStore<IndexItem> store;
        long position = -1;
    }
}