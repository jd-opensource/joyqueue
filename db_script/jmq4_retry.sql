set @sharding = 'message_retry topic hash'
CREATE TABLE `message_retry` (
	`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`message_id` varchar(50) NOT NULL COMMENT '消息编号',
	`business_id` varchar(100) DEFAULT NULL COMMENT '业务编号',
	`topic` varchar(100) NOT NULL COMMENT '主题',
	`app` varchar(100) NOT NULL COMMENT '应用',
	`send_time` datetime NOT NULL COMMENT '发送时间',
	`expire_time` datetime NOT NULL COMMENT '过期时间',
	`retry_time` datetime NOT NULL COMMENT '重试时间',
	`retry_count` int(10) NOT NULL DEFAULT '0' COMMENT '重试次数',
	`data` mediumblob NOT NULL COMMENT '消息体',
	`exception` blob COMMENT '异常信息',
	`create_time` datetime NOT NULL COMMENT '创建时间',
	`create_by` int(10) NOT NULL DEFAULT '0' COMMENT '创建人',
	`update_time` datetime NOT NULL COMMENT '更新时间',
	`update_by` int(10) NOT NULL DEFAULT '0' COMMENT '更新人',
	`status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态,0:成功,1:失败,-2:过期',
	PRIMARY KEY (`id`),
	KEY `idx_topic_app` (`topic`, `app`, `status`, `retry_time`)
) ENGINE = InnoDB AUTO_INCREMENT = 0 CHARSET = utf8 COMMENT '消息重试表'
set @sequence='message_retry id'