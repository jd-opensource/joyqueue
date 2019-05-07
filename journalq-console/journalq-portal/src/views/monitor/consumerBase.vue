<template>
  <div>
    <div class="ml20 mt30">
      <d-input v-model="keyword" :placeholder="keywordTip" class="left mr10" style="width: 213px" @on-enter="getList">
        <span slot="prepend">{{keywordName}}</span>
        <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>
      </d-input>
      <d-button type="primary" v-if="$store.getters.isAdmin" @click="openDialog('subscribeDialog')" class="left mr10">
        订阅
        <icon name="plus-circle" style="margin-left: 5px;">
        </icon>
      </d-button>
    </div>

    <my-table :data="tableData" :showPin="showTablePin" :page="page"
              @on-detail-chart="goDetailChart" @on-current-change="handleCurrentChange" @on-detail="openDetailDialog"
              @on-msg-preview="openMsgPreviewDialog" @on-msg-detail="openMsgDetailDialog" @on-config="openConfigDialog"
              @on-performance-chart="goPerformanceChart" @on-summary-chart="goSummaryChart" @on-size-change="handleSizeChange"/>

    <!--Consumer subscribe dialog-->
    <my-dialog :dialog="subscribeDialog" @on-dialog-cancel="dialogCancel('subscribeDialog')">
      <subscribe ref="subscribe" :search="search" :type="type" :colData="subscribeDialog.colData"
                 :searchUrl="subscribeDialog.urls.search" :addUrl="subscribeDialog.urls.add"
                 :keywordName="keywordName"
                 :doSearch="subscribeDialog.doSearch" @on-refresh="getList"/>
    </my-dialog>

    <!--Detail dialog-->
    <my-dialog :dialog="detailDialog" @on-dialog-cancel="dialogCancel('detailDialog')">
      <d-tabs @on-change="handleTabChange">
        <d-tab-pane label="分组" name="partition" icon="pocket">
          <partition ref="partition" :app="detailDialog.app" :topic="detailDialog.topic"
                     :colData="detailDialog.partition.colData" :namespace="detailDialog.namespace"
                     :type="type" :doSearch="detailDialog.doSearch" :subscribeGroup="detailDialog.subscribeGroup"/>
        </d-tab-pane>
        <d-tab-pane label="客户端连接" name="clientConnection" icon="github">
          <client-connection ref="clientConnection" :app="detailDialog.app" :topic="detailDialog.topic" :namespace="detailDialog.namespace"
                             :type="type" :doSearch="detailDialog.doSearch" :subscribeGroup="detailDialog.subscribeGroup"/>
        </d-tab-pane>
        <d-tab-pane v-if="detailDialog.clientType==2" label="mqtt客户端" name="mqttClient" icon="file-text">
          <mqtt-base-monitor  ref="mqttClient" :client-id="detailDialog.subscribeGroup"  :inputable=false :btns="mqttConnectionsProperties.btns"
                      :col-data="mqttConnectionsProperties.colData"   :search="mqttConnectionsProperties.search"/>
        </d-tab-pane>

        <d-tab-pane  label="消费位置" name="offsetInfo" icon="file-text">
          <offset  ref="offsetInfo" :app="detailDialog.app" :topic="detailDialog.topic" :namespace="detailDialog.namespace"
                      :type="type"  :doSearch="detailDialog.doSearch" :subscribeGroup="detailDialog.subscribeGroup"
                      :searchData="detailDialog" :client-type="detailDialog.clientType"/>
        </d-tab-pane>

        <d-tab-pane label="Broker" name="broker" icon="file-text">
          <broker ref="broker" :app="detailDialog.app" :topic="detailDialog.topic" :namespace="detailDialog.namespace"
                  :type="type" :doSearch="detailDialog.doSearch" :subscribeGroup="detailDialog.subscribeGroup"/>
        </d-tab-pane>

        <d-tab-pane v-if="$store.getters.isAdmin" label="协调者信息" name="coordinatorInfo" icon="file-text">
          <tab-table  ref="coordinatorInfo" :app="detailDialog.app" :topic="detailDialog.topic" :namespace="detailDialog.namespace"
                      :type="type"  :doSearch="detailDialog.doSearch" :client-type="detailDialog.clientType"  :subscribeGroup="detailDialog.subscribeGroup" :searchData="detailDialog"
                      :col-data="coordinatorTable.colData"   :search="coordinatorTable.search"/>
        </d-tab-pane>

        <d-tab-pane v-if="$store.getters.isAdmin" label="消费组成员" name="coordinatorGroupMember" icon="file-text">
          <tab-coordinator-group ref="coordinatorGroupMember" :app="detailDialog.app" :topic="detailDialog.topic" :namespace="detailDialog.namespace"
                     :type="type" :client-type="detailDialog.clientType"  :doSearch="detailDialog.doSearch" :subscribeGroup="detailDialog.subscribeGroup" :searchData="detailDialog"
                     :col-data="coordinatorGroupMemberTable.colData" :search="coordinatorGroupMemberTable.search"/>
        </d-tab-pane>
        <d-tab-pane v-if="$store.getters.isAdmin" label="消费组过期成员" name="coordinatorGroupExpiredMember" icon="file-text">
          <tab-table ref="coordinatorGroupExpiredMember" :app="detailDialog.app" :topic="detailDialog.topic" :namespace="detailDialog.namespace"
                     :type="type" :client-type="detailDialog.clientType"  :doSearch="detailDialog.doSearch" :subscribeGroup="detailDialog.subscribeGroup" :searchData="detailDialog"
                     :col-data="coordinatorGroupExpiredMemberTable.colData" :search="coordinatorGroupExpiredMemberTable.search"/>
        </d-tab-pane>
      </d-tabs>
    </my-dialog>

    <!--Msg preview dialog-->
    <my-dialog :dialog="msgPreviewDialog" @on-dialog-cancel="dialogCancel('msgPreviewDialog')">
      <msg-preview :app="msgPreviewDialog.app" :topic="msgPreviewDialog.topic" :namespace="msgPreviewDialog.namespace"
                   :type="type" :doSearch="msgPreviewDialog.doSearch" :subscribeGroup="msgPreviewDialog.subscribeGroup"/>
    </my-dialog>
    <my-dialog :dialog="msgDetailDialog" @on-dialog-cancel="dialogCancel('msgDetailDialog')">
      <msg-detail :app="msgDetailDialog.app" :topic="msgDetailDialog.topic" :namespace="msgDetailDialog.namespace"
                   :type="type" :doSearch="msgDetailDialog.doSearch" :subscribeGroup="msgDetailDialog.subscribeGroup"/>
    </my-dialog>
    <!--Config dialog-->
    <my-dialog :dialog="configDialog" @on-dialog-confirm="configConsumerConfirm" @on-dialog-cancel="dialogCancel('configDialog')">
      <consumer-config-form ref="configForm" :data="configConsumerData"/>
    </my-dialog>

  </div>
</template>

<script>
import apiRequest from '../../utils/apiRequest.js'
import myTable from '../../components/common/myTable.vue'
import myDialog from '../../components/common/myDialog.vue'
import consumerConfigForm from './consumerConfigForm.vue'
import subscribe from './subscribe.vue'
import broker from './broker.vue'
import partition from './partition.vue'
import clientConnection from './clientConnection.vue'
import msgPreview from './msgPreview.vue'
import tabTable from './tabTable'
import coordinatorGroupMember from './coordinatorGroup.vue'
import partitionExpand from './partitionExpand'
import {timeStampToString} from '../../utils/dateTimeUtils'
import mqttBaseMonitor from '../setting/mqttBaseMonitor'
import offset from './offset'
import {getTopicCode, getAppCode, replaceChartUrl} from '../../utils/common.js'
import MsgDetail from './msgDetail'

export default {
  name: 'consumer-base',
  components: {
    MsgDetail,
    myTable,
    myDialog,
    subscribe,
    broker,
    partition,
    clientConnection,
    msgPreview,
    consumerConfigForm,
    tabTable,
    partitionExpand,
    mqttBaseMonitor,
    offset,
    coordinatorGroupMember
  },
  props: {
    keywordTip: {
      type: String
    },
    keywordName: {
      type: String
    },
    btns: {
      type: Array,
      default: function () {
        return [
          {
            txt: '消费详情',
            method: 'on-detail'
          },
          {
            txt: '配置',
            method: 'on-config'
          }
          // ,
          // {
          //   txt: '详情监控图表',
          //   method: 'on-detail-chart'
          // },
          // {
          //   txt: '汇总监控图表',
          //   method: 'on-summary-chart'
          // },
          // {
          //   txt: '性能监控图表',
          //   method: 'on-performance-chart'
          // }
        ]
      }
    },
    operates: {
      type: Array,
      default: function () {
        return [
          {
            txt: '消息预览',
            method: 'on-msg-preview'
          },
          {
            txt: '消息查询',
            method: 'on-msg-detail'
          }
        ]
      }
    },
    colData: { // 消费者 列表表头
      type: Array
    },
    search: {// 查询条件，我的应用：app:{id:0,code:'',namespace:{id:0,code:''}}  ， 主题中心：topic:{id:0,code:'',namespace:{id:0,code:''}}
      type: Object
    },
    subscribeDialogColData: { // 订阅列表表头
      type: Array
    },
    subscribeUrls: {
      type: Object
    },
    partitionColData: { // 分片列表表头
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
                      title: 'ID',
                      key: 'partitionGroup'
                    },
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
            key: 'partitions'
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
    monitorUrls: {// url variable format: [app], [topic], [namespace]
      type: Object
    }
  },
  data () {
    return {
      urls: {
        search: `/consumer/search`,
        getMonitor: `/monitor/find`,
        previewMessage: '/monitor/preview/message',
        getUrl: `/grafana/getRedirectUrl`
      },
      showTablePin: false,
      tableData: {
        rowData: [],
        colData: this.colData,
        btns: this.btns,
        operates: this.operates
      },
      keyword: '',
      page: {
        page: 1,
        size: 10,
        total: 100
      },
      type: this.$store.getters.consumerType, // 1:生产， 2：消费
      subscribeDialog: {
        visible: false,
        title: '添加消费者',
        width: '950',
        showFooter: false,
        doSearch: false,
        colData: this.subscribeDialogColData, // 订阅框 列表表头,
        urls: {
          add: this.subscribeUrls.add,
          search: this.subscribeUrls.search
        }
      },
      detailDialog: {
        visible: false,
        title: '消费详情',
        width: '1000',
        showFooter: false,
        scrollable: true,
        doSearch: true,
        app: {
          id: 0,
          code: ''
        },
        subscribeGroup: '',
        topic: {
          id: '0',
          code: ''
        },
        namespace: {
          id: '0',
          code: ''
        },
        partition: {
          colData: this.partitionColData
        },
        clientType: -1
      },
      configConsumerData: {},
      configDialog: {
        visible: false,
        title: '消费者配置详情',
        width: '600',
        showFooter: true,
        urls: {
          addOrUpdate: `/consumer/config/addOrUpdate`,
          search: 'consumer/config/search'
        }
      },
      msgPreviewDialog: {
        visible: false,
        title: '生产者详情',
        width: '1000',
        showFooter: false,
        doSearch: true,
        app: {
          id: 0,
          code: ''
        },
        subscribeGroup: '',
        topic: {
          id: '0',
          code: ''
        },
        namespace: {
          id: '0',
          code: ''
        }
      },
      msgDetailDialog: {
        visible: false,
        title: '生产者详情',
        width: '1000',
        showFooter: false,
        doSearch: true,
        app: {
          id: 0,
          code: ''
        },
        subscribeGroup: '',
        topic: {
          id: '0',
          code: ''
        },
        namespace: {
          id: '0',
          code: ''
        }
      },
      mqttConnectionsProperties: {
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
        search: '/monitor/mqtt/proxy/connection/client',
        close: '/monitor/mqtt/proxy/closeConnection',
        executorId: -1,
        inputable: false
      },
      coordinatorTable: {
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
        search: '/monitor/coordinator'
      },
      coordinatorGroupMemberTable: {
        colData: [
          {
            title: 'id',
            key: 'connectionHost'
          },
          {
            title: '最新心跳时间',
            key: 'latestHeartbeat',
            formatter (item) {
              return timeStampToString(item.latestHeartbeat)
            }
          },
          {
            title: '会话超时',
            key: 'sessionTimeout'
          },
          {
            title: '分区分配',
            key: 'assignmentList'
          }
        ],
        search: '/monitor/coordinator/group/member'
      },
      coordinatorGroupExpiredMemberTable: {
        colData: [
          {
            title: 'id',
            key: 'hosts'
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
        search: '/monitor/coordinator/group/expired/member'
      }

    }
  },
  methods: {
    openDialog (dialog) {
      this[dialog].doSearch = true
      this[dialog].visible = true
    },
    openDetailDialog (item) {
      this.detailDialog.app.id = item.app.id
      this.detailDialog.app.code = item.app.code
      this.detailDialog.subscribeGroup = item.subscribeGroup
      this.detailDialog.topic.id = item.topic.id
      this.detailDialog.topic.code = item.topic.code
      this.detailDialog.namespace.id = item.namespace.id
      this.detailDialog.namespace.code = item.namespace.code
      this.detailDialog.clientType = item.clientType

      this.openDialog('detailDialog')
    },
    openMsgPreviewDialog (item) {
      this.msgPreviewDialog.app.id = item.app.id
      this.msgPreviewDialog.app.code = item.app.code
      this.msgPreviewDialog.subscribeGroup = item.subscribeGroup
      this.msgPreviewDialog.topic.id = item.topic.id
      this.msgPreviewDialog.topic.code = item.topic.code
      this.msgPreviewDialog.namespace.id = item.namespace.id
      this.msgPreviewDialog.namespace.code = item.namespace.code
      this.msgPreviewDialog.title = '消息预览 [App: ' + this.msgPreviewDialog.app.code +
              ', Topic: ' + this.msgPreviewDialog.topic.code + ']'
      this.openDialog('msgPreviewDialog')
    },
    openMsgDetailDialog (item) {
      this.msgDetailDialog.app.id = item.app.id
      this.msgDetailDialog.app.code = item.app.code
      this.msgDetailDialog.subscribeGroup = item.subscribeGroup
      this.msgDetailDialog.topic.id = item.topic.id
      this.msgDetailDialog.topic.code = item.topic.code
      this.msgDetailDialog.namespace.id = item.namespace.id
      this.msgDetailDialog.namespace.code = item.namespace.code
      this.msgDetailDialog.title = '消息查询 [App: ' + this.msgDetailDialog.app.code +
        ', Topic: ' + this.msgDetailDialog.topic.code + ']'
      this.openDialog('msgDetailDialog')
    },
    openConfigDialog (item) {
      this.configConsumerData = Object.assign({}, item.config)
      this.configConsumerData['consumerId'] = item.id
      this.configDialog.visible = true
    },
    handleSizeChange (val) {
      this.page.size = val
      this.getList()
    },
    handleCurrentChange (val) {
      this.page.page = val
      this.getList()
    },
    handleTabChange (data) {
      let name = data.name
      this.$refs[name].getList()
    },
    dialogConfirm (dialog) {
      this[dialog].visible = false
      this.getList()
    },
    dialogCancel (dialog) {
      this[dialog].visible = false
      this.getList()
    },
    goSummaryChart (item) {
      if (this.monitorUrls && this.monitorUrls.summary) {
        window.open(replaceChartUrl(this.monitorUrls.summary, item.topic.namespace.code,
          item.topic.code, getAppCode(item.app, item.subscribeGroup)))
      } else {
        apiRequest.get(this.urls.getUrl + '/ct', {}, {}).then((data) => {
          if (data.data) {
            let url = data.data
            if (url.indexOf('?') < 0) {
              url += '?'
            } else if (!url.endsWith('?')) {
              url += '&'
            }
            url = url + 'var-topic=' + getTopicCode(item.topic, item.topic.namespace) + '&var-app=' +
              getAppCode(item.app, item.subscribeGroup)
            window.open(url)
          }
        })
      }
    },
    goDetailChart (item) {
      if (this.monitorUrls && this.monitorUrls.detail) {
        window.open(replaceChartUrl(this.monitorUrls.detail, item.topic.namespace.code,
          item.topic.code, getAppCode(item.app, item.subscribeGroup)))
      } else {
        apiRequest.get(this.urls.getUrl + '/cd', {}, {}).then((data) => {
          if (data.data) {
            let url = data.data
            if (url.indexOf('?') < 0) {
              url += '?'
            } else if (!url.endsWith('?')) {
              url += '&'
            }
            url = url + 'var-topic=' + getTopicCode(item.topic, item.topic.namespace) + '&var-app=' +
              getAppCode(item.app, item.subscribeGroup)
            window.open(url)
          }
        })
      }
    },
    goPerformanceChart (item) {
      if (this.monitorUrls && this.monitorUrls.performance) {
        window.open(replaceChartUrl(this.monitorUrls.performance, item.topic.namespace.code,
          item.topic.code, getAppCode(item.app, item.subscribeGroup)))
      } else {
        apiRequest.get(this.urls.getUrl + '/cp', {}, {}).then((data) => {
          if (data.data) {
            let url = data.data
            if (url.indexOf('?') < 0) {
              url += '?'
            } else if (!url.endsWith('?')) {
              url += '&'
            }
            url = url + 'var-topic=' + getTopicCode(item.topic, item.topic.namespace) + '&var-app=' +
              getAppCode(item.app, item.subscribeGroup)
            window.open(url)
          }
        })
      }
    },
    getMonitor (row, index) {
      let data = {
        topic: {
          id: row.topic.id,
          code: row.topic.code
        },
        namespace: {
          id: row.namespace.id,
          code: row.namespace.code
        },
        app: {
          id: row.app.id,
          code: row.app.code
        },
        subscribeGroup: row.subscribeGroup || '',
        type: this.type
      }

      apiRequest.postBase(this.urls.getMonitor, {}, data, false).then((data) => {
        this.tableData.rowData[index] = Object.assign(row, data.data || [])
        this.$set(this.tableData.rowData, index, this.tableData.rowData[index])
      })
    },
    configConsumerConfirm () {
      this.$refs.configForm.submitForm().then(data => {
        // resolve(data);
        this.addOrUpdateConfig()
      }).catch(() => {
        // reject(error)
        // this.$Message.error(error);
      }) // validate
    },
    addOrUpdateConfig () {
      apiRequest.post(this.configDialog.urls.addOrUpdate, {}, this.configConsumerData).then(() => {
        this.configDialog.visible = false
        this.getList()
      })
    },
    // 查询
    getList () {
      // 查询数据库里的数据
      this.showTablePin = true
      let data = {
        pagination: {
          page: this.page.page,
          size: this.page.size
        },
        query: {
          keyword: this.keyword
        }
      }
      for (var i in this.search) {
        if (this.search.hasOwnProperty(i)) {
          data.query[i] = this.search[i]
        }
      }
      apiRequest.post(this.urls.search, {}, data).then((data) => {
        data.data = data.data || []
        data.pagination = data.pagination || {
          totalRecord: data.data.length
        }
        this.page.total = data.pagination.totalRecord
        this.page.page = data.pagination.page
        this.page.size = data.pagination.size
        this.tableData.rowData = data.data
        this.showTablePin = false
        for (let i = 0; i < this.tableData.rowData.length; i++) {
          this.getMonitor(this.tableData.rowData[i], i)
        }
      })
    }
  },
  mounted () {
    // this.getList();
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  .label{text-align: right; line-height: 32px;}
  .val{}
</style>
