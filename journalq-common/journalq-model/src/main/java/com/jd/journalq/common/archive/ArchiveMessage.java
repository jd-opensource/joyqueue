/**
 *
 */
package com.jd.journalq.common.archive;

import java.util.Date;


/**
 * 归档的公共接口
 */
public interface ArchiveMessage {
    /**
     * 用于创建归档所在数据表的表名的日期，对消费历史记录取消息的到达时间
     *
     * @return 归档时间
     */
    public Date getArchiveTime();

    /**
     * 返回消息类型
     *
     * @return 消息类型
     */
    public String getTopic();
}
