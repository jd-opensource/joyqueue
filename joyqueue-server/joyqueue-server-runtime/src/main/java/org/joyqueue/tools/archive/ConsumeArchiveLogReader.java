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
package org.joyqueue.tools.archive;

import org.joyqueue.broker.archive.ArchiveSerializer;
import org.joyqueue.server.archive.store.HBaseSerializer;
import org.joyqueue.server.archive.store.model.ConsumeLog;
import org.joyqueue.toolkit.network.IpUtil;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * Consume archive log reader
 *
 **/
public class ConsumeArchiveLogReader {

    private static final byte RECORD_START_FLAG= Byte.MIN_VALUE;
    private static final byte RECORD_END_FLAG= Byte.MAX_VALUE;
    // use larger buffer size than consume log file
    private static final int bufferSize=32*1024*1024;
    private ByteBuffer buf= ByteBuffer.allocate(bufferSize);
    private static final DateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static void main(String[] args){
          if(args.length < 5){
              throw new IllegalArgumentException("args too less(base path,start time,end time,message id)," +
                      "such as /archive 1596084370000 1596123970000 ssss F7C0D91A52D59F4E1B05C8273EF41B69");
          }
          String basePath=args[0];
          long startLastModifiedTimeMs=Long.parseLong(args[1]);
          long endLastModifiedTimeMs=Long.parseLong(args[2]);
          String topic=args[3];
          // message id md5 hex
          String messageId=args[4];
          System.out.println(String.format("on %s,Scan for topic:%s,message id %s in %s-%s",basePath,topic,messageId,
                  sdf.format(new Date(startLastModifiedTimeMs)),
                  sdf.format(new Date(endLastModifiedTimeMs))));
          File consumeLogParentFile=new File(basePath);
          if(!consumeLogParentFile.isDirectory()){
              throw new IllegalArgumentException(String.format("%s is not directory",consumeLogParentFile));
          }
          File[] allConsumeLogs=consumeLogParentFile.listFiles();
          ConsumeArchiveLogReader reader=new ConsumeArchiveLogReader(messageId);
          Arrays.sort(allConsumeLogs, Comparator.comparingLong(reader::FileToLong));
          // never read last file
          for(int i=0;i<allConsumeLogs.length-1;i++){
             File log= allConsumeLogs[i];
             if(log.lastModified()>startLastModifiedTimeMs&&log.lastModified()<endLastModifiedTimeMs){
                 try {
                     List<ConsumeLog> consumeLogs = reader.readConsumeLogFromFile(log);
                     reader.onConsumeLogEvent(consumeLogs);
                 }catch (IOException e){
                     System.out.println("io exception:"+e);
                     break;
                 }
             }
         }
    }
    private String messageId;
    //  message id md5 bytes
    private byte[] byteMessageId;
    public ConsumeArchiveLogReader(String messageId){
        this.messageId=messageId;
        this.byteMessageId= HBaseSerializer.hexStrToByteArray(messageId);
    }
    public long FileToLong(File file){
        return Long.parseLong(file.getName());
    }

    /**
     * Consume message log and
     **/
    public void onConsumeLogEvent(List<ConsumeLog> consumeLogs){
      for(ConsumeLog log:consumeLogs){
          // consume log don't contain topic
          if(Arrays.equals(log.getBytesMessageId(),byteMessageId)){
              System.out.println(formatConsumeLog(log));
          }
      }
    }

    /**
     * Format consume log
     **/
    public String formatConsumeLog(ConsumeLog log){
        StringBuilder clientIp = new StringBuilder();
        IpUtil.toAddress(log.getClientIp(), clientIp);
        return String.format("Consume event found, %s   %s   %s    %s",messageId,log.getApp(),clientIp,sdf.format(new Date(log.getConsumeTime())));
    }


    /**
     * Read consume log from log file
     **/
    public List<ConsumeLog> readConsumeLogFromFile(File consumeLog) throws IOException {
         if(!consumeLog.exists()){
             return null;
         }
        List<ConsumeLog> consumeLogs=new ArrayList<>();
        RandomAccessFile randomAccessFile=null;
        FileChannel fileChannel=null;
        try {
             buf.clear();
             randomAccessFile= new RandomAccessFile(consumeLog, "r");
             fileChannel= randomAccessFile.getChannel();
             while (true) {
                 try {
                     //if(readBuffer.)
                     int readSize = fileChannel.read(buf);
                     if (readSize > 0) {
                         buf.flip();
                         // prepare for read
                         consumeLogs.addAll(readConsumeLog(consumeLog.getName(),buf));
                         // process
                         if (buf.hasRemaining()) {
                             buf.compact();
                         }
                         buf.flip();
                     } else {
                         break;
                     }
                 } catch (IOException e) {
                     System.out.println("read exception:" + e);
                     break;
                 }catch (IllegalStateException e){
                     System.out.println("on file: "+consumeLog.getName() + ", " + e);
                     break;
                 }
             }
         }finally {
            if(fileChannel!=null) {
                fileChannel.close();
            }
            if(randomAccessFile!=null) {
                randomAccessFile.close();
            }
         }
         System.out.println("read consume log on "+consumeLog.getName()+" finished!");
        return consumeLogs;
    }

    /**
     * Read consume log from buffer
     **/
    public List<ConsumeLog> readConsumeLog(String logName,ByteBuffer buffer){
        List<ConsumeLog> consumeLogs=new ArrayList<>();
        while(buffer.hasRemaining()){
           byte flag= buffer.get();
           if(flag == RECORD_START_FLAG) {
               ConsumeLog log = ArchiveSerializer.tryRead(buffer);
               if (log != null) {
                   consumeLogs.add(log);
               }
           }else if(flag == RECORD_END_FLAG){
               System.out.println(String.format("Meet %s end flag",logName));
               break;
           }else{
               // reset
               buffer.position(buffer.position()-1);
               throw new IllegalStateException(String.format("Bad format on %s",logName));
           }
        }
        return consumeLogs;
    }

}
