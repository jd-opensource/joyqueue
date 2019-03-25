package com.jd.journalq.repository;

import com.jd.journalq.model.Codeable;
import com.jd.journalq.model.domain.Metric;
import com.jd.journalq.model.query.QMetric;
import org.springframework.stereotype.Repository;

/**
 * 指标
 */
@Repository
public interface MetricRepository extends PageRepository<Metric, QMetric>, Codeable<Metric> {

}
