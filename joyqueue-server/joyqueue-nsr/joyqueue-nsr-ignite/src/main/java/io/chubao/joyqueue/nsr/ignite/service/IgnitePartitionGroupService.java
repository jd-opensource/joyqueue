package io.chubao.joyqueue.nsr.ignite.service;

import com.alibaba.fastjson.JSON;
import com.google.inject.Inject;
import io.chubao.joyqueue.domain.PartitionGroup;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.nsr.ignite.dao.PartitionGroupDao;
import io.chubao.joyqueue.nsr.ignite.model.IgnitePartitionGroup;
import io.chubao.joyqueue.nsr.model.PartitionGroupQuery;
import io.chubao.joyqueue.nsr.service.PartitionGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2018/9/4
 */
public class IgnitePartitionGroupService implements PartitionGroupService {
    private static Logger logger = LoggerFactory.getLogger(IgnitePartitionGroupService.class);

    private PartitionGroupDao partitionGroupDao;

    @Inject
    public IgnitePartitionGroupService(PartitionGroupDao igniteDao) {
        this.partitionGroupDao = igniteDao;
    }

    @Override
    public PartitionGroup findByTopicAndGroup(TopicName topic, int group) {
        return getById(IgnitePartitionGroup.getId(topic, group));
    }

    @Override
    public List<PartitionGroup> getByTopic(TopicName topic) {
        return convert(partitionGroupDao.list(new PartitionGroupQuery(topic.getCode(), topic.getNamespace())));
    }


    public IgnitePartitionGroup toIgniteModel(PartitionGroup model) {
        return new IgnitePartitionGroup(model);
    }

    @Override
    public PartitionGroup getById(String id) {
        return partitionGroupDao.findById(id);
    }

    @Override
    public PartitionGroup get(PartitionGroup model) {
        return partitionGroupDao.findById(toIgniteModel(model).getId());
    }

    @Override
    public void addOrUpdate(PartitionGroup partitionGroup) {
        logger.info("partitiongroup addorUpdate partitionGroup:{}", JSON.toJSONString(partitionGroup));
        partitionGroupDao.addOrUpdate(toIgniteModel(partitionGroup));
    }

    @Override
    public void deleteById(String id) {
        partitionGroupDao.deleteById(id);
    }

    @Override
    public void delete(PartitionGroup model) {
        partitionGroupDao.deleteById(toIgniteModel(model).getId());
    }

    @Override
    public List<PartitionGroup> list() {
        return list(null);
    }

    @Override
    public List<PartitionGroup> list(PartitionGroupQuery query) {
        return convert(partitionGroupDao.list(query));
    }

    @Override
    public PageResult<PartitionGroup> pageQuery(QPageQuery<PartitionGroupQuery> pageQuery) {
        PageResult<IgnitePartitionGroup> pageResult = partitionGroupDao.pageQuery(pageQuery);
        return new PageResult<>(pageResult.getPagination(), convert(pageResult.getResult()));
    }

    List<PartitionGroup> convert(List<IgnitePartitionGroup> groups) {

        List<PartitionGroup> result = new ArrayList<>();
        if (groups != null) {
            result.addAll(groups);
        }

        return result;
    }
}
