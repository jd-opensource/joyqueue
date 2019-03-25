<template>
  <div>
    <my-table :data="tableData" :showPin="showTablePin" :showPagination=false :page="page"
              @on-size-change="handleSizeChange" @on-current-change="handleCurrentChange"/>
  </div>
</template>

<script>
import MyTable from '../../components/common/myTable'
import crud from '../../mixins/crud.js'
import apiRequest from '../../utils/apiRequest.js'
import {timeStampToString} from '../../utils/dateTimeUtils'

export default {
  name: 'task-executor-detail',
  components: {MyTable},
  mixins: [crud],
  props: {
    doSearch: {
      type: Boolean,
      default: false
    },
    executorId: {
      type: Number,
      default: -1
    },
    colData: {
      type: Array,
      default: function () {
        return [
          {
            title: '启动时间',
            key: 'bootstrapTime',
            formatter (item) {
              return timeStampToString(item.bootstrapTime)
            }
          },
          {
            title: '主',
            key: 'leader',
            render: (h, params) => {
              if (params.item.leader === undefined || params.item.leader === {}) {
                return h('span', '')
              }
              let txt = params.item.leader === false ? '否' : '是'
              let color = params.item.leader === false ? 'warning' : 'success'
              return h('DButton', {
                props: {
                  size: 'small',
                  borderless: true,
                  color: color
                }
              }, txt)
            }
          },
          {
            title: '上次任务派发时间',
            key: 'lastDispatchSuccessTime',
            formatter (item) {
              return timeStampToString(item.lastDispatchSuccessTime)
            }
          },
          {
            title: '上次派发任务数',
            key: 'lastDispatchTaskAmount'
          },
          {
            title: '执行中任务数',
            key: 'taskIds',
            formatter (item) {
              if (!item.taskIds) {
                return 0
              }
              return item.taskIds.length
            }
          },
          {
            title: '最新任务开始时间',
            key: 'lastTaskStartTime',
            formatter (item) {
              return timeStampToString(item.lastTaskStartTime)
            }
          },
          {
            title: '平均执行时间(ns)',
            key: 'avgTaskExecuteTime'
          }
        ]
      }
    }
  },
  data () {
    return {
      urls: {
        getMonitor: `/executor/monitor/`
      },
      tableData: {
        rowData: [],
        colData: this.colData
      }
      // producerType: this.$store.getters.producerType
    }
  },
  methods: {
    getList () {
      this.showTablePin = true
      let data = {
        id: this.executorId
      }
      apiRequest.getBase(this.urls.getMonitor + data.id, {}, false).then((data) => {
        data.data = data.data || []
        this.tableData.rowData = [data.data.body]
        this.showTablePin = false
      })
    }
  },
  mounted () {
    this.getList()
  }
}

</script>

<style scoped>

</style>
