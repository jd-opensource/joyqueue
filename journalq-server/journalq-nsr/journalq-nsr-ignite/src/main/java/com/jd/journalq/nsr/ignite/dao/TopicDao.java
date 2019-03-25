package com.jd.journalq.nsr.ignite.dao;

import com.jd.journalq.nsr.ignite.model.IgniteTopic;
import com.jd.journalq.nsr.model.TopicQuery;

public interface TopicDao extends BaseDao<IgniteTopic, TopicQuery, String> {
}
