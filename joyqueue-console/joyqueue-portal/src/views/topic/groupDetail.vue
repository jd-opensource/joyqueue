<template>
  <div>
    <div class="ml20 mt10">
      <d-input v-model="searchData.keyword" placeholder="请输入要查询的IP/名称" class="left"
               style="width: 300px" @on-enter="getList">
        <span slot="prepend">关键词</span>
        <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>
      </d-input>
    </div>
    <my-table :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange"
              @on-current-change="handleCurrentChange" @on-selection-change="handleSelectionChange" @on-del="del" @on-leader="leader" @on-broker-detail="brokerDetail">
    </my-table>

  </div>
</template>

<script>
import apiRequest from '../../utils/apiRequest.js'
import myTable from '../../components/common/myTable.vue'
import crud from '../../mixins/crud.js'
import {getTopicCode} from '../../utils/common.js'
export default {
  name: 'group-detail',
  components: {
    myTable
  },
  props: {
    data: {
      type: Object,
      default () {
        return {}
      }
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
        findMetadata: '/monitor/broker/metadata'
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
            title: 'IP',
            key: 'broker.ip',
            width: '12%'
          },
          {
            title: '端口',
            key: 'broker.port',
            width: '6%'
          },
          {
            title: '角色',
            key: 'role',
            width: '10%',
            render: (h, params) => {
              let label
              switch (params.item.role) {
                case 0:
                  label = 'dynamics'
                  break
                case 1:
                  label = 'master'
                  break
                case 2:
                  label = 'slave'
                  break
                case 3:
                  label = 'leaner'
                  break
                case 4:
                  label = 'outsync'
                  break
              }
              return h('label', {}, label)
            }
          },
          {
            title: 'BrokerLeader',
            key: 'leaderAddress',
            width: '20%',
            render: (h, params) => {
              let label = ''
              if (params.item['leaderBrokerId']) {
                label = params.item['leaderBrokerId'] + ':' + params.item['leaderIp']
              }
              return h('label', {}, label)
            }
          },
          {
            title: '数据中心',
            key: 'dataCenter',
            width: '6%'
          },
          {
            title: '权限',
            key: 'permission',
            width: '10%'
          },
          {
            title: '重试类型',
            key: 'retryType',
            width: '10%'
          }
        ],
        btns: [
          {
            txt: '指定leader',
            method: 'on-leader'
          },
          {
            txt: 'broker详情',
            method: 'on-broker-detail'
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
    brokerDetail(item) {
      this.$router.push({
        name: '/' + this.$i18n.locale + '/setting/brokerMonitor',
        params: {
          brokerId: item.id,
          brokerIp: item.ip,
          brokerPort: item.port
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
          this.getMetadata(this.tableData.rowData[i], i)
        }
      })
    },
    getMetadata (row, index) {
      let brokerId = row.brokerId
      let topicFullName = getTopicCode(this.data.topic, this.data.namespace)
      let group = this.data.groupNo

      apiRequest.getBase(this.urls.findMetadata + '/' + brokerId + '/' + topicFullName + '/' + group, {}, false)
        .then((data) => {
          this.tableData.rowData[index] = Object.assign(row, data.data || [])
          this.$set(this.tableData.rowData, index, this.tableData.rowData[index])
        })
    }
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
