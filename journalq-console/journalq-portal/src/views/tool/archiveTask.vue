<template>
  <div>
    <div class="ml20 mt30">
      <d-date-picker v-model="times" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="timestamp" :default-time="['00:00:00', '23:59:59']">
        <span slot="prepend">日期范围</span>
      </d-date-picker>
      <d-input v-model="searchData.topic" placeholder="队列名" class="left mr10" style="width: 15%">
        <span slot="prepend">队列名</span>
      </d-input>
      <d-button type="primary" @click="getListWithDate">查询</d-button>
    </div>
    <my-table :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange"
              @on-current-change="handleCurrentChange" @on-selection-change="handleSelectionChange"
              @on-del="del" @on-download="download">
    </my-table>
  </div>
</template>

<script>
import myTable from '@/components/common/myTable.vue'
import crud from '@/mixins/crud.js'
import apiRequest from '@/utils/apiRequest.js'
import MyDialog from '../../components/common/myDialog'
import {timeStampToString} from '@/utils/dateTimeUtils'

export default {
  name: 'archive',
  components: {
    MyDialog,
    myTable
  },
  mixins: [ crud ],
  data () {
    return {
      searchData: {
        topic: '',
        beginTime: '',
        endTime: ''
      },
      searchRules: {
      },
      tableData: {
        rowData: [],
        colData: [
          {
            title: 'topic',
            key: 'topic'
          },
          {
            title: '类型',
            key: 'type',
            formatter (row) {
              if (row.type === 1) {
                return '导出'
              }
              if (row.type === 2) {
                return '重试'
              }
            }
          },
          {
            title: '参数',
            key: 'params'
          },
          {
            title: '导出条数',
            key: 'count'
          },
          {
            title: '创建时间',
            key: 'create_Time',
            formatter (item) {
              return timeStampToString(item.createTime)
            }
          },
          {
            title: '状态',
            key: 'status',
            formatter (row) {
              if (row.status === 0) {
                return '新建'
              }
              if (row.status === 1) {
                return '正在执行'
              }
              if (row.status === 2) {
                return '异常'
              }
              if (row.status === 3) {
                return '完成'
              }
            }
          }
        ],
        // 表格操作，如果需要根据特定值隐藏显示， 设置bindKey对应的属性名和bindVal对应的属性值
        btns: [
          {
            txt: '下载消息体',
            method: 'on-download',
            bindKey: 'type',
            bindVal: 1
          }
        ]
      },
      times: []
    }
  },
  methods: {
    getListWithDate () {
      // if (this.searchData.app == '' || this.searchData.topic == '') {
      //   return ;
      // }
      this.searchData.beginTime = this.times[0]
      this.searchData.endTime = this.times[1]
      this.getList()
    },
    download(item) {
      let data = item;
      apiRequest.get(this.urlOrigin.download + "/"+item.id).then(data => {
        this.$Message.success('下载成功')
      })
    }
  },
  mounted () {

  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  .label{text-align: right; line-height: 32px;}
  .val{}
</style>
