<template>
  <div>
    <d-tabs @on-change="menuChange">
      <d-tab-pane label="服务器监控" name="brokerServerMonitor">
        <grid-row>
          <grid-col>
            <d-button type="primary" class="right" style="margin-right: 20px" @click="$refs.brokerServerMonitor.getList()">刷新
              <icon name="plus-circle" style="margin-left: 3px;"></icon>
            </d-button>
          </grid-col>
        </grid-row>
        <grid-row>
          <grid-col>
            <broker-server-monitor ref="brokerServerMonitor"> </broker-server-monitor>
          </grid-col>
        </grid-row>
      </d-tab-pane>
      <d-tab-pane label="生产者详情" name="producer">
        <d-button type="primary" class="right" style="margin-right: 20px" @click="getList">刷新
          <icon name="plus-circle" style="margin-left: 3px;"></icon>
        </d-button>
        <my-table :data="tableData" :showPin="showTablePin" style="overflow-y:auto" :page="page" @on-size-change="handleSizeChange" @on-current-change="handleCurrentChange" @on-selection-change="handleSelectionChange"
                  @on-edit="edit">
        </my-table>
      </d-tab-pane>
      <d-tab-pane label="消费者详情" name="consumer">
        <d-button type="primary" class="right" style="margin-right: 20px" @click="getList">刷新
          <icon name="plus-circle" style="margin-left: 3px;"></icon>
        </d-button>
        <my-table :data="tableData" :showPin="showTablePin" style="overflow-y:auto" :page="page" @on-size-change="handleSizeChange" @on-current-change="handleCurrentChange" @on-selection-change="handleSelectionChange"
                  @on-edit="edit">
        </my-table>
      </d-tab-pane>
      <d-tab-pane label="连接信息" name="brokerConnectionMonitor">
        <grid-row>
          <grid-col>
            <d-button type="primary" class="right" style="margin-right: 20px" @click="$refs.brokerConnectionMonitor.getList()">刷新
              <icon name="plus-circle" style="margin-left: 3px;"></icon>
            </d-button>
          </grid-col>
        </grid-row>
        <grid-row>
          <grid-col>
            <broker-connection-monitor ref="brokerConnectionMonitor" style="overflow-y:auto"></broker-connection-monitor>
          </grid-col>
        </grid-row>
      </d-tab-pane>
      <d-tab-pane label="分区组" name="brokerPartitionGroupMonitor">
        <grid-row>
          <grid-col>
            <d-button type="primary" class="right" style="margin-right: 20px" @click="$refs.brokerPartitionGroupMonitor.getList()">刷新
              <icon name="plus-circle" style="margin-left: 3px;"></icon>
            </d-button>
          </grid-col>
        </grid-row>
        <grid-row>
          <grid-col>
            <broker-partition-group-monitor ref="brokerPartitionGroupMonitor" style="overflow-y:auto"></broker-partition-group-monitor>
          </grid-col>
        </grid-row>
      </d-tab-pane>
      <d-tab-pane label="服务器存储文件" name="brokerStoreTreeViewMonitor">
        <grid-row>
          <grid-col>
            <d-button type="primary" class="right" style="margin-right: 20px" @click="$refs.brokerPartitionGroupMonitor.getList()">刷新
              <icon name="plus-circle" style="margin-left: 3px;"></icon>
            </d-button>
          </grid-col>
        </grid-row>
        <grid-row>
          <grid-col>
            <broker-store-tree-view-monitor ref="brokerStoreTreeViewMonitor"> </broker-store-tree-view-monitor>
          </grid-col>
        </grid-row>
      </d-tab-pane>
    </d-tabs>
  </div>
</template>

<script>
import myTable from '../../components/common/myTable.vue'
import myDialog from '../../components/common/myDialog.vue'
import crud from '../../mixins/crud.js'
import ClientConnection from '../monitor/detail/clientConnection'
import BrokerConnectionMonitor from './brokerConnectionMonitor'
import BrokerPartitionGroupMonitor from './brokerPartitionGroupMonitor'
import BrokerServerMonitor from './brokerServerMonitor.vue'
import BrokerStoreTreeViewMonitor from './brokerStoreTreeViewMonitor.vue'
import {bytesToSize} from '../../utils/common'
import GridRow from '../../components/grid/row'
import GridCol from '../../components/grid/col'
import apiRequest from '../../utils/apiRequest.js'

export default {
  name: 'brokerMonitor',
  components: {
    GridCol,
    GridRow,
    BrokerPartitionGroupMonitor,
    BrokerConnectionMonitor,
    ClientConnection,
    myTable,
    myDialog,
    BrokerServerMonitor,
    BrokerStoreTreeViewMonitor
  },
  props: {
  },
  mixins: [ crud ],
  data () {
    return {
      theme1: 'light',
      brokerId: this.$route.params.brokerId || this.$route.query.brokerId,
      urls: {
        search: '/monitor/broker/topic/search'
      },
      searchData: {
        brokerId: this.$route.params.brokerId || this.$route.query.brokerId,
        type: 1
      },
      searchRules: {
      },
      tableData: {
        rowData: [],
        colData: [
          {
            title: '主题',
            key: 'topic',
            render: (h, params) => {
              let topic = params.item.topic
              let html = []
              let p = h('router-link', {
                style: {
                  'text-decoration': 'underline',
                  color: 'dodgerblue'
                },
                attrs: {
                  to: '/' + this.$i18n.locale + '/topic/detail?id=' + topic + '&topic=' + topic
                }
              }, params.item.topic)
              html.push(p)
              return h('div', {}, html)
            }
          },
          {
            title: '应用',
            key: 'brokerTopicMonitorRecordList',
            render: (h, params) => {
              let list = params.item.brokerTopicMonitorRecordList
              let html = []
              if (list !== undefined) {
                for (let i = 0; i < list.length; i++) {
                  let p = h('router-link', {
                    style: {
                      'text-decoration': 'underline',
                      color: 'dodgerblue'
                    },
                    attrs: {
                      to: '/' + this.$i18n.locale + '/application/detail?app=' + list[i].app + '&id=' + list[i].id
                    }
                  }, list[i].app)
                  html.push(p)
                  if (i < list.length - 1) {
                    html.push(h('br'))
                  }
                }
              }
              return h('div', {}, html)
            }
          },
          {
            title: '连接数',
            key: 'brokerTopicMonitorRecordList',
            render: (h, params) => {
              let list = params.item.brokerTopicMonitorRecordList
              let html = []
              if (list !== undefined) {
                for (let i = 0; i < list.length; i++) {
                  let p = h('div', list[i].connections)
                  html.push(p)
                }
              }
              return h('div', html)
            }
          },
          {
            title: '出/入队数',
            key: 'brokerTopicMonitorRecordList',
            render: (h, params) => {
              let list = params.item.brokerTopicMonitorRecordList
              let html = []
              if (list !== undefined) {
                for (let i = 0; i < list.length; i++) {
                  let p = h('div', list[i].count)
                  html.push(p)
                }
              }
              return h('div', html)
            }
          },
          {
            title: '出/入队流量',
            key: 'brokerTopicMonitorRecordList',
            render: (h, params) => {
              let list = params.item.brokerTopicMonitorRecordList
              let html = []
              if (list !== undefined) {
                for (let i = 0; i < list.length; i++) {
                  let p = h('div', bytesToSize(list[i].totalSize, 2, true))
                  html.push(p)
                }
              }
              return h('div', html)
            }
          }
        ]
      },
      producerTableData: {
        rowData: [],
        colData: [
          {
            title: '主题',
            key: 'topic',
            render: (h, params) => {
              let topic = params.item.topic
              let html = []
              let p = h('router-link', {
                style: {
                  'text-decoration': 'underline',
                  color: 'dodgerblue'
                },
                attrs: {
                  to: '/' + this.$i18n.locale + '/topic/detail?id=' + topic + '&topic=' + topic
                }
              }, params.item.topic)
              html.push(p)
              return h('div', {}, html)
            }
          },
          {
            title: '应用',
            key: 'brokerTopicMonitorRecordList',
            render: (h, params) => {
              let list = params.item.brokerTopicMonitorRecordList
              let html = []
              if (list !== undefined) {
                for (let i = 0; i < list.length; i++) {
                  let p = h('div', '')
                  if (list[i].id) {
                      p = h('router-link', {
                      style: {
                        'text-decoration': 'underline',
                        color: 'dodgerblue'
                      },
                      attrs: {
                        to: '/' + this.$i18n.locale + '/application/detail?app=' + list[i].app + '&id=' + list[i].id
                      }
                    }, list[i].app)
                  } else {
                    p = h('div', list[i].app)
                  }
                  html.push(p)
                  if (i < list.length - 1) {
                    html.push(h('br'))
                  }
                }
              }
              return h('div', {}, html)
            }
          },
          {
            title: '连接数',
            key: 'brokerTopicMonitorRecordList',
            render: (h, params) => {
              let list = params.item.brokerTopicMonitorRecordList
              let html = []
              if (list !== undefined) {
                for (let i = 0; i < list.length; i++) {
                  let p = h('div', list[i].connections)
                  html.push(p)
                }
              }
              return h('div', html)
            }
          },
          {
            title: '入队数',
            key: 'brokerTopicMonitorRecordList',
            render: (h, params) => {
              let list = params.item.brokerTopicMonitorRecordList
              let html = []
              if (list !== undefined) {
                for (let i = 0; i < list.length; i++) {
                  let p = h('div', list[i].count)
                  html.push(p)
                }
              }
              return h('div', html)
            }
          },
          {
            title: '入队流量',
            key: 'brokerTopicMonitorRecordList',
            render: (h, params) => {
              let list = params.item.brokerTopicMonitorRecordList
              let html = []
              if (list !== undefined) {
                for (let i = 0; i < list.length; i++) {
                  let p = h('div', bytesToSize(list[i].totalSize, 2, true))
                  html.push(p)
                }
              }
              return h('div', html)
            }
          },
          {
            title: '入队TPS',
            key: 'brokerTopicMonitorRecordList',
            render: (h, params) => {
              let list = params.item.brokerTopicMonitorRecordList
              let html = []
              if (list !== undefined) {
                for (let i = 0; i < list.length; i++) {
                  let p = h('div', list[i].tps)
                  html.push(p)
                }
              }
              return h('div', {}, html)
            }
          }
        ]
      },
      consumerTableData: {
        rowData: [],
        colData: [
          {
            title: '主题',
            key: 'topic',
            render: (h, params) => {
              let topic = params.item.topic
              let html = []
              let p = h('router-link', {
                style: {
                  'text-decoration': 'underline',
                  color: 'dodgerblue'
                },
                attrs: {
                  to: '/' + this.$i18n.locale + '/topic/detail?id=' + topic + '&topic=' + topic
                }
              }, params.item.topic)
              html.push(p)
              return h('div', {}, html)
            }
          },
          {
            title: '应用',
            key: 'brokerTopicMonitorRecordList',
            render: (h, params) => {
              let list = params.item.brokerTopicMonitorRecordList
              let html = []
              if (list !== undefined) {
                for (let i = 0; i < list.length; i++) {
                  let p = h('div', '')
                  if (list[i].id) {
                      p = h('router-link', {
                      style: {
                        'text-decoration': 'underline',
                        color: 'dodgerblue'
                      },
                      attrs: {
                        to: '/' + this.$i18n.locale + '/application/detail?app=' + list[i].app + '&id=' + list[i].id
                      }
                    }, list[i].app)
                  } else {
                    p = h('div', list[i].app)
                  }
                  html.push(p)
                  if (i < list.length - 1) {
                    html.push(h('br'))
                  }
                }
              }
              return h('div', {}, html)
            }
          },
          {
            title: '连接数',
            key: 'brokerTopicMonitorRecordList',
            render: (h, params) => {
              let list = params.item.brokerTopicMonitorRecordList
              let html = []
              if (list !== undefined) {
                for (let i = 0; i < list.length; i++) {
                  let p = h('div', list[i].connections)
                  html.push(p)
                }
              }
              return h('div', html)
            }
          },
          {
            title: '出队数',
            key: 'brokerTopicMonitorRecordList',
            render: (h, params) => {
              let list = params.item.brokerTopicMonitorRecordList
              let html = []
              if (list !== undefined) {
                for (let i = 0; i < list.length; i++) {
                  let p = h('div', list[i].count)
                  html.push(p)
                }
              }
              return h('div', html)
            }
          },
          {
            title: '出队流量',
            key: 'brokerTopicMonitorRecordList',
            render: (h, params) => {
              let list = params.item.brokerTopicMonitorRecordList
              let html = []
              if (list !== undefined) {
                for (let i = 0; i < list.length; i++) {
                  let p = h('div', bytesToSize(list[i].totalSize, 2, true))
                  html.push(p)
                }
              }
              return h('div', html)
            }
          },
          {
            title: '出队TPS',
            key: 'brokerTopicMonitorRecordList',
            render: (h, params) => {
              let list = params.item.brokerTopicMonitorRecordList
              let html = []
              if (list !== undefined) {
                for (let i = 0; i < list.length; i++) {
                  let p = h('div', list[i].tps)
                  html.push(p)
                }
              }
              return h('div', html)
            }
          },
          {
            title: '重试数',
            key: 'brokerTopicMonitorRecordList',
            render: (h, params) => {
              let list = params.item.brokerTopicMonitorRecordList
              let html = []
              if (list !== undefined) {
                for (let i = 0; i < list.length; i++) {
                  let p = h('div', list[i].retryCount)
                  html.push(p)
                }
              }
              return h('div', {}, html)
            }
          },
          {
            title: '重试tps',
            key: 'brokerTopicMonitorRecordList',
            render: (h, params) => {
              let list = params.item.brokerTopicMonitorRecordList
              let html = []
              if (list !== undefined) {
                for (let i = 0; i < list.length; i++) {
                  let p = h('div', list[i].retryTps)
                  html.push(p)
                }
              }
              return h('div', {}, html)
            }
          },
          {
            title: '积压数',
            key: 'brokerTopicMonitorRecordList',
            render: (h, params) => {
              let list = params.item.brokerTopicMonitorRecordList
              let html = []
              if (list !== undefined) {
                for (let i = 0; i < list.length; i++) {
                  let p = h('div', list[i].backlog)
                  html.push(p)
                }
              }
              return h('div', {}, html)
            }
          }
        ]
        // 表格操作，如果需要根据特定值隐藏显示， 设置bindKey对应的属性名和bindVal对应的属性值
        // btns: [
        //   {
        //     txt: '编辑',
        //     method: 'on-edit'
        //   }
        // ]
      },
      multipleSelection: []
    }
  },
  methods: {
    menuChange (item) {
      if (item.name === 'consumer') {
        this.searchData.type = 2
        this.tableData = this.consumerTableData
        this.getList()
      } else if (item.name === 'producer') {
        this.searchData.type = 1
        this.tableData = this.producerTableData
        this.getList()
      } else {
        this.tableData = this.producerTableData
        this.$refs[item.name].getList()
      }
    },
    getList () {
      this.showTablePin = true
      let data = this.getSearchVal()
      apiRequest.post(this.urlOrigin.search, {}, data).then((data) => {
        if (data === '') {
          return
        }
        data.data = data.data || []
        data.pagination = data.pagination || {
          totalRecord: data.data.length
        }
        this.page.total = data.pagination.totalRecord
        this.page.page = data.pagination.page
        this.page.size = data.pagination.size
        this.tableData.rowData = data.data
        for (let i in this.tableData.rowData) {
          if (this.tableData.rowData.hasOwnProperty(i)) {
            if (this.tableData.rowData[i].brokerTopicMonitorRecordList) {
              for (let j in this.tableData.rowData[i].brokerTopicMonitorRecordList) {
                if (this.tableData.rowData[i].brokerTopicMonitorRecordList.hasOwnProperty(j)) {
                  let app = this.tableData.rowData[i].brokerTopicMonitorRecordList[j].app
                  let idx = app.indexOf('.')
                  if (idx !== -1) {
                    app = app.substring(0, idx)
                  }
                  apiRequest.get('/application/getByCode/' + app, {}).then((data) => {
                    this.tableData.rowData[i].brokerTopicMonitorRecordList[j].id = data.data.id
                    this.$set(this.tableData.rowData, i, this.tableData.rowData[i])
                  })
                }
              }
            }
          }
        }
        this.showTablePin = false
      })
    },
  },
  mounted () {

  }
}
</script>
