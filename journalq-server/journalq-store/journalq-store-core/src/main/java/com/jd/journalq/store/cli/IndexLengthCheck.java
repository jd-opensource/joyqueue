package com.jd.journalq.store.cli;

import com.jd.journalq.store.PartitionGroupStoreManager;
import com.jd.journalq.store.file.PositioningStore;
import com.jd.journalq.store.file.StoreFile;
import com.jd.journalq.store.file.StoreFileImpl;
import com.jd.journalq.store.file.StoreMessageSerializer;
import com.jd.journalq.store.index.IndexItem;
import com.jd.journalq.store.index.IndexSerializer;
import com.jd.journalq.store.message.MessageParser;
import com.jd.journalq.store.utils.PreloadBufferPool;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

/**
 * @author liyue25
 * Date: 2019-03-27
 */
public class IndexLengthCheck {


    public static void main(String[] args) throws IOException {

        String topicBase = "/export/Data/jmq/store/topics";

        if (args.length < 1) {
            showUsage();
            return;
        }

        if (!isNumeric(args[args.length - 1])) {
            topicBase = args[args.length - 1];
        }
        args = Arrays.copyOfRange(args, 0, args.length - 1);

        if (args.length < 1) {
            showUsage();
            return;
        }

        String topic = args[0];

        int group = -1;
        short partition = -1;
        if (args.length > 1) {
            group = Integer.parseInt(args[1]);

            if (args.length > 2) {
                partition = Short.parseShort(args[2]);
            }
        }

        checkIndexLength(topicBase, topic, group, partition);

        System.out.println("All done!");
    }

    private static void checkIndexLength(String topicBase, String topic, int group, short partition) throws IOException {
        PreloadBufferPool bufferPool = new PreloadBufferPool();
        System.out.println(String.format("Store directory: %s.", topicBase));
        System.out.println(String.format("Topic: %s, group: %s, partition: %s.", topic,
                group < 0 ? "ALL" : String.valueOf(group),
                partition < 0 ? "ALL" : String.valueOf(partition)));
        File baseFile = new File(topicBase);
        File topicFile = new File(baseFile, topic);
        List<File> groupFiles = group > 0 ? Collections.singletonList(new File(topicFile, String.valueOf(group))) :
                Arrays.asList(Objects.requireNonNull(topicFile.listFiles(file -> file.isDirectory() && file.getName().matches("\\d+"))));
        for (File groupFile : groupFiles) {

            NavigableMap<Long, StoreFile<ByteBuffer>> journalFileMap = createJournalFileMap(bufferPool, groupFile);


            File indexBase = new File(groupFile, "index");
            List<File> partitionFiles = partition > 0 ? Collections.singletonList(new File(indexBase, String.valueOf(partition))) :
                    Arrays.asList(Objects.requireNonNull(indexBase.listFiles(file -> file.isDirectory() && file.getName().matches("\\d+"))));
            for (File partitionFile : partitionFiles) {
                NavigableMap<Long, StoreFile<IndexItem>> partitionFileMap = createPartitionFileMap(bufferPool, partitionFile);

                for (StoreFile<IndexItem> indexFile : partitionFileMap.values()) {
                    System.out.println("Checking: " + indexFile.file().getAbsolutePath() + "...");
                    int offset = 0;
                    while (offset <= indexFile.writePosition() - IndexItem.STORAGE_SIZE) {
                        try {
                            IndexItem indexItem = indexFile.read(offset, IndexItem.STORAGE_SIZE);
                            StoreFile<ByteBuffer> journalFile = journalFileMap.floorEntry(indexItem.getOffset()).getValue();

                            long relPosition = indexItem.getOffset() - journalFile.position();
                            if (relPosition + Integer.BYTES <= journalFile.fileDataSize()) {
                                int lengthInJournal = journalFile.readByteBuffer((int) relPosition, Integer.BYTES).getInt();

                                if (lengthInJournal != indexItem.getLength()) {
                                    System.out.println("Found length mismatch!");
                                    System.out.println("Partition file: " + indexFile.file().getAbsolutePath());
                                    System.out.println(String.format("Index : %d, length: %d, offset: %d",
                                            (offset + indexFile.position()) / IndexItem.STORAGE_SIZE,
                                            indexItem.getLength(),
                                            indexItem.getOffset()));
                                    System.out.println("Message: ");

                                    System.out.println(MessageParser.getString(journalFile.readByteBuffer((int) relPosition, lengthInJournal)));

                                }
                            }
                        } catch (Throwable t) {
                            System.out.println("Exception! offset: " + offset);
                            t.printStackTrace();
                            System.out.println("Partition file: " + indexFile.file().getAbsolutePath());
                            IndexItem indexItem = indexFile.read(offset, IndexItem.STORAGE_SIZE);
                            System.out.println(String.format("Index : %d, length: %d, offset: %d",
                                    offset / IndexItem.STORAGE_SIZE,
                                    indexItem.getLength(),
                                    indexItem.getOffset()));
                            StoreFile<ByteBuffer> journalFile = journalFileMap.floorEntry(indexItem.getOffset()).getValue();
                            long relPosition = indexItem.getOffset() - journalFile.position();
                            System.out.println(String.format("RelPosition: %d, Journal File: %d, File data size: %d.",
                                    relPosition, journalFile.position(), journalFile.fileDataSize()));
                        }

                        offset += IndexItem.STORAGE_SIZE;

                    }
                    indexFile.unload();
                    for (StoreFile<ByteBuffer> journalFile : journalFileMap.values()) {
                        if (journalFile.hasPage()) {
                            journalFile.unload();
                        }
                    }
                }
            }
            journalFileMap.clear();
        }

    }

    private static NavigableMap<Long, StoreFile<IndexItem>> createPartitionFileMap(PreloadBufferPool bufferPool, File partitionFile) {
        NavigableMap<Long, StoreFile<IndexItem>> partitionFileMap = new TreeMap<>();
        File[] files = partitionFile.listFiles(file -> file.isFile() && file.getName().matches("\\d+"));
        long filePosition;
        if (null != files) {
            for (File file : files) {
                filePosition = Long.parseLong(file.getName());
                partitionFileMap.put(filePosition, new StoreFileImpl<>(
                        filePosition, partitionFile, PositioningStore.Config.DEFAULT_FILE_HEADER_SIZE,
                        new IndexSerializer(),
                        bufferPool, PositioningStore.Config.DEFAULT_FILE_DATA_SIZE));
            }
        }
        return partitionFileMap;
    }

    private static NavigableMap<Long, StoreFile<ByteBuffer>> createJournalFileMap(PreloadBufferPool bufferPool, File groupFile) {
        NavigableMap<Long, StoreFile<ByteBuffer>> journalFileMap = new TreeMap<>();
        File[] files = groupFile.listFiles(file -> file.isFile() && file.getName().matches("\\d+"));
        long filePosition;
        if (null != files) {
            for (File file : files) {
                filePosition = Long.parseLong(file.getName());
                journalFileMap.put(filePosition, new StoreFileImpl<>(
                        filePosition, groupFile, PositioningStore.Config.DEFAULT_FILE_HEADER_SIZE,
                        new StoreMessageSerializer(PartitionGroupStoreManager.Config.DEFAULT_MAX_MESSAGE_LENGTH),
                        bufferPool, PositioningStore.Config.DEFAULT_FILE_DATA_SIZE));
            }
        }
        return journalFileMap;
    }

    private static void showUsage() {
        System.out.println("Usage: IndexLengthCheck topic [partition group] [partition] [topic base dir]");
    }


    private static boolean isNumeric(String strNum) {
        try {
            Long.parseLong(strNum);
        } catch (NumberFormatException | NullPointerException nfe) {
            return false;
        }
        return true;
    }
}
