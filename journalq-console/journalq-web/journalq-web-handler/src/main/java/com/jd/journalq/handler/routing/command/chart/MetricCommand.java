package com.jd.journalq.handler.routing.command.chart;

import com.jd.journalq.handler.routing.command.CommandSupport;
import com.jd.journalq.model.domain.Metric;
import com.jd.journalq.model.query.QMetric;
import com.jd.journalq.service.MetricService;

/**
 * Created by libinghui3 on 2019/3/7.
 */
public class MetricCommand extends CommandSupport<Metric,MetricService,QMetric> {
}
