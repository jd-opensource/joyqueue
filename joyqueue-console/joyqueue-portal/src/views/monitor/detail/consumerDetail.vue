<template>
  <div style="margin-left:-25px;">
    <d-tabs type="card" @on-change="handleTabChange" :value="subTab" size="small">
      <d-tab-pane label="分区组" name="partition" icon="pocket" :closable="false">
        <partition ref="partition" :colData="partitionColData" :doSearch="doSearch" :search="search"/>
      </d-tab-pane>
      <d-tab-pane label="客户端连接" name="clientConnection" icon="github" :closable="false">
        <client-connection ref="clientConnection" :search="search" :doSearch="doSearch"/>
      </d-tab-pane>
      <d-tab-pane label="Broker" name="broker" icon="file-text" :closable="false">
        <broker ref="broker" :search="search" :doSearch="doSearch"/>
      </d-tab-pane>
      <d-tab-pane label="消费位置" name="offsetInfo" icon="file-text" :closable="false">
        <offset ref="offsetInfo" :search="search" :doSearch="doSearch"/>
      </d-tab-pane>
      <d-tab-pane v-if="$store.getters.isAdmin" label="协调者信息" name="coordinatorInfo" icon="file-text"
                  :closable="false">
        <detail-table ref="coordinatorInfo" :doSearch="doSearch" :colData="coordinatorInfo.colData"
                      :urls="coordinatorInfo.urls" :search="search"/>
      </d-tab-pane>
      <d-tab-pane label="消费组成员" name="coordinatorGroupMember" icon="file-text"
                  :closable="false">
        <coordinator-group-member ref="coordinatorGroupMember" :doSearch="doSearch" :search="search"
                               :colData="coordinatorGroupMember.colData" :urls="coordinatorGroupMember.urls"/>
      </d-tab-pane>
      <d-tab-pane  label="消费组过期成员" name="coordinatorGroupExpiredMember"
                  icon="file-text" :closable="false">
        <detail-table ref="coordinatorGroupExpiredMember" :doSearch="doSearch" :search="search"
                   :col-data="coordinatorGroupExpiredMember.colData" :urls="coordinatorGroupExpiredMember.urls"/>
      </d-tab-pane>
      <d-tab-pane v-if="search.clientType==2" label="mqtt客户端" name="mqttClient" icon="file-text" :closable="false">
        <mqtt-base-monitor ref="mqttClient" :client-id="subscribeGroup"  :inputable=false :btns="mqttClient.btns"
                           :col-data="mqttClient.colData" :urls="mqttClient.urls"/>
      </d-tab-pane>
      <div slot="extra">
        <d-button type="primary" size="small" @click="getList" class="mr40">刷新</d-button>
      </div>
    </d-tabs>
  </div>
</template>

<script>
import {timeStampToString} from '../../../utils/dateTimeUtils'
import partition from './partition.vue'
import partitionExpand from './partitionExpand'
import clientConnection from './clientConnection.vue'
import mqttBaseMonitor from '../../setting/mqttBaseMonitor'
import offset from './offset'
import broker from './broker.vue'
import detailTable from './detailTable'
import coordinatorGroupMember from './coordinatorGroupMember.vue'
import {bytesToSize, mergePartitionGroup} from '../../../utils/common'

export default {
  name: 'consumerDetail',
  components: {
    partition,
    partitionExpand,
    clientConnection,
    mqttBaseMonitor,
    offset,
    broker,
    detailTable,
    coordinatorGroupMember
  },
  props: {
    detailType: {
      type: Number
    },
    partitionColData: {
      type: Array,
      default: function () {
        return [
          {
            type: 'expand',
            width: 50,
            render: (h, params) => {
              // console.log(h);
              return h(partitionExpand, {
                props: {
                  row: params.row,
                  colData: [
                    {
                      title: '分区',
                      key: 'partition'
                    },
                    {
                      title: '积压数',
                      key: 'pending.count'
                    },
                    {
                      title: '出队数',
                      key: 'deQuence.count'
                    },
                    {
                      title: 'TPS',
                      key: 'deQuence.tps'
                    },
                    {
                      title: '流量',
                      key: 'deQuence.traffic',
                      formatter (item) {
                        return bytesToSize(item.deQuence.traffic)
                      }
                    }
                  ],
                  subscribe: params.row.subscribe,
                  partitionGroup: params.row.groupNo
                }
              })
            }
          },
          {
            title: 'ID',
            key: 'groupNo'
          },
          {
            title: '主分片',
            key: 'ip'
          },
          {
            title: '分区',
            key: 'partitions',
            formatter (item) {
              return mergePartitionGroup(JSON.parse(item.partitions))
            }
          },
          {
            title: '积压数',
            key: 'pending.count'
          },
          {
            title: '出队数',
            key: 'deQuence.count'
          }
        ]
      }
    },
    coordinatorInfo: {
      type: Object,
      default: function () {
        return {
          colData: [
            {
              title: 'broker',
              key: 'broker',
              formatter (row) {
                return row.broker.ip + ':' + row.broker.port
              }
            },
            {
              title: '协调者',
              key: 'coordinator'
            }
          ],
          urls: {
            search: '/monitor/coordinator'
          }
        }
      }
    },
    coordinatorGroupMember: {
      type: Object,
      default: function () {
        return {
          colData: [
            {
              title: 'id',
              key: 'connectionHost',
              width: '15%'
            },
            {
              title: '最新心跳时间',
              key: 'latestHeartbeat',
              width: '15%',
              formatter (item) {
                return timeStampToString(item.latestHeartbeat)
              }
            },
            {
              title: '会话超时',
              key: 'sessionTimeout',
              width: '10%'
            },
            {
              title: '分区分配',
              key: 'assignmentList',
              width: '60%'
            }
          ],
          urls: {
            search: '/monitor/coordinator/group/member'
          }
        }
      }
    },
    coordinatorGroupExpiredMember: {
      type: Object,
      default: function () {
        return {
          colData: [
            {
              title: 'id',
              key: 'host'
            },
            {
              title: '最新心跳时间',
              key: 'latestHeartbeat',
              formatter (item) {
                return timeStampToString(item.latestHeartbeat)
              }
            },
            {
              title: '过期时间',
              key: 'expireTime',
              formatter (item) {
                return timeStampToString(item.expireTime)
              }
            },
            {
              title: '过期次数',
              key: 'expireTimes'
            }
          ],
          urls: {
            search: '/monitor/coordinator/group/expired/member'
          }
        }
      }
    },
    mqttClient: {
      type: Object,
      default: function () {
        return {
          colData: [
            {
              title: '客户端ID',
              key: 'clientId',
              width: 800
            },
            {
              title: '应用',
              key: 'application',
              width: 300
            },
            {
              title: 'IP',
              key: 'ipAddress',
              width: 1200
            },
            {
              title: '非持久化会话',
              key: 'cleanSession'
              // },{
              //   title:'保留消息',
              //   key:'isWillRetain'
            },
            {
              type: 'expand',
              title: '分组名称',
              // key:'clientGroupName',
              render: (h, params) => {
                return h('span', params.row.clientGroupName)
              }
            },
            {
              //   title:'服务等级',
              //   key:'willQos'
              // },{
              title: '版本',
              key: 'mqttVersion'
            },
            {
              title: '遗嘱标识',
              key: 'willFlag'
            },
            {
              title: '存活时间(s)',
              key: 'keepAliveTimeSeconds'
            },
            {
              title: '创建时间',
              key: 'createdTime',
              width: 400,
              formatter (item) {
                return timeStampToString(item.createdTime)
              }
            },
            {
              title: '操作时间',
              key: 'lastOperateTime',
              width: 400,
              formatter (item) {
                return timeStampToString(item.lastOperateTime)
              }
            }
          ],
          btns: [
            {
              txt: '断开',
              method: 'on-close-connection'
            }
          ],
          urls: {
            search: '/monitor/mqtt/proxy/connection/client',
            close: '/monitor/mqtt/proxy/closeConnection'
          },
          executorId: -1,
          inputable: false
        }
      }
    },
    search: {
      type: Object,
      default: function () {
        return {
          app: {
            code: ''
          },
          topic: {
            code: ''
          },
          namespace: {
            code: ''
          },
          subscribeGroup: '',
          type: this.$store.getters.consumerType,
          clientType: this.$store.getters.clientType
        }
      }
    }
  },
  data () {
    return {
      doSearch: true,
      subTab: 'partition'
      // type: this.$store.getters.consumerType,
      // app: {
      //   id: 0,
      //   code: ''
      // },
      // subscribeGroup: '',
      // topic: {
      //   id: '',
      //   code: ''
      // },
      // namespace: {
      //   id: '',
      //   code: ''
      // },
      // clientType: -1
    }
  },
  methods: {
    handleTabChange (data) {
      if (this.$route.query.consumerDetailVisible === '1') {
        let name = data.name
        this.$refs[name].search.app.code = this.$route.query.app || ''
        this.$refs[name].search.topic.code = this.$route.query.topic || ''
        this.$refs[name].search.namespace.code = this.$route.query.namespace || ''
        this.$refs[name].search.subscribeGroup = this.$route.query.subscribeGroup || ''
        this.$refs[name].search.clientType = this.$route.query.clientType != -1 ? this.$route.query.clientType : -1

        let routeName = ''
        if (this.detailType === this.$store.getters.appDetailType) {
          routeName = `/${this.$i18n.locale}/application/detail`
        } else {
          routeName = `/${this.$i18n.locale}/topic/detail`
        }

        this.$router.push({
          name: routeName,
          query: {
            id: this.$route.query.id,
            app: this.$route.query.app || '',
            topic: this.$route.query.topic || '',
            namespace: this.$route.query.namespace || '',
            clientType: this.$route.query.clientType != -1 ? this.$route.query.clientType : -1,
            subscribeGroup: this.$route.query.subscribeGroup || '',
            subTab: name,
            tab: this.$route.query.tab,
            producerDetailVisible: this.$route.query.producerDetailVisible || '0',
            consumerDetailVisible: this.$route.query.consumerDetailVisible || '0'
          }
        })
        this.$refs[name].getList()
      }
    },
    getList () {
      this.$refs[this.subTab].getList()
    }
  },
  watch: {
    '$route' (to, from) {
      if (to.query.tab === 'consumerDetail') {
        this.subTab = to.query.subTab || this.subTab
      }
    }
  }
}
</script>

<style scoped>

</style>
