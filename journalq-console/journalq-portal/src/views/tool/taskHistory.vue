<template>
  <div>
    <div class="ml20 mt30">
      <d-date-picker v-model="times" type="datetimerange" range-separator="至" start-placeholder="开始日期" value-format="timestamp" end-placeholder="结束日期" :default-time="['00:00:00', '23:59:59']"></d-date-picker>
      <d-input v-model="searchData.keyword" placeholder="请输入任务类型" class="left mr10" style="width: 20%" />
      <d-button type="primary" @click="getListWithDate">查询</d-button>
    </div>
    <my-table :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange"
              @on-current-change="handleCurrentChange" @on-selection-change="handleSelectionChange"
              @on-del="del">
    </my-table>
  </div>
</template>

<script>
import myTable from '../../components/common/myTable.vue'
import myDialog from '../../components/common/myDialog.vue'
import crud from '../../mixins/crud.js'
import {timeStampToString} from '../../utils/dateTimeUtils'

export default {
  name: 'taskHistory',
  components: {
    myTable,
    myDialog
  },
  mixins: [ crud ],
  data () {
    return {
      searchData: {
        keyword: '',
        beginTime: '',
        endTime: ''
      },
      searchRules: {
      },
      tableData: {
        rowData: [],
        colData: [
          {
            title: 'ID',
            key: 'id'
          },
          {
            title: '任务类型',
            key: 'type'
          }, {
            title: '执行器',
            key: 'owner'
          },
          {
            title: '参数',
            key: 'url'
          }, {
            title: '重试次数',
            key: 'retryCount'
          }, {
            title: '重试时间',
            key: 'retryTime',
            formatter (item) {
              if (item.retryTime != null) { return timeStampToString(item.retryTime) } else {
                return 0
              }
            }
          }, {
            title: '创建时间',
            key: 'createTime',
            formatter (item) {
              return timeStampToString(item.createTime)
            }
          }, {
            title: '更新时间',
            key: 'updateTime',
            formatter (item) {
              return timeStampToString(item.updateTime)
            }
          }, {
            title: '状态',
            key: 'status',
            formatter (row) {
              if (row.status === 1) {
                return '新建'
              }
              if (row.status === 0) {
                return '审核中'
              }
              if (row.status === -1) {
                return '删除'
              }
              if (row.status === 2) {
                return '失败需要重试'
              }
              if (row.status === 3) {
                return '已派发'
              }
              if (row.status === 4) {
                return '执行中'
              }
              if (row.status === 5) {
                return '成功'
              }
              if (row.status === 6) {
                return '失败不重试'
              }
            }
          }
        ],
        // 表格操作，如果需要根据特定值隐藏显示， 设置bindKey对应的属性名和bindVal对应的属性值
        btns: [
          // {
          //   txt: '编辑',
          //   method: 'on-edit'
          // },
          {
            txt: '删除',
            method: 'on-del'
          }
        ]
      },
      multipleSelection: [],
      addDialog: {
        visible: false,
        title: '新建任务',
        showFooter: true
      },
      addData: {
        type: '',
        referId: 0,
        priority: 0,
        daemons: 1,
        url: '',
        cron: '',
        dispatchType: 0,
        retry: 1
      },
      editDialog: {
        visible: false,
        title: '编辑任务',
        showFooter: true
      },
      editData: {},
      statusEnum: [
        {'key': -1, 'value': '删除'},
        {'key': 0, 'value': '审核中'},
        {'key': 1, 'value': '新增'},
        {'key': 2, 'value': '失败需要重试'},
        {'key': 3, 'value': '已派发'},
        {'key': 4, 'value': '执行中'},
        {'key': 5, 'value': '成功'},
        {'key': 6, 'value': '失败不重试'}
      ],
      times: []
    }
  },
  methods: {
    openDialog (dialog) {
      this[dialog].visible = true
      this.addData.type = ''
      this.addData.referId = 0
      this.addData.priority = 0
      this.addData.daemons = 1
      this.addData.url = ''
      this.addData.cron = ''
      this.addData.dispatchType = 0
      this.addData.retry = 1
    },
    getListWithDate () {
      this.searchData.beginTime = this.times[0]
      this.searchData.endTime = this.times[1]
      this.getList()
    }
  },
  mounted () {
    this.searchData.beginTime = this.times[0]
    this.searchData.endTime = this.times[1]
    this.getList()
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  .label{text-align: right; line-height: 32px;}
  .val{}
</style>
