package com.jd.journalq.server.retry.console;

import com.google.common.collect.Lists;
import com.jd.journalq.domain.ConsumeRetry;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.exception.JMQException;
import com.jd.journalq.model.PageResult;
import com.jd.journalq.model.Pagination;
import com.jd.journalq.server.retry.api.ConsoleMessageRetry;
import com.jd.journalq.server.retry.api.RetryPolicyProvider;
import com.jd.journalq.server.retry.db.DBMessageRetry;
import com.jd.journalq.server.retry.model.RetryMessageModel;
import com.jd.journalq.server.retry.model.RetryQueryCondition;
import com.jd.journalq.server.retry.model.RetryStatus;
import com.jd.journalq.toolkit.db.DaoUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

/**
 * 管理端重试管理实现
 * <p>
 * Created by chengzhiliang on 2019/2/20.
 */
public class DbConsoleMessageRetry implements ConsoleMessageRetry<Long> {

    private final Logger logger = LoggerFactory.getLogger(DbConsoleMessageRetry.class);

    // 数据源
    private DataSource dataSource;
    // 缓存服务
    private DBMessageRetry dbMessageRetry = new DBMessageRetry();
    // start flag
    private boolean isStartFlag = false;

    @Override
    public void start() throws Exception {
        dbMessageRetry.start();
        dataSource = dbMessageRetry.getDataSource();
        isStartFlag = true;

        logger.info("ConsoleMessageRetry is started.");
    }

    @Override
    public void stop() {
        dbMessageRetry.stop();
        isStartFlag = false;

        logger.info("ConsoleMessageRetry is stop.");
    }

    @Override
    public boolean isStarted() {
        return isStartFlag;
    }

    private final static String GET_BYID = "select * from message_retry where id = ?";
    private final static String QUERY_SQL = "select * from message_retry where topic = ? and app = ? and status = ? ";
    private final static String COUNT_SQL = "select count(*) from message_retry where topic = ? and app = ? and status = ? ";

    @Override
    public PageResult<ConsumeRetry> queryConsumeRetryList(RetryQueryCondition retryQueryCondition) throws JMQException {

        String queryDataSql = addCondition(retryQueryCondition, QUERY_SQL, true);
        PageResult<ConsumeRetry> pageResult = new PageResult<>();
        try {
            List<ConsumeRetry> list = DaoUtil.queryList(dataSource, queryDataSql, new DaoUtil.QueryCallback<ConsumeRetry>() {
                @Override
                public ConsumeRetry map(final ResultSet rs) throws Exception {
                    ConsumeRetry message = new ConsumeRetry();

                    message.setId(rs.getLong(1));
                    message.setMessageId(rs.getString(2));
                    message.setBusinessId(rs.getString(3));
                    message.setTopic(rs.getString(4));
                    message.setApp(rs.getString(5));
                    message.setSendTime(rs.getTimestamp(6).getTime());
                    message.setExpireTime(rs.getTimestamp(7).getTime());
                    message.setRetryTime(rs.getTimestamp(8).getTime());
                    message.setRetryCount(rs.getShort(9));
                    message.setData(rs.getBytes(10));
                    message.setException(rs.getBytes(11));
                    message.setCreateTime(rs.getTimestamp(12).getTime());
                    message.setCreateBy(rs.getInt(13));
                    message.setUpdateTime(rs.getTimestamp(14).getTime());
                    message.setUpdateBy(rs.getInt(15));
                    message.setStatus(rs.getShort(16));

                    return message;
                }

                @Override
                public void before(final PreparedStatement statement) throws Exception {
                    statement.setString(1, retryQueryCondition.getTopic());
                    statement.setString(2, retryQueryCondition.getApp());
                    statement.setShort(3, retryQueryCondition.getStatus());
                }
            });
            pageResult.setResult(list);

            Pagination pagination = retryQueryCondition.getPagination();
            int countRecords = countRecords(retryQueryCondition);
            pagination.setTotalRecord(countRecords);

            pageResult.setPagination(pagination);

            return pageResult;
        } catch (Exception e) {
            throw new JMQException(ToStringBuilder.reflectionToString(retryQueryCondition), e, JMQCode.CN_DB_ERROR.getCode());
        }
    }
    @Override
    public ConsumeRetry getConsumeRetryById(Long id) throws JMQException {

        try {
            ConsumeRetry consumeRetry = DaoUtil.queryObject(dataSource, GET_BYID, new DaoUtil.QueryCallback<ConsumeRetry>() {

                @Override
                public ConsumeRetry map(ResultSet rs) throws Exception {
                    ConsumeRetry message = new ConsumeRetry();

                    message.setId(rs.getLong(1));
                    message.setMessageId(rs.getString(2));
                    message.setBusinessId(rs.getString(3));
                    message.setTopic(rs.getString(4));
                    message.setApp(rs.getString(5));
                    message.setSendTime(rs.getTimestamp(6).getTime());
                    message.setExpireTime(rs.getTimestamp(7).getTime());
                    message.setRetryTime(rs.getTimestamp(8).getTime());
                    message.setRetryCount(rs.getShort(9));
                    message.setData(rs.getBytes(10));
                    message.setException(rs.getBytes(11));
                    message.setCreateTime(rs.getTimestamp(12).getTime());
                    message.setCreateBy(rs.getInt(13));
                    message.setUpdateTime(rs.getTimestamp(14).getTime());
                    message.setUpdateBy(rs.getInt(15));
                    message.setStatus(rs.getShort(16));

                    return message;
                }

                @Override
                public void before(PreparedStatement statement) throws Exception {
                    statement.setLong(1, id);
                }
            });
            return consumeRetry;
        } catch (Exception e) {
            throw new JMQException(ToStringBuilder.reflectionToString(id),e,JMQCode.CN_DB_ERROR.getCode());
        }
    }
    /**
     * 根据条件查询总记录数
     *
     * @param retryQueryCondition
     * @return
     * @throws Exception
     */
    private int countRecords(RetryQueryCondition retryQueryCondition) throws Exception {
        String countSql = addCondition(retryQueryCondition, COUNT_SQL, false);
        int count = (int) DaoUtil.queryObject(dataSource, countSql, new DaoUtil.QueryCallback<Integer>() {
            @Override
            public void before(PreparedStatement statement) throws Exception {
                statement.setString(1, retryQueryCondition.getTopic());
                statement.setString(2, retryQueryCondition.getApp());
                statement.setShort(3, retryQueryCondition.getStatus());
            }

            @Override
            public Integer map(ResultSet rs) throws Exception {
                return rs.getInt(1);
            }
        });

        return count;
    }

    /**
     * 拼接查询SQL
     *
     * @param retryQueryCondition
     * @param sql
     * @return
     */
    private String addCondition(RetryQueryCondition retryQueryCondition, String sql, boolean pageFlag) {
        // 是否包含开始时间
        long startTime = retryQueryCondition.getStartTime();
        if (startTime != 0) {
            sql += " send_time >= " + startTime;
        }

        // 是否包含结束时间
        long endTime = retryQueryCondition.getEndTime();
        if (endTime != 0) {
            sql += " send_time <= " + endTime;
        }

        // 是否包含业务主键
        String buzId = retryQueryCondition.getBusinessId();
        if (StringUtils.isNotEmpty(buzId)) {
            sql += " business_id = '" + buzId + "'";
        }

        if (pageFlag) {
            // 查询起始位置
            long startIndex = retryQueryCondition.getPagination().getStart();
            // 查询条数
            int count = retryQueryCondition.getPagination().getPage() * retryQueryCondition.getPagination().getSize();
            sql += " limit " + startIndex + " , " + count;

        }

        logger.debug("Console query sql:{}", sql);

        return sql;
    }


    private final static String UPDATE_SQL = "update message_retry set status = ?,update_time = ?, update_by = ? where topic = ? and id = ? ";

    @Override
    public void updateStatus(String topic, String app, Long[] messageIds, RetryStatus status, long updateTime, int updateBy) throws Exception {
        int update = DaoUtil.update(dataSource, Lists.newArrayList(messageIds), UPDATE_SQL, (DaoUtil.UpdateCallback<Long>) (statement, target) -> {
            statement.setShort(1, status.getValue());
            statement.setTimestamp(2, new Timestamp(updateTime));
            statement.setInt(3, updateBy);
            statement.setString(4, topic);
            statement.setLong(5, target);
        });
        if (update > 0) {
            logger.info("update retry message success by topic:{}, messageId:{}, status:{}", topic, Arrays.toString(messageIds), status);
        } else {
            logger.error("update retry message error by topic:{}, messageId:{}, status:{}", topic, Arrays.toString(messageIds), status);
        }
    }

    @Override
    public void addRetry(List<RetryMessageModel> retryMessageModelList) throws JMQException {
        dbMessageRetry.addRetry(retryMessageModelList);
    }

    @Override
    public void retrySuccess(String topic, String app, Long[] messageIds) throws JMQException {
        dbMessageRetry.retrySuccess(topic, app, messageIds);
    }

    @Override
    public void retryError(String topic, String app, Long[] messageIds) throws JMQException {
        dbMessageRetry.retryError(topic, app, messageIds);
    }

    @Override
    public void retryExpire(String topic, String app, Long[] messageIds) throws JMQException {
        dbMessageRetry.retryExpire(topic, app, messageIds);
    }

    @Override
    public List<RetryMessageModel> getRetry(String topic, String app, short count, long startIndex) throws JMQException {
        // 该方法会操作缓存和数据库，管理端调用会影响broker
        throw new UnsupportedOperationException();
    }

    @Override
    public int countRetry(String topic, String app) throws JMQException {
        return dbMessageRetry.countRetry(topic, app);
    }

    @Override
    public void setRetryPolicyProvider(RetryPolicyProvider retryPolicyProvider) {
        throw new UnsupportedOperationException();
    }
}
