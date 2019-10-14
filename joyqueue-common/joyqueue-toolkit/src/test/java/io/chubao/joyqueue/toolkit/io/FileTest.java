package io.chubao.joyqueue.toolkit.io;

import org.junit.Test;

public class FileTest {

    @Test
    public void tree(){
        Directory directory=new Directory();
        String path="/export/Data/journalq/";
        Files.tree(path,directory);
        System.out.println(directory);
    }
}
