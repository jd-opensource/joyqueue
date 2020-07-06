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
package org.joyqueue.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * joyqueue异常码枚举类
 *
 * <p>
 * 该类定义了JMQ框架抛出的所有异常码和消息提示文本，用于统一约定和管理所有异常信息
 * </p>
 * <p>
 * JMQ按功能大致分为：公用、服务端、客户端、存储、复制以及流程六大模块。为了方便
 * 管理异常码,所有的异常码命名和分段也按照模块来划分。
 * </p>
 * <p>以下是具体的码段及前缀分配规则：</p>
 * <table  summary="JoyQueue exception code ">
 * <tr>
 * <th>模块名</th>
 * <th>预留码段</th>
 * <th>命名前缀</th>
 * <th>举例</th>
 * </tr>
 * <tr>
 * <td>成功</td>
 * <td>0</td>
 * <td>SUCCESS</td>
 * <td>SUCCESS(0, '成功')</td>
 * </tr>
 * <tr>
 * <td>公用</td>
 * <td>1 ~ 50</td>
 * <td>CN_</td>
 * <td>CN_COMMON_ERROR(23,"公用出错")</td>
 * </tr>
 * <tr>
 * <td>服务端</td>
 * <td>51 ~ 70</td>
 * <td>SR_</td>
 * <td>SR_SERVER_ERROR(51, "服务端出错")</td>
 * </tr>
 * <tr>
 * <td>客户端</td>
 * <td>71 ~ 90</td>
 * <td>CT_</td>
 * <td>CL_CLIENT_ERROR(71, "客户端出错")</td>
 * </tr>
 * <tr>
 * <td>存储</td>
 * <td>91 ~ 110</td>
 * <td>SE_</td>
 * <td>SE_STORAGE_ERROR(91, "存储出错")</td>
 * </tr>
 * <tr>
 * <td>复制</td>
 * <td>111 ~ 130</td>
 * <td>CY_</td>
 * <td>CY_COPY_ERROR(111, "复制出错")</td>
 * </tr>
 * <tr>
 * <td>流程</td>
 * <td>131 ~ 150</td>
 * <td>FW_</td>
 * <td>FW_FLOW_ERROR(131, "流程出错")</td>
 * </tr>
 * </table>
 * <strong>注：实现者应严格按照该规则定义自己的异常码</strong>
 *
 * 14-4-19 上午10:43
 * @author Jame.HU
 * @version V1.0
 *
 */
public enum JoyQueueCode {
    // 0：表示成功
    SUCCESS(0, "成功"),

    // 1 ~ 50 公共异常码段, 以CN_开头
    CN_NO_PERMISSION(1, "无权限"), //TODO 无权限太笼统，须详细划分一下
    CN_AUTHENTICATION_ERROR(2, "认证失败"),
    CN_SERVICE_NOT_AVAILABLE(3, "服务不可用"),
    CN_UNKNOWN_ERROR(4, "未知异常"),
    CN_DB_ERROR(5, "数据库异常"),
    CN_PARAM_ERROR(6, "参数错误"),
    CN_NEGATIVE_VOTE(7, "反对票"),
    CN_CHECKSUM_ERROR(8, "校验和出错"),
    CN_INIT_ERROR(9, "服务初始化出错"),

    CN_CONNECTION_ERROR(20, "连接出错,%s"),
    CN_CONNECTION_TIMEOUT(21, "连接超时,%s"),
    CN_REQUEST_TIMEOUT(22, "请求超时,%s"),
    CN_REQUEST_ERROR(23, "请求发送异常"),
    CN_REQUEST_EXCESSIVE(24, "异步请求过多"),
    CN_THREAD_INTERRUPTED(25, "线程被中断"),
    CN_THREAD_EXECUTOR_BUSY(26, "线程执行器繁忙"),
    CN_COMMAND_UNSUPPORTED(27, "请求命令不被支持,%s"),
    CN_DECODE_ERROR(28, "解码出错"),
    CN_PLUGIN_NOT_IMPLEMENT(29, "插件没有实现"),

    CN_TRANSACTION_PREPARE_ERROR(30, "事务准备失败"),
    CN_TRANSACTION_EXECUTE_ERROR(31, "本地事务执行失败"),
    CN_TRANSACTION_COMMIT_ERROR(32, "事务提交失败"),
    CN_TRANSACTION_ROLLBACK_ERROR(33, "事务回滚失败"),
    CN_TRANSACTION_NOT_EXISTS(34, "事务不存在"),
    CN_TRANSACTION_UNSUPPORTED(35, "分布式事务不支持"),
    CN_BATCH_NOT_ONE_PARTITION(36, "批量存储的消息不在同一个partition内"),

    // 71 ~ 90 客户端异常码段, 以CT_开头
    CT_NO_CLUSTER(71, "主题 %s 没有可用集群信息"),
    CT_SEQUENTIAL_BROKER_AMBIGUOUS(72, "多余顺序消息BROKER信息"),
    CT_NO_CONSUMER_RECORD(73, "本地偏移量管理消费没有消息信息记录"),
    CT_LIMIT_REQUEST(74,"请求限流"),
    CT_LOW_VERSION(75,"客户端版本低"),
    CT_MESSAGE_BODY_NULL(76, "消息体为空"),

    // 91 ~ 110 存储异常码段, 以SE_开头
    SE_IO_ERROR(91, "IO异常"),
    SE_INDEX_OVERFLOW(92, "消息序号超过最大值"),
    SE_INDEX_UNDERFLOW(93, "消息序号小于最小值"),
    //    SE_OFFSET_OVERFLOW(92, "偏移量越界"),
//    SE_MESSAGE_SIZE_EXCEEDED(93, "消息体大小超过最大限制"),
    SE_DISK_FULL(94, "磁盘满了"),
//    SE_CREATE_FILE_ERROR(95, "创建文件失败"),
//    SE_FLUSH_TIMEOUT(96, "刷盘超时"),
//    SE_INVALID_JOURNAL(97, "无效日志数据，文件:%d 位置:%d"),
//    SE_INVALID_OFFSET(98, "无效位置，位置:%d"),
//    SE_REPLICATION_ERROR(99, "复制不成功"),
//    SE_ENQUEUE_SLOW(100, "创建队列，入队慢"),
    SE_DISK_FLUSH_SLOW(101, "磁盘刷新慢"),
    //    SE_APPEND_MESSAGE_SLOW(102, "追加消息处理慢"),
//    SE_QUEUE_NOT_EXISTS(103, "消费队列不存在，主题:%s"),
//    SE_FATAL_ERROR(104, "致命异常"),
//    SE_PENDING(105, "请求挂起"),
//    SE_APPEND_MESSAGE_RATE_LIMIT(106, "写触发限流"),
    SE_SERIALIZER_ERROR(107, "序列化/反序列化错误"),
    SE_WRITE_TIMEOUT(108, "写入超时"),
    SE_WRITE_FAILED(109, "写入错误"),
    SE_READ_FAILED(110, "读取错误"),

    // 111 ~ 130 复制异常码段, 以CY_开头
    CY_REPLICATE_ENQUEUE_TIMEOUT(111, "复制入队超时"),
    CY_REPLICATE_TIMEOUT(112, "复制超时"),
    CY_REPLICATE_ERROR(113, "复制异常"),
    CY_GET_OFFSET_ERROR(114, "从主取复制位置错误"),
    CY_FLUSH_OFFSET_ERROR(115, "刷新偏移量异常"),
    CY_STATUS_ERROR(116, "同步状态不对"),
    CY_NOT_DEGRADE(117, "复制不能降级"),

    // 131 ~ 150 , 180 ~ 200 , 服务端流程异常以FW_开头
    FW_CONNECTION_EXISTS(131, "连接已经存在"),
    FW_CONNECTION_NOT_EXISTS(132, "连接不存在"),
    FW_PRODUCER_NOT_EXISTS(134, "生产者不存在"),
    FW_CONSUMER_NOT_EXISTS(136, "消费者不存在"),
    FW_TRANSACTION_EXISTS(137, "事务已经存在"),
    FW_TRANSACTION_NOT_EXISTS(138, "事务不存在"),
    FW_COMMIT_ERROR(139, "提交事务失败"),
    FW_CONSUMER_ACK_FAIL(140, "消费者ack失败"),
    FW_PUT_MESSAGE_ERROR(141, "添加消息失败"),
    FW_GET_MESSAGE_ERROR(142, "获取消息失败"),
    FW_FLUSH_SEQUENTIAL_STATE_ERROR(143, "刷新顺序消息服务异常"),
    FW_PUT_MESSAGE_TOPIC_NOT_WRITE(144, "该分组被主题设置为禁止发送"),
    FW_GET_MESSAGE_TOPIC_NOT_READ(145, "该分组被主题设置为禁止消费"),
    FW_PUT_MESSAGE_APP_CLIENT_IP_NOT_WRITE(146, "该连接被应用者禁止发送"),
    FW_GET_MESSAGE_APP_CLIENT_IP_NOT_READ(147, "该连接被应用者禁止消费"),
    FW_TRANSACTION_LIMIT(148, "该主题未提交的事务数量达到限制数"),
    FW_CONSUMER_REMOTE_ERROR(149, "已经开启跨机房消费，本机房不能消费"),
    FW_ELECTION_ERROR(150, "选举异常"),
    FW_COORDINATOR_NOT_AVAILABLE(181, "协调者不可用"),
    FW_COORDINATOR_PARTITION_ASSIGNOR_TYPE_NOT_EXIST(182, "协调者分配类型不存在, %s"),
    FW_COORDINATOR_PARTITION_ASSIGNOR_ERROR(183, "协调者分配错误"),
    FW_FETCH_MESSAGE_INDEX_OUT_OF_RANGE(184, "拉取消息index超出范围"),
    FW_COORDINATOR_PARTITION_ASSIGNOR_NO_PARTITIONS(185, "协调者分配错误，没有可用分区"),
    FW_FETCH_TOPIC_MESSAGE_PAUSED(186, "主题暂停消费"),
    FW_FETCH_TOPIC_MESSAGE_BROKER_NOT_LEADER(187, "不是该主题leader"),
    FW_PRODUCE_MESSAGE_BROKER_NOT_LEADER(188, "不是该主题leader"),
    FW_TOPIC_NOT_EXIST(189, "TOPIC不存在"),
    FW_TOPIC_NO_PARTITIONGROUP(190, "TOPIC无可用分组"),
    FW_PARTITION_BROKER_NOT_LEADER(191, "partition在当前broker上不是leader"),
    FW_BROKER_NOT_READABLE(192, "当前broker不可读"),
    FW_BROKER_NOT_WRITABLE(193, "当前broker不可写"),

    // 151~160 agent错误，以JA开头
    JA_COMMAND_ERROR(151, "命令执行失败"),
    JA_COMMAND_EXISTS(152, "该命令正在执行"),

    // 161~170 agent错误，以TN开头
    TN_COMMAND_NOT_EXISTS(161, "命令不存在"),
    TN_COMMAND_ERROR(162, "命令%s执行失败"),
    TN_COMMAND_FORMAT_ERROR(163, "命令%s格式错误"),
    TN_COMMAND_PWD_ERROR(164, "账号密码错误"),

    //200-229 为telnet协议异常,以TL开头
    TL_GET_INFO_NULL(200,"通过Telnet获取结果为空"),

    //240-249 重试异常
    RETRY_ADD(240,"添加重试消息异常"),
    RETRY_GET(241, "获取重试消息异常"),
    RETRY_UPDATE(242, "更新重试消息异常"),
    RETRY_COUNT(243, "统计重试消息异常"),
    RETRY_DISABLED(244,"重试服务未打开异常"),
    RETRY_TOKEN_LIMIT(245,"重试服务无可用 token"),

    //250~259 消费位置
    CONSUME_POSITION_NULL(250, "消费位置空异常"),
    CONSUME_POSITION_UPDATE_ERROR(251, "消费位置更新异常"),
    CONSUME_POSITION_META_DATA_NULL(252, "消费位置元数据匹配异常"),

    NSR_REGISTER_ERR_BROKER_NOT_EXIST(260,"BROKER 不存在");


    private static Map<Integer, JoyQueueCode> codes = new HashMap<Integer, JoyQueueCode>();
    private int code;
    private String message;

    static {
        for (JoyQueueCode joyQueueCode : JoyQueueCode.values()) {
            codes.put(joyQueueCode.code, joyQueueCode);
        }
    }

    JoyQueueCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static JoyQueueCode valueOf(int code) {
        return codes.get(code);
    }

    public int getCode() {
        return code;
    }

    public String getMessage(Object... args) {
        if (args.length < 1) {
            return message;
        }
        return String.format(message, args);
    }
}
