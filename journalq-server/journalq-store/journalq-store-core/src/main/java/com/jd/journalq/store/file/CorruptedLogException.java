package com.jd.journalq.store.file;

/**
 * 文件已损坏
 * @author liyue25
 * Date: 2018/9/29
 */
public class CorruptedLogException extends RuntimeException {
    public CorruptedLogException() {super();}
    public CorruptedLogException(String msg) {super(msg);}
}
