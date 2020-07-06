/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.nsr.ignite.service;

import com.alibaba.fastjson.JSON;
import com.google.inject.Inject;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.TopicName;
import org.joyqueue.nsr.ignite.dao.PartitionGroupDao;
import org.joyqueue.nsr.ignite.model.IgnitePartitionGroup;
import org.joyqueue.nsr.model.PartitionGroupQuery;
import org.joyqueue.nsr.service.internal.PartitionGroupInternalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2018/9/4
 */
public class IgnitePartitionGroupInternalService implements PartitionGroupInternalService {
    private static Logger logger = LoggerFactory.getLogger(IgnitePartitionGroupInternalService.class);

    private PartitionGroupDao partitionGroupDao;

    @Inject
    public IgnitePartitionGroupInternalService(PartitionGroupDao igniteDao) {
        this.partitionGroupDao = igniteDao;
    }

    @Override
    public PartitionGroup getByTopicAndGroup(TopicName topic, int group) {
        return getById(IgnitePartitionGroup.getId(topic, group));
    }

    @Override
    public List<PartitionGroup> getByTopic(TopicName topic) {
        return convert(partitionGroupDao.list(new PartitionGroupQuery(topic.getCode(), topic.getNamespace())));
    }

    @Override
    public List<PartitionGroup> getAll() {
        return convert(partitionGroupDao.list(null));
    }

    @Override
    public PartitionGroup add(PartitionGroup partitionGroup) {
        logger.info("partitiongroup add partitionGroup:{}", JSON.toJSONString(partitionGroup));
        partitionGroupDao.addOrUpdate(toIgniteModel(partitionGroup));
        return partitionGroup;
    }

    @Override
    public PartitionGroup update(PartitionGroup partitionGroup) {
        logger.info("partitiongroup update partitionGroup:{}", JSON.toJSONString(partitionGroup));
        partitionGroupDao.addOrUpdate(toIgniteModel(partitionGroup));
        return partitionGroup;
    }

    @Override
    public void delete(String id) {
        partitionGroupDao.deleteById(id);
    }

    public IgnitePartitionGroup toIgniteModel(PartitionGroup model) {
        return new IgnitePartitionGroup(model);
    }

    @Override
    public PartitionGroup getById(String id) {
        return partitionGroupDao.findById(id);
    }

    public PartitionGroup addOrUpdate(PartitionGroup partitionGroup) {
        logger.info("partitiongroup addOrUpdate partitionGroup:{}", JSON.toJSONString(partitionGroup));
        partitionGroupDao.addOrUpdate(toIgniteModel(partitionGroup));
        return partitionGroup;
    }

    List<PartitionGroup> convert(List<IgnitePartitionGroup> groups) {

        List<PartitionGroup> result = new ArrayList<>();
        if (groups != null) {
            result.addAll(groups);
        }

        return result;
    }
}
