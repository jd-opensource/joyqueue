package io.chubao.joyqueue.service;

import io.chubao.joyqueue.model.Codeable;
import io.chubao.joyqueue.model.domain.Metric;
import io.chubao.joyqueue.model.query.QMetric;

public interface MetricService extends PageService<Metric, QMetric>, Codeable<Metric> {

    Metric findByAliasCode(String aliasCode);

}
