<template>
  <div>
    <div class="ml20">
      <d-date-picker v-model="times" type="daterange" class="left mr5 mt10" range-separator="至"
                     start-placeholder="开始日期" end-placeholder="结束日期" value-format="timestamp"
                     :default-time="['00:00:00', '23:59:59']"  style="width:370px" @on-enter="getList">
        <span slot="prepend">日期范围</span>
      </d-date-picker>
      <d-input v-model="search.topic" placeholder="队列名" class="left mr5 mt10" style="width: 213px"
               @on-enter="getList">
        <span slot="prepend">队列名</span>
      </d-input>
      <d-input v-model="search.businessId" placeholder="业务ID" class="left mr5 mt10" style="width: 213px"
               @on-enter="getList">
        <span slot="prepend">业务ID</span>
      </d-input>
      <d-button class="left mr5 mt10" type="primary" color="success" @click="getList">查询</d-button>
      <slot name="extendBtn"></slot>
    </div>
    <my-table :showPagination="false" :showPin="showTablePin" :data="tableData" :page="page" @on-size-change="handleSizeChange"
              @on-current-change="handleCurrentChange" @on-selection-change="handleSelectionChange"
              @on-consume="consume" @on-download="download" @on-retry="retry">
    </my-table>
    <div style="text-align: right; margin-right: 50px">
      <d-button type="primary" v-if="!firstDis" @click="getList" class="left mr10">首页</d-button>
      <d-button color="info" disabled v-else>首页</d-button>

      <d-button type="primary" v-if="!nextDis" @click="getNext">下一页</d-button>
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
    search: {
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
    },
    btns: {
      type: Array,
      default: function () {
        return [
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
      }
    }
  },
  data () {
    return {
      urls: {
        search: '/archive/search',
        consume: '/archive/consume',
        download: '/archive/download',
        retry: '/archive/retry'
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
        btns: this.btns
      },
      multipleSelection: [],
      times: [
        new Date(new Date().setHours(0, 0, 0, 0)),
        new Date(new Date().setHours(23, 59, 59, 0))
      ]
    }
  },
  methods: {
    getList () {
      this.firstDis = false
      this.nextDis = true
      this.getListWithDate(false)
    },
    getNext () {
      this.firstDis = true
      this.nextDis = true
      this.getListWithDate(true)
    },
    getListWithDate (isNext) {
      if (!this.times || this.times.length < 2) {
        this.$Message.error('日期时间范围不能为空')
        return
      }
      if (!this.search.topic) {
        this.$Message.error('队列名不能为空')
        return
      }
      let oldData = this.tableData.rowData
      this.search.beginTime = this.times[0]
      this.search.endTime = this.times[1]
      if (isNext) {
        this.search.sendTime = oldData[oldData.length - 1].sendTime
      } else {
        this.search.sendTime = this.search.beginTime
      }
      apiRequest.post(this.urlOrigin.search, {}, this.search).then((data) => {
        this.tableData.rowData = data.data
        if (this.tableData.rowData && this.tableData.rowData.length < this.search.count) {
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
