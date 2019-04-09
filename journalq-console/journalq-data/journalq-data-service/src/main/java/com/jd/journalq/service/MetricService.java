package com.jd.journalq.service;

import com.jd.journalq.model.Codeable;
import com.jd.journalq.model.domain.Metric;
import com.jd.journalq.model.query.QMetric;

public interface MetricService extends PageService<Metric, QMetric>, Codeable<Metric> {

    Metric findByAliasCode(String aliasCode);

}
