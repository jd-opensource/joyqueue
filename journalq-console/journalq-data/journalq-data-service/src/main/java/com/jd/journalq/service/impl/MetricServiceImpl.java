package com.jd.journalq.service.impl;

import com.jd.journalq.model.domain.Metric;
import com.jd.journalq.model.query.QMetric;
import com.jd.journalq.repository.MetricRepository;
import com.jd.journalq.service.MetricService;
import org.springframework.stereotype.Service;

@Service("metricService")
public class MetricServiceImpl extends PageServiceSupport<Metric, QMetric, MetricRepository> implements MetricService {

    public Metric findByCode(String code) {
        return repository.findByCode(code);
    }
}
