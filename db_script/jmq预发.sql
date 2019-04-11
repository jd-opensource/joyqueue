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

#laf_jmq 预发最新脚本
#alarm_event
#alarm_policy
#alarm_type

CREATE TABLE `application` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键自增id',
  `code` varchar(128) NOT NULL COMMENT '应用 代码',
  `name` varchar(64) NOT NULL COMMENT '应用 名称',
  `system` varchar(128) DEFAULT NULL COMMENT '所属系统',
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
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_code` (`code`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='应用';

CREATE TABLE `application_user` (
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
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_app_user_id` (`app_id`,`user_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='应用用户关联表';

CREATE TABLE `apply` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `type` varchar(255) NOT NULL DEFAULT '' COMMENT '申请类型',
  `topic_id` varchar(128) NOT NULL COMMENT '主题ID',
  `topic_code` varchar(128) DEFAULT NULL COMMENT '主题代码',
  `app_id` bigint(20) DEFAULT NULL COMMENT '应用ID',
  `app_code` varchar(128) DEFAULT NULL COMMENT '应用代码',
  `payload` varchar(1024) DEFAULT NULL COMMENT '额外的信息，根据不同的申请类型不同JSON内容',
  `description` varchar(512) DEFAULT NULL COMMENT '描述',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_by` bigint(20) NOT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  `update_by` bigint(20) NOT NULL COMMENT '修改人',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：-1 删除，0 已取消，1 申请中，2 已完成',
  `flow_id` bigint(20) NOT NULL COMMENT '审批流程配置ID',
  `namespace_id` varchar(128) DEFAULT NULL COMMENT '命名空间ID',
  `namespace_code` varchar(128) DEFAULT NULL COMMENT '命名空间代码',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='申请表';

CREATE TABLE `audit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `apply_id` bigint(20) NOT NULL COMMENT '申请id',
  `node_no` tinyint(4) NOT NULL DEFAULT '0' COMMENT '审批流节点序号',
  `audit_role` varchar(64) NOT NULL COMMENT '审批人角色',
  `user_id` bigint(20) NOT NULL COMMENT '审批人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_by` bigint(20) NOT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  `update_by` bigint(20) NOT NULL COMMENT '修改人',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：-1 删除，0 新建，1 待审批，2通过，3拒绝',
  `suggestion` varchar(2048) DEFAULT NULL COMMENT '审批意见',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='审批表';

CREATE TABLE `audit_flow` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `type` varchar(255) NOT NULL COMMENT '申请类型',
  `applicant_confirm` tinyint(4) DEFAULT '0' COMMENT '是否需要申请人确认',
  `manual_execute` tinyint(4) DEFAULT '0' COMMENT '是否需要申请人手动执行',
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
  `audit_roles` varchar(1024) NOT NULL COMMENT '审批角色ID，多个以逗号隔开',
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


CREATE TABLE `broker_group` (
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
  KEY `idx_code_status` (`code`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Broker分组表';

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

#chart
#chart_series
#chart_set
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='执行器';

CREATE TABLE `hosts` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `datacenter_id` varchar(512) DEFAULT NULL COMMENT '数据中心id',
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

#metric
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

CREATE TABLE `partition_group_replica` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `namespace_id` bigint(20) NOT NULL COMMENT 'namespace',
  `namespace_code` varchar(128) NOT NULL COMMENT 'namespace',
  `topic_id` bigint(20) NOT NULL COMMENT '主题ID',
  `topic_code` varchar(128) NOT NULL COMMENT '主题代码',
  `group_no` int(10) NOT NULL COMMENT '分片组序号',
  `broker_id` int(10) NOT NULL COMMENT 'Broker.id',
  `role` tinyint(4) DEFAULT '0' COMMENT '0:dynamic,1:master,2:slave,3:learner',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_by` bigint(20) NOT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  `update_by` bigint(20) NOT NULL COMMENT '修改人',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：-1 删除，0 禁用，1 启用',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='分片组-Broker关联表';


CREATE TABLE `task` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `type` varchar(64) NOT NULL COMMENT '任务目标',
  `dispatch_type` int(11) NOT NULL COMMENT '派发类型',
  `priority` int(11) DEFAULT NULL COMMENT '优先级',
  `owner` varchar(64) DEFAULT NULL COMMENT '执行器',
  `url` varchar(200) DEFAULT NULL COMMENT '参数',
  `cron` varchar(200) DEFAULT NULL COMMENT '正则',
  `refer_id` bigint(20) unsigned DEFAULT NULL COMMENT '关联ID',
  `mutex` varchar(64) DEFAULT NULL COMMENT '锁',
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

CREATE TABLE `user` (
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
  UNIQUE KEY `uniq_code` (`code`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户表';

#默认管理员
update user set role = 1 where code = 'chenyanying3' or id = 1
#重试表
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
KEY `idx_topic_app` (`topic`,`app`,`status`,`retry_time`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8 COMMENT='消息重试表'


alter table apply modify `topic_id` varchar(128) DEFAULT NULL COMMENT 'topicId';
