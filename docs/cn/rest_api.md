1.获取所有主题的监控信息
-------------
- 接口路径 /monitor/topics
- 请求类型 GET
- 功能描述 获取所有主题的监控信息
- 参数 

|    字段    | 类型  |  备注  |
|:--------:|:---:|:----:|
|   page   | int |  页数  |
| pageSize | int | 每页大小 |
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
{	"code":200,	"data":{		"commitDate":"22.07.2019 @ 15:15:30 CST",		"revision":"626d7a5",		"startupTime":1563793192389	},	"message":"SUCCESS"}
```
3.获取主题连接详细监控信息
--------------
- 接口路径 /monitor/topic/:topic/connections/detail
- 请求类型 GET
- 功能描述 获取主题连接详细监控信息
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
4.获取主题下应用分区的监控信息
----------------
- 接口路径 /monitor/topic/:topic/app/:app/partition/:partition
- 请求类型 GET
- 功能描述 获取主题下应用分区的监控信息
- 参数 

|    字段     |   类型   | 备注  |
|:---------:|:------:|:---:|
|   topic   | String | 主题  |
|    app    | String | 应用  |
| partition | short  | 分区  |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/app/test_app/partition/0
```
响应: 
```
{	"code":200,	"data":{		"app":"test_app",		"deQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"enQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partition":0,		"topic":"test_topic_1"	},	"message":"SUCCESS"}
```
5.是否有写权限
--------
- 接口路径 /monitor/topic/:topic/app/:app/writable
- 请求类型 GET
- 功能描述 是否有写权限
- 参数 

|   字段    |   类型   | 备注  |
|:-------:|:------:|:---:|
|  topic  | String | 主题  |
|   app   | String | 应用  |
| address | String | 地址  |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/app/test_app/writable?address=null
```
响应: 
```
{	"code":200,	"data":{		"joyQueueCode":"SUCCESS",		"success":true	},	"message":"SUCCESS"}
```
6.获取主题下应用分区组的生产监控信息
-------------------
- 接口路径 /monitor/topic/:topic/app/:app/producer/partitionGroup/:partitionGroupId
- 请求类型 GET
- 功能描述 获取主题下应用分区组的生产监控信息
- 参数 

|        字段        |   类型   | 备注  |
|:----------------:|:------:|:---:|
|      topic       | String | 主题  |
|       app        | String | 应用  |
| partitionGroupId |  int   | 分区组 |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/app/test_app/producer/partitionGroup/0
```
响应: 
```
{	"code":200,	"data":{		"app":"test_app",		"enQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partitionGroupId":0,		"topic":"test_topic_1"	},	"message":"SUCCESS"}
```
7.获取主题下应用的所有分区的监控信息
-------------------
- 接口路径 /monitor/topic/:topic/app/:app/partitions
- 请求类型 GET
- 功能描述 获取主题下应用的所有分区的监控信息
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String | 主题  |
|  app  | String | 应用  |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/app/test_app/partitions
```
响应: 
```
{	"code":200,	"data":[],	"message":"SUCCESS"}
```
8.获取主题下应用分区的消费监控信息
------------------
- 接口路径 /monitor/topic/:topic/app/:app/consumer/partition/:partition
- 请求类型 GET
- 功能描述 获取主题下应用分区的消费监控信息
- 参数 

|    字段     |   类型   | 备注  |
|:---------:|:------:|:---:|
|   topic   | String | 主题  |
|    app    | String | 应用  |
| partition | short  | 分区  |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/app/test_app/consumer/partition/0
```
响应: 
```
{	"code":200,	"data":{		"app":"test_app",		"deQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partition":0,		"pending":{			"count":0		},		"topic":"test_topic_1"	},	"message":"SUCCESS"}
```
9.获取主题下应用的生产者连接详细监控信息
---------------------
- 接口路径 /monitor/topic/:topic/app/:app/producer/connections/detail
- 请求类型 GET
- 功能描述 获取主题下应用的生产者连接详细监控信息
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
10.获取主题下应用分组的消费监控信息
-------------------
- 接口路径 /monitor/topic/:topic/app/:app/consumer/partitionGroup/:partitionGroupId
- 请求类型 GET
- 功能描述 获取主题下应用分组的消费监控信息
- 参数 

|        字段        |   类型   | 备注  |
|:----------------:|:------:|:---:|
|      topic       | String | 主题  |
|       app        | String | 应用  |
| partitionGroupId |  int   | 分区组 |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/app/test_app/consumer/partitionGroup/0
```
响应: 
```
{	"code":200,	"data":{		"app":"test_app",		"deQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partitionGroupId":0,		"pending":{			"count":0		},		"topic":"test_topic_1"	},	"message":"SUCCESS"}
```
11.获取组的协调者详情
------------
- 接口路径 /monitor/coordinator/group/:groupId/detail
- 请求类型 GET
- 功能描述 获取组的协调者详情
- 参数 

|   字段    |   类型   | 备注  |
|:-------:|:------:|:---:|
| groupId | String |  组  |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/coordinator/group/abc/detail
```
响应: 
```
{	"code":200,	"data":{		"current":{			"address":"10.0.5.244:50088",			"backEndPort":50089,			"dataCenter":"UNKNOWN",			"id":1563793189,			"ip":"10.0.5.244",			"managerPort":50091,			"monitorPort":50090,			"permission":"FULL",			"port":50088,			"retryType":"RemoteRetry"		},		"partitionGroup":4,		"replicas":[],		"topic":{			"code":"__group_coordinators",			"fullName":"__group_coordinators",			"namespace":""		}	},	"message":"SUCCESS"}
```
12.获取主题下应用所有分区的生产监控信息
---------------------
- 接口路径 /monitor/topic/:topic/app/:app/producer/partitions
- 请求类型 GET
- 功能描述 获取主题下应用所有分区的生产监控信息
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String | 主题  |
|  app  | String | 应用  |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/app/test_app/producer/partitions
```
响应: 
```
{	"code":200,	"data":[{		"app":"test_app",		"enQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partition":5,		"topic":"test_topic_1"	},{		"app":"test_app",		"enQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partition":6,		"topic":"test_topic_1"	},{		"app":"test_app",		"enQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partition":3,		"topic":"test_topic_1"	},{		"app":"test_app",		"enQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partition":4,		"topic":"test_topic_1"	},{		"app":"test_app",		"enQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partition":9,		"topic":"test_topic_1"	},{		"app":"test_app",		"enQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partition":7,		"topic":"test_topic_1"	},{		"app":"test_app",		"enQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partition":8,		"topic":"test_topic_1"	},{		"app":"test_app",		"enQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partition":1,		"topic":"test_topic_1"	},{		"app":"test_app",		"enQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partition":2,		"topic":"test_topic_1"	},{		"app":"test_app",		"enQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partition":0,		"topic":"test_topic_1"	}],	"message":"SUCCESS"}
```
13.获取主题下应用所有分区组的生产监控信息
----------------------
- 接口路径 /monitor/topic/:topic/app/:app/producer/partitionGroups
- 请求类型 GET
- 功能描述 获取主题下应用所有分区组的生产监控信息
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String | 主题  |
|  app  | String | 应用  |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/app/test_app/producer/partitionGroups
```
响应: 
```
{	"code":200,	"data":[{		"app":"test_app",		"enQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partitionGroupId":5,		"topic":"test_topic_1"	},{		"app":"test_app",		"enQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partitionGroupId":6,		"topic":"test_topic_1"	},{		"app":"test_app",		"enQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partitionGroupId":3,		"topic":"test_topic_1"	},{		"app":"test_app",		"enQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partitionGroupId":4,		"topic":"test_topic_1"	},{		"app":"test_app",		"enQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partitionGroupId":9,		"topic":"test_topic_1"	},{		"app":"test_app",		"enQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partitionGroupId":7,		"topic":"test_topic_1"	},{		"app":"test_app",		"enQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partitionGroupId":8,		"topic":"test_topic_1"	},{		"app":"test_app",		"enQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partitionGroupId":1,		"topic":"test_topic_1"	},{		"app":"test_app",		"enQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partitionGroupId":2,		"topic":"test_topic_1"	},{		"app":"test_app",		"enQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partitionGroupId":0,		"topic":"test_topic_1"	}],	"message":"SUCCESS"}
```
14.获取主题下应用所有分区组的监控信息
--------------------
- 接口路径 /monitor/topic/:topic/app/:app/partitionGroups
- 请求类型 GET
- 功能描述 获取主题下应用所有分区组的监控信息
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String | 主题  |
|  app  | String | 应用  |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/app/test_app/partitionGroups
```
响应: 
```
{	"code":200,	"data":[],	"message":"SUCCESS"}
```
15.获取主题下应用的消费者连接详细监控信息
----------------------
- 接口路径 /monitor/topic/:topic/app/:app/consumer/connections/detail
- 请求类型 GET
- 功能描述 获取主题下应用的消费者连接详细监控信息
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
16.获取扩展监控信息，包括额外的积压信息等
----------------------
- 接口路径 /metrics
- 请求类型 GET
- 功能描述 获取扩展监控信息，包括额外的积压信息等
- 参数 

|    字段     |  类型  |     备注      |
|:---------:|:----:|:-----------:|
| timeStamp | long | 时间戳，会写回到返回值 |
示例: 请求: 
```
curl -X GET http://localhost:50090/metrics?timeStamp=19395935
```
响应: 
```
{	"code":200,	"data":{		"brokerId":1563793189,		"brokerStat":{			"brokerId":1563793189,			"connectionStat":{				"connection":0,				"connectionMap":{},				"consumer":0,				"producer":0			},			"deQueueStat":{				"avg":0.0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"total":0,				"totalSize":0,				"totalTraffic":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"enQueueStat":{				"avg":0.0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"total":0,				"totalSize":0,				"totalTraffic":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"replicationStat":{				"appendStat":{					"avg":0.0,					"max":0.0,					"min":0.0,					"oneMinuteRate":0,					"size":0,					"total":0,					"totalSize":0,					"totalTraffic":0,					"tp90":0.0,					"tp99":0.0,					"tps":0,					"traffic":0				},				"partitionGroup":0,				"replicaStat":{					"avg":0.0,					"max":0.0,					"min":0.0,					"oneMinuteRate":0,					"size":0,					"total":0,					"totalSize":0,					"totalTraffic":0,					"tp90":0.0,					"tp99":0.0,					"tps":0,					"traffic":0				}			},			"topicStats":{				"test_topic_1":{					"appStats":{						"test_app":{							"app":"test_app",							"connectionStat":{								"connection":0,								"connectionMap":{},								"consumer":0,								"producer":0							},							"consumerStat":{								"app":"test_app",								"connectionStat":{									"connection":0,									"connectionMap":{},									"consumer":0,									"producer":0								},								"deQueueStat":{									"avg":0.0,									"max":0.0,									"min":0.0,									"oneMinuteRate":0,									"size":0,									"total":0,									"totalSize":0,									"totalTraffic":0,									"tp90":0.0,									"tp99":0.0,									"tps":0,									"traffic":0								},								"partitionGroupStatMap":{0:{										"app":"test_app",										"deQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"enQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"partitionGroup":0,										"partitionStatMap":{},										"replicationStat":{											"appendStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											},											"partitionGroup":0,											"replicaStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											}										},										"topic":"test_topic_1"									}								},								"retryStat":{									"failure":{										"avg":0.0,										"count":0,										"max":0.0,										"meanRate":0,										"min":0.0,										"oneMinuteRate":0,										"tp75":0.0,										"tp90":0.0,										"tp95":0.0,										"tp99":0.0,										"tp999":0.0									},									"success":{										"avg":0.0,										"count":0,										"max":0.0,										"meanRate":0,										"min":0.0,										"oneMinuteRate":0,										"tp75":0.0,										"tp90":0.0,										"tp95":0.0,										"tp99":0.0,										"tp999":0.0									}								},								"topic":"test_topic_1"							},							"partitionGroupStatMap":{},							"producerStat":{								"app":"test_app",								"connectionStat":{									"connection":0,									"connectionMap":{},									"consumer":0,									"producer":0								},								"enQueueStat":{									"avg":0.0,									"max":0.0,									"min":0.0,									"oneMinuteRate":0,									"size":0,									"total":0,									"totalSize":0,									"totalTraffic":0,									"tp90":0.0,									"tp99":0.0,									"tps":0,									"traffic":0								},								"partitionGroupStatMap":{0:{										"app":"test_app",										"deQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"enQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"partitionGroup":0,										"partitionStatMap":{0:{												"app":"test_app",												"deQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"enQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"partition":0,												"topic":"test_topic_1"											}										},										"replicationStat":{											"appendStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											},											"partitionGroup":0,											"replicaStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											}										},										"topic":"test_topic_1"									},1:{										"app":"test_app",										"deQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"enQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"partitionGroup":1,										"partitionStatMap":{1:{												"app":"test_app",												"deQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"enQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"partition":1,												"topic":"test_topic_1"											}										},										"replicationStat":{											"appendStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											},											"partitionGroup":0,											"replicaStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											}										},										"topic":"test_topic_1"									},2:{										"app":"test_app",										"deQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"enQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"partitionGroup":2,										"partitionStatMap":{2:{												"app":"test_app",												"deQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"enQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"partition":2,												"topic":"test_topic_1"											}										},										"replicationStat":{											"appendStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											},											"partitionGroup":0,											"replicaStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											}										},										"topic":"test_topic_1"									},3:{										"app":"test_app",										"deQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"enQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"partitionGroup":3,										"partitionStatMap":{3:{												"app":"test_app",												"deQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"enQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"partition":3,												"topic":"test_topic_1"											}										},										"replicationStat":{											"appendStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											},											"partitionGroup":0,											"replicaStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											}										},										"topic":"test_topic_1"									},4:{										"app":"test_app",										"deQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"enQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"partitionGroup":4,										"partitionStatMap":{4:{												"app":"test_app",												"deQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"enQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"partition":4,												"topic":"test_topic_1"											}										},										"replicationStat":{											"appendStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											},											"partitionGroup":0,											"replicaStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											}										},										"topic":"test_topic_1"									},5:{										"app":"test_app",										"deQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"enQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"partitionGroup":5,										"partitionStatMap":{5:{												"app":"test_app",												"deQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"enQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"partition":5,												"topic":"test_topic_1"											}										},										"replicationStat":{											"appendStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											},											"partitionGroup":0,											"replicaStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											}										},										"topic":"test_topic_1"									},6:{										"app":"test_app",										"deQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"enQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"partitionGroup":6,										"partitionStatMap":{6:{												"app":"test_app",												"deQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"enQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"partition":6,												"topic":"test_topic_1"											}										},										"replicationStat":{											"appendStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											},											"partitionGroup":0,											"replicaStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											}										},										"topic":"test_topic_1"									},7:{										"app":"test_app",										"deQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"enQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"partitionGroup":7,										"partitionStatMap":{7:{												"app":"test_app",												"deQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"enQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"partition":7,												"topic":"test_topic_1"											}										},										"replicationStat":{											"appendStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											},											"partitionGroup":0,											"replicaStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											}										},										"topic":"test_topic_1"									},8:{										"app":"test_app",										"deQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"enQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"partitionGroup":8,										"partitionStatMap":{8:{												"app":"test_app",												"deQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"enQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"partition":8,												"topic":"test_topic_1"											}										},										"replicationStat":{											"appendStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											},											"partitionGroup":0,											"replicaStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											}										},										"topic":"test_topic_1"									},9:{										"app":"test_app",										"deQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"enQueueStat":{											"avg":0.0,											"max":0.0,											"min":0.0,											"oneMinuteRate":0,											"size":0,											"total":0,											"totalSize":0,											"totalTraffic":0,											"tp90":0.0,											"tp99":0.0,											"tps":0,											"traffic":0										},										"partitionGroup":9,										"partitionStatMap":{9:{												"app":"test_app",												"deQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"enQueueStat":{													"avg":0.0,													"max":0.0,													"min":0.0,													"oneMinuteRate":0,													"size":0,													"total":0,													"totalSize":0,													"totalTraffic":0,													"tp90":0.0,													"tp99":0.0,													"tps":0,													"traffic":0												},												"partition":9,												"topic":"test_topic_1"											}										},										"replicationStat":{											"appendStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											},											"partitionGroup":0,											"replicaStat":{												"avg":0.0,												"max":0.0,												"min":0.0,												"oneMinuteRate":0,												"size":0,												"total":0,												"totalSize":0,												"totalTraffic":0,												"tp90":0.0,												"tp99":0.0,												"tps":0,												"traffic":0											}										},										"topic":"test_topic_1"									}								},								"topic":"test_topic_1"							},							"topic":"test_topic_1"						}					},					"connectionStat":{						"connection":0,						"connectionMap":{},						"consumer":0,						"producer":0					},					"deQueueStat":{						"avg":0.0,						"max":0.0,						"min":0.0,						"oneMinuteRate":0,						"size":0,						"total":0,						"totalSize":0,						"totalTraffic":0,						"tp90":0.0,						"tp99":0.0,						"tps":0,						"traffic":0					},					"enQueueStat":{						"avg":0.0,						"max":0.0,						"min":0.0,						"oneMinuteRate":0,						"size":0,						"total":0,						"totalSize":0,						"totalTraffic":0,						"tp90":0.0,						"tp99":0.0,						"tps":0,						"traffic":0					},					"partitionGroupStatMap":{},					"topic":"test_topic_1"				}			}		},		"heap":{			"committed":271056896,			"init":268435456,			"max":3817865216,			"used":88770272		},		"nonHeap":{			"committed":98893824,			"init":2555904,			"max":-1,			"used":96666648		},		"timeStamp":19395935,		"topicPendingStatMap":{			"test_topic_1":{				"pending":0,				"pendingStatSubMap":{					"test_app":{						"app":"test_app",						"pending":0,						"pendingStatSubMap":{0:{								"app":"test_app",								"partitionGroup":0,								"pending":0,								"pendingStatSubMap":{0:0								},								"topic":"test_topic_1"							},1:{								"app":"test_app",								"partitionGroup":1,								"pending":0,								"pendingStatSubMap":{1:0								},								"topic":"test_topic_1"							},2:{								"app":"test_app",								"partitionGroup":2,								"pending":0,								"pendingStatSubMap":{2:0								},								"topic":"test_topic_1"							},3:{								"app":"test_app",								"partitionGroup":3,								"pending":0,								"pendingStatSubMap":{3:0								},								"topic":"test_topic_1"							},4:{								"app":"test_app",								"partitionGroup":4,								"pending":0,								"pendingStatSubMap":{4:0								},								"topic":"test_topic_1"							},5:{								"app":"test_app",								"partitionGroup":5,								"pending":0,								"pendingStatSubMap":{5:0								},								"topic":"test_topic_1"							},6:{								"app":"test_app",								"partitionGroup":6,								"pending":0,								"pendingStatSubMap":{6:0								},								"topic":"test_topic_1"							},7:{								"app":"test_app",								"partitionGroup":7,								"pending":0,								"pendingStatSubMap":{7:0								},								"topic":"test_topic_1"							},8:{								"app":"test_app",								"partitionGroup":8,								"pending":0,								"pendingStatSubMap":{8:0								},								"topic":"test_topic_1"							},9:{								"app":"test_app",								"partitionGroup":9,								"pending":0,								"pendingStatSubMap":{9:0								},								"topic":"test_topic_1"							}						},						"topic":"test_topic_1"					}				},				"topic":"test_topic_1"			}		}	},	"message":"SUCCESS"}
```
17.获取主题下应用所有分区的消费监控信息
---------------------
- 接口路径 /monitor/topic/:topic/app/:app/consumer/partitions
- 请求类型 GET
- 功能描述 获取主题下应用所有分区的消费监控信息
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String | 主题  |
|  app  | String | 应用  |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/app/test_app/consumer/partitions
```
响应: 
```
{	"code":200,	"data":[{		"app":"test_app",		"deQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partition":5,		"pending":{			"count":0		},		"topic":"test_topic_1"	},{		"app":"test_app",		"deQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partition":6,		"pending":{			"count":0		},		"topic":"test_topic_1"	},{		"app":"test_app",		"deQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partition":3,		"pending":{			"count":0		},		"topic":"test_topic_1"	},{		"app":"test_app",		"deQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partition":4,		"pending":{			"count":0		},		"topic":"test_topic_1"	},{		"app":"test_app",		"deQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partition":9,		"pending":{			"count":0		},		"topic":"test_topic_1"	},{		"app":"test_app",		"deQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partition":7,		"pending":{			"count":0		},		"topic":"test_topic_1"	},{		"app":"test_app",		"deQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partition":8,		"pending":{			"count":0		},		"topic":"test_topic_1"	},{		"app":"test_app",		"deQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partition":1,		"pending":{			"count":0		},		"topic":"test_topic_1"	},{		"app":"test_app",		"deQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partition":2,		"pending":{			"count":0		},		"topic":"test_topic_1"	},{		"app":"test_app",		"deQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partition":0,		"pending":{			"count":0		},		"topic":"test_topic_1"	}],	"message":"SUCCESS"}
```
18.获取主题下应用的生产监控信息
-----------------
- 接口路径 /monitor/topic/:topic/app/:app/producer
- 请求类型 GET
- 功能描述 获取主题下应用的生产监控信息
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String | 主题  |
|  app  | String | 应用  |
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

|    字段     |   类型    |    备注    |
|:---------:|:-------:|:--------:|
| namespace | String  |   作用域    |
|  groupId  | String  |    组     |
|   topic   | String  |    主题    |
| isFormat  | boolean | 是否格式化元数据 |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/coordinator/namespace/joyqueue/group/abc/members?topic=test_topic_1&isFormat=true
```
响应: 
```
{	"code":200,	"message":"SUCCESS"}
```
20.获取所有生产监控信息
-------------
- 接口路径 /monitor/producers
- 请求类型 GET
- 功能描述 获取所有生产监控信息
- 参数 

|    字段    | 类型  |  备注  |
|:--------:|:---:|:----:|
|   page   | int |  页数  |
| pageSize | int | 每页数量 |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/producers?page=0&pageSize=10
```
响应: 
```
{	"code":200,	"data":{		"data":[],		"page":0,		"pageSize":10,		"pages":1,		"total":1	},	"message":"SUCCESS"}
```
21.是否有读权限
---------
- 接口路径 /monitor/topic/:topic/app/:app/readable
- 请求类型 GET
- 功能描述 是否有读权限
- 参数 

|   字段    |   类型   | 备注  |
|:-------:|:------:|:---:|
|  topic  | String | 主题  |
|   app   | String | 应用  |
| address | String | 地址  |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/app/test_app/readable?address=null
```
响应: 
```
{	"code":200,	"data":{		"joyQueueCode":"SUCCESS",		"success":true	},	"message":"SUCCESS"}
```
22.获取主题下应用分区的生产监控信息
-------------------
- 接口路径 /monitor/topic/:topic/app/:app/producer/partition/:partition
- 请求类型 GET
- 功能描述 获取主题下应用分区的生产监控信息
- 参数 

|    字段     |   类型   | 备注  |
|:---------:|:------:|:---:|
|   topic   | String | 主题  |
|    app    | String | 应用  |
| partition | short  | 分区  |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/app/test_app/producer/partition/0
```
响应: 
```
{	"code":200,	"data":{		"app":"test_app",		"enQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partition":0,		"topic":"test_topic_1"	},	"message":"SUCCESS"}
```
23.获取主题消费者连接详细监控信息
------------------
- 接口路径 /monitor/topic/:topic/consumer/connections/detail
- 请求类型 GET
- 功能描述 获取主题消费者连接详细监控信息
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
24.获取所有消费监控信息
-------------
- 接口路径 /monitor/consumers
- 请求类型 GET
- 功能描述 获取所有消费监控信息
- 参数 

|    字段    | 类型  |  备注  |
|:--------:|:---:|:----:|
|   page   | int |  页数  |
| pageSize | int | 每页数量 |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/consumers?page=0&pageSize=10
```
响应: 
```
{	"code":200,	"data":{		"data":[],		"page":0,		"pageSize":10,		"pages":1,		"total":1	},	"message":"SUCCESS"}
```
25.获取监控信息
---------
- 接口路径 /monitor/broker
- 请求类型 GET
- 功能描述 获取监控信息
- 参数 无


示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/broker
```
响应: 
```
{	"code":200,	"data":{		"bufferPoolMonitorInfo":{			"directUsed":"0",			"maxMemorySize":"3.2 GB",			"mmpUsed":"0",			"plMonitorInfos":[				{					"bufferSize":"128 MB",					"cached":"384 MB",					"totalSize":"384 MB",					"usedPreLoad":"0"				},				{					"bufferSize":"512 kB",					"cached":"1.5 MB",					"totalSize":"1.5 MB",					"usedPreLoad":"0"				}			],			"plUsed":"385.5 MB",			"used":"385.5 MB"		},		"connection":{			"consumer":0,			"producer":0,			"total":0		},		"deQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"election":{			"started":true		},		"enQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"nameServer":{			"started":true		},		"replication":{			"appendStat":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partitionGroup":0,			"replicaStat":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"started":true		},		"store":{			"freeSpace":"40.6 GB",			"started":true,			"totalSpace":"233.5 GB"		}	},	"message":"SUCCESS"}
```
26.获取连接数监控信息
------------
- 接口路径 /monitor/connections
- 请求类型 GET
- 功能描述 获取连接数监控信息
- 参数 无


示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/connections
```
响应: 
```
{	"code":200,	"data":{		"consumer":0,		"producer":0,		"total":0	},	"message":"SUCCESS"}
```
27.获取主题下应用分区组的监控信息
------------------
- 接口路径 /monitor/topic/:topic/app/:app/partitionGroup/:partitionGroup
- 请求类型 GET
- 功能描述 获取主题下应用分区组的监控信息
- 参数 

|       字段       |   类型   | 备注  |
|:--------------:|:------:|:---:|
|     topic      | String | 主题  |
|      app       | String | 应用  |
| partitionGroup |  int   | 分区组 |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/app/test_app/partitionGroup/0
```
响应: 
```
{	"code":200,	"data":{		"app":"test_app",		"deQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"enQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"flushPosition":0,		"indexPosition":0,		"leftPosition":0,		"partitionGroup":0,		"replication":{			"appendStat":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partitionGroup":0,			"replicaStat":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"started":false		},		"replicationPosition":0,		"rightPosition":0,		"topic":"test_topic_1"	},	"message":"SUCCESS"}
```
28.获取主题下所有分区的监控信息
-----------------
- 接口路径 /monitor/topic/:topic/partitions
- 请求类型 GET
- 功能描述 获取主题下所有分区的监控信息
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String | 主题  |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/partitions
```
响应: 
```
{	"code":200,	"data":[],	"message":"SUCCESS"}
```
29.获取归档监控
---------
- 接口路径 /monitor/archive/info
- 请求类型 GET
- 功能描述 获取归档监控
- 参数 无


示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/archive/info
```
响应: 
```
{	"code":500,	"message":"java.lang.NullPointerException"}
```
30.获取主题下应用的连接数监控信息
------------------
- 接口路径 /monitor/topic/:topic/app/:app/connections
- 请求类型 GET
- 功能描述 获取主题下应用的连接数监控信息
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
31.获取主题下应用所有分区组的消费监控信息
----------------------
- 接口路径 /monitor/topic/:topic/app/:app/consumer/partitionGroups
- 请求类型 GET
- 功能描述 获取主题下应用所有分区组的消费监控信息
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String | 主题  |
|  app  | String | 应用  |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/app/test_app/consumer/partitionGroups
```
响应: 
```
{	"code":200,	"data":[{		"app":"test_app",		"deQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partitionGroupId":5,		"pending":{			"count":0		},		"topic":"test_topic_1"	},{		"app":"test_app",		"deQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partitionGroupId":6,		"pending":{			"count":0		},		"topic":"test_topic_1"	},{		"app":"test_app",		"deQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partitionGroupId":3,		"pending":{			"count":0		},		"topic":"test_topic_1"	},{		"app":"test_app",		"deQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partitionGroupId":4,		"pending":{			"count":0		},		"topic":"test_topic_1"	},{		"app":"test_app",		"deQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partitionGroupId":9,		"pending":{			"count":0		},		"topic":"test_topic_1"	},{		"app":"test_app",		"deQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partitionGroupId":7,		"pending":{			"count":0		},		"topic":"test_topic_1"	},{		"app":"test_app",		"deQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partitionGroupId":8,		"pending":{			"count":0		},		"topic":"test_topic_1"	},{		"app":"test_app",		"deQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partitionGroupId":1,		"pending":{			"count":0		},		"topic":"test_topic_1"	},{		"app":"test_app",		"deQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partitionGroupId":2,		"pending":{			"count":0		},		"topic":"test_topic_1"	},{		"app":"test_app",		"deQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partitionGroupId":0,		"pending":{			"count":0		},		"topic":"test_topic_1"	}],	"message":"SUCCESS"}
```
32.获取多个列表的监控信息
--------------
- 接口路径 /monitor/topics/search
- 请求类型 GET
- 功能描述 获取多个列表的监控信息
- 参数 

|   字段   |      类型      |  备注  |
|:------:|:------------:|:----:|
| topics | List<String> | 主题列表 |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topics/search?topics=["a","b"]
```
响应: 
```

```
33.获取主题的监控信息
------------
- 接口路径 /monitor/topic/:topic
- 请求类型 GET
- 功能描述 获取主题的监控信息
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
34.获取连接详细监控信息
-------------
- 接口路径 /monitor/connections/detail
- 请求类型 GET
- 功能描述 获取连接详细监控信息
- 参数 无


示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/connections/detail
```
响应: 
```
{	"code":200,	"data":{		"clients":[],		"consumer":0,		"producer":0,		"total":0	},	"message":"SUCCESS"}
```
35.获取主题下应用连接详细监控信息
------------------
- 接口路径 /monitor/topic/:topic/app/:app/connections/detail
- 请求类型 GET
- 功能描述 获取主题下应用连接详细监控信息
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
36.获取主题连接数监控信息
--------------
- 接口路径 /monitor/topic/:topic/connections
- 请求类型 GET
- 功能描述 获取主题连接数监控信息
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
37.获取发送归档数量
-----------
- 接口路径 /monitor/archive/send
- 请求类型 GET
- 功能描述 获取发送归档数量
- 参数 无


示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/archive/send
```
响应: 
```
{	"code":500,	"message":"java.lang.NullPointerException"}
```
38.获取主题元数据
----------
- 接口路径 /monitor/topic/:topic/metadata
- 请求类型 GET
- 功能描述 获取主题元数据
- 参数 

|    字段     |   类型    |                       备注                       |
|:---------:|:-------:|:----------------------------------------------:|
|   topic   | String  |                       主题                       |
| isCluster | boolean | true - 从ClusterManager获取，false - 从NameServer获取 |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/metadata?isCluster=null
```
响应: 
```
{	"code":200,	"data":{		"name":{			"code":"test_topic_1",			"fullName":"test_topic_1",			"namespace":""		},		"partitionGroups":{0:{				"brokers":{1563793189:{						"address":"10.0.5.244:50088",						"backEndPort":50089,						"dataCenter":"UNKNOWN",						"id":1563793189,						"ip":"10.0.5.244",						"managerPort":50091,						"monitorPort":50090,						"permission":"FULL",						"port":50088,						"retryType":"RemoteRetry"					}				},				"electType":"fix",				"group":0,				"id":"test_topic_1.0",				"isrs":[1563793189],				"leader":1563793189,				"leaderBroker":{					"address":"10.0.5.244:50088",					"backEndPort":50089,					"dataCenter":"UNKNOWN",					"id":1563793189,					"ip":"10.0.5.244",					"managerPort":50091,					"monitorPort":50090,					"permission":"FULL",					"port":50088,					"retryType":"RemoteRetry"				},				"learners":[],				"partitions":[0],				"recLeader":-1,				"replicas":[1563793189],				"term":0,				"topic":{					"code":"test_topic_1",					"fullName":"test_topic_1",					"namespace":""				}			},1:{				"brokers":{1563793189:{						"address":"10.0.5.244:50088",						"backEndPort":50089,						"dataCenter":"UNKNOWN",						"id":1563793189,						"ip":"10.0.5.244",						"managerPort":50091,						"monitorPort":50090,						"permission":"FULL",						"port":50088,						"retryType":"RemoteRetry"					}				},				"electType":"fix",				"group":1,				"id":"test_topic_1.1",				"isrs":[1563793189],				"leader":1563793189,				"leaderBroker":{					"address":"10.0.5.244:50088",					"backEndPort":50089,					"dataCenter":"UNKNOWN",					"id":1563793189,					"ip":"10.0.5.244",					"managerPort":50091,					"monitorPort":50090,					"permission":"FULL",					"port":50088,					"retryType":"RemoteRetry"				},				"learners":[],				"partitions":[1],				"recLeader":-1,				"replicas":[1563793189],				"term":0,				"topic":{					"code":"test_topic_1",					"fullName":"test_topic_1",					"namespace":""				}			},2:{				"brokers":{1563793189:{						"address":"10.0.5.244:50088",						"backEndPort":50089,						"dataCenter":"UNKNOWN",						"id":1563793189,						"ip":"10.0.5.244",						"managerPort":50091,						"monitorPort":50090,						"permission":"FULL",						"port":50088,						"retryType":"RemoteRetry"					}				},				"electType":"fix",				"group":2,				"id":"test_topic_1.2",				"isrs":[1563793189],				"leader":1563793189,				"leaderBroker":{					"address":"10.0.5.244:50088",					"backEndPort":50089,					"dataCenter":"UNKNOWN",					"id":1563793189,					"ip":"10.0.5.244",					"managerPort":50091,					"monitorPort":50090,					"permission":"FULL",					"port":50088,					"retryType":"RemoteRetry"				},				"learners":[],				"partitions":[2],				"recLeader":-1,				"replicas":[1563793189],				"term":0,				"topic":{					"code":"test_topic_1",					"fullName":"test_topic_1",					"namespace":""				}			},3:{				"brokers":{1563793189:{						"address":"10.0.5.244:50088",						"backEndPort":50089,						"dataCenter":"UNKNOWN",						"id":1563793189,						"ip":"10.0.5.244",						"managerPort":50091,						"monitorPort":50090,						"permission":"FULL",						"port":50088,						"retryType":"RemoteRetry"					}				},				"electType":"fix",				"group":3,				"id":"test_topic_1.3",				"isrs":[1563793189],				"leader":1563793189,				"leaderBroker":{					"address":"10.0.5.244:50088",					"backEndPort":50089,					"dataCenter":"UNKNOWN",					"id":1563793189,					"ip":"10.0.5.244",					"managerPort":50091,					"monitorPort":50090,					"permission":"FULL",					"port":50088,					"retryType":"RemoteRetry"				},				"learners":[],				"partitions":[3],				"recLeader":-1,				"replicas":[1563793189],				"term":0,				"topic":{					"code":"test_topic_1",					"fullName":"test_topic_1",					"namespace":""				}			},4:{				"brokers":{1563793189:{						"address":"10.0.5.244:50088",						"backEndPort":50089,						"dataCenter":"UNKNOWN",						"id":1563793189,						"ip":"10.0.5.244",						"managerPort":50091,						"monitorPort":50090,						"permission":"FULL",						"port":50088,						"retryType":"RemoteRetry"					}				},				"electType":"fix",				"group":4,				"id":"test_topic_1.4",				"isrs":[1563793189],				"leader":1563793189,				"leaderBroker":{					"address":"10.0.5.244:50088",					"backEndPort":50089,					"dataCenter":"UNKNOWN",					"id":1563793189,					"ip":"10.0.5.244",					"managerPort":50091,					"monitorPort":50090,					"permission":"FULL",					"port":50088,					"retryType":"RemoteRetry"				},				"learners":[],				"partitions":[4],				"recLeader":-1,				"replicas":[1563793189],				"term":0,				"topic":{					"code":"test_topic_1",					"fullName":"test_topic_1",					"namespace":""				}			},5:{				"brokers":{1563793189:{						"address":"10.0.5.244:50088",						"backEndPort":50089,						"dataCenter":"UNKNOWN",						"id":1563793189,						"ip":"10.0.5.244",						"managerPort":50091,						"monitorPort":50090,						"permission":"FULL",						"port":50088,						"retryType":"RemoteRetry"					}				},				"electType":"fix",				"group":5,				"id":"test_topic_1.5",				"isrs":[1563793189],				"leader":1563793189,				"leaderBroker":{					"address":"10.0.5.244:50088",					"backEndPort":50089,					"dataCenter":"UNKNOWN",					"id":1563793189,					"ip":"10.0.5.244",					"managerPort":50091,					"monitorPort":50090,					"permission":"FULL",					"port":50088,					"retryType":"RemoteRetry"				},				"learners":[],				"partitions":[5],				"recLeader":-1,				"replicas":[1563793189],				"term":0,				"topic":{					"code":"test_topic_1",					"fullName":"test_topic_1",					"namespace":""				}			},6:{				"brokers":{1563793189:{						"address":"10.0.5.244:50088",						"backEndPort":50089,						"dataCenter":"UNKNOWN",						"id":1563793189,						"ip":"10.0.5.244",						"managerPort":50091,						"monitorPort":50090,						"permission":"FULL",						"port":50088,						"retryType":"RemoteRetry"					}				},				"electType":"fix",				"group":6,				"id":"test_topic_1.6",				"isrs":[1563793189],				"leader":1563793189,				"leaderBroker":{					"address":"10.0.5.244:50088",					"backEndPort":50089,					"dataCenter":"UNKNOWN",					"id":1563793189,					"ip":"10.0.5.244",					"managerPort":50091,					"monitorPort":50090,					"permission":"FULL",					"port":50088,					"retryType":"RemoteRetry"				},				"learners":[],				"partitions":[6],				"recLeader":-1,				"replicas":[1563793189],				"term":0,				"topic":{					"code":"test_topic_1",					"fullName":"test_topic_1",					"namespace":""				}			},7:{				"brokers":{1563793189:{						"address":"10.0.5.244:50088",						"backEndPort":50089,						"dataCenter":"UNKNOWN",						"id":1563793189,						"ip":"10.0.5.244",						"managerPort":50091,						"monitorPort":50090,						"permission":"FULL",						"port":50088,						"retryType":"RemoteRetry"					}				},				"electType":"fix",				"group":7,				"id":"test_topic_1.7",				"isrs":[1563793189],				"leader":1563793189,				"leaderBroker":{					"address":"10.0.5.244:50088",					"backEndPort":50089,					"dataCenter":"UNKNOWN",					"id":1563793189,					"ip":"10.0.5.244",					"managerPort":50091,					"monitorPort":50090,					"permission":"FULL",					"port":50088,					"retryType":"RemoteRetry"				},				"learners":[],				"partitions":[7],				"recLeader":-1,				"replicas":[1563793189],				"term":0,				"topic":{					"code":"test_topic_1",					"fullName":"test_topic_1",					"namespace":""				}			},8:{				"brokers":{1563793189:{						"address":"10.0.5.244:50088",						"backEndPort":50089,						"dataCenter":"UNKNOWN",						"id":1563793189,						"ip":"10.0.5.244",						"managerPort":50091,						"monitorPort":50090,						"permission":"FULL",						"port":50088,						"retryType":"RemoteRetry"					}				},				"electType":"fix",				"group":8,				"id":"test_topic_1.8",				"isrs":[1563793189],				"leader":1563793189,				"leaderBroker":{					"address":"10.0.5.244:50088",					"backEndPort":50089,					"dataCenter":"UNKNOWN",					"id":1563793189,					"ip":"10.0.5.244",					"managerPort":50091,					"monitorPort":50090,					"permission":"FULL",					"port":50088,					"retryType":"RemoteRetry"				},				"learners":[],				"partitions":[8],				"recLeader":-1,				"replicas":[1563793189],				"term":0,				"topic":{					"code":"test_topic_1",					"fullName":"test_topic_1",					"namespace":""				}			},9:{				"brokers":{1563793189:{						"address":"10.0.5.244:50088",						"backEndPort":50089,						"dataCenter":"UNKNOWN",						"id":1563793189,						"ip":"10.0.5.244",						"managerPort":50091,						"monitorPort":50090,						"permission":"FULL",						"port":50088,						"retryType":"RemoteRetry"					}				},				"electType":"fix",				"group":9,				"id":"test_topic_1.9",				"isrs":[1563793189],				"leader":1563793189,				"leaderBroker":{					"address":"10.0.5.244:50088",					"backEndPort":50089,					"dataCenter":"UNKNOWN",					"id":1563793189,					"ip":"10.0.5.244",					"managerPort":50091,					"monitorPort":50090,					"permission":"FULL",					"port":50088,					"retryType":"RemoteRetry"				},				"learners":[],				"partitions":[9],				"recLeader":-1,				"replicas":[1563793189],				"term":0,				"topic":{					"code":"test_topic_1",					"fullName":"test_topic_1",					"namespace":""				}			}		},		"partitions":1,		"priorityPartitions":[],		"type":"TOPIC"	},	"message":"SUCCESS"}
```
39.获取主题的所有分区组监控信息
-----------------
- 接口路径 /monitor/topic/:topic/partitionGroups
- 请求类型 GET
- 功能描述 获取主题的所有分区组监控信息
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String | 主题  |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/partitionGroups
```
响应: 
```
{	"code":200,	"data":[],	"message":"SUCCESS"}
```
40.获取主题生产者详细监控信息
----------------
- 接口路径 /monitor/topic/:topic/producer/connections/detail
- 请求类型 GET
- 功能描述 获取主题生产者详细监控信息
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
41.获取分区监控信息
-----------
- 接口路径 /monitor/topic/:topic/partition/:partition
- 请求类型 GET
- 功能描述 获取分区监控信息
- 参数 

|    字段     |   类型   | 备注  |
|:---------:|:------:|:---:|
|   topic   | String | 主题  |
| partition | short  | 分区  |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/partition/0
```
响应: 
```
{	"code":200,	"data":{		"deQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"enQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"partition":0,		"topic":"test_topic_1"	},	"message":"SUCCESS"}
```
42.获取主题下分区组的监控信息
----------------
- 接口路径 /monitor/topic/:topic/partitionGroup/:partitionGroupId
- 请求类型 GET
- 功能描述 获取主题下分区组的监控信息
- 参数 

|       字段       |   类型   | 备注  |
|:--------------:|:------:|:---:|
|     topic      | String | 主题  |
| partitionGroup |  int   | 分区组 |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/partitionGroup/0Id
```
响应: 
```
{	"code":200,	"data":{		"deQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"enQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"flushPosition":0,		"indexPosition":0,		"leftPosition":0,		"partitionGroup":0,		"replication":{			"appendStat":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"partitionGroup":0,			"replicaStat":{				"avg":0.0,				"count":0,				"max":0.0,				"min":0.0,				"oneMinuteRate":0,				"size":0,				"totalSize":0,				"tp90":0.0,				"tp99":0.0,				"tps":0,				"traffic":0			},			"started":false		},		"replicationPosition":0,		"rightPosition":0,		"topic":"test_topic_1"	},	"message":"SUCCESS"}
```
43.获取主题下应用的消费监控信息
-----------------
- 接口路径 /monitor/topic/:topic/app/:app/consumer
- 请求类型 GET
- 功能描述 获取主题下应用的消费监控信息
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String | 主题  |
|  app  | String | 应用  |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/topic/test_topic_1/app/test_app/consumer
```
响应: 
```
{	"code":200,	"data":{		"app":"test_app",		"connections":0,		"deQueue":{			"avg":0.0,			"count":0,			"max":0.0,			"min":0.0,			"oneMinuteRate":0,			"size":0,			"totalSize":0,			"tp90":0.0,			"tp99":0.0,			"tps":0,			"traffic":0		},		"pending":{			"count":0		},		"retry":{			"count":0,			"failure":0,			"success":0		},		"topic":"test_topic_1"	},	"message":"SUCCESS"}
```
44.获得协调者组元数据
------------
- 接口路径 /monitor/coordinator/namespace/:namespace/group/:groupId
- 请求类型 GET
- 功能描述 获得协调者组元数据
- 参数 

|    字段     |   类型    |    备注    |
|:---------:|:-------:|:--------:|
| namespace | String  |   作用域    |
|  groupId  | String  |    组     |
|   topic   | String  |    主题    |
| isFormat  | boolean | 是否格式化元数据 |
示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/coordinator/namespace/joyqueue/group/abc?topic=test_topic_1&isFormat=true
```
响应: 
```
{	"code":200,	"message":"SUCCESS"}
```
45.获取消费归档数量
-----------
- 接口路径 /monitor/archive/consume
- 请求类型 GET
- 功能描述 获取消费归档数量
- 参数 无


示例: 请求: 
```
curl -X GET http://localhost:50090/monitor/archive/consume
```
响应: 
```
{	"code":500,	"message":"java.lang.NullPointerException"}
```
46.获取主题下分区组的度量信息
----------------
- 接口路径 /manage/topic/:topic/partitionGroup/:partitionGroup/store/metric
- 请求类型 GET
- 功能描述 获取主题下分区组的度量信息
- 参数 

|       字段       |   类型   | 备注  |
|:--------------:|:------:|:---:|
|     topic      | String | 主题  |
| partitionGroup |  int   | 分区组 |
示例: 请求: 
```
curl -X GET http://localhost:50090/manage/topic/test_topic_1/partitionGroup/0/store/metric
```
响应: 
```
{	"code":200,	"data":{		"flushPosition":0,		"indexPosition":0,		"leftPosition":0,		"partitionGroup":0,		"partitionMetrics":[{			"leftIndex":0,			"partition":0,			"rightIndex":0		}],		"replicationPosition":0,		"rightPosition":0	},	"message":"SUCCESS"}
```
47.获取绝对路径目录下所有文件
----------------
- 接口路径 /manage/store/absolutePathFiles
- 请求类型 GET
- 功能描述 获取绝对路径目录下所有文件
- 参数 

|  字段  |   类型   | 备注  |
|:----:|:------:|:---:|
| path | String | 目录  |
示例: 请求: 
```
curl -X GET http://localhost:50090/manage/store/absolutePathFiles?path=xxxx
```
响应: 
```
{	"code":200,	"message":"SUCCESS"}
```
48.获取主题下分区组指针处的消息
-----------------
- 接口路径 /manage/topic/:topic/partitionGroup/:partitionGroup/store/messages
- 请求类型 GET
- 功能描述 获取主题下分区组指针处的消息
- 参数 

|       字段       |   类型   | 备注  |
|:--------------:|:------:|:---:|
|     topic      | String | 主题  |
| partitionGroup |  int   | 分区组 |
|    position    |  long  | 指针  |
|     count      |  int   | 数量  |
示例: 请求: 
```
curl -X GET http://localhost:50090/manage/topic/test_topic_1/partitionGroup/0/store/messages?position=0&count=10
```
响应: 
```
{	"code":500,	"message":"java.lang.NullPointerException"}
```
49.初始化协调者
---------
- 接口路径 /manage/coordinator/init
- 请求类型 POST
- 功能描述 初始化协调者
- 参数 无


示例: 
50.获取主题的度量信息
------------
- 接口路径 /manage/topic/:topic/store/metric
- 请求类型 GET
- 功能描述 获取主题的度量信息
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String | 主题  |
示例: 请求: 
```
curl -X GET http://localhost:50090/manage/topic/test_topic_1/store/metric
```
响应: 
```
{	"code":200,	"data":{		"partitionGroupMetrics":[{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":5,			"partitionMetrics":[{				"leftIndex":0,				"partition":5,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":6,			"partitionMetrics":[{				"leftIndex":0,				"partition":6,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":3,			"partitionMetrics":[{				"leftIndex":0,				"partition":3,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":4,			"partitionMetrics":[{				"leftIndex":0,				"partition":4,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":9,			"partitionMetrics":[{				"leftIndex":0,				"partition":9,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":7,			"partitionMetrics":[{				"leftIndex":0,				"partition":7,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":8,			"partitionMetrics":[{				"leftIndex":0,				"partition":8,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":1,			"partitionMetrics":[{				"leftIndex":0,				"partition":1,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":2,			"partitionMetrics":[{				"leftIndex":0,				"partition":2,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":0,			"partitionMetrics":[{				"leftIndex":0,				"partition":0,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		}],		"topic":"test_topic_1"	},	"message":"SUCCESS"}
```
51.获取主题下分区索引处的消息
----------------
- 接口路径 /manage/topic/:topic/partition/:partition/store/messages
- 请求类型 GET
- 功能描述 获取主题下分区索引处的消息
- 参数 

|    字段     |   类型   | 备注  |
|:---------:|:------:|:---:|
|   topic   | String | 主题  |
| partition | short  | 分区  |
|   index   |  long  | 索引  |
|   count   |  int   | 数量  |
示例: 请求: 
```
curl -X GET http://localhost:50090/manage/topic/test_topic_1/partition/0/store/messages?index=0&count=10
```
响应: 
```
{	"code":500,	"message":"java.lang.NullPointerException"}
```
52.关闭主题下应用的所有消费者连接
------------------
- 接口路径 /manage/topic/:topic/app/:app/consumers
- 请求类型 DELETE
- 功能描述 关闭主题下应用的所有消费者连接
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String | 主题  |
|  app  | String | 应用  |
示例: 
53.获取文件中指针处的消息
--------------
- 接口路径 /manage/store/messages
- 请求类型 GET
- 功能描述 获取文件中指针处的消息
- 参数 

|        字段         |   类型    |   备注    |
|:-----------------:|:-------:|:-------:|
|       file        | String  |   文件    |
|     position      |  long   |   指针    |
|       count       |   int   |   数量    |
| includeFileHeader | boolean | 是否包含文件头 |
示例: 请求: 
```
curl -X GET http://localhost:50090/manage/store/messages?file=abc&position=0&count=10&includeFileHeader=null
```
响应: 
```
{	"code":500,	"message":"java.lang.NullPointerException"}
```
54.更新主题下分区组的选举轮次
----------------
- 接口路径 /manage/election/metadata/updateTerm/topic/:topic/partitionGroup/:partitionGroup/:term
- 请求类型 GET
- 功能描述 更新主题下分区组的选举轮次
- 参数 

|       字段       |   类型   | 备注  |
|:--------------:|:------:|:---:|
|     topic      | String | 主题  |
| partitionGroup |  int   | 分区组 |
|      term      |  int   | 轮次  |
示例: 
55.设置主题下应用分区的确认位置到最大
--------------------
- 接口路径 /manage/topic/:topic/app/:app/partition/:partition/maxAck
- 请求类型 PUT
- 功能描述 设置主题下应用分区的确认位置到最大
- 参数 

|    字段     |   类型   | 备注  |
|:---------:|:------:|:---:|
|   topic   | String | 主题  |
|    app    | String | 应用  |
| partition | short  | 分区  |
示例: 
56.获取主题下分区的索引信息
---------------
- 接口路径 /manage/topic/:topic/partition/:partition/store/indexes
- 请求类型 GET
- 功能描述 获取主题下分区的索引信息
- 参数 

|    字段     |   类型   | 备注  |
|:---------:|:------:|:---:|
|   topic   | String | 主题  |
| partition | short  | 分区  |
|   index   |  long  | 索引  |
|   count   |  int   | 数量  |
示例: 请求: 
```
curl -X GET http://localhost:50090/manage/topic/test_topic_1/partition/0/store/indexes?index=0&count=10
```
响应: 
```
{	"code":200,	"data":[],	"message":"SUCCESS"}
```
57.根据时间获取主题下所有分区的确认位置
---------------------
- 接口路径 /manage/topic/:topic/app/:app/timestamp/:timestamp/ackByTime
- 请求类型 GET
- 功能描述 根据时间获取主题下所有分区的确认位置
- 参数 

|    字段     |   类型   | 备注  |
|:---------:|:------:|:---:|
|   topic   | String | 主题  |
|    app    | String | 应用  |
| timestamp |  long  | 时间戳 |
示例: 请求: 
```
curl -X GET http://localhost:50090/manage/topic/test_topic_1/app/test_app/timestamp/923e7937e932/ackByTime
```
响应: 
```
{	"code":200,	"data":[		{			"index":-1,			"lastAckTime":0,			"lastPullTime":0,			"leftIndex":0,			"partition":5,			"rightIndex":0		},		{			"index":-1,			"lastAckTime":0,			"lastPullTime":0,			"leftIndex":0,			"partition":6,			"rightIndex":0		},		{			"index":-1,			"lastAckTime":0,			"lastPullTime":0,			"leftIndex":0,			"partition":3,			"rightIndex":0		},		{			"index":-1,			"lastAckTime":0,			"lastPullTime":0,			"leftIndex":0,			"partition":4,			"rightIndex":0		},		{			"index":-1,			"lastAckTime":0,			"lastPullTime":0,			"leftIndex":0,			"partition":9,			"rightIndex":0		},		{			"index":-1,			"lastAckTime":0,			"lastPullTime":0,			"leftIndex":0,			"partition":7,			"rightIndex":0		},		{			"index":-1,			"lastAckTime":0,			"lastPullTime":0,			"leftIndex":0,			"partition":8,			"rightIndex":0		},		{			"index":-1,			"lastAckTime":0,			"lastPullTime":0,			"leftIndex":0,			"partition":1,			"rightIndex":0		},		{			"index":-1,			"lastAckTime":0,			"lastPullTime":0,			"leftIndex":0,			"partition":2,			"rightIndex":0		},		{			"index":-1,			"lastAckTime":0,			"lastPullTime":0,			"leftIndex":0,			"partition":0,			"rightIndex":0		}	],	"message":"SUCCESS"}
```
58.获取主题下应用的最新消息
---------------
- 接口路径 /manage/topic/:topic/app/:app/message/last
- 请求类型 GET
- 功能描述 获取主题下应用的最新消息
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String | 主题  |
|  app  | String | 应用  |
| count |  int   | 数量  |
示例: 请求: 
```
curl -X GET http://localhost:50090/manage/topic/test_topic_1/app/test_app/message/last?count=10
```
响应: 
```
{	"code":200,	"data":[],	"message":"SUCCESS"}
```
59.根据时间设置主题下应用分区的确认位置
---------------------
- 接口路径 /manage/topic/:topic/app/:app/partition/:partition/ackByTime
- 请求类型 PUT
- 功能描述 根据时间设置主题下应用分区的确认位置
- 参数 

|    字段     |   类型   | 备注  |
|:---------:|:------:|:---:|
|   topic   | String | 主题  |
|    app    | String | 应用  |
| partition | short  | 分区  |
| timestamp |  long  | 时间戳 |
示例: 
60.获取主题下应用所有分区的确认位置
-------------------
- 接口路径 /manage/topic/:topic/app/:app/acks
- 请求类型 GET
- 功能描述 获取主题下应用所有分区的确认位置
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String | 主题  |
|  app  | String | 应用  |
示例: 请求: 
```
curl -X GET http://localhost:50090/manage/topic/test_topic_1/app/test_app/acks
```
响应: 
```
{	"code":200,	"data":[		{			"index":0,			"lastAckTime":-1,			"lastPullTime":-1,			"leftIndex":0,			"partition":5,			"rightIndex":0		},		{			"index":0,			"lastAckTime":-1,			"lastPullTime":-1,			"leftIndex":0,			"partition":6,			"rightIndex":0		},		{			"index":0,			"lastAckTime":-1,			"lastPullTime":-1,			"leftIndex":0,			"partition":3,			"rightIndex":0		},		{			"index":0,			"lastAckTime":-1,			"lastPullTime":-1,			"leftIndex":0,			"partition":4,			"rightIndex":0		},		{			"index":0,			"lastAckTime":-1,			"lastPullTime":-1,			"leftIndex":0,			"partition":9,			"rightIndex":0		},		{			"index":0,			"lastAckTime":-1,			"lastPullTime":-1,			"leftIndex":0,			"partition":7,			"rightIndex":0		},		{			"index":0,			"lastAckTime":-1,			"lastPullTime":-1,			"leftIndex":0,			"partition":8,			"rightIndex":0		},		{			"index":0,			"lastAckTime":-1,			"lastPullTime":-1,			"leftIndex":0,			"partition":1,			"rightIndex":0		},		{			"index":0,			"lastAckTime":-1,			"lastPullTime":-1,			"leftIndex":0,			"partition":2,			"rightIndex":0		},		{			"index":0,			"lastAckTime":-1,			"lastPullTime":-1,			"leftIndex":0,			"partition":0,			"rightIndex":0		}	],	"message":"SUCCESS"}
```
61.获取目录下所有文件
------------
- 接口路径 /manage/store/files/:path
- 请求类型 GET
- 功能描述 获取目录下所有文件
- 参数 

|  字段  |   类型   | 备注  |
|:----:|:------:|:---:|
| path | String | 目录  |
示例: 请求: 
```
curl -X GET http://localhost:50090/manage/store/files/xxxx
```
响应: 
```
{	"code":200,	"message":"SUCCESS"}
```
62.读主题下分区组的文件
-------------
- 接口路径 /manage/topic/:topic/partitionGroup/:partitionGroup/store/data
- 请求类型 GET
- 功能描述 读主题下分区组的文件
- 参数 

|       字段       |   类型   | 备注  |
|:--------------:|:------:|:---:|
|     topic      | String | 主题  |
| partitionGroup |  int   | 分区组 |
|    position    |  long  | 指针  |
|     length     |  int   | 长度  |
示例: 请求: 
```
curl -X GET http://localhost:50090/manage/topic/test_topic_1/partitionGroup/0/store/data?position=0&length=100
```
响应: 
```
{	"code":500,	"message":"java.lang.NullPointerException"}
```
63.关闭主题下应用的所有生产者连接
------------------
- 接口路径 /manage/topic/:topic/app/:app/producers
- 请求类型 DELETE
- 功能描述 关闭主题下应用的所有生产者连接
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String | 主题  |
|  app  | String | 应用  |
示例: 
64.根据时间获取主题下应用分区的确认位置
---------------------
- 接口路径 /manage/topic/:topic/app/:app/partition/:partition/ackByTime
- 请求类型 GET
- 功能描述 根据时间获取主题下应用分区的确认位置
- 参数 

|    字段     |   类型   | 备注  |
|:---------:|:------:|:---:|
|   topic   | String | 主题  |
|    app    | String | 应用  |
| partition | short  | 分区  |
| timestamp |  long  | 时间戳 |
示例: 请求: 
```
curl -X GET http://localhost:50090/manage/topic/test_topic_1/app/test_app/partition/0/ackByTime?timestamp=923e7937e932
```
响应: 
```
{	"code":200,	"data":-1,	"message":"SUCCESS"}
```
65.获取主题下应用的积压消息
---------------
- 接口路径 /manage/topic/:topic/app/:app/message/pending
- 请求类型 GET
- 功能描述 获取主题下应用的积压消息
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String | 主题  |
|  app  | String | 应用  |
| count |  int   | 数量  |
示例: 请求: 
```
curl -X GET http://localhost:50090/manage/topic/test_topic_1/app/test_app/message/pending?count=10
```
响应: 
```
{	"code":200,	"data":[],	"message":"SUCCESS"}
```
66.获取所有主题名
----------
- 接口路径 /manage/topic/list
- 请求类型 GET
- 功能描述 获取所有主题名
- 参数 无


示例: 请求: 
```
curl -X GET http://localhost:50090/manage/topic/list
```
响应: 
```
{	"code":200,	"data":[		"test_topic",		"__transaction_coordinators",		"test_topic_4",		"test_topic_3",		"test_topic_2",		"__group_coordinators",		"test_topic_0",		"test_topic_1"	],	"message":"SUCCESS"}
```
67.获取主题下所有分区的度量信息
-----------------
- 接口路径 /manage/topic/:topic/partitionGroup/detail
- 请求类型 GET
- 功能描述 获取主题下所有分区的度量信息
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String | 主题  |
示例: 请求: 
```
curl -X GET http://localhost:50090/manage/topic/test_topic_1/partitionGroup/detail
```
响应: 
```
{	"code":200,	"data":[		{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":9,			"partitions":"9",			"replicationPosition":0,			"rightPosition":0		},		{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":0,			"partitions":"0",			"replicationPosition":0,			"rightPosition":0		},		{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":7,			"partitions":"7",			"replicationPosition":0,			"rightPosition":0		},		{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":6,			"partitions":"6",			"replicationPosition":0,			"rightPosition":0		},		{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":1,			"partitions":"1",			"replicationPosition":0,			"rightPosition":0		},		{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":8,			"partitions":"8",			"replicationPosition":0,			"rightPosition":0		},		{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":4,			"partitions":"4",			"replicationPosition":0,			"rightPosition":0		},		{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":3,			"partitions":"3",			"replicationPosition":0,			"rightPosition":0		},		{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":2,			"partitions":"2",			"replicationPosition":0,			"rightPosition":0		},		{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":5,			"partitions":"5",			"replicationPosition":0,			"rightPosition":0		}	],	"message":"SUCCESS"}
```
68.移除协调组信息
----------
- 接口路径 /manage/coordinator/namespace/:namespace/group/:groupId
- 请求类型 DELETE
- 功能描述 移除协调组信息
- 参数 

|    字段     |   类型   | 备注  |
|:---------:|:------:|:---:|
| namespace | String | 作用域 |
|  groupId  | String |  组  |
示例: 
69.返回主题下分区组的选举元数据
-----------------
- 接口路径 /manage/election/metadata/describeTopic/topic/:topic/partitionGroup/:partitionGroup
- 请求类型 GET
- 功能描述 返回主题下分区组的选举元数据
- 参数 

|       字段       |   类型   |  备注  |
|:--------------:|:------:|:----:|
|     topic      | String |  主题  |
| partitionGroup |  int   | 分区组组 |
示例: 请求: 
```
curl -X GET http://localhost:50090/manage/election/metadata/describeTopic/topic/test_topic_1/partitionGroup/0
```
响应: 
```
{	"code":200,	"data":"{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563793193478,\"votedFor\":-1}",	"message":"SUCCESS"}
```
70.根据时间设置主题下应用所有分区的确认位置
-----------------------
- 接口路径 /manage/topic/:topic/app/:app/ackByTime
- 请求类型 PUT
- 功能描述 根据时间设置主题下应用所有分区的确认位置
- 参数 

|    字段     |   类型   | 备注  |
|:---------:|:------:|:---:|
|   topic   | String | 主题  |
|    app    | String | 应用  |
| timestamp |  long  | 时间戳 |
示例: 
71.获取主题下应用分区的消息
---------------
- 接口路径 /manage/topic/:topic/app/:app/partition/:partition/message
- 请求类型 GET
- 功能描述 获取主题下应用分区的消息
- 参数 

|    字段     |   类型   | 备注  |
|:---------:|:------:|:---:|
|   topic   | String | 主题  |
|    app    | String | 应用  |
| partition | short  | 分区  |
|   index   |  long  | 索引  |
|   count   |  int   | 数量  |
示例: 请求: 
```
curl -X GET http://localhost:50090/manage/topic/test_topic_1/app/test_app/partition/0/message?index=0&count=10
```
响应: 
```
{	"code":200,	"data":[],	"message":"SUCCESS"}
```
72.获取文件中指针处的索引信息
----------------
- 接口路径 /manage/store/indexes
- 请求类型 GET
- 功能描述 获取文件中指针处的索引信息
- 参数 

|        字段         |   类型    |   备注    |
|:-----------------:|:-------:|:-------:|
|       file        | String  |   文件    |
|     position      |  long   |   指针    |
|       count       |   int   |   数量    |
| includeFileHeader | boolean | 是否包含文件头 |
示例: 请求: 
```
curl -X GET http://localhost:50090/manage/store/indexes?file=abc&position=0&count=10&includeFileHeader=null
```
响应: 
```
{	"code":200,	"data":[],	"message":"SUCCESS"}
```
73.设置主题下应用所有分区的确认位置到最大
----------------------
- 接口路径 /manage/topic/:topic/app/:app/maxAck
- 请求类型 PUT
- 功能描述 设置主题下应用所有分区的确认位置到最大
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String | 主题  |
|  app  | String | 应用  |
示例: 
74.获取所有主题的度量信息
--------------
- 接口路径 /manage/topic/store/metrics
- 请求类型 GET
- 功能描述 获取所有主题的度量信息
- 参数 无


示例: 请求: 
```
curl -X GET http://localhost:50090/manage/topic/store/metrics
```
响应: 
```
{	"code":200,	"data":[{		"partitionGroupMetrics":[{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":6,			"partitionMetrics":[{				"leftIndex":0,				"partition":6,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":7,			"partitionMetrics":[{				"leftIndex":0,				"partition":7,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":4,			"partitionMetrics":[{				"leftIndex":0,				"partition":4,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":5,			"partitionMetrics":[{				"leftIndex":0,				"partition":5,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":8,			"partitionMetrics":[{				"leftIndex":0,				"partition":8,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":9,			"partitionMetrics":[{				"leftIndex":0,				"partition":9,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":2,			"partitionMetrics":[{				"leftIndex":0,				"partition":2,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":3,			"partitionMetrics":[{				"leftIndex":0,				"partition":3,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":0,			"partitionMetrics":[{				"leftIndex":0,				"partition":0,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":1,			"partitionMetrics":[{				"leftIndex":0,				"partition":1,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		}],		"topic":"test_topic_0"	},{		"partitionGroupMetrics":[{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":4,			"partitionMetrics":[{				"leftIndex":0,				"partition":4,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":5,			"partitionMetrics":[{				"leftIndex":0,				"partition":5,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":2,			"partitionMetrics":[{				"leftIndex":0,				"partition":2,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":3,			"partitionMetrics":[{				"leftIndex":0,				"partition":3,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":8,			"partitionMetrics":[{				"leftIndex":0,				"partition":8,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":9,			"partitionMetrics":[{				"leftIndex":0,				"partition":9,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":6,			"partitionMetrics":[{				"leftIndex":0,				"partition":6,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":7,			"partitionMetrics":[{				"leftIndex":0,				"partition":7,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":0,			"partitionMetrics":[{				"leftIndex":0,				"partition":0,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":1,			"partitionMetrics":[{				"leftIndex":0,				"partition":1,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		}],		"topic":"test_topic_2"	},{		"partitionGroupMetrics":[{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":2,			"partitionMetrics":[{				"leftIndex":0,				"partition":2,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":3,			"partitionMetrics":[{				"leftIndex":0,				"partition":3,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":0,			"partitionMetrics":[{				"leftIndex":0,				"partition":0,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":1,			"partitionMetrics":[{				"leftIndex":0,				"partition":1,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":6,			"partitionMetrics":[{				"leftIndex":0,				"partition":6,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":7,			"partitionMetrics":[{				"leftIndex":0,				"partition":7,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":4,			"partitionMetrics":[{				"leftIndex":0,				"partition":4,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":5,			"partitionMetrics":[{				"leftIndex":0,				"partition":5,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":8,			"partitionMetrics":[{				"leftIndex":0,				"partition":8,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":9,			"partitionMetrics":[{				"leftIndex":0,				"partition":9,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		}],		"topic":"test_topic_4"	},{		"partitionGroupMetrics":[{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":4,			"partitionMetrics":[{				"leftIndex":0,				"partition":4,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":3,			"partitionMetrics":[{				"leftIndex":0,				"partition":3,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":6,			"partitionMetrics":[{				"leftIndex":0,				"partition":6,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":5,			"partitionMetrics":[{				"leftIndex":0,				"partition":5,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":8,			"partitionMetrics":[{				"leftIndex":0,				"partition":8,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":7,			"partitionMetrics":[{				"leftIndex":0,				"partition":7,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":9,			"partitionMetrics":[{				"leftIndex":0,				"partition":9,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":0,			"partitionMetrics":[{				"leftIndex":0,				"partition":0,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":2,			"partitionMetrics":[{				"leftIndex":0,				"partition":2,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":1,			"partitionMetrics":[{				"leftIndex":0,				"partition":1,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		}],		"topic":"__transaction_coordinators"	},{		"partitionGroupMetrics":[{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":0,			"partitionMetrics":[{				"leftIndex":0,				"partition":0,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":2,			"partitionMetrics":[{				"leftIndex":0,				"partition":2,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":1,			"partitionMetrics":[{				"leftIndex":0,				"partition":1,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":4,			"partitionMetrics":[{				"leftIndex":0,				"partition":4,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":3,			"partitionMetrics":[{				"leftIndex":0,				"partition":3,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":6,			"partitionMetrics":[{				"leftIndex":0,				"partition":6,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":5,			"partitionMetrics":[{				"leftIndex":0,				"partition":5,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":8,			"partitionMetrics":[{				"leftIndex":0,				"partition":8,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":7,			"partitionMetrics":[{				"leftIndex":0,				"partition":7,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":9,			"partitionMetrics":[{				"leftIndex":0,				"partition":9,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		}],		"topic":"test_topic"	},{		"partitionGroupMetrics":[{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":5,			"partitionMetrics":[{				"leftIndex":0,				"partition":5,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":6,			"partitionMetrics":[{				"leftIndex":0,				"partition":6,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":3,			"partitionMetrics":[{				"leftIndex":0,				"partition":3,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":4,			"partitionMetrics":[{				"leftIndex":0,				"partition":4,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":9,			"partitionMetrics":[{				"leftIndex":0,				"partition":9,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":7,			"partitionMetrics":[{				"leftIndex":0,				"partition":7,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":8,			"partitionMetrics":[{				"leftIndex":0,				"partition":8,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":1,			"partitionMetrics":[{				"leftIndex":0,				"partition":1,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":2,			"partitionMetrics":[{				"leftIndex":0,				"partition":2,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":0,			"partitionMetrics":[{				"leftIndex":0,				"partition":0,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		}],		"topic":"test_topic_1"	},{		"partitionGroupMetrics":[{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":3,			"partitionMetrics":[{				"leftIndex":0,				"partition":3,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":4,			"partitionMetrics":[{				"leftIndex":0,				"partition":4,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":1,			"partitionMetrics":[{				"leftIndex":0,				"partition":1,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":2,			"partitionMetrics":[{				"leftIndex":0,				"partition":2,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":7,			"partitionMetrics":[{				"leftIndex":0,				"partition":7,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":8,			"partitionMetrics":[{				"leftIndex":0,				"partition":8,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":5,			"partitionMetrics":[{				"leftIndex":0,				"partition":5,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":6,			"partitionMetrics":[{				"leftIndex":0,				"partition":6,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":0,			"partitionMetrics":[{				"leftIndex":0,				"partition":0,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":9,			"partitionMetrics":[{				"leftIndex":0,				"partition":9,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		}],		"topic":"test_topic_3"	},{		"partitionGroupMetrics":[{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":3,			"partitionMetrics":[{				"leftIndex":0,				"partition":3,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":2,			"partitionMetrics":[{				"leftIndex":0,				"partition":2,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":1,			"partitionMetrics":[{				"leftIndex":0,				"partition":1,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":0,			"partitionMetrics":[{				"leftIndex":0,				"partition":0,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":9,			"partitionMetrics":[{				"leftIndex":0,				"partition":9,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":8,			"partitionMetrics":[{				"leftIndex":0,				"partition":8,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":7,			"partitionMetrics":[{				"leftIndex":0,				"partition":7,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":6,			"partitionMetrics":[{				"leftIndex":0,				"partition":6,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":5,			"partitionMetrics":[{				"leftIndex":0,				"partition":5,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		},{			"flushPosition":0,			"indexPosition":0,			"leftPosition":0,			"partitionGroup":4,			"partitionMetrics":[{				"leftIndex":0,				"partition":4,				"rightIndex":0			}],			"replicationPosition":0,			"rightPosition":0		}],		"topic":"__group_coordinators"	}],	"message":"SUCCESS"}
```
75.读文件
------
- 接口路径 /manage/store/file
- 请求类型 GET
- 功能描述 读文件
- 参数 

|    字段    |   类型   | 备注  |
|:--------:|:------:|:---:|
|   file   | String | 文件  |
| position |  long  | 指针  |
|  length  |  int   | 长度  |
示例: 请求: 
```
curl -X GET http://localhost:50090/manage/store/file?file=abc&position=0&length=100
```
响应: 
```
{	"code":200,	"data":"LENGTH(4): 0\nPARTITION(2): 0\nINDEX(8): 0\nTERM(4): 0\nMAGIC(2): 0\nSYS(2): 0\nPRIORITY(1): 00(0)\nCLIENT_IP(16): \n\tHex: 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 \n\tString: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\nCLIENT_TIMESTAMP(8): 0 (1970-01-01 08:00:00.000)\nSTORAGE_TIMESTAMP(4): 0 (1970-01-01 08:00:00.000)\nCRC(8): 0\nFLAG(2): 0\nBODY(0): \n\tHex: \n\tString: \nBIZ_ID(0): \n\tHex: \n\tString: \nPROPERTY(0): \n\tHex: \n\tString: \nEXPAND(0): \n\tHex: \n\tString: \nAPP(0): \n\tHex: \n\tString: \n",	"message":"SUCCESS"}
```
76.设置主题下应用分区的确认位置
-----------------
- 接口路径 /manage/topic/:topic/app/:app/partition/:partition/ack
- 请求类型 PUT
- 功能描述 设置主题下应用分区的确认位置
- 参数 

|    字段     |   类型   | 备注  |
|:---------:|:------:|:---:|
|   topic   | String | 主题  |
|    app    | String | 应用  |
| partition | short  | 分区  |
|   index   |  long  | 索引  |
示例: 
77.恢复选举元数据
----------
- 接口路径 /manage/election/metadata/restore
- 请求类型 GET
- 功能描述 恢复选举元数据
- 参数 无


示例: 请求: 
```
curl -X GET http://localhost:50090/manage/election/metadata/restore
```
响应: 
```
{	"code":200,	"message":"SUCCESS"}
```
78.获取主题下分区的度量信息
---------------
- 接口路径 /manage/topic/:topic/partition/:partition/store/metric
- 请求类型 GET
- 功能描述 获取主题下分区的度量信息
- 参数 

|    字段     |   类型   | 备注  |
|:---------:|:------:|:---:|
|   topic   | String | 主题  |
| partition | short  | 分区  |
示例: 请求: 
```
curl -X GET http://localhost:50090/manage/topic/test_topic_1/partition/0/store/metric
```
响应: 
```
{	"code":200,	"data":{		"leftIndex":0,		"partition":0,		"rightIndex":0	},	"message":"SUCCESS"}
```
79.获取主题下应用的消息，如果有积压返回积压，否则返回最新几条
--------------------------------
- 接口路径 /manage/topic/:topic/app/:app/message/view
- 请求类型 GET
- 功能描述 获取主题下应用的消息，如果有积压返回积压，否则返回最新几条
- 参数 

|  字段   |   类型   | 备注  |
|:-----:|:------:|:---:|
| topic | String | 主题  |
|  app  | String | 应用  |
| count |  int   | 数量  |
示例: 请求: 
```
curl -X GET http://localhost:50090/manage/topic/test_topic_1/app/test_app/message/view?count=10
```
响应: 
```
{	"code":200,	"data":[],	"message":"SUCCESS"}
```
80.返回当前选举元数据
------------
- 接口路径 /manage/election/metadata/describe
- 请求类型 GET
- 功能描述 返回当前选举元数据
- 参数 无


示例: 请求: 
```
curl -X GET http://localhost:50090/manage/election/metadata/describe
```
响应: 
```
{	"code":200,	"data":"{{\"partitionGroupId\":0,\"topic\":\"test_topic_0\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360161,\"votedFor\":-1},{\"partitionGroupId\":2,\"topic\":\"test_topic_0\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360166,\"votedFor\":-1},{\"partitionGroupId\":0,\"topic\":\"test_topic_2\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360188,\"votedFor\":-1},{\"partitionGroupId\":1,\"topic\":\"test_topic_1\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360218,\"votedFor\":-1},{\"partitionGroupId\":1,\"topic\":\"test_topic_0\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360164,\"votedFor\":-1},{\"partitionGroupId\":0,\"topic\":\"test_topic_1\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360215,\"votedFor\":-1},{\"partitionGroupId\":4,\"topic\":\"test_topic_0\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360171,\"votedFor\":-1},{\"partitionGroupId\":2,\"topic\":\"test_topic_2\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360193,\"votedFor\":-1},{\"partitionGroupId\":3,\"topic\":\"test_topic_1\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360224,\"votedFor\":-1},{\"partitionGroupId\":0,\"topic\":\"test_topic_4\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360282,\"votedFor\":-1},{\"partitionGroupId\":1,\"topic\":\"test_topic_3\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360321,\"votedFor\":-1},{\"partitionGroupId\":3,\"topic\":\"test_topic_0\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360169,\"votedFor\":-1},{\"partitionGroupId\":1,\"topic\":\"test_topic_2\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360190,\"votedFor\":-1},{\"partitionGroupId\":2,\"topic\":\"test_topic_1\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360221,\"votedFor\":-1},{\"partitionGroupId\":0,\"topic\":\"test_topic_3\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360317,\"votedFor\":-1},{\"partitionGroupId\":6,\"topic\":\"test_topic_0\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360177,\"votedFor\":-1},{\"partitionGroupId\":4,\"topic\":\"test_topic_2\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360198,\"votedFor\":-1},{\"partitionGroupId\":5,\"topic\":\"test_topic_1\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360231,\"votedFor\":-1},{\"partitionGroupId\":2,\"topic\":\"test_topic_4\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360289,\"votedFor\":-1},{\"partitionGroupId\":3,\"topic\":\"test_topic_3\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360328,\"votedFor\":-1},{\"partitionGroupId\":5,\"topic\":\"test_topic_0\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360174,\"votedFor\":-1},{\"partitionGroupId\":3,\"topic\":\"test_topic_2\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360196,\"votedFor\":-1},{\"partitionGroupId\":4,\"topic\":\"test_topic_1\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360228,\"votedFor\":-1},{\"partitionGroupId\":1,\"topic\":\"test_topic_4\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360286,\"votedFor\":-1},{\"partitionGroupId\":2,\"topic\":\"test_topic_3\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360324,\"votedFor\":-1},{\"partitionGroupId\":8,\"topic\":\"test_topic_0\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360183,\"votedFor\":-1},{\"partitionGroupId\":6,\"topic\":\"test_topic_2\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360203,\"votedFor\":-1},{\"partitionGroupId\":7,\"topic\":\"test_topic_1\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360236,\"votedFor\":-1},{\"partitionGroupId\":4,\"topic\":\"test_topic_4\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360297,\"votedFor\":-1},{\"partitionGroupId\":5,\"topic\":\"test_topic_3\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360334,\"votedFor\":-1},{\"partitionGroupId\":7,\"topic\":\"test_topic_0\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360180,\"votedFor\":-1},{\"partitionGroupId\":5,\"topic\":\"test_topic_2\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360200,\"votedFor\":-1},{\"partitionGroupId\":6,\"topic\":\"test_topic_1\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360233,\"votedFor\":-1},{\"partitionGroupId\":3,\"topic\":\"test_topic_4\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360293,\"votedFor\":-1},{\"partitionGroupId\":4,\"topic\":\"test_topic_3\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360331,\"votedFor\":-1},{\"partitionGroupId\":8,\"topic\":\"test_topic_2\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360208,\"votedFor\":-1},{\"partitionGroupId\":9,\"topic\":\"test_topic_1\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360242,\"votedFor\":-1},{\"partitionGroupId\":6,\"topic\":\"test_topic_4\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360303,\"votedFor\":-1},{\"partitionGroupId\":7,\"topic\":\"test_topic_3\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360339,\"votedFor\":-1},{\"partitionGroupId\":9,\"topic\":\"test_topic_0\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360185,\"votedFor\":-1},{\"partitionGroupId\":7,\"topic\":\"test_topic_2\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360206,\"votedFor\":-1},{\"partitionGroupId\":8,\"topic\":\"test_topic_1\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360239,\"votedFor\":-1},{\"partitionGroupId\":5,\"topic\":\"test_topic_4\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360300,\"votedFor\":-1},{\"partitionGroupId\":6,\"topic\":\"test_topic_3\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360336,\"votedFor\":-1},{\"partitionGroupId\":8,\"topic\":\"test_topic_4\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360309,\"votedFor\":-1},{\"partitionGroupId\":9,\"topic\":\"test_topic_3\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360346,\"votedFor\":-1},{\"partitionGroupId\":9,\"topic\":\"test_topic_2\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360212,\"votedFor\":-1},{\"partitionGroupId\":7,\"topic\":\"test_topic_4\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360307,\"votedFor\":-1},{\"partitionGroupId\":8,\"topic\":\"test_topic_3\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360342,\"votedFor\":-1},{\"partitionGroupId\":2,\"topic\":\"test_topic\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360385,\"votedFor\":-1},{\"partitionGroupId\":3,\"topic\":\"test_topic\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360388,\"votedFor\":-1},{\"partitionGroupId\":4,\"topic\":\"test_topic\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360390,\"votedFor\":-1},{\"partitionGroupId\":5,\"topic\":\"test_topic\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360393,\"votedFor\":-1},{\"partitionGroupId\":0,\"topic\":\"test_topic\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360380,\"votedFor\":-1},{\"partitionGroupId\":1,\"topic\":\"test_topic\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360383,\"votedFor\":-1},{\"partitionGroupId\":6,\"topic\":\"test_topic\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360397,\"votedFor\":-1},{\"partitionGroupId\":7,\"topic\":\"test_topic\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360400,\"votedFor\":-1},{\"partitionGroupId\":8,\"topic\":\"test_topic\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360402,\"votedFor\":-1},{\"partitionGroupId\":9,\"topic\":\"test_topic\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360406,\"votedFor\":-1},{\"partitionGroupId\":0,\"topic\":\"__group_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360349,\"votedFor\":-1},{\"partitionGroupId\":3,\"topic\":\"__group_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360359,\"votedFor\":-1},{\"partitionGroupId\":4,\"topic\":\"__group_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360363,\"votedFor\":-1},{\"partitionGroupId\":1,\"topic\":\"__group_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360352,\"votedFor\":-1},{\"partitionGroupId\":2,\"topic\":\"__group_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360355,\"votedFor\":-1},{\"partitionGroupId\":7,\"topic\":\"__group_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360371,\"votedFor\":-1},{\"partitionGroupId\":8,\"topic\":\"__group_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360374,\"votedFor\":-1},{\"partitionGroupId\":5,\"topic\":\"__group_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360365,\"votedFor\":-1},{\"partitionGroupId\":6,\"topic\":\"__group_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360368,\"votedFor\":-1},{\"partitionGroupId\":9,\"topic\":\"__group_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360376,\"votedFor\":-1},{\"partitionGroupId\":9,\"topic\":\"test_topic_4\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"fix\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360313,\"votedFor\":-1},{\"partitionGroupId\":9,\"topic\":\"__transaction_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360278,\"votedFor\":-1},{\"partitionGroupId\":8,\"topic\":\"__transaction_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360275,\"votedFor\":-1},{\"partitionGroupId\":7,\"topic\":\"__transaction_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360272,\"votedFor\":-1},{\"partitionGroupId\":6,\"topic\":\"__transaction_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360268,\"votedFor\":-1},{\"partitionGroupId\":5,\"topic\":\"__transaction_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360265,\"votedFor\":-1},{\"partitionGroupId\":4,\"topic\":\"__transaction_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360261,\"votedFor\":-1},{\"partitionGroupId\":3,\"topic\":\"__transaction_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360257,\"votedFor\":-1},{\"partitionGroupId\":2,\"topic\":\"__transaction_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360253,\"votedFor\":-1},{\"partitionGroupId\":1,\"topic\":\"__transaction_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360250,\"votedFor\":-1},{\"partitionGroupId\":0,\"topic\":\"__transaction_coordinators\"}:{\"allNodes\":[{\"address\":\"10.0.5.244:50089\",\"nodeId\":1563793189,\"priority\":0,\"state\":\"FOLLOWER\",\"voteGranted\":false}],\"currentTerm\":0,\"electType\":\"raft\",\"leaderId\":1563793189,\"learners\":[],\"localNodeId\":1563793189,\"timestamp\":1563795360247,\"votedFor\":-1}}",	"message":"SUCCESS"}
```
81.获取主题下应用的确认位置
---------------
- 接口路径 /manage/topic/:topic/app/:app/partition/:partition/ack
- 请求类型 GET
- 功能描述 获取主题下应用的确认位置
- 参数 

|    字段     |   类型   | 备注  |
|:---------:|:------:|:---:|
|   topic   | String | 主题  |
|    app    | String | 应用  |
| partition | short  | 分区  |
示例: 请求: 
```
curl -X GET http://localhost:50090/manage/topic/test_topic_1/app/test_app/partition/0/ack
```
响应: 
```
{	"code":200,	"data":0,	"message":"SUCCESS"}
```
