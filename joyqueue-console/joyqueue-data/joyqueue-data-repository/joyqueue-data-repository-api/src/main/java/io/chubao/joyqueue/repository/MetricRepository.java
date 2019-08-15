package io.chubao.joyqueue.repository;

import io.chubao.joyqueue.model.Codeable;
import io.chubao.joyqueue.model.domain.Metric;
import io.chubao.joyqueue.model.query.QMetric;
import org.springframework.stereotype.Repository;

/**
 * 指标
 */
@Repository
public interface MetricRepository extends PageRepository<Metric, QMetric>, Codeable<Metric> {

    Metric findByAliasCode(String aliasCode);

}
