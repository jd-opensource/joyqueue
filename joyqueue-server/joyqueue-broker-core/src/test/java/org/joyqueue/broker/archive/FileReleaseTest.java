package org.joyqueue.broker.archive;

import org.joyqueue.toolkit.io.Files;
import org.joyqueue.toolkit.time.SystemClock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class FileReleaseTest {
    String userPath = System.getProperty("java.io.tmpdir");
    String baseDir=userPath+"/archive";
    int  fileSize= 1024*1024*16;

    @Before
    public void prepare(){
        emptyDir(baseDir);
    }

    @Test
    public void releaseTest() throws Exception {


        String fileName=String.valueOf(SystemClock.now());
        File baseFile=new File(baseDir);
        if(!baseFile.exists()){
            baseFile.mkdirs();
        }
        File file=new File(baseDir,fileName);
        System.out.println(file.getAbsolutePath());
        RandomAccessFile raf=new RandomAccessFile(file,"rw");
        FileChannel channel=raf.getChannel();
        MappedByteBuffer buffer=channel.map(FileChannel.MapMode.READ_WRITE,0,fileSize);
        channel.close();
        raf.close();

        Thread.sleep(10000);
        // un close  and roll next file
        fileName= String.valueOf(SystemClock.now());
        file=new File(baseDir,fileName);
        System.out.println(file.getAbsolutePath());
        raf=new RandomAccessFile(file,"rw");
        channel=raf.getChannel();
        buffer=channel.map(FileChannel.MapMode.READ_WRITE,0,fileSize);
        channel.close();
        raf.close();

        buffer.position(fileSize-1);
        Thread.sleep(60000*10);
    }


    @After
    public  void close(){
        emptyDir(baseDir);
    }

    /**
     * Empty directory
     **/
    public void emptyDir(String path){
        File file=new File(path);
        if(file.exists()){
            Files.deleteDirectory(file);
        }
    }
}
