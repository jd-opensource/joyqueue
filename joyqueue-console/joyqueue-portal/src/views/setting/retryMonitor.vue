<template>
<div>
  <div class="ml20 mt30">
    <d-select v-model="searchData.status" size="small" style="width:100px">
      <d-option v-for="item in retryStatus" :value="item.value" :key="item.value">{{ item.key }}</d-option>
    </d-select>
    <d-input v-model="searchData.top" oninput="value = value.trim()" placeholder="请输入查询最大记录数" class="left mr10"
             style="width:300px" @on-enter="getList">
      <span slot="prepend">最大记录数</span>
      <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>
    </d-input>
    <slot name="extendBtn"></slot>
    <d-button type="primary" @click="openDialog('cleanAllRetryMessage')">清理全部重试<icon name="trash" style="margin-left: 5px;"></icon></d-button>
  </div>
  <my-table :data="tableData" :showPin="showTablePin" :page="page" :showPagination="false" @on-del="del">
  </my-table>

  <!--批量清理-->
  <my-dialog :dialog="cleanAllRetryMessage" @on-dialog-confirm="cleanAllConfirm()" @on-dialog-cancel="dialogCancel('cleanAllRetryMessage')">
    <grid-row class="mb10">
      <grid-col :span="6" class="label">过期时间:</grid-col>
      <grid-col :span="16" class="val">
        <d-date-picker
          v-model="expireDate"
          type="datetime"
          placeholder="选择日期时间"
          :picker-options="pickerOptions1" >
        </d-date-picker>
      </grid-col>
      <grid-col offset="4" style="margin-top: 10px">
        <d-tooltip  content="删除过期时间以前的重试消息(不包含重试中状态的消息)"><Icon name="help-circle" class="help-icon" size="16" color="#4F8EEB" />
        </d-tooltip>
      </grid-col>
    </grid-row>
  </my-dialog>

  <my-dialog :dialog="cleanSingleConsumerDialog" @on-dialog-confirm="cleanSingleConsumerConfirm()" @on-dialog-cancel="dialogCancel('cleanSingleConsumerDialog')">
    <grid-row class="mb10">
      <grid-col :span="6" class="label">主题:</grid-col>
      <grid-col :span="16" class="val">
        <d-input v-model="cleanup.topic" oninput="value = value.trim()"></d-input>
      </grid-col>
      <grid-col :span="6" class="label">应用:</grid-col>
      <grid-col :span="16" class="val">
        <d-input v-model="cleanup.appCode" oninput="value = value.trim()"></d-input>
      </grid-col>
      <grid-col :span="6" class="label">过期时间:</grid-col>
      <grid-col :span="16" class="val">
        <d-date-picker
          v-model="expireDate"
          type="datetime"
          placeholder="选择日期时间"
          :picker-options="pickerOptions1" >
        </d-date-picker>
      </grid-col>
      <grid-col offset="4" style="margin-top: 10px">
        <d-tooltip  content="删除过期时间以前的重试消息(不包含重试中状态的消息)"><Icon name="help-circle" class="help-icon" size="16" color="#4F8EEB" />
        </d-tooltip>
      </grid-col>
    </grid-row>
  </my-dialog>
</div>
</template>

<script>
import apiRequest from '../../utils/apiRequest.js'
import myTable from '../../components/common/myTable.vue'
import myDialog from '../../components/common/myDialog.vue'
import crud from '../../mixins/crud.js'
import BrokerMonitor from './brokerMonitor'
import {timeStampToString} from '../../utils/dateTimeUtils'

export default {
  name: 'retryMonitor',
  components: {
    BrokerMonitor,
    myTable,
    myDialog
  },
  mixins: [crud],
  props: {
    urls: {
      type: Object,
      default: function () {
        return {
          search: '/retry/monitor/search',
          cleanSingleConsumer: '/retry/cleanup/consumer',
          cleanAll: '/retry/cleanup/allConsumer'
        }
      }
    },
    btns: {
      type: Array,
      default: function () {
        return [
          {
            txt: '删除',
            method: 'on-del'
          }
        ]
      }
    },
    colData: {
      type: Array,
      default: function () {
        return [
          {
            title: '主题',
            key: 'topic',
            width: '15%'
          },
          {
            title: '应用',
            key: 'app',
            width: '15%'
          },
          {
            title: '最小发送时间',
            key: 'minSendTime',
            width: '25%',
            formatter (row) {
              return timeStampToString(row.minSendTime)
            }
          },
          {
            title: '最大发送时间',
            key: 'maxSendTime',
            width: '25%',
            formatter (row) {
              return timeStampToString(row.maxSendTime)
            }
          },
          {
            title: '记录数',
            key: 'count',
            width: '15%'
          },
          {
            title: '消费订阅',
            key: 'existSubscribe',
            width: '15%'
          }
        ]
      }
    }
  },
  data () {
    return {
      tableData: {
        rowData: [],
        colData: this.colData,
        btns: this.btns
      },
      searchData: {
        status: 1,
        top: 20
      },
      cleanup: {
        topic: '',
        appCode: '',
        time: ''
      },
      expireDate: '',
      pickerOptions1: {
        shortcuts: [{
          text: '7天以前',
          onClick (picker) {
            const date = new Date()
            date.setTime(date.getTime() - 7 * 3600 * 1000 * 24)
            picker.$emit('pick', date)
          }
        }]
      },
      cleanAllRetryMessage: {
        visible: false,
        title: '清理重试',
        showFooter: true
      },
      cleanSingleConsumerDialog: {
        visible: false,
        title: '清理重试',
        showFooter: true
      },
      retryStatus: [
        {key: '重试中', value: 1},
        {key: '重试成功', value: 0},
        {key: '已删除', value: -1},
        {key: '已过期', value: -2}
      ]
    }
  },
  methods: {
    getList () {
      this.showTablePin = true
      let data = this.searchData
      apiRequest.get(this.urlOrigin.search + '?status=' + data.status + '&top=' + data.top).then((data) => {
        if (data.status === 200) {
          data.data = data.data || []
          this.tableData.rowData = data.data
        }
        this.showTablePin = false
      })
    },
    cleanAllConfirm () {
      if (this.expireDate == null || this.expireDate === '') {
        this.$Message.error('请选择时间')
        return
      }
      let time = this.expireDate.getTime()
      apiRequest.delete(this.urlOrigin.cleanAll, {'time': time}, {}).then((data) => {
        if (data.status === 200) {
          this.$Dialog.success({
            content: '批量删除成功'
          })
          this.dialogCancel('cleanAllRetryMessage')
          this.getList()
        } else {
          this.$Dialog.error({
            content: data.message
          })
        }
      })
    },
    cleanSingleConsumerConfirm () {
      if (this.expireDate == null || this.expireDate === '') {
        this.$Message.error('请选择时间')
        return
      }
      let time = this.expireDate.getTime()
      let data = this.cleanup
      data.time = time
      apiRequest.delete(this.urlOrigin.cleanSingleConsumer, data, {}).then((data) => {
        if (data.status === 200) {
          this.$Dialog.success({
            content: '删除成功'
          })
          this.dialogCancel('cleanSingleConsumerDialog')
          this.getList()
        } else {
          this.$Dialog.error({
            content: data.message
          })
        }
      })
    },
    del (item) {
      this.cleanup.topic = item.topic
      this.cleanup.appCode = item.app
      this.openDialog('cleanSingleConsumerDialog')
    }
  },
  mounted () {
    this.getList()
  },
  computed: {
    curLang () {
      return this.$i18n.locale
    }
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
.label{text-align: right; line-height: 32px;}
.val{}
</style>
