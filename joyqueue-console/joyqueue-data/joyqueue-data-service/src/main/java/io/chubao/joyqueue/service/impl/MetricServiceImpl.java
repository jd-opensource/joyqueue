package io.chubao.joyqueue.service.impl;

import io.chubao.joyqueue.model.domain.Metric;
import io.chubao.joyqueue.model.query.QMetric;
import io.chubao.joyqueue.repository.MetricRepository;
import io.chubao.joyqueue.service.MetricService;
import org.springframework.stereotype.Service;

@Service("metricService")
public class MetricServiceImpl extends PageServiceSupport<Metric, QMetric, MetricRepository> implements MetricService {

    public Metric findByCode(String code) {
        return repository.findByCode(code);
    }

    @Override
    public Metric findByAliasCode(String aliasCode) {
        return repository.findByAliasCode(aliasCode);
    }
}
