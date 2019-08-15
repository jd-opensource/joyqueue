/**
 * Copyright 2018 The JoyQueue Authors.
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
package io.chubao.joyqueue.store.cli;

import io.chubao.joyqueue.store.PartitionGroupStoreManager;
import io.chubao.joyqueue.store.StoreConfig;
import io.chubao.joyqueue.store.StoreLock;
import io.chubao.joyqueue.store.StoreLockedException;
import io.chubao.joyqueue.store.file.PositioningStore;
import io.chubao.joyqueue.store.file.StoreMessageSerializer;
import io.chubao.joyqueue.store.index.IndexItem;
import io.chubao.joyqueue.store.index.IndexSerializer;
import io.chubao.joyqueue.store.message.BatchMessageParser;
import io.chubao.joyqueue.store.message.MessageParser;
import io.chubao.joyqueue.store.utils.PreloadBufferPool;
import io.chubao.joyqueue.toolkit.format.Format;
import io.chubao.joyqueue.toolkit.time.SystemClock;

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
 * Date: 2019-07-09
 */
public class RecoverStore {
    public static void main(String [] args) throws IOException {
        UserInput userInput = new UserInput();
        Scanner scanner = new Scanner(System.in);
        System.out.println("JoyQueue store recover tool.");

        getDataPath(userInput, scanner);
        getTopic(userInput, scanner);
        getGroup(userInput, scanner);
        getStartPosition(userInput, scanner);
        confirm(scanner);

        File groupHome = new File(userInput.dataPath + File.separator + "store" +
                File.separator + "topics" + File.separator + userInput.topic +
                File.separator + userInput.group);
        File lockFile = new File(userInput.dataPath + File.separator + "store" + File.separator + "lock");
        StoreLock storeLock = new StoreLock(lockFile);
        try {
            storeLock.lock();
            Runtime.getRuntime().addShutdownHook(new Thread(storeLock::unlock));

            deleteNonContinousFiles(groupHome, userInput.startPosition);
            checkIndices(groupHome, userInput.startPosition);
            recoverStore(groupHome, userInput);
            System.out.println("All Done!");

        } catch (StoreLockedException sle) {
            System.out.println("Failed to acquire the store lock! " +
                    "Make sure no JoyQueue server is running!");
        }
    }

    private static void recoverStore(File groupHome, UserInput userInput) {
        PartitionGroupStoreManager partitionGroupStoreManger = new PartitionGroupStoreManager(
                userInput.topic, userInput.group, groupHome, new PartitionGroupStoreManager.Config(),
                PreloadBufferPool.getInstance(),
                new ScheduledThreadPoolExecutor(1));
        partitionGroupStoreManger.recover();
        partitionGroupStoreManger.close();
    }

    private static void getDataPath(UserInput userInput, Scanner scanner) {
        boolean isInputValid = false;
        while (!isInputValid) {

            System.out.print(String.format("Where is the JoyQueue data path [%s]? ", userInput.dataPath));
            String input = scanner.nextLine();
            if(input.trim().isEmpty()) {
                isInputValid = true;
            } else {
                File dataPath = new File(input);
                if(isInputValid = dataPath.isDirectory()){
                    File topicDir = new File(userInput.dataPath + File.separator + "store" + File.separator + "topics");
                    if(!topicDir.isDirectory()) {
                        System.out.println(String.format("Topic dir is NOT valid: %s!", topicDir.getAbsolutePath()));
                        isInputValid = false;
                    }

                    File [] topicFiles = topicDir.listFiles(File::isDirectory);
                    if(null == topicFiles || topicFiles.length == 0) {
                        System.out.println(String.format("No topic in topic dir: %s.", topicDir.getAbsolutePath()));
                        isInputValid = false;
                    }
                    if(isInputValid) {
                        userInput.dataPath = input;
                    }
                }
            }
            if(isInputValid){
                System.out.println(String.format("Using data path: %s.", new File(userInput.dataPath).getAbsolutePath()));
            } else {
                System.out.println(String.format("invalid data path: %s.", input));
            }
        }
    }

    private static void getTopic(UserInput userInput, Scanner scanner) {
        File topicDir = new File(userInput.dataPath + File.separator + "store" + File.separator + "topics");
        File [] topicFiles = topicDir.listFiles(File::isDirectory);

        assert topicFiles != null;

        String [] topics = Arrays.stream(topicFiles)
                .map(File::getName)
                .sorted()
                .toArray(String[]::new);
        // 打印所有的topic

        System.out.println(String.format("Found topics: %s.",
                String.join(", ", topics)));
        userInput.topic = topics[0];

        boolean isInputValid = false;
        while (!isInputValid) {

            System.out.print(String.format("Select your topic [%s]: ", userInput.topic));
            String input = scanner.nextLine();
            if(input.trim().isEmpty()) {
                isInputValid = true;
            } else {
                if(isInputValid = Arrays.asList(topics).contains(input)) {
                    userInput.topic = input;
                }
            }
            if(isInputValid){
                System.out.println(String.format("Using topic: %s.", userInput.topic));
            } else {
                System.out.println(String.format("invalid input: %s.", input));
            }
        }
    }
    private static void getGroup(UserInput userInput, Scanner scanner) {
        File topicHome = new File(userInput.dataPath + File.separator + "store" +
                File.separator + "topics" + File.separator + userInput.topic);

        File [] groupFiles = topicHome.listFiles(file -> file.isDirectory() && file.getName().matches("\\d+"));
        if(null == groupFiles || groupFiles.length == 0) {
            System.out.println(String.format("No group in topic dir: %s, exit.", topicHome.getAbsolutePath()));
            System.exit(0);
        }


        int [] groups = Arrays.stream(groupFiles)
                .map(File::getName)
                .mapToInt(Integer::parseInt)
                .sorted()
                .toArray();
        // 打印所有的topic

        System.out.println(String.format("Found groups: %s.",
                Arrays.stream(groups).mapToObj(String::valueOf).collect(Collectors.joining(", "))));
        userInput.group = groups[0];

        boolean isInputValid = false;
        while (!isInputValid) {

            System.out.print(String.format("Select your group [%d]: ", userInput.group));
            String input = scanner.nextLine();
            if(input.trim().isEmpty()) {
                isInputValid = true;
            } else {
                try {
                    int inputGroup = Integer.parseInt(input);
                    if (isInputValid = contains(groups, inputGroup)) {
                        userInput.group = inputGroup;
                    }
                } catch (NumberFormatException e) {
                    isInputValid = false;
                }
            }
            if(isInputValid){
                System.out.println(String.format("Using group: %d.", userInput.group));
            } else {
                System.out.println(String.format("invalid input: %s.", input));
            }
        }
    }


    private static void getStartPosition(UserInput userInput, Scanner scanner) {
        File groupHome = new File(userInput.dataPath + File.separator + "store" +
                File.separator + "topics" + File.separator + userInput.topic +
                File.separator + userInput.group);

        File [] messageFiles = groupHome.listFiles(file -> file.isFile() && file.getName().matches("\\d+"));
        if(null == messageFiles || messageFiles.length == 0) {
            System.out.println(String.format("No message files in topic group dir: %s, exit.", groupHome.getAbsolutePath()));
            System.exit(0);
        }


        long [] longFileNames = Arrays.stream(messageFiles)
                .map(File::getName)
                .mapToLong(Long::parseLong)
                .sorted()
                .toArray();

        System.out.println(String.format("Found message files [Filename/FileSize]: %s.",
                Arrays.stream(longFileNames)
                        .mapToObj(l -> new File(groupHome,String.valueOf(l)))
                        .map(file -> file.getName() + "/" + file.length())
                        .collect(Collectors.joining(", "))));

        userInput.startPosition = longFileNames[0];
        boolean isInputValid = false;
        while (!isInputValid) {

            System.out.print(String.format("Scan from position [%d]: ", userInput.startPosition));
            String input = scanner.nextLine();
            if(input.trim().isEmpty()) {
                isInputValid = true;
            } else {
                try {
                    long inputStartPosition = Long.parseLong(input);
                    if (isInputValid = (inputStartPosition >= longFileNames[0])) {
                        userInput.startPosition = inputStartPosition;
                    }
                } catch (NumberFormatException e) {
                    isInputValid = false;
                }
            }
            if(isInputValid){
                System.out.println(String.format("Scan will start from position: %d.", userInput.startPosition));
            } else {
                System.out.println(String.format("invalid input: %s.", input));
            }
        }
    }

    private static void confirm(Scanner scanner) {
        boolean isInputValid = false;
        while (!isInputValid) {

            System.out.print("A full scan will be applied, " +
                    "BAD files may be DELETED or TRUNCATED! " +
                    "Type \"Y or y\" to continue or type \"Q or q\" to quit: ");
            String input = scanner.nextLine();
            if("q".equalsIgnoreCase(input)) {
                System.exit(0);
            }
            isInputValid = "y".equalsIgnoreCase(input);
        }
    }




    private static class UserInput {
        private String topic = null;
        private int group = 0;
        private long startPosition = 0L;
        private String dataPath = System.getProperty("joyqueue.data.path", System.getProperty("user.home") + File.separator + ".joyqueue");
    }

    private static boolean contains(final int[] array, final int v) {

        boolean result = false;

        for(int i : array){
            if(i == v){
                result = true;
                break;
            }
        }

        return result;
    }


    /**
     * 检查文件是否连续，如果不连续，删除后面的文件。
     */
    private static void deleteNonContinousFiles(File base, long start) {

        System.out.println("Checking files " + base.getAbsolutePath() + " ...");
        List<File> files = Arrays.stream(Objects.requireNonNull(base.listFiles(file -> file.isFile() && file.getName().matches("\\d+"))))
                .filter(file -> start < 0 || Long.parseLong(file.getName()) >= start)
                .sorted(Comparator.comparing(file -> Long.parseLong(file.getName())))
                .collect(Collectors.toList());
        long lastPos = -1;
        boolean delMark = false;
        for (File file : files) {
            if (delMark) {
                delete(file);
            }  else {
                long filePos = Long.parseLong(file.getName());
                long dataLength = file.length() - PositioningStore.Config.DEFAULT_FILE_HEADER_SIZE;
                if (lastPos < 0) {
                    lastPos = filePos + dataLength;
                } else {
                    if (lastPos != filePos) {
                        System.out.println(String.format("Files are not continuous! expect: %d, actual file name: %d.", lastPos, filePos));
                        delete(file);
                        delMark = true;
                    } else {
                        lastPos += dataLength;
                    }
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


    private static void checkIndices(File base, long startPosition) throws IOException {
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
        PreloadBufferPool pool = PreloadBufferPool.getInstance();

        PositioningStore<ByteBuffer> logStore =
                new PositioningStore<>(base, new PositioningStore.Config(StoreConfig.DEFAULT_MESSAGE_FILE_SIZE),
                        pool, new StoreMessageSerializer(PartitionGroupStoreManager.Config.DEFAULT_MAX_MESSAGE_LENGTH));
        logStore.recover();

        if (startPosition < 0) startPosition = logStore.left();
        File indexBase = new File(base, "index");
        Map<Short, Partition> partitionMap = new HashMap<>();
        int progress = 0;
        long start = SystemClock.now();
        for (short p : partitions) {
            File partitionBase = new File(indexBase, String.valueOf(p));
            deleteNonContinousFiles(partitionBase, -1);
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
                System.out.println("Rollback store to position " + Format.formatWithComma(position) + ".");

                truncate(base, position);
                break;

            }


            if(indexPosition >= partition.store.left() && indexPosition < partition.store.right()){

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
                    System.out.println("Rollback partition index to " + Format.formatWithComma(logIndex.getIndex()) + ".");

                    partition.store.close();
                    truncate(partition.store.base(), indexPosition);
                    partition.store.recover();

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
                long passed = SystemClock.now() - start;
                long remaining = passed / progress * (100 - progress);
                System.out.println(progress + "%, " + formatDuration(Duration.ofMillis(remaining)));
            }
            position += length;
        }
        logStore.close();
    }

    private static class Partition {
        PositioningStore<IndexItem> store;
        long position = -1;
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
            fileMap.tailMap(deleteFrom).values().forEach(RecoverStore::delete);
        }

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
}
