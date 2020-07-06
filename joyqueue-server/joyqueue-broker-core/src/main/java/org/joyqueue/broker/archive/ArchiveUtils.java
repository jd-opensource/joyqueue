package org.joyqueue.broker.archive;

/**
 * Common utils
 *
 **/
public class ArchiveUtils {

    /**
     *  Message unique id
     **/
    public static String messageId(String topic,short partition,long messageIndex){
        return topic+partition+messageIndex;
    }

}
