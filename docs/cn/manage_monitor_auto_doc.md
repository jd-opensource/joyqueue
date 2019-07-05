1.获取所有topic监控信息
---------------
- 接口路径 /monitor/topics
- 请求类型 GET
- 功能描述 获取所有topic监控信息
- 参数 

|    字段    | 类型  | 备注  |
|:--------:|:---:|:---:|
|   page   | int |     |
| pageSize | int |     |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topics?page=0&pageSize=10
```
响应: 
```
{	"code":200,	"data":{		"data":[],		"page":0,		"pageSize":10,		"pages":1,		"total":8	},	"message":"SUCCESS"}
```
2.获取启动信息
--------
- 接口路径 /startInfo
- 请求类型 GET
- 功能描述 获取启动信息
- 参数 无


示例: 请求: 
```
curl -X GET http://localhost:50090/startInfo
```
响应: 
```
{	"code":200,	"data":{		"commitDate":"05.07.2019 @ 17:40:55 CST",		"revision":"6fddb45",		"startupTime":1562321594122	},	"message":"SUCCESS"}
```
3.获取连接明细
--------
- 接口路径 /monitor/topic/:topic/connections/detail
- 请求类型 GET
- 功能描述 获取连接明细
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String | 主题  |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/connections/detail
```
响应: 
```
{	"code":200,	"data":{		"clients":[],		"consumer":0,		"producer":0,		"total":0	},	"message":"SUCCESS"}
```
4.获取partition监控信息
-----------------
- 接口路径 /monitor/topic/:topic/app/:app/partition/:partition
- 请求类型 GET
- 功能描述 获取partition监控信息
- 参数 

|    字段     |   类型   |    备注     |
|:---------:|:------:|:---------:|
|   topic   | String |    主题     |
|    app    | String |    应用     |
| partition | short  | partition |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/app/test_app/partition/0
```
响应: 
```
{	"code":200,	"data":{		"app":"test_app",		"deQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"enQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partition":0,		"topic":"test_topic_1"	},	"message":"SUCCESS"}
```
5.getWritableResult
-------------------
- 接口路径 /monitor/topic/:topic/app/:app/writable
- 请求类型 GET
- 功能描述 无
- 参数 无


示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/:topic/app/:app/writable
```
响应: 
```
{	"code":200,	"data":{		"joyQueueCode":"FW_TOPIC_NOT_EXIST",		"success":false	},	"message":"SUCCESS"}
```
6.获取生产者信息
---------
- 接口路径 /monitor/topic/:topic/app/:app/producer/partitionGroup/:partitionGroupId
- 请求类型 GET
- 功能描述 获取生产者信息
- 参数 

|        字段        |   类型   | 备注  |
|:----------------:|:------:|:---:|
|      topic       | String |     |
|       app        | String |     |
| partitionGroupId |  int   |     |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/app/test_app/producer/partitionGroup/0
```
响应: 
```
{	"code":200,	"data":{		"app":"test_app",		"enQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partitionGroupId":0,		"topic":"test_topic_1"	},	"message":"SUCCESS"}
```
7.获取partition监控信息
-----------------
- 接口路径 /monitor/topic/:topic/app/:app/partitions
- 请求类型 GET
- 功能描述 获取partition监控信息
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String |     |
|  app  | String |     |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/app/test_app/partitions
```
响应: 
```
{	"code":200,	"data":[],	"message":"SUCCESS"}
```
8.获取消费者信息
---------
- 接口路径 /monitor/topic/:topic/app/:app/consumer/partition/:partition
- 请求类型 GET
- 功能描述 获取消费者信息
- 参数 

|    字段     |   类型   | 备注  |
|:---------:|:------:|:---:|
|   topic   | String |     |
|    app    | String |     |
| partition | short  |     |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/app/test_app/consumer/partition/0
```
响应: 
```
{	"code":200,	"data":{		"app":"test_app",		"deQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partition":0,		"pending":{			"count":0		},		"topic":"test_topic_1"	},	"message":"SUCCESS"}
```
9.获取连接明细
--------
- 接口路径 /monitor/topic/:topic/app/:app/producer/connections/detail
- 请求类型 GET
- 功能描述 获取连接明细
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String | 主题  |
|  app  | String | 应用  |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/app/test_app/producer/connections/detail
```
响应: 
```
{	"code":200,	"data":{		"clients":[],		"consumer":0,		"producer":0,		"total":0	},	"message":"SUCCESS"}
```
10.获取消费者信息
----------
- 接口路径 /monitor/topic/:topic/app/:app/consumer/partitionGroup/:partitionGroupId
- 请求类型 GET
- 功能描述 获取消费者信息
- 参数 

|        字段        |   类型   | 备注  |
|:----------------:|:------:|:---:|
|      topic       | String |     |
|       app        | String |     |
| partitionGroupId |  int   |     |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/app/test_app/consumer/partitionGroup/0
```
响应: 
```
{	"code":200,	"data":{		"app":"test_app",		"deQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partitionGroupId":0,		"pending":{			"count":0		},		"topic":"test_topic_1"	},	"message":"SUCCESS"}
```
11.查找协调者
--------
- 接口路径 /monitor/coordinator/group/:groupId/detail
- 请求类型 GET
- 功能描述 查找协调者
- 参数 

|   字段    |   类型   | 备注  |
|:-------:|:------:|:---:|
| groupId | String |     |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/coordinator/group/abc/detail
```
响应: 
```
{	"code":200,	"data":{		"current":{			"address":"10.0.16.231:50088",			"backEndPort":50089,			"dataCenter":"UNKNOWN",			"id":1562321590,			"ip":"10.0.16.231",			"managerPort":50091,			"monitorPort":50090,			"permission":"FULL",			"port":50088,			"retryType":"RemoteRetry"		},		"partitionGroup":4,		"replicas":[],		"topic":{			"code":"__group_coordinators",			"fullName":"__group_coordinators",			"namespace":""		}	},	"message":"SUCCESS"}
```
12.获取生产者信息
----------
- 接口路径 /monitor/topic/:topic/app/:app/producer/partitions
- 请求类型 GET
- 功能描述 获取生产者信息
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String |     |
|  app  | String |     |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/app/test_app/producer/partitions
```
响应: 
```
{	"code":200,	"data":[		{			"app":"test_app",			"enQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partition":5,			"topic":"test_topic_1"		},		{			"app":"test_app",			"enQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partition":6,			"topic":"test_topic_1"		},		{			"app":"test_app",			"enQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partition":3,			"topic":"test_topic_1"		},		{			"app":"test_app",			"enQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partition":4,			"topic":"test_topic_1"		},		{			"app":"test_app",			"enQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partition":9,			"topic":"test_topic_1"		},		{			"app":"test_app",			"enQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partition":7,			"topic":"test_topic_1"		},		{			"app":"test_app",			"enQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partition":8,			"topic":"test_topic_1"		},		{			"app":"test_app",			"enQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partition":1,			"topic":"test_topic_1"		},		{			"app":"test_app",			"enQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partition":2,			"topic":"test_topic_1"		},		{			"app":"test_app",			"enQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partition":0,			"topic":"test_topic_1"		}	],	"message":"SUCCESS"}
```
13.获取生产者信息
----------
- 接口路径 /monitor/topic/:topic/app/:app/producer/partitionGroups
- 请求类型 GET
- 功能描述 获取生产者信息
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String |     |
|  app  | String |     |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/app/test_app/producer/partitionGroups
```
响应: 
```
{	"code":200,	"data":[		{			"app":"test_app",			"enQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partitionGroupId":5,			"topic":"test_topic_1"		},		{			"app":"test_app",			"enQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partitionGroupId":6,			"topic":"test_topic_1"		},		{			"app":"test_app",			"enQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partitionGroupId":3,			"topic":"test_topic_1"		},		{			"app":"test_app",			"enQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partitionGroupId":4,			"topic":"test_topic_1"		},		{			"app":"test_app",			"enQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partitionGroupId":9,			"topic":"test_topic_1"		},		{			"app":"test_app",			"enQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partitionGroupId":7,			"topic":"test_topic_1"		},		{			"app":"test_app",			"enQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partitionGroupId":8,			"topic":"test_topic_1"		},		{			"app":"test_app",			"enQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partitionGroupId":1,			"topic":"test_topic_1"		},		{			"app":"test_app",			"enQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partitionGroupId":2,			"topic":"test_topic_1"		},		{			"app":"test_app",			"enQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partitionGroupId":0,			"topic":"test_topic_1"		}	],	"message":"SUCCESS"}
```
14.获取partitionGroup监控信息
-----------------------
- 接口路径 /monitor/topic/:topic/app/:app/partitionGroups
- 请求类型 GET
- 功能描述 获取partitionGroup监控信息
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String |     |
|  app  | String |     |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/app/test_app/partitionGroups
```
响应: 
```
{	"code":200,	"data":[],	"message":"SUCCESS"}
```
15.获取连接明细
---------
- 接口路径 /monitor/topic/:topic/app/:app/consumer/connections/detail
- 请求类型 GET
- 功能描述 获取连接明细
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String | 主题  |
|  app  | String | 应用  |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/app/test_app/consumer/connections/detail
```
响应: 
```
{	"code":200,	"data":{		"clients":[],		"consumer":0,		"producer":0,		"total":0	},	"message":"SUCCESS"}
```
16.thread safe
broker state 扩展信息
--------------------------------
- 接口路径 /metrics
- 请求类型 GET
- 功能描述 thread safe
broker state 扩展信息,扩展信息包含topic>app>partitionGroup>partition积压
and broker id
- 参数 

|    字段     |  类型  | 备注  |
|:---------:|:----:|:---:|
| timeStamp | long |     |
示例: 请求: 
```
curl -X GET http://localhost:50090/metrics?timeStamp=19395935
```
响应: 
```
{	"code":200,	"data":{		"brokerId":1562321590,		"brokerStat":{			"brokerId":1562321590,			"connectionStat":{				"connection":0,				"connectionMap":{},				"consumer":0,				"producer":0			},			"deQueueStat":{				"avg":0.0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"total":0,				"totalSize":0,				"totalTraffic":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"enQueueStat":{				"avg":0.0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"total":0,				"totalSize":0,				"totalTraffic":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"replicationStat":{				"appendStat":{					"avg":0.0,					"max":0.0,					"min":0.0,					"oneMinuteRate":0,					"size":0,					"total":0,					"totalSize":0,					"totalTraffic":0,					"tp90":0.0,					"tp99":0.0,					"tps":0,					"traffic":0				},				"partitionGroup":0,				"replicaStat":{					"avg":0.0,					"max":0.0,					"min":0.0,					"oneMinuteRate":0,					"size":0,					"total":0,					"totalSize":0,					"totalTraffic":0,					"tp90":0.0,					"tp99":0.0,					"tps":0,					"traffic":0				}			},			"topicStats":{				"test_topic_1":{					"appStats":{						"test_app":{							"app":"test_app",							"connectionStat":{								"connection":0,								"connectionMap":{},								"consumer":0,								"producer":0							},							"consumerStat":{								"app":"test_app",								"connectionStat":{									"connection":0,									"connectionMap":{},									"consumer":0,									"producer":0								},								"deQueueStat":{									"avg":0.0,									"max":0.0,									"min":0.0,									"oneMinuteRate":0,									"size":0,									"total":0,									"totalSize":0,									"totalTraffic":0,									"tp90":0.0,									"tp99":0.0,									"tps":0,									"traffic":0								},								"partitionGroupStatMap":{0:{										"app":"test_app",										"deQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"enQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"partitionGroup":0,										"partitionStatMap":{},										"replicationStat":{											"appendStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											},											"partitionGroup":0,											"replicaStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											}										},										"topic":"test_topic_1"									}								},								"retryStat":{									"failure":{										"avg":0.0,										"count":0,										"max":0.0,										"meanRate":0,										"min":0.0,										"oneMinuteRate":0,										"tp75":0.0,										"tp90":0.0,										"tp95":0.0,										"tp99":0.0,										"tp999":0.0									},									"success":{										"avg":0.0,										"count":0,										"max":0.0,										"meanRate":0,										"min":0.0,										"oneMinuteRate":0,										"tp75":0.0,										"tp90":0.0,										"tp95":0.0,										"tp99":0.0,										"tp999":0.0									}								},								"topic":"test_topic_1"							},							"partitionGroupStatMap":{},							"producerStat":{								"app":"test_app",								"connectionStat":{									"connection":0,									"connectionMap":{},									"consumer":0,									"producer":0								},								"enQueueStat":{									"avg":0.0,									"max":0.0,									"min":0.0,									"oneMinuteRate":0,									"size":0,									"total":0,									"totalSize":0,									"totalTraffic":0,									"tp90":0.0,									"tp99":0.0,									"tps":0,									"traffic":0								},								"partitionGroupStatMap":{0:{										"app":"test_app",										"deQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"enQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"partitionGroup":0,										"partitionStatMap":{0:{												"app":"test_app",												"deQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"enQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"partition":0,												"topic":"test_topic_1"											}										},										"replicationStat":{											"appendStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											},											"partitionGroup":0,											"replicaStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											}										},										"topic":"test_topic_1"									},1:{										"app":"test_app",										"deQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"enQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"partitionGroup":1,										"partitionStatMap":{1:{												"app":"test_app",												"deQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"enQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"partition":1,												"topic":"test_topic_1"											}										},										"replicationStat":{											"appendStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											},											"partitionGroup":0,											"replicaStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											}										},										"topic":"test_topic_1"									},2:{										"app":"test_app",										"deQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"enQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"partitionGroup":2,										"partitionStatMap":{2:{												"app":"test_app",												"deQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"enQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"partition":2,												"topic":"test_topic_1"											}										},										"replicationStat":{											"appendStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											},											"partitionGroup":0,											"replicaStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											}										},										"topic":"test_topic_1"									},3:{										"app":"test_app",										"deQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"enQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"partitionGroup":3,										"partitionStatMap":{3:{												"app":"test_app",												"deQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"enQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"partition":3,												"topic":"test_topic_1"											}										},										"replicationStat":{											"appendStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											},											"partitionGroup":0,											"replicaStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											}										},										"topic":"test_topic_1"									},4:{										"app":"test_app",										"deQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"enQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"partitionGroup":4,										"partitionStatMap":{4:{												"app":"test_app",												"deQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"enQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"partition":4,												"topic":"test_topic_1"											}										},										"replicationStat":{											"appendStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											},											"partitionGroup":0,											"replicaStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											}										},										"topic":"test_topic_1"									},5:{										"app":"test_app",										"deQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"enQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"partitionGroup":5,										"partitionStatMap":{5:{												"app":"test_app",												"deQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"enQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"partition":5,												"topic":"test_topic_1"											}										},										"replicationStat":{											"appendStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											},											"partitionGroup":0,											"replicaStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											}										},										"topic":"test_topic_1"									},6:{										"app":"test_app",										"deQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"enQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"partitionGroup":6,										"partitionStatMap":{6:{												"app":"test_app",												"deQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"enQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"partition":6,												"topic":"test_topic_1"											}										},										"replicationStat":{											"appendStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											},											"partitionGroup":0,											"replicaStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											}										},										"topic":"test_topic_1"									},7:{										"app":"test_app",										"deQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"enQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"partitionGroup":7,										"partitionStatMap":{7:{												"app":"test_app",												"deQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"enQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"partition":7,												"topic":"test_topic_1"											}										},										"replicationStat":{											"appendStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											},											"partitionGroup":0,											"replicaStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											}										},										"topic":"test_topic_1"									},8:{										"app":"test_app",										"deQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"enQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"partitionGroup":8,										"partitionStatMap":{8:{												"app":"test_app",												"deQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"enQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"partition":8,												"topic":"test_topic_1"											}										},										"replicationStat":{											"appendStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											},											"partitionGroup":0,											"replicaStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											}										},										"topic":"test_topic_1"									},9:{										"app":"test_app",										"deQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"enQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"partitionGroup":9,										"partitionStatMap":{9:{												"app":"test_app",												"deQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"enQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"partition":9,												"topic":"test_topic_1"											}										},										"replicationStat":{											"appendStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											},											"partitionGroup":0,											"replicaStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											}										},										"topic":"test_topic_1"									}								},								"topic":"test_topic_1"							},							"topic":"test_topic_1"						}					},					"connectionStat":{						"connection":0,						"connectionMap":{},						"consumer":0,						"producer":0					},					"deQueueStat":{						"avg":0.0,						"max":0.0,						"min":0.0,						"oneMinuteRate":0,						"size":0,						"total":0,						"totalSize":0,						"totalTraffic":0,						"tp90":0.0,						"tp99":0.0,						"tps":0,						"traffic":0					},					"enQueueStat":{						"avg":0.0,						"max":0.0,						"min":0.0,						"oneMinuteRate":0,						"size":0,						"total":0,						"totalSize":0,						"totalTraffic":0,						"tp90":0.0,						"tp99":0.0,						"tps":0,						"traffic":0					},					"partitionGroupStatMap":{},					"topic":"test_topic_1"				}			}		},		"heap":{			"committed":1391984640,			"init":268435456,			"max":3817865216,			"used":330325944		},		"nonHeap":{			"committed":98107392,			"init":2555904,			"max":-1,			"used":95778232		},		"timeStamp":19395935,		"topicPendingStatMap":{			"test_topic_1":{				"pending":0,				"pendingStatSubMap":{					"test_app":{						"app":"test_app",						"pending":0,						"pendingStatSubMap":{0:{								"app":"test_app",								"partitionGroup":0,								"pending":0,								"pendingStatSubMap":{0:0								},								"topic":"test_topic_1"							},1:{								"app":"test_app",								"partitionGroup":1,								"pending":0,								"pendingStatSubMap":{1:0								},								"topic":"test_topic_1"							},2:{								"app":"test_app",								"partitionGroup":2,								"pending":0,								"pendingStatSubMap":{2:0								},								"topic":"test_topic_1"							},3:{								"app":"test_app",								"partitionGroup":3,								"pending":0,								"pendingStatSubMap":{3:0								},								"topic":"test_topic_1"							},4:{								"app":"test_app",								"partitionGroup":4,								"pending":0,								"pendingStatSubMap":{4:0								},								"topic":"test_topic_1"							},5:{								"app":"test_app",								"partitionGroup":5,								"pending":0,								"pendingStatSubMap":{5:0								},								"topic":"test_topic_1"							},6:{								"app":"test_app",								"partitionGroup":6,								"pending":0,								"pendingStatSubMap":{6:0								},								"topic":"test_topic_1"							},7:{								"app":"test_app",								"partitionGroup":7,								"pending":0,								"pendingStatSubMap":{7:0								},								"topic":"test_topic_1"							},8:{								"app":"test_app",								"partitionGroup":8,								"pending":0,								"pendingStatSubMap":{8:0								},								"topic":"test_topic_1"							},9:{								"app":"test_app",								"partitionGroup":9,								"pending":0,								"pendingStatSubMap":{9:0								},								"topic":"test_topic_1"							}						},						"topic":"test_topic_1"					}				},				"topic":"test_topic_1"			}		}	},	"message":"SUCCESS"}
```
17.获取消费者信息
----------
- 接口路径 /monitor/topic/:topic/app/:app/consumer/partitions
- 请求类型 GET
- 功能描述 获取消费者信息
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String |     |
|  app  | String |     |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/app/test_app/consumer/partitions
```
响应: 
```
{	"code":200,	"data":[		{			"app":"test_app",			"deQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partition":5,			"pending":{				"count":0			},			"topic":"test_topic_1"		},		{			"app":"test_app",			"deQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partition":6,			"pending":{				"count":0			},			"topic":"test_topic_1"		},		{			"app":"test_app",			"deQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partition":3,			"pending":{				"count":0			},			"topic":"test_topic_1"		},		{			"app":"test_app",			"deQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partition":4,			"pending":{				"count":0			},			"topic":"test_topic_1"		},		{			"app":"test_app",			"deQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partition":9,			"pending":{				"count":0			},			"topic":"test_topic_1"		},		{			"app":"test_app",			"deQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partition":7,			"pending":{				"count":0			},			"topic":"test_topic_1"		},		{			"app":"test_app",			"deQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partition":8,			"pending":{				"count":0			},			"topic":"test_topic_1"		},		{			"app":"test_app",			"deQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partition":1,			"pending":{				"count":0			},			"topic":"test_topic_1"		},		{			"app":"test_app",			"deQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partition":2,			"pending":{				"count":0			},			"topic":"test_topic_1"		},		{			"app":"test_app",			"deQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partition":0,			"pending":{				"count":0			},			"topic":"test_topic_1"		}	],	"message":"SUCCESS"}
```
18.获取生产者信息
----------
- 接口路径 /monitor/topic/:topic/app/:app/producer
- 请求类型 GET
- 功能描述 获取生产者信息
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String |     |
|  app  | String |     |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/app/test_app/producer
```
响应: 
```
{	"code":200,	"data":{		"app":"test_app",		"connections":0,		"enQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"topic":"test_topic_1"	},	"message":"SUCCESS"}
```
19.获得协调者组成员
-----------
- 接口路径 /monitor/coordinator/namespace/:namespace/group/:groupId/members
- 请求类型 GET
- 功能描述 获得协调者组成员
- 参数 

|    字段     |   类型    | 备注  |
|:---------:|:-------:|:---:|
| namespace | String  |     |
|  groupId  | String  |     |
|   topic   | String  |     |
| isFormat  | boolean |     |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/coordinator/namespace/journalq/group/abc/members?topic=test_topic_1&isFormat=true
```
响应: 
```
{	"code":200,	"message":"SUCCESS"}
```
20.获取所有生产topic监控信息
------------------
- 接口路径 /monitor/producers
- 请求类型 GET
- 功能描述 获取所有生产topic监控信息
- 参数 

|    字段    | 类型  | 备注  |
|:--------:|:---:|:---:|
|   page   | int |     |
| pageSize | int |     |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/producers?page=0&pageSize=10
```
响应: 
```
{	"code":200,	"data":{		"data":[],		"page":0,		"pageSize":10,		"pages":1,		"total":1	},	"message":"SUCCESS"}
```
21.getReadableResult
--------------------
- 接口路径 /monitor/topic/:topic/app/:app/readable
- 请求类型 GET
- 功能描述 无
- 参数 无


示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/:topic/app/:app/readable
```
响应: 
```
{	"code":200,	"data":{		"joyQueueCode":"FW_TOPIC_NOT_EXIST",		"success":false	},	"message":"SUCCESS"}
```
22.获取生产者信息
----------
- 接口路径 /monitor/topic/:topic/app/:app/producer/partition/:partition
- 请求类型 GET
- 功能描述 获取生产者信息
- 参数 

|    字段     |   类型   |    备注     |
|:---------:|:------:|:---------:|
|   topic   | String |           |
|    app    | String |           |
| partition | short  | partition |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/app/test_app/producer/partition/0
```
响应: 
```
{	"code":200,	"data":{		"app":"test_app",		"enQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partition":0,		"topic":"test_topic_1"	},	"message":"SUCCESS"}
```
23.获取连接明细
---------
- 接口路径 /monitor/topic/:topic/consumer/connections/detail
- 请求类型 GET
- 功能描述 获取连接明细
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String | 主题  |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/consumer/connections/detail
```
响应: 
```
{	"code":200,	"data":{		"clients":[],		"consumer":0,		"producer":0,		"total":0	},	"message":"SUCCESS"}
```
24.获取所有消费topic监控信息
------------------
- 接口路径 /monitor/consumers
- 请求类型 GET
- 功能描述 获取所有消费topic监控信息
- 参数 

|    字段    | 类型  | 备注  |
|:--------:|:---:|:---:|
|   page   | int |     |
| pageSize | int |     |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/consumers?page=0&pageSize=10
```
响应: 
```
{	"code":200,	"data":{		"data":[],		"page":0,		"pageSize":10,		"pages":1,		"total":1	},	"message":"SUCCESS"}
```
25.获取broker信息
-------------
- 接口路径 /monitor/broker
- 请求类型 GET
- 功能描述 获取broker信息
- 参数 无


示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/broker
```
响应: 
```
{	"code":200,	"data":{		"bufferPoolMonitorInfo":{			"directUsed":"0",			"maxMemorySize":"3.2 GB",			"mmpUsed":"0",			"plMonitorInfos":[				{					"bufferSize":"128 MB",					"cached":"384 MB",					"totalSize":"384 MB",					"usedPreLoad":"0"				},				{					"bufferSize":"512 kB",					"cached":"1.5 MB",					"totalSize":"1.5 MB",					"usedPreLoad":"0"				}			],			"plUsed":"385.5 MB",			"used":"385.5 MB"		},		"connection":{			"consumer":0,			"producer":0,			"total":0		},		"deQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"election":{			"started":true		},		"enQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"nameServer":{			"started":true		},		"replication":{			"appendStat":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partitionGroup":0,			"replicaStat":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"started":true		},		"store":{			"freeSpace":"44.6 GB",			"started":true,			"totalSpace":"233.5 GB"		}	},	"message":"SUCCESS"}
```
26.获取当前连接数信息
------------
- 接口路径 /monitor/connections
- 请求类型 GET
- 功能描述 获取当前连接数信息
- 参数 无


示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/connections
```
响应: 
```
{	"code":200,	"data":{		"consumer":0,		"producer":0,		"total":0	},	"message":"SUCCESS"}
```
27.获取partitionGroup监控信息
-----------------------
- 接口路径 /monitor/topic/:topic/app/:app/partitionGroup/:partitionGroup
- 请求类型 GET
- 功能描述 获取partitionGroup监控信息
- 参数 

|       字段       |   类型   |       备注       |
|:--------------:|:------:|:--------------:|
|     topic      | String |       主题       |
|      app       | String |       应用       |
| partitionGroup |  int   | partitionGroup |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/app/test_app/partitionGroup/0
```
响应: 
```
{	"code":200,	"data":{		"app":"test_app",		"deQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"enQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"flushPosition":0,		"indexPosition":0,		"leftPosition":0,		"partitionGroup":0,		"replication":{			"appendStat":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partitionGroup":0,			"replicaStat":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"started":false		},		"replicationPosition":0,		"rightPosition":0,		"topic":"test_topic_1"	},	"message":"SUCCESS"}
```
28.获取partition监控信息
------------------
- 接口路径 /monitor/topic/:topic/partitions
- 请求类型 GET
- 功能描述 获取partition监控信息
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String |     |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/partitions
```
响应: 
```
{	"code":200,	"data":[],	"message":"SUCCESS"}
```
29.getArchiveMonitorInfo
------------------------
- 接口路径 /monitor/archive/info
- 请求类型 GET
- 功能描述 无
- 参数 无


示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/archive/info
```
响应: 
```
{	"code":500,	"message":"java.lang.NullPointerException"}
```
30.获取当前连接数信息
------------
- 接口路径 /monitor/topic/:topic/app/:app/connections
- 请求类型 GET
- 功能描述 获取当前连接数信息
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String | 主题  |
|  app  | String | 应用  |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/app/test_app/connections
```
响应: 
```
{	"code":200,	"data":{		"consumer":0,		"producer":0,		"total":0	},	"message":"SUCCESS"}
```
31.获取消费者信息
----------
- 接口路径 /monitor/topic/:topic/app/:app/consumer/partitionGroups
- 请求类型 GET
- 功能描述 获取消费者信息
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String |     |
|  app  | String |     |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/app/test_app/consumer/partitionGroups
```
响应: 
```
{	"code":200,	"data":[		{			"app":"test_app",			"deQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partitionGroupId":5,			"pending":{				"count":0			},			"topic":"test_topic_1"		},		{			"app":"test_app",			"deQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partitionGroupId":6,			"pending":{				"count":0			},			"topic":"test_topic_1"		},		{			"app":"test_app",			"deQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partitionGroupId":3,			"pending":{				"count":0			},			"topic":"test_topic_1"		},		{			"app":"test_app",			"deQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partitionGroupId":4,			"pending":{				"count":0			},			"topic":"test_topic_1"		},		{			"app":"test_app",			"deQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partitionGroupId":9,			"pending":{				"count":0			},			"topic":"test_topic_1"		},		{			"app":"test_app",			"deQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partitionGroupId":7,			"pending":{				"count":0			},			"topic":"test_topic_1"		},		{			"app":"test_app",			"deQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partitionGroupId":8,			"pending":{				"count":0			},			"topic":"test_topic_1"		},		{			"app":"test_app",			"deQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partitionGroupId":1,			"pending":{				"count":0			},			"topic":"test_topic_1"		},		{			"app":"test_app",			"deQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partitionGroupId":2,			"pending":{				"count":0			},			"topic":"test_topic_1"		},		{			"app":"test_app",			"deQueue":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partitionGroupId":0,			"pending":{				"count":0			},			"topic":"test_topic_1"		}	],	"message":"SUCCESS"}
```
32.获取topic监控信息
--------------
- 接口路径 /monitor/topics/search
- 请求类型 GET
- 功能描述 获取topic监控信息
- 参数 

|   字段   |      类型      | 备注  |
|:------:|:------------:|:---:|
| topics | List<String> | 主题  |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topics/search?topics=["a","b"]
```
响应: 
```

```
33.获取topic监控信息
--------------
- 接口路径 /monitor/topic/:topic
- 请求类型 GET
- 功能描述 获取topic监控信息
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String | 主题  |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1
```
响应: 
```
{	"code":200,	"data":{		"connection":{			"consumer":0,			"producer":0,			"total":0		},		"deQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"enQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"topic":"test_topic_1"	},	"message":"SUCCESS"}
```
34.返回当前所有连接数
------------
- 接口路径 /monitor/connections/detail
- 请求类型 GET
- 功能描述 返回当前所有连接数
- 参数 无


示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/connections/detail
```
响应: 
```
{	"code":200,	"data":{		"clients":[],		"consumer":0,		"producer":0,		"total":0	},	"message":"SUCCESS"}
```
35.获取连接明细
---------
- 接口路径 /monitor/topic/:topic/app/:app/connections/detail
- 请求类型 GET
- 功能描述 获取连接明细
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String | 主题  |
|  app  | String | 应用  |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/app/test_app/connections/detail
```
响应: 
```
{	"code":200,	"data":{		"clients":[],		"consumer":0,		"producer":0,		"total":0	},	"message":"SUCCESS"}
```
36.获取当前连接数信息
------------
- 接口路径 /monitor/topic/:topic/connections
- 请求类型 GET
- 功能描述 获取当前连接数信息
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String | 主题  |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/connections
```
响应: 
```
{	"code":200,	"data":{		"consumer":0,		"producer":0,		"total":0	},	"message":"SUCCESS"}
```
37.getSendBackLogNum
--------------------
- 接口路径 /monitor/archive/send
- 请求类型 GET
- 功能描述 无
- 参数 无


示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/archive/send
```
响应: 
```
{	"code":500,	"message":"java.lang.NullPointerException"}
```
38.getTopicMetadata
-------------------
- 接口路径 /monitor/topic/:topic/metadata
- 请求类型 GET
- 功能描述 无
- 参数 无


示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/:topic/metadata
```
响应: 
```
{	"code":200,	"message":"SUCCESS"}
```
39.获取partitionGroup监控信息
-----------------------
- 接口路径 /monitor/topic/:topic/partitionGroups
- 请求类型 GET
- 功能描述 获取partitionGroup监控信息
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String |     |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/partitionGroups
```
响应: 
```
{	"code":200,	"data":[],	"message":"SUCCESS"}
```
40.获取连接明细
---------
- 接口路径 /monitor/topic/:topic/producer/connections/detail
- 请求类型 GET
- 功能描述 获取连接明细
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String | 主题  |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/producer/connections/detail
```
响应: 
```
{	"code":200,	"data":{		"clients":[],		"consumer":0,		"producer":0,		"total":0	},	"message":"SUCCESS"}
```
41.获取partition监控信息
------------------
- 接口路径 /monitor/topic/:topic/partition/:partition
- 请求类型 GET
- 功能描述 获取partition监控信息
- 参数 

|    字段     |   类型   |    备注     |
|:---------:|:------:|:---------:|
|   topic   | String |    主题     |
| partition | short  | partition |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/partition/0
```
响应: 
```
{	"code":200,	"data":{		"deQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"enQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partition":0,		"topic":"test_topic_1"	},	"message":"SUCCESS"}
```
42.获取partitionGroup监控信息
-----------------------
- 接口路径 /monitor/topic/:topic/partitionGroup/:partitionGroupId
- 请求类型 GET
- 功能描述 获取partitionGroup监控信息
- 参数 

|       字段       |   类型   |       备注       |
|:--------------:|:------:|:--------------:|
|     topic      | String |       主题       |
| partitionGroup |  int   | partitionGroup |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/partitionGroup/0Id
```
响应: 
```
{	"code":200,	"data":{		"deQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"enQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"flushPosition":0,		"indexPosition":0,		"leftPosition":0,		"partitionGroup":0,		"replication":{			"appendStat":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partitionGroup":0,			"replicaStat":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"started":false		},		"replicationPosition":0,		"rightPosition":0,		"topic":"test_topic_1"	},	"message":"SUCCESS"}
```
43.获取消费者信息
----------
- 接口路径 /monitor/topic/:topic/app/:app/consumer
- 请求类型 GET
- 功能描述 获取消费者信息
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String |     |
|  app  | String |     |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/app/test_app/consumer
```
响应: 
```
{	"code":200,	"data":{		"app":"test_app",		"connections":0,		"deQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"pending":{			"count":0		},		"retry":{			"count":0,			"failure":0,			"success":0		},		"topic":"test_topic_1"	},	"message":"SUCCESS"}
```
44.获得协调者组
---------
- 接口路径 /monitor/coordinator/namespace/:namespace/group/:groupId
- 请求类型 GET
- 功能描述 获得协调者组
- 参数 

|    字段     |   类型    | 备注  |
|:---------:|:-------:|:---:|
| namespace | String  |     |
|  groupId  | String  |     |
|   topic   | String  |     |
| isFormat  | boolean |     |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/coordinator/namespace/journalq/group/abc?topic=test_topic_1&isFormat=true
```
响应: 
```
{	"code":200,	"message":"SUCCESS"}
```
45.getConsumeBacklogNum
-----------------------
- 接口路径 /monitor/archive/consume
- 请求类型 GET
- 功能描述 无
- 参数 无


示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/archive/consume
```
响应: 
```
{	"code":500,	"message":"java.lang.NullPointerException"}
```
46.partitionGroupMetric
-----------------------
- 接口路径 /manage/topic/:topic/partitionGroup/:partitionGroup/store/metric
- 请求类型 GET
- 功能描述 无
- 参数 无


示例: 请求: 
```
curl -X GET http://localhost:50090/manage/topic/:topic/partitionGroup/:partitionGroup/store/metric
```
响应: 
```
{	"code":500,	"message":"java.lang.NullPointerException"}
```
47.listAbsolutePathFiles
------------------------
- 接口路径 /manage/store/absolutePathFiles
- 请求类型 GET
- 功能描述 无
- 参数 无


示例: 请求: 
```
curl -X GET http://localhost:50090/manage/store/absolutePathFiles
```
响应: 
```
{	"code":500,	"message":"java.lang.NullPointerException"}
```
48.读取消息
-------
- 接口路径 /manage/topic/:topic/partitionGroup/:partitionGroup/store/messages
- 请求类型 GET
- 功能描述 读取消息
- 参数 

|       字段       |   类型   | 备注  |
|:--------------:|:------:|:---:|
|     topic      | String |     |
| partitionGroup |  int   |     |
|    position    |  long  |     |
|     count      |  int   |     |
示例: 请求: 
```
curl -X GET http://localhost:50090/manage/topic/test_topic_1/partitionGroup/0/store/messages?position=0&count=10
```
响应: 
```
{	"code":500,	"message":"java.lang.NullPointerException"}
```
49.初始化协调者主题
-----------
- 接口路径 /manage/coordinator/init
- 请求类型 POST
- 功能描述 初始化协调者主题
- 参数 无


示例: 
50.topicMetric
--------------
- 接口路径 /manage/topic/:topic/store/metric
- 请求类型 GET
- 功能描述 无
- 参数 无


示例: 请求: 
```
curl -X GET http://localhost:50090/manage/topic/:topic/store/metric
```
响应: 
```
{	"code":200,	"data":{		"partitionGroupMetrics":[],		"topic":":topic"	},	"message":"SUCCESS"}
```
51.readPartitionMessage
-----------------------
- 接口路径 /manage/topic/:topic/partition/:partition/store/messages
- 请求类型 GET
- 功能描述 无
- 参数 无


示例: 请求: 
```
curl -X GET http://localhost:50090/manage/topic/:topic/partition/:partition/store/messages
```
响应: 
```
{	"code":200,	"data":[],	"message":"SUCCESS"}
```
52.关闭消费者连接
返回关闭的连接数
-------------------
- 接口路径 /manage/topic/:topic/app/:app/consumers
- 请求类型 DELETE
- 功能描述 关闭消费者连接
返回关闭的连接数
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String |     |
|  app  | String |     |
示例: 
53.readMessage
--------------
- 接口路径 /manage/store/messages
- 请求类型 GET
- 功能描述 无
- 参数 无


示例: 请求: 
```
curl -X GET http://localhost:50090/manage/store/messages
```
响应: 
```
{	"code":500,	"message":"java.lang.NullPointerException"}
```
54.updateTerm
-------------
- 接口路径 /manage/election/metadata/updateTerm/topic/:topic/partitionGroup/:partitionGroup/:term
- 请求类型 GET
- 功能描述 无
- 参数 无


示例: 请求: 
```
curl -X GET http://localhost:50090/manage/election/metadata/updateTerm/topic/:topic/partitionGroup/:partitionGroup/:term
```
响应: 
```
{	"code":500,	"message":"java.lang.NullPointerException"}
```
55.设置最大ack索引
------------
- 接口路径 /manage/topic/:topic/app/:app/partition/:partition/maxAck
- 请求类型 PUT
- 功能描述 设置最大ack索引
- 参数 

|    字段     |   类型   | 备注  |
|:---------:|:------:|:---:|
|   topic   | String |     |
|    app    | String |     |
| partition | short  |     |
示例: 
56.读取索引
-------
- 接口路径 /manage/topic/:topic/partition/:partition/store/indexes
- 请求类型 GET
- 功能描述 读取索引
- 参数 

|    字段     |   类型   | 备注  |
|:---------:|:------:|:---:|
|   topic   | String |     |
| partition | short  |     |
|   index   |  long  |     |
|   count   |  int   |     |
示例: 请求: 
```
curl -X GET http://localhost:50090/manage/topic/test_topic_1/partition/0/store/indexes?index=0&count=10
```
响应: 
```
{	"code":200,	"data":[],	"message":"SUCCESS"}
```
57.根据时间返回ack
------------
- 接口路径 /manage/topic/:topic/app/:app/timestamp/:timestamp/ackByTime
- 请求类型 GET
- 功能描述 根据时间返回ack
- 参数 

|    字段     |   类型   | 备注  |
|:---------:|:------:|:---:|
|   topic   | String |     |
|    app    | String |     |
| timestamp |  long  |     |
示例: 请求: 
```
curl -X GET http://localhost:50090/manage/topic/test_topic_1/app/test_app/timestamp/923e7937e932/ackByTime
```
响应: 
```
{	"code":200,	"data":[		{			"index":-1,			"lastAckTime":0,			"lastPullTime":0,			"leftIndex":0,			"partition":5,			"rightIndex":0		},		{			"index":-1,			"lastAckTime":0,			"lastPullTime":0,			"leftIndex":0,			"partition":6,			"rightIndex":0		},		{			"index":-1,			"lastAckTime":0,			"lastPullTime":0,			"leftIndex":0,			"partition":3,			"rightIndex":0		},		{			"index":-1,			"lastAckTime":0,			"lastPullTime":0,			"leftIndex":0,			"partition":4,			"rightIndex":0		},		{			"index":-1,			"lastAckTime":0,			"lastPullTime":0,			"leftIndex":0,			"partition":9,			"rightIndex":0		},		{			"index":-1,			"lastAckTime":0,			"lastPullTime":0,			"leftIndex":0,			"partition":7,			"rightIndex":0		},		{			"index":-1,			"lastAckTime":0,			"lastPullTime":0,			"leftIndex":0,			"partition":8,			"rightIndex":0		},		{			"index":-1,			"lastAckTime":0,			"lastPullTime":0,			"leftIndex":0,			"partition":1,			"rightIndex":0		},		{			"index":-1,			"lastAckTime":0,			"lastPullTime":0,			"leftIndex":0,			"partition":2,			"rightIndex":0		},		{			"index":-1,			"lastAckTime":0,			"lastPullTime":0,			"leftIndex":0,			"partition":0,			"rightIndex":0		}	],	"message":"SUCCESS"}
```
58.获取最后message
--------------
- 接口路径 /manage/topic/:topic/app/:app/message/last
- 请求类型 GET
- 功能描述 获取最后message
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String |     |
|  app  | String |     |
| count |  int   |     |
示例: 请求: 
```
curl -X GET http://localhost:50090/manage/topic/test_topic_1/app/test_app/message/last?count=10
```
响应: 
```
{	"code":200,	"data":[],	"message":"SUCCESS"}
```
59.根据时间设置ack
------------
- 接口路径 /manage/topic/:topic/app/:app/partition/:partition/ackByTime
- 请求类型 PUT
- 功能描述 根据时间设置ack
- 参数 

|    字段     |   类型   | 备注  |
|:---------:|:------:|:---:|
|   topic   | String |     |
|    app    | String |     |
| partition | short  |     |
| timestamp |  long  |     |
示例: 
60.返回最大ack索引
------------
- 接口路径 /manage/topic/:topic/app/:app/acks
- 请求类型 GET
- 功能描述 返回最大ack索引
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String |     |
|  app  | String |     |
示例: 请求: 
```
curl -X GET http://localhost:50090/manage/topic/test_topic_1/app/test_app/acks
```
响应: 
```
{	"code":200,	"data":[		{			"index":0,			"lastAckTime":-1,			"lastPullTime":-1,			"leftIndex":0,			"partition":5,			"rightIndex":0		},		{			"index":0,			"lastAckTime":-1,			"lastPullTime":-1,			"leftIndex":0,			"partition":6,			"rightIndex":0		},		{			"index":0,			"lastAckTime":-1,			"lastPullTime":-1,			"leftIndex":0,			"partition":3,			"rightIndex":0		},		{			"index":0,			"lastAckTime":-1,			"lastPullTime":-1,			"leftIndex":0,			"partition":4,			"rightIndex":0		},		{			"index":0,			"lastAckTime":-1,			"lastPullTime":-1,			"leftIndex":0,			"partition":9,			"rightIndex":0		},		{			"index":0,			"lastAckTime":-1,			"lastPullTime":-1,			"leftIndex":0,			"partition":7,			"rightIndex":0		},		{			"index":0,			"lastAckTime":-1,			"lastPullTime":-1,			"leftIndex":0,			"partition":8,			"rightIndex":0		},		{			"index":0,			"lastAckTime":-1,			"lastPullTime":-1,			"leftIndex":0,			"partition":1,			"rightIndex":0		},		{			"index":0,			"lastAckTime":-1,			"lastPullTime":-1,			"leftIndex":0,			"partition":2,			"rightIndex":0		},		{			"index":0,			"lastAckTime":-1,			"lastPullTime":-1,			"leftIndex":0,			"partition":0,			"rightIndex":0		}	],	"message":"SUCCESS"}
```
61.列出store中给定path的所有文件
----------------------
- 接口路径 /manage/store/files/:path
- 请求类型 GET
- 功能描述 列出store中给定path的所有文件
- 参数 

|  字段  |   类型   |       备注        |
|:----:|:------:|:---------------:|
| path | String | 相对store根目录的相对路径 |
示例: 请求: 
```
curl -X GET http://localhost:50090/manage/store/files/xxxx
```
响应: 
```
{	"code":200,	"message":"SUCCESS"}
```
62.readPartitionGroupStore
--------------------------
- 接口路径 /manage/topic/:topic/partitionGroup/:partitionGroup/store/data
- 请求类型 GET
- 功能描述 无
- 参数 无


示例: 请求: 
```
curl -X GET http://localhost:50090/manage/topic/:topic/partitionGroup/:partitionGroup/store/data
```
响应: 
```
{	"code":500,	"message":"java.lang.NullPointerException"}
```
63.关闭生产者连接
返回关闭的连接数
-------------------
- 接口路径 /manage/topic/:topic/app/:app/producers
- 请求类型 DELETE
- 功能描述 关闭生产者连接
返回关闭的连接数
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String |     |
|  app  | String |     |
示例: 
64.根据时间返回ack
------------
- 接口路径 /manage/topic/:topic/app/:app/partition/:partition/ackByTime
- 请求类型 GET
- 功能描述 根据时间返回ack
- 参数 

|    字段     |   类型   | 备注  |
|:---------:|:------:|:---:|
|   topic   | String |     |
|    app    | String |     |
| partition | short  |     |
| timestamp |  long  |     |
示例: 请求: 
```
curl -X GET http://localhost:50090/manage/topic/test_topic_1/app/test_app/partition/0/ackByTime?timestamp=923e7937e932
```
响应: 
```
{	"code":200,	"data":-1,	"message":"SUCCESS"}
```
65.获取积压message
--------------
- 接口路径 /manage/topic/:topic/app/:app/message/pending
- 请求类型 GET
- 功能描述 获取积压message
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String |     |
|  app  | String |     |
| count |  int   |     |
示例: 请求: 
```
curl -X GET http://localhost:50090/manage/topic/test_topic_1/app/test_app/message/pending?count=10
```
响应: 
```
{	"code":200,	"data":[],	"message":"SUCCESS"}
```
66.topics
---------
- 接口路径 /manage/topic/list
- 请求类型 GET
- 功能描述 无
- 参数 无


示例: 请求: 
```
curl -X GET http://localhost:50090/manage/topic/list
```
响应: 
```
{	"code":200,	"data":[		"test_topic",		"__transaction_coordinators",		"test_topic_4",		"test_topic_3",		"test_topic_2",		"__group_coordinators",		"test_topic_0",		"test_topic_1"	],	"message":"SUCCESS"}
```
67.读取partition
--------------
- 接口路径 /manage/topic/:topic/partitionGroup/detail
- 请求类型 GET
- 功能描述 读取partition
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String |     |
示例: 请求: 
```
curl -X GET http://localhost:50090/manage/topic/test_topic_1/partitionGroup/detail
```
响应: 
```
{	"code":200,	"data":[		{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":9,			"partitions":"9",			"replicationPosition":0,			"rightPosition":0		},		{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":0,			"partitions":"0",			"replicationPosition":0,			"rightPosition":0		},		{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":7,			"partitions":"7",			"replicationPosition":0,			"rightPosition":0		},		{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":6,			"partitions":"6",			"replicationPosition":0,			"rightPosition":0		},		{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":1,			"partitions":"1",			"replicationPosition":0,			"rightPosition":0		},		{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":8,			"partitions":"8",			"replicationPosition":0,			"rightPosition":0		},		{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":4,			"partitions":"4",			"replicationPosition":0,			"rightPosition":0		},		{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":3,			"partitions":"3",			"replicationPosition":0,			"rightPosition":0		},		{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":2,			"partitions":"2",			"replicationPosition":0,			"rightPosition":0		},		{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":5,			"partitions":"5",			"replicationPosition":0,			"rightPosition":0		}	],	"message":"SUCCESS"}
```
68.移除协调者组
---------
- 接口路径 /manage/coordinator/namespace/:namespace/group/:groupId
- 请求类型 DELETE
- 功能描述 移除协调者组
- 参数 

|    字段     |   类型   | 备注  |
|:---------:|:------:|:---:|
| namespace | String |     |
|  groupId  | String |     |
示例: 
69.describeTopic
----------------
- 接口路径 /manage/election/metadata/describeTopic/topic/:topic/partitionGroup/:partitionGroup
- 请求类型 GET
- 功能描述 无
- 参数 无


示例: 请求: 
```
curl -X GET http://localhost:50090/manage/election/metadata/describeTopic/topic/:topic/partitionGroup/:partitionGroup
```
响应: 
```
{	"code":200,	"data":"null",	"message":"SUCCESS"}
```
70.根据时间设置ack
------------
- 接口路径 /manage/topic/:topic/app/:app/ackByTime
- 请求类型 PUT
- 功能描述 根据时间设置ack
- 参数 

|    字段     |   类型   | 备注  |
|:---------:|:------:|:---:|
|   topic   | String |     |
|    app    | String |     |
| timestamp |  long  |     |
示例: 
71.获取message
------------
- 接口路径 /manage/topic/:topic/app/:app/partition/:partition/message
- 请求类型 GET
- 功能描述 获取message
- 参数 

|    字段     |   类型   | 备注  |
|:---------:|:------:|:---:|
|   topic   | String |     |
|    app    | String |     |
| partition | short  |     |
|   index   |  long  |     |
|   count   |  int   |     |
示例: 请求: 
```
curl -X GET http://localhost:50090/manage/topic/test_topic_1/app/test_app/partition/0/message?index=0&count=10
```
响应: 
```
{	"code":200,	"data":[],	"message":"SUCCESS"}
```
72.readIndices
--------------
- 接口路径 /manage/store/indexes
- 请求类型 GET
- 功能描述 无
- 参数 无


示例: 请求: 
```
curl -X GET http://localhost:50090/manage/store/indexes
```
响应: 
```
{	"code":500,	"message":"java.lang.NullPointerException"}
```
73.设置最大ack索引
------------
- 接口路径 /manage/topic/:topic/app/:app/maxAck
- 请求类型 PUT
- 功能描述 设置最大ack索引
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String |     |
|  app  | String |     |
示例: 
74.Store度量信息
------------
- 接口路径 /manage/topic/store/metrics
- 请求类型 GET
- 功能描述 Store度量信息
- 参数 无


示例: 请求: 
```
curl -X GET http://localhost:50090/manage/topic/store/metrics
```
响应: 
```
{	"code":200,	"data":[{		"partitionGroupMetrics":[{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":6,			"partitionMetrics":[{				"leftIndex":0,				"partition":6,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":7,			"partitionMetrics":[{				"leftIndex":0,				"partition":7,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":4,			"partitionMetrics":[{				"leftIndex":0,				"partition":4,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":5,			"partitionMetrics":[{				"leftIndex":0,				"partition":5,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":8,			"partitionMetrics":[{				"leftIndex":0,				"partition":8,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":9,			"partitionMetrics":[{				"leftIndex":0,				"partition":9,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":2,			"partitionMetrics":[{				"leftIndex":0,				"partition":2,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":3,			"partitionMetrics":[{				"leftIndex":0,				"partition":3,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":0,			"partitionMetrics":[{				"leftIndex":0,				"partition":0,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":1,			"partitionMetrics":[{				"leftIndex":0,				"partition":1,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		}],		"topic":"test_topic_0"	},{		"partitionGroupMetrics":[{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":4,			"partitionMetrics":[{				"leftIndex":0,				"partition":4,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":5,			"partitionMetrics":[{				"leftIndex":0,				"partition":5,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":2,			"partitionMetrics":[{				"leftIndex":0,				"partition":2,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":3,			"partitionMetrics":[{				"leftIndex":0,				"partition":3,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":8,			"partitionMetrics":[{				"leftIndex":0,				"partition":8,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":9,			"partitionMetrics":[{				"leftIndex":0,				"partition":9,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":6,			"partitionMetrics":[{				"leftIndex":0,				"partition":6,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":7,			"partitionMetrics":[{				"leftIndex":0,				"partition":7,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":0,			"partitionMetrics":[{				"leftIndex":0,				"partition":0,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":1,			"partitionMetrics":[{				"leftIndex":0,				"partition":1,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		}],		"topic":"test_topic_2"	},{		"partitionGroupMetrics":[{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":2,			"partitionMetrics":[{				"leftIndex":0,				"partition":2,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":3,			"partitionMetrics":[{				"leftIndex":0,				"partition":3,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":0,			"partitionMetrics":[{				"leftIndex":0,				"partition":0,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":1,			"partitionMetrics":[{				"leftIndex":0,				"partition":1,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":6,			"partitionMetrics":[{				"leftIndex":0,				"partition":6,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":7,			"partitionMetrics":[{				"leftIndex":0,				"partition":7,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":4,			"partitionMetrics":[{				"leftIndex":0,				"partition":4,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":5,			"partitionMetrics":[{				"leftIndex":0,				"partition":5,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":8,			"partitionMetrics":[{				"leftIndex":0,				"partition":8,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":9,			"partitionMetrics":[{				"leftIndex":0,				"partition":9,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		}],		"topic":"test_topic_4"	},{		"partitionGroupMetrics":[{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":4,			"partitionMetrics":[{				"leftIndex":0,				"partition":4,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":3,			"partitionMetrics":[{				"leftIndex":0,				"partition":3,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":6,			"partitionMetrics":[{				"leftIndex":0,				"partition":6,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":5,			"partitionMetrics":[{				"leftIndex":0,				"partition":5,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":8,			"partitionMetrics":[{				"leftIndex":0,				"partition":8,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":7,			"partitionMetrics":[{				"leftIndex":0,				"partition":7,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":9,			"partitionMetrics":[{				"leftIndex":0,				"partition":9,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":0,			"partitionMetrics":[{				"leftIndex":0,				"partition":0,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":2,			"partitionMetrics":[{				"leftIndex":0,				"partition":2,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":1,			"partitionMetrics":[{				"leftIndex":0,				"partition":1,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		}],		"topic":"__transaction_coordinators"	},{		"partitionGroupMetrics":[{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":0,			"partitionMetrics":[{				"leftIndex":0,				"partition":0,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":2,			"partitionMetrics":[{				"leftIndex":0,				"partition":2,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":1,			"partitionMetrics":[{				"leftIndex":0,				"partition":1,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":4,			"partitionMetrics":[{				"leftIndex":0,				"partition":4,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":3,			"partitionMetrics":[{				"leftIndex":0,				"partition":3,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":6,			"partitionMetrics":[{				"leftIndex":0,				"partition":6,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":5,			"partitionMetrics":[{				"leftIndex":0,				"partition":5,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":8,			"partitionMetrics":[{				"leftIndex":0,				"partition":8,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":7,			"partitionMetrics":[{				"leftIndex":0,				"partition":7,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":9,			"partitionMetrics":[{				"leftIndex":0,				"partition":9,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		}],		"topic":"test_topic"	},{		"partitionGroupMetrics":[{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":5,			"partitionMetrics":[{				"leftIndex":0,				"partition":5,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":6,			"partitionMetrics":[{				"leftIndex":0,				"partition":6,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":3,			"partitionMetrics":[{				"leftIndex":0,				"partition":3,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":4,			"partitionMetrics":[{				"leftIndex":0,				"partition":4,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":9,			"partitionMetrics":[{				"leftIndex":0,				"partition":9,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":7,			"partitionMetrics":[{				"leftIndex":0,				"partition":7,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":8,			"partitionMetrics":[{				"leftIndex":0,				"partition":8,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":1,			"partitionMetrics":[{				"leftIndex":0,				"partition":1,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":2,			"partitionMetrics":[{				"leftIndex":0,				"partition":2,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":0,			"partitionMetrics":[{				"leftIndex":0,				"partition":0,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		}],		"topic":"test_topic_1"	},{		"partitionGroupMetrics":[{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":3,			"partitionMetrics":[{				"leftIndex":0,				"partition":3,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":4,			"partitionMetrics":[{				"leftIndex":0,				"partition":4,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":1,			"partitionMetrics":[{				"leftIndex":0,				"partition":1,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":2,			"partitionMetrics":[{				"leftIndex":0,				"partition":2,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":7,			"partitionMetrics":[{				"leftIndex":0,				"partition":7,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":8,			"partitionMetrics":[{				"leftIndex":0,				"partition":8,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":5,			"partitionMetrics":[{				"leftIndex":0,				"partition":5,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":6,			"partitionMetrics":[{				"leftIndex":0,				"partition":6,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":0,			"partitionMetrics":[{				"leftIndex":0,				"partition":0,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":9,			"partitionMetrics":[{				"leftIndex":0,				"partition":9,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		}],		"topic":"test_topic_3"	},{		"partitionGroupMetrics":[{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":3,			"partitionMetrics":[{				"leftIndex":0,				"partition":3,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":2,			"partitionMetrics":[{				"leftIndex":0,				"partition":2,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":1,			"partitionMetrics":[{				"leftIndex":0,				"partition":1,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":0,			"partitionMetrics":[{				"leftIndex":0,				"partition":0,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":9,			"partitionMetrics":[{				"leftIndex":0,				"partition":9,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":8,			"partitionMetrics":[{				"leftIndex":0,				"partition":8,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":7,			"partitionMetrics":[{				"leftIndex":0,				"partition":7,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":6,			"partitionMetrics":[{				"leftIndex":0,				"partition":6,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":5,			"partitionMetrics":[{				"leftIndex":0,				"partition":5,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":4,			"partitionMetrics":[{				"leftIndex":0,				"partition":4,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		}],		"topic":"__group_coordinators"	}],	"message":"SUCCESS"}
```
75.裸接口
------
- 接口路径 /manage/store/file
- 请求类型 GET
- 功能描述 裸接口
- 参数 

|    字段    |   类型   | 备注  |
|:--------:|:------:|:---:|
|   file   | String |     |
| position |  long  |     |
|  length  |  int   |     |
示例: 请求: 
```
curl -X GET http://localhost:50090/manage/store/file?file=abc&position=0&length=100
```
响应: 
```
{	"code":200,	"data":"LENGTH(4): 0\nPARTITION(2): 0\nINDEX(8): 0\nTERM(4): 0\nMAGIC(2): 0\nSYS(2): 0\nPRIORITY(1): 00(0)\nCLIENT_IP(16): \n\tHex: 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 \n\tString: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\nCLIENT_TIMESTAMP(8): 0 (1970-01-01 08:00:00.000)\nSTORAGE_TIMESTAMP(4): 0 (1970-01-01 08:00:00.000)\nCRC(8): 0\nFLAG(2): 0\nBODY(0): \n\tHex: \n\tString: \nBIZ_ID(0): \n\tHex: \n\tString: \nPROPERTY(0): \n\tHex: \n\tString: \nEXPAND(0): \n\tHex: \n\tString: \nAPP(0): \n\tHex: \n\tString: \n",	"message":"SUCCESS"}
```
76.设置ackindex
-------------
- 接口路径 /manage/topic/:topic/app/:app/partition/:partition/ack
- 请求类型 PUT
- 功能描述 设置ackindex
- 参数 

|    字段     |   类型   | 备注  |
|:---------:|:------:|:---:|
|   topic   | String |     |
|    app    | String |     |
| partition | short  |     |
|   index   |  long  |     |
示例: 
77.restoreElectionMetadata
--------------------------
- 接口路径 /manage/election/metadata/restore
- 请求类型 GET
- 功能描述 无
- 参数 无


示例: 请求: 
```
curl -X GET http://localhost:50090/manage/election/metadata/restore
```
响应: 
```
{	"code":200,	"message":"SUCCESS"}
```
78.partitionMetric
------------------
- 接口路径 /manage/topic/:topic/partition/:partition/store/metric
- 请求类型 GET
- 功能描述 无
- 参数 无


示例: 请求: 
```
curl -X GET http://localhost:50090/manage/topic/:topic/partition/:partition/store/metric
```
响应: 
```
{	"code":500,	"message":"java.lang.NullPointerException"}
```
79.获取message，如果有积压消息返回积压，否则返回最后几条
---------------------------------
- 接口路径 /manage/topic/:topic/app/:app/message/view
- 请求类型 GET
- 功能描述 获取message，如果有积压消息返回积压，否则返回最后几条
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String |     |
|  app  | String |     |
| count |  int   |     |
示例: 请求: 
```
curl -X GET http://localhost:50090/manage/topic/test_topic_1/app/test_app/message/view?count=10
```
响应: 
```
{	"code":200,	"data":[],	"message":"SUCCESS"}
```
80.describe
-----------
- 接口路径 /manage/election/metadata/describe
- 请求类型 GET
- 功能描述 无
- 参数 无


示例: 请求: 
```
curl -X GET http://localhost:50090/manage/election/metadata/describe
```
响应: 
```
{	"code":200,	"data":"{{\"partitionGroupId\":0,\"topic\":\"test_topic_0\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752324,\"votedFor\":-1},{\"partitionGroupId\":2,\"topic\":\"test_topic_0\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752329,\"votedFor\":-1},{\"partitionGroupId\":0,\"topic\":\"test_topic_2\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752349,\"votedFor\":-1},{\"partitionGroupId\":1,\"topic\":\"test_topic_1\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752380,\"votedFor\":-1},{\"partitionGroupId\":1,\"topic\":\"test_topic_0\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752327,\"votedFor\":-1},{\"partitionGroupId\":0,\"topic\":\"test_topic_1\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752377,\"votedFor\":-1},{\"partitionGroupId\":4,\"topic\":\"test_topic_0\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752334,\"votedFor\":-1},{\"partitionGroupId\":2,\"topic\":\"test_topic_2\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752354,\"votedFor\":-1},{\"partitionGroupId\":3,\"topic\":\"test_topic_1\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752396,\"votedFor\":-1},{\"partitionGroupId\":0,\"topic\":\"test_topic_4\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752448,\"votedFor\":-1},{\"partitionGroupId\":1,\"topic\":\"test_topic_3\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752482,\"votedFor\":-1},{\"partitionGroupId\":3,\"topic\":\"test_topic_0\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752331,\"votedFor\":-1},{\"partitionGroupId\":1,\"topic\":\"test_topic_2\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752352,\"votedFor\":-1},{\"partitionGroupId\":2,\"topic\":\"test_topic_1\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752392,\"votedFor\":-1},{\"partitionGroupId\":0,\"topic\":\"test_topic_3\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752478,\"votedFor\":-1},{\"partitionGroupId\":6,\"topic\":\"test_topic_0\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752339,\"votedFor\":-1},{\"partitionGroupId\":4,\"topic\":\"test_topic_2\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752360,\"votedFor\":-1},{\"partitionGroupId\":5,\"topic\":\"test_topic_1\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752403,\"votedFor\":-1},{\"partitionGroupId\":2,\"topic\":\"test_topic_4\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752455,\"votedFor\":-1},{\"partitionGroupId\":3,\"topic\":\"test_topic_3\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752494,\"votedFor\":-1},{\"partitionGroupId\":5,\"topic\":\"test_topic_0\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752337,\"votedFor\":-1},{\"partitionGroupId\":3,\"topic\":\"test_topic_2\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752357,\"votedFor\":-1},{\"partitionGroupId\":4,\"topic\":\"test_topic_1\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752400,\"votedFor\":-1},{\"partitionGroupId\":1,\"topic\":\"test_topic_4\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752452,\"votedFor\":-1},{\"partitionGroupId\":2,\"topic\":\"test_topic_3\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752486,\"votedFor\":-1},{\"partitionGroupId\":8,\"topic\":\"test_topic_0\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752344,\"votedFor\":-1},{\"partitionGroupId\":6,\"topic\":\"test_topic_2\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752366,\"votedFor\":-1},{\"partitionGroupId\":7,\"topic\":\"test_topic_1\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752409,\"votedFor\":-1},{\"partitionGroupId\":4,\"topic\":\"test_topic_4\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752461,\"votedFor\":-1},{\"partitionGroupId\":5,\"topic\":\"test_topic_3\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752501,\"votedFor\":-1},{\"partitionGroupId\":7,\"topic\":\"test_topic_0\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752341,\"votedFor\":-1},{\"partitionGroupId\":5,\"topic\":\"test_topic_2\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752363,\"votedFor\":-1},{\"partitionGroupId\":6,\"topic\":\"test_topic_1\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752406,\"votedFor\":-1},{\"partitionGroupId\":3,\"topic\":\"test_topic_4\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752459,\"votedFor\":-1},{\"partitionGroupId\":4,\"topic\":\"test_topic_3\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752497,\"votedFor\":-1},{\"partitionGroupId\":8,\"topic\":\"test_topic_2\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752371,\"votedFor\":-1},{\"partitionGroupId\":9,\"topic\":\"test_topic_1\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752415,\"votedFor\":-1},{\"partitionGroupId\":6,\"topic\":\"test_topic_4\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752467,\"votedFor\":-1},{\"partitionGroupId\":7,\"topic\":\"test_topic_3\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752507,\"votedFor\":-1},{\"partitionGroupId\":9,\"topic\":\"test_topic_0\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752346,\"votedFor\":-1},{\"partitionGroupId\":7,\"topic\":\"test_topic_2\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752368,\"votedFor\":-1},{\"partitionGroupId\":8,\"topic\":\"test_topic_1\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752412,\"votedFor\":-1},{\"partitionGroupId\":5,\"topic\":\"test_topic_4\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752464,\"votedFor\":-1},{\"partitionGroupId\":6,\"topic\":\"test_topic_3\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752504,\"votedFor\":-1},{\"partitionGroupId\":8,\"topic\":\"test_topic_4\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752473,\"votedFor\":-1},{\"partitionGroupId\":9,\"topic\":\"test_topic_3\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752514,\"votedFor\":-1},{\"partitionGroupId\":9,\"topic\":\"test_topic_2\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752374,\"votedFor\":-1},{\"partitionGroupId\":7,\"topic\":\"test_topic_4\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752470,\"votedFor\":-1},{\"partitionGroupId\":8,\"topic\":\"test_topic_3\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752510,\"votedFor\":-1},{\"partitionGroupId\":2,\"topic\":\"test_topic\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752581,\"votedFor\":-1},{\"partitionGroupId\":3,\"topic\":\"test_topic\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752585,\"votedFor\":-1},{\"partitionGroupId\":4,\"topic\":\"test_topic\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752595,\"votedFor\":-1},{\"partitionGroupId\":5,\"topic\":\"test_topic\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752601,\"votedFor\":-1},{\"partitionGroupId\":0,\"topic\":\"test_topic\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752563,\"votedFor\":-1},{\"partitionGroupId\":1,\"topic\":\"test_topic\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752578,\"votedFor\":-1},{\"partitionGroupId\":6,\"topic\":\"test_topic\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752605,\"votedFor\":-1},{\"partitionGroupId\":7,\"topic\":\"test_topic\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752608,\"votedFor\":-1},{\"partitionGroupId\":8,\"topic\":\"test_topic\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752612,\"votedFor\":-1},{\"partitionGroupId\":9,\"topic\":\"test_topic\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752615,\"votedFor\":-1},{\"partitionGroupId\":0,\"topic\":\"__group_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752517,\"votedFor\":-1},{\"partitionGroupId\":3,\"topic\":\"__group_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752526,\"votedFor\":-1},{\"partitionGroupId\":4,\"topic\":\"__group_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752529,\"votedFor\":-1},{\"partitionGroupId\":1,\"topic\":\"__group_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752520,\"votedFor\":-1},{\"partitionGroupId\":2,\"topic\":\"__group_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752523,\"votedFor\":-1},{\"partitionGroupId\":7,\"topic\":\"__group_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752539,\"votedFor\":-1},{\"partitionGroupId\":8,\"topic\":\"__group_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752541,\"votedFor\":-1},{\"partitionGroupId\":5,\"topic\":\"__group_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752532,\"votedFor\":-1},{\"partitionGroupId\":6,\"topic\":\"__group_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752535,\"votedFor\":-1},{\"partitionGroupId\":9,\"topic\":\"__group_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752559,\"votedFor\":-1},{\"partitionGroupId\":9,\"topic\":\"test_topic_4\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752476,\"votedFor\":-1},{\"partitionGroupId\":9,\"topic\":\"__transaction_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752445,\"votedFor\":-1},{\"partitionGroupId\":8,\"topic\":\"__transaction_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752442,\"votedFor\":-1},{\"partitionGroupId\":7,\"topic\":\"__transaction_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752439,\"votedFor\":-1},{\"partitionGroupId\":6,\"topic\":\"__transaction_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752436,\"votedFor\":-1},{\"partitionGroupId\":5,\"topic\":\"__transaction_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752434,\"votedFor\":-1},{\"partitionGroupId\":4,\"topic\":\"__transaction_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752431,\"votedFor\":-1},{\"partitionGroupId\":3,\"topic\":\"__transaction_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752428,\"votedFor\":-1},{\"partitionGroupId\":2,\"topic\":\"__transaction_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752426,\"votedFor\":-1},{\"partitionGroupId\":1,\"topic\":\"__transaction_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752423,\"votedFor\":-1},{\"partitionGroupId\":0,\"topic\":\"__transaction_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.16.231:50089\",\"nodeId\":1562321590,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1562321590,\"learners\":[],\"localNodeId\":1562321590,\"timestamp\":1562321752420,\"votedFor\":-1}}",	"message":"SUCCESS"}
```
81.返回最大ack索引
------------
- 接口路径 /manage/topic/:topic/app/:app/partition/:partition/ack
- 请求类型 GET
- 功能描述 返回最大ack索引
- 参数 

|    字段     |   类型   | 备注  |
|:---------:|:------:|:---:|
|   topic   | String |     |
|    app    | String |     |
| partition | short  |     |
示例: 请求: 
```
curl -X GET http://localhost:50090/manage/topic/test_topic_1/app/test_app/partition/0/ack
```
响应: 
```
{	"code":200,	"data":0,	"message":"SUCCESS"}
```
