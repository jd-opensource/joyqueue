<template>
  <div>
    <div class="ml20 mt10">
      <d-input v-model="searchData.keyword" placeholder="请输入要查询的IP/名称" class="left"
               oninput="value = value.trim()"
               style="width: 300px" @on-enter="getList">
        <span slot="prepend">关键词</span>
        <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>
      </d-input>
    </div>
    <my-table :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange"
              @on-current-change="handleCurrentChange" @on-selection-change="handleSelectionChange" @on-del="del"
              @on-leader="leader" @on-broker-detail="brokerDetail" @on-monitor-charts="goMonitorChart">
    </my-table>

  </div>
</template>

<script>
import apiRequest from '../../utils/apiRequest.js'
import myTable from '../../components/common/myTable.vue'
import crud from '../../mixins/crud.js'
import {brokerRoleTypeRender, brokerPermissionTypeRender} from '../../utils/common.js'
import {timeStampToString} from '../../utils/dateTimeUtils'

export default {
  name: 'group-detail',
  components: {
    myTable
  },
  props: {
    data: {
      type: Object
    }
  },
  mixins: [ crud ],
  data () {
    return {
      partitionGroup: {},
      urls: {
        search: '/partitionGroupReplica/search',
        del: '/partitionGroupReplica/delete',
        leader: '/partitionGroupReplica/leader',
        // findMetadata: '/monitor/broker/metadata',
        findCharts: '/monitor/broker/findCharts/',
        findDetail: '/monitor/broker/findBrokerDetail/'
      },
      searchData: {
        keyword: ''
      },
      tableData: {
        rowData: [{}],
        colData: [
          {
            title: 'BrokerId',
            key: 'brokerId',
            width: '10%'
          },
          {
            title: '分组',
            key: 'groupNo',
            width: '6%'
          },
          {
            title: 'IP:端口',
            key: 'broker.ip',
            width: '14%',
            render: (h, params) => {
              if (!params.item.broker) {
                return h('label', '')
              }
              const ip = params.item.broker.ip
              const port = params.item.broker.port
              return h('label', {
                style: {
                  color: '#3366FF'
                },
                on: {
                  click: () => {
                    this.$router.push({
                      path: '/' + this.$i18n.locale + '/setting/brokerMonitor',
                      query: {
                        brokerId: params.item.broker.id,
                        brokerIp: ip,
                        brokerPort: port
                      }
                    })
                  },
                  mousemove: (event) => {
                    event.target.style.cursor = 'pointer'
                  }
                }
              }, `${ip}:${port}`)
            }
          },
          // {
          //   title: 'Broker查询到的Leader',
          //   key: 'leaderAddress',
          //   width: '12%',
          //   render: (h, params) => {
          //     let label = ''
          //     if (params.item['leaderBrokerId']) {
          //       label = params.item['leaderBrokerId'] + ':' + params.item['leaderIp']
          //     }
          //     return h('label', {}, label)
          //   }
          // },
          {
            title: '机房 (编码/名称)',
            key: 'dataCenter.code',
            width: '17%',
            formatter (item) {
              if (item.dataCenter) {
                return item.dataCenter.code + '/' + item.dataCenter.name
              }
            }
          },
          {
            title: '版本',
            key: 'startupInfo.version',
            width: '12%'
          },
          {
            title: '启动时间',
            key: 'startupInfo.startupTime',
            width: '12%',
            formatter (item) {
              if (item.startupInfo) {
                return timeStampToString(item.startupInfo.startupTime)
              }
            }
          },
          {
            title: '权限',
            key: 'broker.permission',
            width: '7%',
            render: (h, params) => {
              if (params.item.broker) {
                return brokerPermissionTypeRender(h, params.item.broker.permission)
              }
            }
          },
          // {
          //   title: '重试类型',
          //   key: 'broker.retryType',
          //   width: '8%'
          // },
          {
            title: '角色',
            key: 'role',
            width: '8%',
            render: (h, params) => {
              return brokerRoleTypeRender(h, params.item.role)
            }
          }
        ],
        btns: [
          {
            txt: '指定leader',
            method: 'on-leader'
          },
          {
            txt: '监控图表',
            method: 'on-monitor-charts'
          }
        ]
      },
      multipleSelection: []
    }
  },
  methods: {
    handleSelectionChange (val) {
      this.multipleSelection = val
      this.$emit('on-choosed-broker', val)
    },
    brokerDetail (item) {
      this.$router.push({
        path: '/' + this.$i18n.locale + '/setting/brokerMonitor',
        query: {
          brokerId: item.broker.id,
          brokerIp: item.broker.ip,
          brokerPort: item.broker.port
        }
      })
    },
    goMonitorChart (item) {
      apiRequest.getBase(this.urls.findCharts + item.broker.id, {}, false).then((data) => {
        if (data.data) {
          for (let chart of data.data) {
            window.open(chart)
          }
        }
      })
    },
    leader (item) {
      let brokerIds = []
      for (let i = 0; i < this.multipleSelection.length; i++) {
        brokerIds.push(this.multipleSelection[i].brokerId)
      }
      item.outSyncReplicas = brokerIds

      let data = item
      apiRequest.post(this.urls.leader, {}, data).then((data) => {
        if (data.code === 200) {
          this.$Message.success('更新成功')
          this.getList()
        } else {
          this.$Message.error('更新失败')
        }
      })
    },
    // 查询
    getList () {
      // 1. 查询数据库里的数据
      this.showTablePin = true
      let data = {
        pagination: {
          page: this.page.page,
          size: this.page.size
        },
        query: {
          topic: this.partitionGroup.topic,
          namespace: this.partitionGroup.namespace,
          groupNo: this.partitionGroup.groupNo
        }
      }
      apiRequest.post(this.urlOrigin.search, {}, data).then((data) => {
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
          this.getDetail(this.tableData.rowData[i], i)
        }
      })
    },
    getDetail (row, index) {
      apiRequest.getBase(this.urls.findDetail + row.brokerId, {}, false)
        .then((data) => {
          this.tableData.rowData[index] = Object.assign(row, data.data || [])
          this.$set(this.tableData.rowData, index, this.tableData.rowData[index])
        })
    }
    // ,getMetadata (row, index) {
    //   let brokerId = row.brokerId
    //   let topicFullName = getTopicCode(this.data.topic, this.data.namespace)
    //   let group = this.data.groupNo
    //
    //   apiRequest.getBase(this.urls.findMetadata + '?brokerId=' + brokerId + '&topicFullName=' + topicFullName + '&group=' + group, {}, false)
    //     .then((data) => {
    //       this.tableData.rowData[index] = Object.assign(row, data.data || [])
    //       this.$set(this.tableData.rowData, index, this.tableData.rowData[index])
    //     })
    // }
  },
  mounted () {
    this.partitionGroup = this.data
    this.getList()
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  .label{text-align: right; line-height: 32px;}
  .val{}
</style>
