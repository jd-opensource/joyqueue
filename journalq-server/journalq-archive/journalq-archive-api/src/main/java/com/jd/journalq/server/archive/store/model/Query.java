package com.jd.journalq.server.archive.store.model;

/**
 * Created by chengzhiliang on 2018/12/4.
 */
public interface Query {

    <T> T getQueryCondition();
}
