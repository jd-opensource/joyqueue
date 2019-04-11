<template>
  <div>
    <div class="ml20 mt30">
      <d-date-picker v-model="times" type="daterange" class="left mr5" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="timestamp" :default-time="['00:00:00', '23:59:59']">
        <span slot="prepend">日期范围</span>
      </d-date-picker>
      <d-input v-model="searchData.topic" placeholder="队列名" class="left mr5" style="width: 15%">
        <span slot="prepend">队列名</span>
      </d-input>
      <d-input v-model="searchData.businessId" placeholder="业务ID" class="left mr5" style="width: 15%">
        <span slot="prepend">业务ID</span>
      </d-input>
      <d-button type="primary" color="success" @click="getListWithDate(false)">查询</d-button>
      <d-button type="primary" class="left ml10" @click="exportArchive">归档导出</d-button>
      <d-button type="primary" class="left ml10" @click="batchRetry">归档转重试</d-button>
      <d-button type="primary" class="left ml10" @click="toArchiveTask">任务详情</d-button>
    </div>
    <my-table :showPagination="false" :showPin="showTablePin" :data="tableData" :page="page" @on-size-change="handleSizeChange"
              @on-current-change="handleCurrentChange" @on-selection-change="handleSelectionChange"
              @on-consume="consume" @on-download="download" @on-retry="retry">
    </my-table>
    <div style="float: right">
      <d-button type="primary" v-if="!firstDis" @click="getListWithDate(false)">首页</d-button>
      <d-button color="info" disabled v-else>首页</d-button>

      <d-button type="primary" v-if="!nextDis" @click="getListWithDate(true)">下一页</d-button>
      <d-button color="info" disabled v-else>下一页</d-button>
    </div>
    <my-dialog :dialog="showDialog">
      <my-table :showPagination="false" :data="consumeData" style="padding: 0px">
      </my-table>
    </my-dialog>
  </div>
</template>

<script>
import myTable from '../../components/common/myTable.vue'
import crud from '../../mixins/crud.js'
import apiRequest from '../../utils/apiRequest.js'
import MyDialog from '../../components/common/myDialog'
import {timeStampToString} from '../../utils/dateTimeUtils'

export default {
  name: 'archive',
  components: {
    MyDialog,
    myTable
  },
  mixins: [ crud ],
  props: {
    searchData: {
      type: Object,
      default: function () {
        return {
          topic: '',
          businessId: '',
          beginTime: '',
          endTime: '',
          sendTime: '',
          messageId: '',
          count: 10
        }
      }
    }
  },
  data () {
    return {
      urls: {
        search: '/archive/search'
      },
      firstDis: true,
      nextDis: true,
      showDialog: {
        visible: false,
        title: '消费记录',
        showFooter: false,
        width: '1000px'
      },
      consumeData: {
        rowData: [],
        colData: [
          {
            title: '消息ID',
            key: 'messageId'
          },
          // {
          //   title:"队列",
          //   key: 'topic'
          // },
          {
            title: '消费时间',
            key: 'consumeTime',
            formatter (item) {
              return timeStampToString(item.consumeTime)
            }
          },
          {
            title: '消费者',
            key: 'app'
          },
          {
            title: '消费者主机',
            key: 'clientIpStr'
          }
          // ,
          // {
          //   title:"归档时间",
          //   key: 'messageId'
          // }
        ]
      },
      tableData: {
        rowData: [],
        colData: [
          {
            title: '消息ID',
            key: 'messageId'
          },
          {
            title: '业务ID',
            key: 'businessId'
          }, {
            title: '发送时间',
            key: 'sendTime',
            formatter (item) {
              return timeStampToString(item.sendTime)
            }
          },
          // ,{
          //   title:'接收时间',
          //   key: 'type'
          // },{
          //   title:'归档时间',
          //   key: 'createTime'
          // }
          {
            title: '生产者IP',
            key: 'clientIpStr'
          },
          {
            title: '生产者',
            key: 'app'
          },
          {
            title: '队列',
            key: 'topic'
          }
        ],
        // 表格操作，如果需要根据特定值隐藏显示， 设置bindKey对应的属性名和bindVal对应的属性值
        btns: [
          {
            txt: '下载消息体',
            method: 'on-download'
          },
          {
            txt: '归档转重试',
            method: 'on-retry'
          },
          {
            txt: '查看消费',
            method: 'on-consume'
          }
        ]
      },
      multipleSelection: [],
      addDialog: {
        visible: false,
        title: '新建任务',
        showFooter: false
      },
      times: []
    }
  },
  methods: {
    toArchiveTask () {
      this.$router.push({
        name: `/${this.$i18n.locale}/tool/archiveTask`,
        query: {}
      })
    },
    getListWithDate (isNext) {
      if (!this.times || this.times.length < 2 || !this.searchData.topic) {
        this.$Dialog.error({
          content: '起始时间,结束时间 topic 都不能为空'
        })
        return false
      }
      this.firstDis = true
      let oldData = this.tableData.rowData
      this.searchData.beginTime = this.times[0]
      this.searchData.endTime = this.times[1]
      if (isNext) {
        this.searchData.sendTime = oldData[oldData.length - 1].sendTime
        this.firstDis = false
      } else {
        this.searchData.sendTime = this.searchData.beginTime
        this.firstDis = true
      }
      // this.getList();
      apiRequest.post(this.urlOrigin.search, {}, this.searchData).then((data) => {
        this.tableData.rowData = data.data
        // let newData = this.tableData.rowData;
        console.log(this.tableData.rowData.length)
        if (this.tableData.rowData.length < this.searchData.count) {
          this.nextDis = true
        } else {
          this.nextDis = false
        }
      })
    },
    download (item) {
      let data = '?topic=' + item.topic + '&sendTime=' + item.sendTime + '&businessId=' + item.businessId + '&messageId=' + item.messageId
      apiRequest.get(this.urlOrigin.download + data).then(data => {
        this.$Message.success('下载成功')
      })
    },
    retry (item) {
      apiRequest.post(this.urlOrigin.retry, {}, item).then(data => {
        this.$Message.success('操作成功')
      })
    },
    consume (item) {
      this.showDialog.visible = true
      apiRequest.get(this.urlOrigin.consume + '/' + item.messageId).then((data) => {
        this.consumeData.rowData = data.data
      })
    },
    exportArchive () {
      if (!this.times || this.times.length < 2 || !this.searchData.topic) {
        this.$Dialog.error({
          content: '起始时间,结束时间 topic 都不能为空'
        })
        return false
      }
      this.searchData.beginTime = this.times[0]
      this.searchData.endTime = this.times[1]
      this.$Dialog.confirm({
        title: '提示',
        content: '确定按查询条件批量导出归档消息吗？'
      }).then(() => {
        apiRequest.post(this.urlOrigin.export, {}, this.searchData).then(data => {
          this.$Message.success('操作成功')
        })
      })
    },
    batchRetry () {
      if (!this.times || this.times.length < 2 || !this.searchData.topic) {
        this.$Dialog.error({
          content: '起始时间,结束时间 topic 都不能为空'
        })
        return false
      }
      this.searchData.beginTime = this.times[0]
      this.searchData.endTime = this.times[1]
      this.$Dialog.confirm({
        title: '提示',
        content: '确定按查询条件批量重试消费消息吗？'
      }).then(() => {
        apiRequest.post(this.urlOrigin.batchRetry, {}, this.searchData).then(data => {
          this.$Message.success('操作成功')
        })
      })
    }
  },
  mounted () {
    // this.searchData.beginTime=this.times[0];
    // this.searchData.endTime=this.times[1];
    //
    // this.getList();
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  .label{text-align: right; line-height: 32px;}
  .val{}
</style>
