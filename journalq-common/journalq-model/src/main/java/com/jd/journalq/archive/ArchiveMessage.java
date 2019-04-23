/**
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
/**
 *
 */
package com.jd.journalq.archive;

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
     Date getArchiveTime();

    /**
     * 返回消息类型
     *
     * @return 消息类型
     */
     String getTopic();
}
