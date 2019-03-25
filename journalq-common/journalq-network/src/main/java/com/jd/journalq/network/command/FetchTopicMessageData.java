package com.jd.journalq.network.command;

/**
 * FetchTopicMessageData
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/13
 */
public class FetchTopicMessageData {

    private int count;

    public FetchTopicMessageData() {

    }

    public FetchTopicMessageData(int count) {
        this.count = count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }
}