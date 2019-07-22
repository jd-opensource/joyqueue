# 配置

配置项 | 默认值 | 说明
-- | -- | --
application.data.path | ${HOME}/joyqueue | JoyQueue数据目录
broker.frontend-server.transport.server.port | 50088 | JoyQueue Server与客户端通信的端口
broker.backend-server.transport.server.port | 50089 | 内部端口，JoyQueue Server各节点之间通信的端口
manager.export.port | 50090 | Broker监控服务的端口
nameserver.nsr.manage.port | 50091 | JoyQueue Server rest API 端口
nameserver.transport.server.port | 50092 | 内部端口，JoyQueue Server各节点之间通信的端口。