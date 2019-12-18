<template>
  <div>
      <p> 主题分组数(partitionGroup)：{{broker.partitionGroups}};分组Leader数:{{broker.leaders}}</p>
      <my-table :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange"
                @on-current-change="handleCurrentChange" @on-selection-change="handleSelectionChange"
                @on-edit="edit">
      </my-table>
  </div>
</template>

<script>
import myTable from '../../components/common/myTable.vue'
import myDialog from '../../components/common/myDialog.vue'
import crud from '../../mixins/crud.js'
import ClientConnection from '../monitor/detail/clientConnection'
import apiRequest from '../../utils/apiRequest.js'

export default {
  name: 'brokerPartitionGroupMonitor',
  components: {
    ClientConnection,
    myTable,
    myDialog
  },
  props: {
    brokerId: {
      type: Number
    }
  },
  mixins: [ crud ],
  data () {
    return {
      urls: {
        search: '/monitor/broker/partition/search'
      },
      searchData: {
        brokerId: this.brokerId
      },
      searchRules: {
      },
      broker:{},
      tableData: {
        rowData: [],
        colData: [
          {
            title: '主题',
            key: 'topic'
          },
          {
            title: '存储大小',
            key: 'storageSize'
          },
          {
            title: 'partitionGroup',
            key: 'partitionGroupMetricList',
            render: (h, params) => {
              var list = params.item.partitionGroupMetricList
              if (list!= null && list.length > 0) {
                list = list.slice().sort((a,b) => a.partitionGroup-b.partitionGroup)
              }
              var html = []
              if (list != undefined) {
                for (var i = 0; i < list.length; i++) {
                  var p = h('div', {style: 'border-bottom: 1px solid #ECECEC;'}, list[i].partitionGroup)
                  html.push(p)
                }
              }
              return h('div', {}, html)
            }
          },
          {
            title: '分组存储大小',
            key: 'partitionGroupMetricList',
            render: (h, params) => {
              var list = params.item.partitionGroupMetricList
              if (list!= null && list.length > 0) {
                list = list.slice().sort((a,b) => a.partitionGroup-b.partitionGroup)
              }
              var html = []
              if (list != undefined) {
                for (var i = 0; i < list.length; i++) {
                  var p = h('div', {style: 'border-bottom: 1px solid #ECECEC;'},list[i].storageSize)
                  html.push(p)
                }
              }
              return h('div', {}, html)
            }
          },
          {
            title: 'leader',
            key: 'partitionGroupMetricList',
            render: (h, params) => {
              var list = params.item.partitionGroupMetricList
              if (list!= null && list.length > 0) {
                list = list.slice().sort((a,b) => a.partitionGroup-b.partitionGroup)
              }
              var html = []
              if (list != undefined) {
                for (var i = 0; i < list.length; i++) {
                  var p = h('div', {style: 'border-bottom: 1px solid #ECECEC;'},list[i].leader)
                  html.push(p)
                }
              }
              return h('div', {}, html)
            }
          },
          {
            title: 'partition',
            key: 'partitionGroupMetricList',
            render: (h, params) => {
              var list = params.item.partitionGroupMetricList;
              if (list!= null && list.length > 0) {
                list = list.slice().sort((a,b) => a.partitionGroup-b.partitionGroup)
              }
              var html = []
              if (list != undefined) {
                for (var i = 0; i < list.length; i++) {
                  var p = h('div', {style: 'border-bottom: 1px solid #ECECEC;'}, list[i].partitions)
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
    getList(){
      this.showTablePin = true
      let searchKey = this.getSearchVal()
      apiRequest.post(this.urlOrigin.search, {}, searchKey).then((data) => {
        if (data === '') {
          return
        }
        let realData=data.data||{}
        let pageResult =realData.pageResult
        pageResult.data =pageResult.result  || []
        pageResult.pagination = pageResult.pagination || {
          totalRecord: pageResult.data.length
        }
        this.broker=realData.extras;
        this.page.total = pageResult.pagination.totalRecord
        this.page.page = pageResult.pagination.page
        this.page.size = pageResult.pagination.size
        this.tableData.rowData = pageResult.data
        this.showTablePin = false
      })
    }
  },
  mounted () {
    this.getList()
  }
}
</script>
