--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--



-- 导出  表 laf_config.application 结构
CREATE TABLE IF NOT EXISTS `application` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键自增id',
  `code` varchar(128) NOT NULL COMMENT '应用 代码',
  `name` varchar(64) NOT NULL COMMENT '应用 名称',
  `system` varchar(64) NOT NULL COMMENT '所属系统',
  `department` varchar(128) NOT NULL COMMENT '所属部门',
  `owner_id` bigint(20) NOT NULL COMMENT '拥有者id',
  `owner_code` varchar(64) NOT NULL COMMENT '拥有者code',
  `source` tinyint(4) NOT NULL DEFAULT '0' COMMENT '来源：0 手动，jone 1， jdos 2 ',
  `sign` int(11) NOT NULL DEFAULT '0' COMMENT '签名',
  `create_by` bigint(20) NOT NULL COMMENT '创建者id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint(20) NOT NULL DEFAULT '0' COMMENT '修改人',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：-1 删除，0 禁用，1 启用',
  `alias_code` varchar(128) NOT NULL COMMENT '别名',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='应用';

-- 导出  表 laf_config.application_token 结构
CREATE TABLE IF NOT EXISTS `application_token` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键自增id',
  `app_id` bigint(20) NOT NULL COMMENT '应用id',
  `app_code` varchar(128) NOT NULL COMMENT '应用code',
  `token` varchar(40) NOT NULL COMMENT '应用token',
  `effective_time` datetime NOT NULL  COMMENT '生效时间',
  `expiration_time` datetime NOT NULL COMMENT '失效时间',
  `create_by` bigint(20) DEFAULT NULL COMMENT '创建者id',
  `create_by_code` varchar(64) DEFAULT NULL COMMENT '创建者code',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint(20) DEFAULT NULL COMMENT '修改者id',
  `update_by_code` varchar(64) DEFAULT NULL COMMENT '修改者code',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：-1 删除，0 禁用，1 启用',
  PRIMARY KEY (`id`),
  KEY `idx_app_id` (`app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='应用token表';


-- 导出  表 laf_config.application_user 结构
CREATE TABLE IF NOT EXISTS `application_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `app_id` bigint(20) NOT NULL COMMENT '应用id',
  `app_code` varchar(128) NOT NULL COMMENT '应用code',
  `user_id` bigint(20) NOT NULL COMMENT '用户id',
  `user_code` varchar(64) NOT NULL COMMENT '用户code',
  `update_by` bigint(20) DEFAULT '0' COMMENT '修改人',
  `update_by_code` varchar(64) DEFAULT 'auto' COMMENT '修改人',
  `update_time` datetime NOT NULL COMMENT '记录更新时间',
  `create_by` bigint(20) DEFAULT '0' COMMENT '创建人',
  `create_by_code` varchar(64) DEFAULT 'auto' COMMENT '创建人code',
  `create_time` datetime NOT NULL COMMENT '记录创建时间',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：-1 删除，0 禁用，1 启用',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='应用用户关联表';

-- 导出  表 laf_config.user 结构
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `code` varchar(64) NOT NULL COMMENT '用户erp',
  `name` varchar(64) NOT NULL COMMENT '用户中文名',
  `org_id` varchar(20) DEFAULT NULL COMMENT '组织id',
  `org_name` varchar(128) DEFAULT NULL COMMENT '组织名',
  `email` varchar(50) DEFAULT NULL COMMENT '邮箱',
  `mobile` varchar(11) DEFAULT NULL COMMENT '手机号码',
  `role` tinyint(4) NOT NULL DEFAULT '0' COMMENT '权限：0 普通用户，1 管理员',
  `sign` int(11) NOT NULL DEFAULT '0' COMMENT '签名',
  `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint(20) DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：-1 删除，0 禁用，1 启用',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_code` (`code`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户表';

-- 导出  表 config 结构
CREATE TABLE IF NOT EXISTS `config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `name` varchar(128) NOT NULL COMMENT '名称',
  `group` varchar(128) NOT NULL COMMENT '类别',
  `key` varchar(128) NOT NULL COMMENT 'key',
  `value` varchar(1500) DEFAULT NULL COMMENT 'value',
  `password` tinyint(4) DEFAULT NULL COMMENT '是否是密码',
  `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint(20) DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：-1 删除，0 禁用，1 启用',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_key` (`key`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='配置表';


CREATE TABLE IF NOT EXISTS `namespace` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `code` varchar(128) NOT NULL COMMENT '代码',
  `name` varchar(64) NOT NULL COMMENT '名称',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_by` bigint(20) NOT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  `update_by` bigint(20) NOT NULL COMMENT '修改人',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：-1 删除，0 禁用，1 启用',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_code` (`code`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='主题表';


CREATE TABLE IF NOT EXISTS `topic` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `code` varchar(128) NOT NULL COMMENT '代码',
  `name` varchar(64) NOT NULL COMMENT '名称',
  `namespace_id` bigint(20) NOT NULL COMMENT 'namespace.id',
  `namespace_code` varchar(64) NOT NULL COMMENT 'namespace.code',
  `partitions` smallint(6) DEFAULT NULL COMMENT 'partitions',
  `archive` tinyint(4) DEFAULT '1' COMMENT '是否归档',
  `description` varchar(1024) DEFAULT NULL COMMENT '描述',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_by` bigint(20) NOT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  `update_by` bigint(20) NOT NULL COMMENT '修改人',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：-1 删除，0 禁用，1 启用',
  `type` tinyint(4) DEFAULT '0' COMMENT '类型：0:topic,1:queue（2:broadcast）',
  `labels` varchar(1024) DEFAULT NULL COMMENT '标签',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='主题表';

CREATE TABLE IF NOT EXISTS `broker_group` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `code` varchar(128) DEFAULT NULL COMMENT '代码',
  `name` varchar(64) DEFAULT NULL COMMENT '名称',
  `description` varchar(512) DEFAULT NULL COMMENT '描述',
  `labels` varchar(1024) DEFAULT NULL COMMENT '标签',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_by` bigint(20) NOT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  `update_by` bigint(20) NOT NULL COMMENT '修改人',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：-1 删除，0 禁用，1 启用',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_code` (`code`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Broker分组表';


CREATE TABLE IF NOT EXISTS `topic_partition_group` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `namespace_id` bigint(20) NOT NULL COMMENT 'namespace.id',
  `namespace_code` varchar(128) NOT NULL COMMENT 'namespace.code',
  `topic_id` bigint(20) NOT NULL COMMENT '主题ID',
  `topic_code` varchar(128) NOT NULL COMMENT '主题代码',
  `group_no` int(10) NOT NULL COMMENT '分片组序号',
  `partitions` varchar(512) NOT NULL COMMENT '分片集合，以逗号隔开',
  `elect_type` tinyint(4) DEFAULT 0 COMMENT '选举类型，0：raft，1：fix',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_by` bigint(20) NOT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  `update_by` bigint(20) NOT NULL COMMENT '修改人',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：-1 删除，0 禁用，1 启用',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='主题-分片组关联表';


CREATE TABLE IF NOT EXISTS `partition_group_replica` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `namespace_id` bigint(20) NOT NULL COMMENT 'namespace',
  `namespace_code` varchar(128) NOT NULL COMMENT 'namespace',
  `topic_id` bigint(20) NOT NULL COMMENT '主题ID',
  `topic_code` varchar(128) NOT NULL COMMENT '主题代码',
  `group_no` int(10) NOT NULL COMMENT '分片组序号',
  `broker_id` int(10) NOT NULL COMMENT 'Broker.id',
  `role` tinyint(4) DEFAULT 0 COMMENT '0:dynamic,1:master,2:slave,3:learner',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_by` bigint(20) NOT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  `update_by` bigint(20) NOT NULL COMMENT '修改人',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：-1 删除，0 禁用，1 启用',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_topic_group_ns_broker` (`topic_id`, `group_no`, `namespace_id`, `broker_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='分片组-Broker关联表';

CREATE TABLE `hosts` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `datacenter_id` bigint(20) DEFAULT NULL COMMENT '数据中心id',
  `datacenter_code` varchar(512) DEFAULT NULL COMMENT '数据中心code',
  `ip` varchar(20) NOT NULL COMMENT 'IP',
  `rack` varchar(10) DEFAULT NULL COMMENT '机架',
  `vendor` varchar(20) DEFAULT NULL COMMENT '品牌',
  `cpu_capacity` varchar(50) DEFAULT NULL COMMENT 'cpu容量',
  `disk_capacity` varchar(50) DEFAULT NULL COMMENT '磁盘容量',
  `mem_capacity` varchar(50) DEFAULT NULL COMMENT '内存容量',
  `switch_board` varchar(50) DEFAULT NULL COMMENT '交换机',
  `labels` varchar(500) DEFAULT NULL COMMENT '标签',
  `server_type` varchar(20) DEFAULT NULL COMMENT '服务器类型',
  `disk_type` varchar(20) DEFAULT NULL COMMENT '磁盘类型',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_by` bigint(20) NOT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  `update_by` bigint(20) NOT NULL COMMENT '修改人',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态: -1 删除，0 禁用，1 启用但未生成Broker，2 启用且已生成Broker ',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='主机表';


CREATE TABLE `broker` (
  `id` bigint(20) unsigned NOT NULL COMMENT '主键id， 同步获取',
  `datacenter_id` bigint(20) DEFAULT NULL COMMENT '数据中心id',
  `datacenter_code` varchar(512) DEFAULT NULL COMMENT '数据中心code',
  `host_id` bigint(20) NOT NULL COMMENT 'hosts.id',
  `ip` varchar(20) NOT NULL COMMENT 'IP',
  `port` int(11) NOT NULL DEFAULT '50088' COMMENT '端口号',
  `group_id` bigint(20) DEFAULT NULL COMMENT 'Broker分组id',
  `group_code` varchar(128) DEFAULT NULL COMMENT 'Broker分组code',
  `retry_type` varchar(50) NOT NULL COMMENT '重试类型',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_by` bigint(20) NOT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  `update_by` bigint(20) NOT NULL COMMENT '修改人',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态: -1 删除，0 禁用，1 启用',
  PRIMARY KEY (`id`),
  KEY `idx_address` (`ip`,`port`) USING BTREE,
  KEY `idx_name` (`host_id`,`status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='broker';

CREATE TABLE `consumer` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `namespace_id` bigint(20) NOT NULL COMMENT 'namespace',
  `namespace_code` varchar(64) NOT NULL COMMENT 'namespace',
  `topic_id` bigint(20) NOT NULL COMMENT '主题ID',
  `topic_code` varchar(128) NOT NULL COMMENT 'topic 代码',
  `app_id` bigint(20) NOT NULL COMMENT '应用 id',
  `app_code` varchar(128) NOT NULL COMMENT '应用 代码',
  `subscribe_group` varchar(64) NOT NULL COMMENT '订阅分组',
  `client_type` tinyint(4) DEFAULT NULL COMMENT '客户端类型, 0:jmq, 1:kafka, 2:mqtt, 10:others',
  `create_by` bigint(11) NOT NULL DEFAULT '0' COMMENT '创建人',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `update_by` bigint(11) NOT NULL DEFAULT '0' COMMENT '修改人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `status` tinyint(3) NOT NULL COMMENT '状态: -1 删除，0 禁用，1 启用',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='消费表';


CREATE TABLE `consumer_config` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `consumer_id` bigint(20) NOT NULL COMMENT '消费者ID',
  `near_by` tinyint(1) NOT NULL DEFAULT '0' COMMENT '就近消费',
  `paused` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否暂停消费',
  `archive` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否归档',
  `retry` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用重试服务',
  `ack_timeout` int(11) NOT NULL DEFAULT '0' COMMENT '应答超时时间',
  `batch_size` int(11) NOT NULL DEFAULT '0' COMMENT '批量大小',
  `max_retrys` int(11) NOT NULL DEFAULT '0' COMMENT '最大重试次数',
  `max_retry_delay` int(11) NOT NULL DEFAULT '0' COMMENT '最大重试间隔',
  `retry_delay` int(11) NOT NULL DEFAULT '0' COMMENT '重试间隔',
  `use_exponential_backoff` tinyint(1) NOT NULL DEFAULT '0' COMMENT '指数增加重试间隔时间',
  `backoff_multiplier` double NOT NULL DEFAULT '0' COMMENT '重试指数系数',
  `expire_time` int(11) NOT NULL DEFAULT '0' COMMENT '重试消息过期时间',
  `delay` int(10) DEFAULT '0' COMMENT '延迟消费时间',
  `concurrent` tinyint(1) DEFAULT '0' COMMENT '并行消费',
  `create_by` bigint(11) NOT NULL DEFAULT '0' COMMENT '创建人',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `update_by` bigint(11) NOT NULL DEFAULT '0' COMMENT '修改人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `status` tinyint(3) NOT NULL COMMENT '状态',
  `prefetch_size` smallint(6) NOT NULL DEFAULT '0' COMMENT '预取消息条数',
  `black_list` varchar(1000) DEFAULT NULL COMMENT '消费黑名单',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_consumer` (`consumer_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='消费配置表';

CREATE TABLE `producer` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `namespace_id` bigint(20) NOT NULL COMMENT 'namespace',
  `namespace_code` varchar(128) NOT NULL COMMENT 'namespace',
  `topic_id` bigint(20) NOT NULL COMMENT '主题ID',
  `topic_code` varchar(128) NOT NULL COMMENT 'topic 代码',
  `subscribe_group` varchar(64) NOT NULL COMMENT '订阅分组',
  `app_id` bigint(20) NOT NULL COMMENT '应用 id',
  `app_code` varchar(128) NOT NULL COMMENT '应用 代码',
  `client_type` tinyint(4) DEFAULT NULL COMMENT '客户端类型',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_by` bigint(11) NOT NULL DEFAULT '0' COMMENT '创建人',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  `update_by` bigint(11) NOT NULL DEFAULT '0' COMMENT '修改人',
  `status` tinyint(3) NOT NULL COMMENT '状态',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='生产者表';


CREATE TABLE `producer_config` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `producer_id` bigint(20) NOT NULL COMMENT '生产者 ID',
  `near_by` tinyint(1) NOT NULL DEFAULT '0' COMMENT '就近发送',
  `weight` varchar(1000) DEFAULT NULL COMMENT '集群实例发送权重',
  `single` tinyint(1) DEFAULT NULL COMMENT '单发送者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_by` bigint(11) NOT NULL DEFAULT '0' COMMENT '创建人',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  `update_by` bigint(11) NOT NULL DEFAULT '0' COMMENT '修改人',
  `status` tinyint(3) NOT NULL COMMENT '状态',
  `black_list` varchar(1000) DEFAULT NULL COMMENT '生产黑名单',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_producer` (`producer_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='生产配置表';


CREATE TABLE `task` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `type` varchar(50) NOT NULL COMMENT '任务目标',
  `dispatch_type` int(11) NOT NULL COMMENT '派发类型',
  `priority` int(11) DEFAULT NULL COMMENT '优先级',
  `owner` varchar(50) DEFAULT NULL COMMENT '执行器',
  `url` varchar(200) DEFAULT NULL COMMENT '参数',
  `cron` varchar(200) DEFAULT NULL COMMENT '正则',
  `refer_id` bigint(20) unsigned DEFAULT NULL COMMENT '关联ID',
  `mutex` varchar(50) DEFAULT NULL COMMENT '锁',
  `daemons` tinyint(4) DEFAULT NULL COMMENT '是否守护',
  `retry` tinyint(4) DEFAULT NULL COMMENT '是否重试',
  `retry_count` int(11) DEFAULT NULL COMMENT '重试次数',
  `max_retry_count` int(11) DEFAULT NULL COMMENT '最大重试次数',
  `retry_time` datetime DEFAULT NULL COMMENT '下次重试时间',
  `exception` varchar(2048) DEFAULT NULL COMMENT '异常信息',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_by` bigint(20) NOT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `update_by` bigint(20) NOT NULL COMMENT '更新人',
  `status` tinyint(4) NOT NULL COMMENT '状态',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='任务表';

CREATE TABLE `apply` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `type` varchar(255) NOT NULL COMMENT '申请类型',
  `topic_id` bigint(20) DEFAULT NULL COMMENT '主题ID',
  `topic_code` varchar(128) DEFAULT NULL COMMENT 'topic 代码',
  `app_id` bigint(20) DEFAULT NULL COMMENT '应用 id',
  `app_code` varchar(128) DEFAULT NULL COMMENT '应用 代码',
  `payload` varchar(1024) DEFAULT NULL COMMENT '额外的信息，根据不同的申请类型不同JSON内容',
  `description` varchar(512) DEFAULT NULL COMMENT '描述',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_by` bigint(20) NOT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  `update_by` bigint(20) NOT NULL COMMENT '修改人',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：-1 删除，0 已取消，1 申请中，2 已完成',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='申请表';

CREATE TABLE `audit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `apply_id` bigint(20) NOT NULL COMMENT '申请id',
  `role_group` tinyint(4) NOT NULL DEFAULT '0' COMMENT '审批人审批次序',
  `role_code` varchar(64) NOT NULL COMMENT '审批人角色',
  user_id  bigint(20) NOT NULL COMMENT '审批人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_by` bigint(20) NOT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  `update_by` bigint(20) NOT NULL COMMENT '修改人',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：-1 删除，0 新建，1 待审批，2通过，3拒绝',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='审批表';
#2018-11-29 begin#
alter table consumer drop column client_type;
alter table producer drop column client_type;
alter table `application` drop index  `uniq_code`;
alter table `application_token` drop index  `uniq_app_token`;
alter table `application_user` drop index  `uniq_app_user_id`;
alter table `topic` drop index  `uniq_code`;
alter table `topic_partition_group` drop index  `uniq_topic_group_ns`;
alter table `partition_group_replica` drop index  `uniq_topic_group_ns_broker`;
alter table `consumer` drop index  `idx_topic_app`;
alter table `producer` drop index  `idx_topic_app`;

alter table producer add column   client_type tinyint(4) DEFAULT NULL COMMENT '客户端类型';
alter table consumer add column   client_type tinyint(4) DEFAULT NULL COMMENT '客户端类型';
#2018-11-29 end#

CREATE TABLE `oper_log` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `type` int(11) NOT NULL COMMENT '类型',
  `identity` bigint(20) NOT NULL COMMENT '操作资源ID',
  `oper_type` int(11) NOT NULL COMMENT '操作类型',
  `target` varchar(1500) DEFAULT NULL COMMENT '目标',
  `result` varchar(1024) DEFAULT NULL COMMENT '操作结果，成功或异常信息',
  `description` varchar(512) DEFAULT NULL COMMENT '操作描述，url或其他',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_by` bigint(20) NOT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `update_by` bigint(20) NOT NULL COMMENT '更新人',
  `status` tinyint(4) NOT NULL COMMENT '状态',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';

#修改audit字段 2018-12-5
alter table `audit` change `role_group` `group_no` tinyint(4) NOT NULL DEFAULT '0' COMMENT '审批人审批次序';
alter table `audit` change `role_code` `audit_role` varchar(64) NOT NULL COMMENT '审批人角色';
#修改apply字段 2018-12-6
alter table apply modify `topic_id` bigint(20) DEFAULT NULL COMMENT '主题ID';
alter table apply modify `topic_code` varchar(128) DEFAULT NULL COMMENT '主题代码';
alter table apply modify `app_id` bigint(20) DEFAULT NULL COMMENT '应用ID';
alter table apply modify `app_code` varchar(128) DEFAULT NULL COMMENT '应用代码';
#修改application字段 2018-12-6
alter table application modify `system` varchar(128) DEFAULT NULL COMMENT '所属系统';
#添加apply,audit字段 2018-12-7
alter table apply add `flow_id` bigint(20) NOT NULL COMMENT '审批流程配置ID';
alter table audit add `suggestion` varchar(2048) DEFAULT NULL COMMENT '审批意见';
#修改audit字段 2018-12-10
alter table audit change `group_no` `node_no` tinyint(4) NOT NULL DEFAULT '0' COMMENT '审批流节点序号';
-- alter table `config` drop index  `uniq_key`;

CREATE TABLE `audit_flow` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `type` varchar(255) NOT NULL COMMENT '申请类型',
  `applicant_confirm` tinyint(4) DEFAULT 0 COMMENT '是否需要申请人确认',
  `manual_execute` tinyint(4) DEFAULT 0 COMMENT '是否需要申请人手动执行',
  `execute_class` varchar(512) DEFAULT NULL COMMENT '手动执行类',
  `execute_method` varchar(128) DEFAULT NULL COMMENT '手动执行方法',
  `description` varchar(512) DEFAULT NULL COMMENT '描述',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_by` bigint(20) NOT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  `update_by` bigint(20) NOT NULL COMMENT '修改人',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：-1 删除，0 禁用，1 启用',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='审批流';

CREATE TABLE `audit_node` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `flow_id` bigint(20) NOT NULL COMMENT '审批流ID',
  `node_no` tinyint(4) NOT NULL DEFAULT '0' COMMENT '审批节点序号',
  `audit_roles` varchar(256) NOT NULL COMMENT '审批角色ID数组集合',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_by` bigint(20) NOT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  `update_by` bigint(20) NOT NULL COMMENT '修改人',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：-1 删除，0 禁用，1 启用',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='审批节点';

CREATE TABLE `audit_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `code` varchar(128) NOT NULL COMMENT 'code',
  `name` varchar(128) NOT NULL COMMENT '名称',
  `sql` varchar(2048) DEFAULT NULL COMMENT '审批人查询SQL条件',
  `user_codes` varchar(1024) DEFAULT NULL COMMENT '审批人ERP数组集合',
  `description` varchar(512) DEFAULT NULL COMMENT '描述',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_by` bigint(20) NOT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  `update_by` bigint(20) NOT NULL COMMENT '修改人',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：-1 删除，0 禁用，1 启用',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='审批角色';

#唯一索引存在bug,相同code只能被删除一次
ALTER TABLE `broker_group` DROP INDEX `uniq_code`;
#apply添加namespace字段 2018-12-25
alter table apply add `namespace_id` bigint(20) DEFAULT NULL COMMENT '命名空间ID',
  add `namespace_code` varchar(128) DEFAULT NULL COMMENT '命名空间代码';

# 添加executor表，且nsr接口上线后有些更新脚本 20190118
CREATE TABLE `executor` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(64) NOT NULL COMMENT '服务执行器名称',
  `type` tinyint(4) NOT NULL COMMENT '服务执行器类型',
  `ip` varchar(20) NOT NULL COMMENT '服务执行器IP',
  `jmx_port` int(11) NOT NULL DEFAULT '7654' COMMENT '服务执行器端口',
  `description` varchar(512) DEFAULT NULL COMMENT '描述',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_by` bigint(20) NOT NULL DEFAULT '0' COMMENT '创建人',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  `update_by` bigint(20) NOT NULL DEFAULT '0' COMMENT '修改人',
  `status` int(4) NOT NULL DEFAULT '1' COMMENT '状态(1启用，0停用，-1删除)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '执行器';

#2019.1.10 nsr接口版本上线时,记得将apply等表中涉及topic_id,namespace_id字段改为varchar
alter table apply modify `topic_id` varchar(128) DEFAULT NULL COMMENT 'topicId';
alter table apply modify `namespace_id` varchar(128) DEFAULT NULL COMMENT 'namespaceId';
alter table hosts modify `datacenter_id` varchar(128) DEFAULT NULL COMMENT 'datacenterId';

#broker 关联表
CREATE TABLE `broker_group_related` (
  `id` bigint(20) unsigned NOT NULL COMMENT 'brokerId',
  `group_id` bigint(20) DEFAULT NULL COMMENT 'Broker分组id',
  `group_code` varchar(128) DEFAULT NULL COMMENT 'Broker分组code',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_by` bigint(20) NOT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  `update_by` bigint(20) NOT NULL COMMENT '修改人',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态: -1 删除，0 禁用，1 启用',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='broker';

############################未执行#######################################
# chart, chart series, metric and chart set table 2018-12-27
# CREATE TABLE `chart` (
#   `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '图表id',
#   `chart_set_id` bigint(20) DEFAULT NULL COMMENT '图集id',
#   `chart_set_code` varchar(128) DEFAULT NULL COMMENT '图集编码',
#   `code` varchar(128) DEFAULT NULL COMMENT '图编码',
#   `title` varchar(64) DEFAULT NULL COMMENT '标题',
#   `xaxis_title` varchar(64) DEFAULT NULL COMMENT 'x轴title',
#   `xaxis_label` varchar(64) DEFAULT NULL COMMENT 'x轴label',
#   `yaxis_title` varchar(64) DEFAULT NULL COMMENT 'y轴title',
#   `yaxis_label` varchar(64) DEFAULT NULL COMMENT 'y轴label',
#   `type` varchar(64) DEFAULT NULL COMMENT '图表类型',
#   `create_time` datetime NOT NULL COMMENT '创建时间',
#   `create_by` bigint(20) NOT NULL COMMENT '创建人',
#   `update_time` datetime NOT NULL COMMENT '修改时间',
#   `update_by` bigint(20) NOT NULL COMMENT '修改人',
#   `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：-1 删除，0 禁用，1 启用',
#   PRIMARY KEY (`id`)
# ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='图管理';
#
# CREATE TABLE `chart_series` (
#   `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '序列id',
#   `chart_id` bigint(20) NOT NULL COMMENT '图表ID',
#   `chart_code` varchar(128) DEFAULT NULL COMMENT '图表代码',
#   `name` varchar(64) NOT NULL COMMENT '名称',
#   `type` tinyint(4)  NOT NULL COMMENT '类型',
#   `metric_code` varchar(128) NOT NULL COMMENT '指标代码',
#   `ratio` double NOT NULL DEFAULT '1' COMMENT '值转换比率',
#   `create_time` datetime NOT NULL COMMENT '创建时间',
#   `create_by` bigint(20) NOT NULL COMMENT '创建人',
#   `update_time` datetime NOT NULL COMMENT '修改时间',
#   `update_by` bigint(20) NOT NULL COMMENT '修改人',
#   `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：-1 删除，0 禁用，1 启用',
#   PRIMARY KEY (`id`)
# ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='图序列管理';
#
# CREATE TABLE `chart_set` (
#   `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '图集id',
#   `code` varchar(128) NOT NULL COMMENT '图集编码',
#   `name` varchar(64) NOT NULL COMMENT '图集名称',
#   `create_time` datetime NOT NULL COMMENT '创建时间',
#   `create_by` bigint(20) NOT NULL COMMENT '创建人',
#   `update_time` datetime NOT NULL COMMENT '修改时间',
#   `update_by` bigint(20) NOT NULL COMMENT '修改人',
#   `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：-1 删除，0 禁用，1 启用',
#   PRIMARY KEY (`id`)
# ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='图集管理';

CREATE TABLE `metric` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `code` varchar(128) NOT NULL COMMENT '代码',
  `alias_code` varchar(256) NOT NULL COMMENT '值,唯一',
  `name` varchar(64) NOT NULL COMMENT '名称',
  `type` tinyint(4) NOT NULL COMMENT '类型：0 atomic, 1 aggregator, 10 others(mdc)',
--   `charts` varchar(64) DEFAULT NULL COMMENT '监控图表数组：0 其他，1 生产详情，2 消费详情，3 生产汇总，4 消费汇总，5 主机监控，6 broker监控',
  `source` varchar(128) DEFAULT NULL COMMENT '来源指标code',
  `provider` varchar(128) DEFAULT NULL COMMENT '指标提供方',
  `description` varchar(1000) DEFAULT NULL COMMENT '聚合描述',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_by` bigint(20) NOT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  `update_by` bigint(20) NOT NULL COMMENT '修改人',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：-1 删除，0 禁用，1 启用',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='指标';


########################### JD ####################################
CREATE TABLE `alarm_rule_template` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `alarm_template_id` bigint(20) NOT NULL COMMENT '通知内容模板，可以是ID，可以是内容',
  `notice_producer` tinyint(4) DEFAULT '0' COMMENT '是否通知生产者，0 否，1 是',
  `metric_code` varchar(128) NOT NULL COMMENT '代码',
  `alarm_level` tinyint(4) DEFAULT '0' COMMENT '报警级别,0 普通，1严重，2 非常严重',
  `alarm_users` varchar(64) NOT NULL COMMENT '报警用户类型集合, 0 管理员，1 用户',
  `alarm_ways` varchar(64) NOT NULL COMMENT '报警方式集合，0 短信，1 邮件，2 咚咚，3 语音',
  `topic` varchar(128) DEFAULT NULL COMMENT 'topic code',
  `app` varchar(128) DEFAULT NULL COMMENT 'app code',
  `hosts` varchar(64) DEFAULT NULL COMMENT '主机ip',
  `broker` varchar(64) DEFAULT NULL COMMENT 'Broker id',
  `threshold` double DEFAULT NULL COMMENT '报警阀值',
  `threshold_count` smallint(6) DEFAULT NULL COMMENT '报警检测次数',
  `trend_value` double DEFAULT NULL COMMENT '报警趋势值',
  `trend_type` tinyint(4) DEFAULT NULL COMMENT '报警趋势值类型',
  `detect_duration` smallint(6) DEFAULT '1' COMMENT '报警检测持续时间，默认单位分钟',
  `detect_period` smallint(6) DEFAULT '1' COMMENT '报警检测间隔，默认单位分钟',
  `alarm_interval` int(11) DEFAULT NULL COMMENT '报警时间间隔,默认单位分钟',
  `effective_time` varchar(64) DEFAULT NULL COMMENT '每日生效时间',
  `expiration_time` varchar(64) DEFAULT NULL COMMENT '每日失效时间',
  #   `exclude_type` tinyint(4) DEFAULT NULL COMMENT '排斥类型,0 topic，1 app，2 hosts，3 broker',
  #   `alarm_exclude` varchar(10000) DEFAULT NULL COMMENT '排斥值',
  `create_by` bigint(20) DEFAULT NULL COMMENT '创建者id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint(20) DEFAULT NULL COMMENT '修改者id',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：-1 删除，0 禁用，1 启用',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='默认报警规则表';


# CREATE TABLE `alarm_type` (
#   `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键自增id',
#   `code` varchar(128) DEFAULT NULL COMMENT '代码',
#   `name` varchar(64) DEFAULT NULL COMMENT '名称',
#   `metric` varchar(50) DEFAULT NULL COMMENT '指标',
#   `user_type` varchar(20) DEFAULT NULL COMMENT '用户类型',
#   `endpoint` varchar(20) DEFAULT NULL COMMENT '接入方',
#   `suggestion` varchar(2000) DEFAULT NULL COMMENT '建议',
#   `url` varchar(500) DEFAULT NULL COMMENT '链接',
#   `description` varchar(2000) DEFAULT NULL COMMENT '描述',
#   `create_by` bigint(20) NOT NULL COMMENT '创建者id',
#   `create_time` datetime NOT NULL COMMENT '创建时间',
#   `update_by` bigint(20) NOT NULL DEFAULT '0' COMMENT '修改人',
#   `update_time` datetime NOT NULL COMMENT '修改时间',
#   `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：-1 删除，0 禁用，1 启用',
#   PRIMARY KEY (`id`)
# ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='报警类型表';

# end

######################归档表#####################################
CREATE TABLE IF NOT EXISTS `archive_task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键自增id',
  `type` tinyint(4) NOT NULL DEFAULT '0' COMMENT '来源：1.导出 2:重试 ',
  `params` varchar(128) NOT NULL COMMENT '条件',
  `topic` varchar(128) NOT NULL COMMENT 'topic',
  `send_time` datetime COMMENT '发送时间',
  `total` bigint(20) COMMENT '总条数',
  `count` bigint(20) COMMENT '处理条数',
  `url` varchar(128) COMMENT 'url',
  `create_by` bigint(20) NOT NULL COMMENT '创建者id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint(20) NOT NULL DEFAULT '0' COMMENT '修改人',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：-1 删除，0 禁用，1 启用',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='归档任务表';




##################################运行稳定后再删除################################################
#delete表
drop table partition_group_replica;
drop table topic_partition_group;
drop table topic;
drop table producer_config;
drop table producer;
drop table partition_group_replica;
drop table consumer_config;
drop table consumer;
drop table config;
drop table namespace;
drop table broker;
drop table application_token;

#########

CREATE TABLE `user_token` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `code` varchar(64) NOT NULL COMMENT '用户erp',
  `token` varchar(64) DEFAULT '' COMMENT '用户中文名',
  `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint(20) DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：-1 删除，0 禁用，1 启用',
  `expire_time` datetime DEFAULT NULL COMMENT 'token 过期时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_code` (`code`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8 COMMENT='用户token表';




