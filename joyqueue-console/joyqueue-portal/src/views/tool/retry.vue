<template>
  <div>
    <div class="headLine2">
      <d-input v-model="search.topic" oninput="value = value.trim()" placeholder="请输入主题名" class="input2" @on-enter="getList">
        <span slot="prepend">主题名</span>
      </d-input>
      <d-input v-model="search.app" oninput="value = value.trim()" placeholder="请输入消费者" class="input2" @on-enter="getList">
        <span slot="prepend">消费者</span>
      </d-input>
      <d-select v-model="search.status" class="input3" @on-change="getList">
        <span slot="prepend">状态</span>
        <d-option v-for="item in statusList" :value="item.key" :key="item.key">{{ item.value }}</d-option>
      </d-select>
      <d-date-picker class="input5"
                     v-model="times"
                     size="large"
                     type="datetimerange"
                     range-separator="至"
                     start-placeholder="开始日期"
                     end-placeholder="结束日期"
                     value-format="timestamp"
                     :default-time="['00:00:00', '23:59:59']">
        <span slot="prepend">发送时间</span>
      </d-date-picker>
      <d-input v-model="businessId" oninput="value = value.trim()" placeholder="请输入业务ID" class="input2" @on-enter="getList">
        <span slot="prepend">业务ID</span>
      </d-input>
      <d-button class="button2" type="primary" @click="getList">查询
        <icon name="search" style="margin-left: 3px;"></icon>
      </d-button>
      <d-button class="button2" type="primary" @click="batchDeleteTip">批量删除
        <icon name="search" style="margin-left: 3px;"></icon>
      </d-button>
    </div>

    <my-table :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange"
              @on-current-change="handleCurrentChange" @on-selection-change="handleSelectionChange"
              @on-del="del" @on-download="download" @on-recovery="recovery">
    </my-table>
  </div>
</template>

<script>
import myTable from '../../components/common/myTable.vue'
import crud from '../../mixins/crud.js'
import apiRequest from '../../utils/apiRequest.js'
import {timeStampToString} from '../../utils/dateTimeUtils'

export default {
  name: 'retry',
  components: {
    myTable
  },
  mixins: [ crud ],
  props: {
    search: {
      type: Object,
      default: function () {
        return {
          topic: '',
          app: '',
          status: 1
        }
      }
    }
  },
  data () {
    return {
      urls: {
        search: '/retry/search',
        del: '/retry/delete',
        download: '/retry/download',
        recovery: '/retry/recovery',
        batchDelete: '/retry/batchDelete'
      },
      businessId: '',
      statusList: [
        {key: 1, value: '重试中'},
        {key: -2, value: '过期'},
        {key: -1, value: '已删除'},
        {key: 0, value: '成功'}
      ],
      showTablePin: false,
      // page1: {
      //   page: 1,
      //   size: 10,
      //   total: 100
      // },
      tableData: {
        rowData: [],
        colData: [
          {
            title: '主键',
            key: 'id'
          },
          {
            title: '主题',
            key: 'topic'
          },
          {
            title: '应用',
            key: 'app'
          },
          {
            title: '业务ID',
            key: 'businessId'
          },
          {
            title: '次数',
            key: 'retryCount'
          },
          {
            title: '发送时间',
            key: 'sendTime',
            formatter (item) {
              return timeStampToString(item.sendTime)
            }
          },
          {
            title: '下次重试时间',
            key: 'retryTime',
            formatter (item) {
              return timeStampToString(item.retryTime)
            }
          },
          {
            title: '过期时间',
            key: 'expireTime',
            formatter (item) {
              return timeStampToString(item.expireTime)
            }
          }
        ],
        // 表格操作，如果需要根据特定值隐藏显示， 设置bindKey对应的属性名和bindVal对应的属性值
        btns: [
          // {
          //   txt: '恢复',
          //   method: 'on-recovery'
          // },
          {
            txt: '下载',
            method: 'on-download'
          },
          {
            txt: '删除',
            method: 'on-del',
            bindKey: 'status',
            bindVal: [0, -2, 1]
          }
        ]
      },
      editData: {},
      times: []
    }
  },
  methods: {
    getList () {
      this.showTablePin = true
      let data = {
        pagination: {
          page: this.page.page,
          size: this.page.size
        },
        query: {
          topic: this.search.topic,
          app: this.search.app,
          status: this.search.status,
          businessId: this.businessId
        }
      }

      if (this.times && this.times.length === 2) {
        data.query.beginTime = this.times[0]
        data.query.endTime = this.times[1]
      } else {
        data.query.beginTime = ''
        data.query.endTime = ''
      }

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
        this.showTablePin = false
      })
    },
    batchDeleteTip(){
      this.$Dialog.confirm({
        title: '提示',
        content: '将按查询条件(所有分页)批量删除重试数据'
      }).then(() => {
        this.batchDelete();
      // this.$Message.info('点击了「确认」按钮')
    }).catch(() => {
        // this.$Message.info('点击了「取消」按钮')
      })
    },
    batchDelete(){

      let data = {
        topic: this.search.topic,
        app: this.search.app,
        status: this.search.status,
        businessId: this.businessId
      }

      if (this.times && this.times.length === 2) {
        data.beginTime = this.times[0]
        data.endTime = this.times[1]
      } else {
        data.beginTime = ''
        data.endTime = ''
      }
      apiRequest.post(this.urlOrigin.batchDelete, {}, data).then((data) => {
        if (data.status === 200) {
          this.$Dialog.success({
            content: '批量删除成功'
          })
          this.getList()
        } else {
          this.$Dialog.error({
            content: data.message
          })
        }
      })
    },
    download (item) {
      document.location.assign('v1' + this.urls.download + '?id=' + item.id + '&topic=' + item.topic)
      // apiRequest.get(this.urlOrigin.download + '/' + item.id).then()
    },
    recovery (item) {
      apiRequest.put(this.urlOrigin.recovery + '?id=' + item.id + 'topic=' + item.topic).then(data => {
        if (data.status === 200) {
          this.$Dialog.success({
            content: '恢复成功'
          })
          this.getList()
        } else {
          this.$Dialog.error({
            content: data.message
          })
        }
      })
    },
    del (item) {
      apiRequest.delete(this.urlOrigin.del + '?id=' + item.id + '&topic=' + item.topic).then(data => {
        if (data.status === 200) {
          this.$Dialog.success({
            content: '删除成功'
          })
          this.getList()
        }
      })
    }
  },
  mounted () {
    // this.getList()
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  .label{text-align: right; line-height: 32px;}
  .val{}
</style>
