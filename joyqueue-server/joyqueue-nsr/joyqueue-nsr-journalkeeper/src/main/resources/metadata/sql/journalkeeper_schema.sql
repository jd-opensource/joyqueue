CREATE TABLE IF NOT EXISTS `topic` (
	`id` varchar(255) NOT NULL,
	`code` varchar(255),
	`namespace` varchar(255),
	`partitions` int(11),
	`priority_partitions` varchar(255),
	`type` tinyint(1),
	PRIMARY KEY (`id`)
);
CREATE INDEX IF NOT EXISTS idx_code_namespace ON topic(`code`, `namespace`);
ALTER TABLE `topic` ADD COLUMN IF NOT EXISTS `policy` varchar(1024) AFTER `type`;

CREATE TABLE IF NOT EXISTS `partition_group` (
	`id` varchar(255) NOT NULL,
	`namespace` varchar(255),
	`topic` varchar(255),
	`group` int(11),
	`leader` int(11),
	`isrs` varchar(255),
	`term` int(11),
	`partitions` varchar(1024),
	`learners` varchar(1024),
	`replicas` varchar(1024),
	`out_sync_replicas` varchar(1024),
	`elect_Type` tinyint(1),
	`rec_leader` int(11),
	PRIMARY KEY (`id`)
);
CREATE INDEX IF NOT EXISTS idx_topic_namespace ON partition_group(`topic`, `namespace`);

CREATE TABLE IF NOT EXISTS `broker` (
	`id` bigint(11) NOT NULL,
	`ip` varchar(255),
	`port` int(11),
	`data_center` varchar(255),
	`retry_type` varchar(255),
	`permission` varchar(255),
	PRIMARY KEY (`id`)
);
CREATE INDEX IF NOT EXISTS idx_ip_port ON broker(`ip`, `port`);
CREATE INDEX IF NOT EXISTS idx_retry_type ON broker(`retry_type`);
CREATE INDEX IF NOT EXISTS idx_data_center ON broker(`data_center`);

CREATE TABLE IF NOT EXISTS `app_token` (
	`id` varchar(255) NOT NULL,
	`app` varchar(255),
	`token` varchar(255),
	`effective_time` datetime,
	`expiration_time` datetime,
	PRIMARY KEY (`id`)
);
CREATE INDEX IF NOT EXISTS idx_app_token ON app_token(`app`, `token`);
CREATE INDEX IF NOT EXISTS idx_token ON app_token(`token`);

CREATE TABLE IF NOT EXISTS `consumer` (
	`id` varchar(255) NOT NULL,
	`namespace` varchar(255),
	`topic` varchar(255),
	`app` varchar(255),
	`topic_type` tinyint(1),
	`client_type` tinyint(1),
	`group` varchar(255),
	`referer` varchar(255),
	`consume_policy` varchar(1024),
	`retry_policy` varchar(1024),
	`limit_policy` varchar(1024),
	PRIMARY KEY (`id`)
);
CREATE INDEX IF NOT EXISTS idx_topic_namespace_app ON consumer(`topic`, `namespace`, `app`);
CREATE INDEX IF NOT EXISTS idx_referer ON consumer(`referer`);
CREATE INDEX IF NOT EXISTS idx_app ON consumer(`app`);

CREATE TABLE IF NOT EXISTS `producer` (
	`id` varchar(255) NOT NULL,
	`namespace` varchar(255),
	`topic` varchar(255),
	`app` varchar(255),
	`client_type` tinyint(1),
	`produce_policy` varchar(1024),
	`limit_policy` varchar(1024),
	PRIMARY KEY (`id`)
);
CREATE INDEX IF NOT EXISTS idx_topic_namespace_app ON producer(`topic`, `namespace`, `app`);
CREATE INDEX IF NOT EXISTS idx_app ON producer(`app`);

CREATE TABLE IF NOT EXISTS `namespace` (
	`id` varchar(255) NOT NULL,
	`code` varchar(255),
	`name` varchar(255),
	PRIMARY KEY (`id`)
);
CREATE INDEX IF NOT EXISTS idx_code ON namespace(`code`);

CREATE TABLE IF NOT EXISTS `datacenter` (
	`id` varchar(255) NOT NULL,
	`region` varchar(255),
	`code` varchar(255),
	`name` varchar(255),
	`url` varchar(255),
	PRIMARY KEY (`id`)
);
CREATE INDEX IF NOT EXISTS idx_code ON datacenter(`code`);

CREATE TABLE IF NOT EXISTS `partition_group_replica` (
	`id` varchar(255) NOT NULL,
	`topic` varchar(255),
	`namespace` varchar(255),
	`broker_id` bigint(11),
	`group` int(11),
	PRIMARY KEY (`id`)
);
CREATE INDEX IF NOT EXISTS idx_topic_namespace_group ON partition_group_replica(`topic`, `namespace`, `group`);
CREATE INDEX IF NOT EXISTS idx_broker ON partition_group_replica(`broker_id`);

CREATE TABLE IF NOT EXISTS `config` (
	`id` varchar(255) NOT NULL,
	`key` varchar(255),
	`value` varchar(255),
	`group` varchar(1024),
	PRIMARY KEY (`id`)
);
CREATE INDEX IF NOT EXISTS idx_key_group ON config(`key`, `group`);
CREATE INDEX IF NOT EXISTS idx_group ON config(`group`);