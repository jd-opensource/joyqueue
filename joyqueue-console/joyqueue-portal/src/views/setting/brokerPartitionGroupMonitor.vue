<template>
  <div>
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
import {mergePartitionGroup} from '../../utils/common'

export default {
  name: 'brokerPartitionGroupMonitor',
  components: {
    ClientConnection,
    myTable,
    myDialog
  },
  props: {
  },
  mixins: [ crud ],
  data () {
    return {
      brokerId: this.$route.params.brokerId || this.$route.query.brokerId,
      urls: {
        search: '/monitor/broker/partition/search'
      },
      searchData: {
        brokerId: this.$route.params.brokerId || this.$route.query.brokerId
      },
      searchRules: {
      },
      tableData: {
        rowData: [],
        colData: [
          {
            title: '主题',
            key: 'topic'
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
  },
  mounted () {
    this.getList()
  }
}
</script>
