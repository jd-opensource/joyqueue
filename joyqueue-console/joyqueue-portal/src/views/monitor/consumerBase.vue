<template>
  <div>
    <div class="headLine">
      <d-input v-model="keyword" :placeholder="keywordTip" class="input" @on-enter="getList">
        <span slot="prepend">{{keywordName}}</span>
        <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>
      </d-input>
      <d-button-group>
        <d-button v-if="$store.getters.isAdmin" @click="openAndQueryDialog('subscribeDialog', 'subscribe')" class="button">
          订阅
          <icon name="plus-circle" style="margin-left: 3px;"></icon>
        </d-button>
        <d-button type="primary" @click="getList" class="button">刷新
          <icon name="refresh-cw" style="margin-left: 3px;"></icon>
        </d-button>
      </d-button-group>
    </div>

    <my-table :data="tableData" :showPin="showTablePin" :page="page" :showPagination="this.showPagination"
              @on-detail-chart="goDetailChart" @on-current-change="handleCurrentChange" @on-detail="openDetailTab"
              @on-msg-preview="openMsgPreviewDialog" @on-msg-detail="openMsgDetailDialog" @on-config="openConfigDialog"
              @on-performance-chart="goPerformanceChart" @on-summary-chart="goSummaryChart" @on-compare-chart="goCompareChart"
              @on-rateLimit="openRateLimitDialog" @on-size-change="handleSizeChange" @on-cancel-subscribe="cancelSubscribe"
              @on-offset="goOffsetChart"/>

    <d-button class="right load-btn" v-if="this.curIndex < this.cacheList.length-1" type="primary" @click="getRestList">加载更多
      <icon name="refresh-cw" style="margin-left: 3px;"></icon>
    </d-button>

    <!--Consumer subscribe dialog-->
    <my-dialog :dialog="subscribeDialog" @on-dialog-cancel="dialogCancel('subscribeDialog')">
      <subscribe ref="subscribe" :search="search" :type="type" :colData="subscribeDialog.colData"
                 :searchUrl="subscribeDialog.urls.search" :addUrl="subscribeDialog.urls.add"
                 :keywordName="keywordName" @on-refresh="getList"/>
    </my-dialog>

    <!--Msg preview dialog-->
    <my-dialog :dialog="msgPreviewDialog" @on-dialog-cancel="dialogCancel('msgPreviewDialog')">
      <msg-preview ref="msgPreview" :message-types="messageTypes" :app="msgPreviewDialog.app" :topic="msgPreviewDialog.topic" :namespace="msgPreviewDialog.namespace"
                   :type="type" :subscribeGroup="msgPreviewDialog.subscribeGroup" :messageType="messageType" @update:messageType="messageType = $event" />
    </my-dialog>

    <!--Msg detail dialog-->
    <my-dialog :dialog="msgDetailDialog" @on-dialog-cancel="dialogCancel('msgDetailDialog')">
      <msg-detail ref="msgDetail" :message-types="messageTypes" :app="msgDetailDialog.app" :topic="msgDetailDialog.topic" :namespace="msgDetailDialog.namespace"
                  :type="type" :subscribeGroup="msgDetailDialog.subscribeGroup" :messageType="messageType" @update:messageType="messageType = $event"/>
    </my-dialog>

    <!--Config dialog-->
    <my-dialog :dialog="configDialog" @on-dialog-confirm="configConsumerConfirm" @on-dialog-cancel="dialogCancel('configDialog')">
      <consumer-config-form
        ref="configForm"
        :data="configConsumerData"
        :tips="configDialogTips"
        :archive-disabled="archiveConfigDisabled"
        :nearby-disabled="nearbyConfigDisabled"
        :concurrent-disabled="concurrentConfigDisabled"
      />
    </my-dialog>

    <!--Rate limit dialog-->
    <my-dialog :dialog="rateLimitDialog" @on-dialog-confirm="rateLimitConfirm" @on-dialog-cancel="dialogCancel('rateLimitDialog')">
      <rate-limit ref="rateLimit" :limitTraffic="rateLimitDialog.limitTraffic" :limitTps="rateLimitDialog.limitTps"
                  :is-consumer="1"/>
    </my-dialog>

  </div>
</template>

<script>
import apiRequest from '../../utils/apiRequest.js'
import myTable from '../../components/common/myTable.vue'
import myDialog from '../../components/common/myDialog.vue'
import consumerConfigForm from './consumerConfigForm.vue'
import subscribe from './subscribe.vue'
import msgPreview from './msgPreview.vue'
import {getTopicCode, getAppCode, replaceChartUrl} from '../../utils/common.js'
import MsgDetail from './msgDetail'
import RateLimit from './rateLimit'
import ButtonGroup from '../../components/button/button-group'
import apiUrl from '../../utils/apiUrl.js'

export default {
  name: 'consumer-base',
  components: {
    RateLimit,
    MsgDetail,
    myTable,
    myDialog,
    subscribe,
    msgPreview,
    consumerConfigForm,
    ButtonGroup
  },
  props: {
    configDialogTips: {
      type: String,
      default: undefined
    },
    archiveConfigDisabled: {
      type: Boolean,
      default: false
    },
    nearbyConfigDisabled: {
      type: Boolean,
      default: false
    },
    concurrentConfigDisabled: {
      type: Boolean,
      default: false
    },
    keywordTip: {
      type: String
    },
    keywordName: {
      type: String
    },
    btns: {
      type: Array,
      default: function () {
        return [
          {
            txt: '消费详情',
            method: 'on-detail'
          },
          {
            txt: '配置',
            method: 'on-config'
          },
          {
            txt: '取消订阅',
            method: 'on-cancel-subscribe'
          }
          // ,
          // {
          //   txt: '详情监控图表',
          //   method: 'on-detail-chart'
          // },
          // {
          //   txt: '汇总监控图表',
          //   method: 'on-summary-chart'
          // },
          // {
          //   txt: '性能监控图表',
          //   method: 'on-performance-chart'
          // }
        ]
      }
    },
    operates: {
      type: Array,
      default: function () {
        return [
          {
            txt: '消息预览',
            method: 'on-msg-preview'
          },
          {
            txt: '消息查询',
            method: 'on-msg-detail'
          },
          {
            txt: '限流',
            method: 'on-rateLimit'
          }
        ]
      }
    },
    btnGroups: {
      type: Object
    },
    colData: { // 消费者 列表表头
      type: Array
    },
    showPagination: {
      type: Boolean
    },
    search: {// 查询条件，我的应用：app:{id:0,code:'',namespace:{id:0,code:''}}  ， 主题中心：topic:{id:0,code:'',namespace:{id:0,code:''}}
      type: Object
    },
    subscribeDialogColData: { // 订阅列表表头
      type: Array
    },
    subscribeUrls: {
      type: Object
    },
    monitorUrls: {// url variable format: [app], [topic], [namespace]
      type: Object
    }
  },
  data () {
    return {
      curIndex: 0,
      cacheList: [],
      urls: {
        search: `/consumer/search`,
        getMonitor: `/monitor/find`,
        previewMessage: '/monitor/preview/message',
        del: `/consumer/delete`,
        messageTypes: '/archive/message-types'
      },
      showTablePin: false,
      tableData: {
        rowData: [],
        colData: this.colData,
        btns: this.btns,
        operates: this.operates,
        btnGroups: this.btnGroups
      },
      keyword: '',
      page: {
        page: 1,
        size: 10,
        total: 100
      },
      rateLimitDialog: {
        visible: false,
        title: '限流',
        width: '500',
        showFooter: true,
        // doSearch: false,
        limitTps: 0,
        limitTraffic: 0
      },
      type: this.$store.getters.consumerType, // 1:生产， 2：消费
      subscribeDialog: {
        visible: false,
        title: '添加消费者',
        width: '950',
        showFooter: false,
        // doSearch: false,
        colData: this.subscribeDialogColData, // 订阅框 列表表头,
        urls: {
          add: this.subscribeUrls.add,
          search: this.subscribeUrls.search
        }
      },
      configConsumerData: {},
      configDialog: {
        visible: false,
        title: '消费配置',
        width: '1000',
        showFooter: true,
        urls: {
          addOrUpdate: `/consumer/config/addOrUpdate`,
          search: 'consumer/config/search',
          messageTypes: '/archive/message-types'
        }
      },
      msgPreviewDialog: {
        visible: false,
        title: '生产者详情',
        width: '1000',
        showFooter: false,
        // doSearch: false,
        app: {
          id: 0,
          code: ''
        },
        subscribeGroup: '',
        topic: {
          id: '',
          code: ''
        },
        namespace: {
          id: '',
          code: ''
        }
      },
      msgDetailDialog: {
        visible: false,
        title: '生产者详情',
        width: '1000',
        showFooter: false,
        // doSearch: false,
        app: {
          id: 0,
          code: ''
        },
        subscribeGroup: '',
        topic: {
          id: '',
          code: ''
        },
        namespace: {
          id: '',
          code: ''
        }
      },
      messageType: undefined,
      messageTypes: [
        'UTF8 TEXT'
      ],
      monitorUIds: {
        detail: this.$store.getters.uIds.consumer.detail,
        summary: this.$store.getters.uIds.consumer.summary,
        compare: this.$store.getters.uIds.consumer.compare,
        offset: this.$store.getters.uIds.consumer.offset
      }
    }
  },
  methods: {
    openDialog (dialog) {
      this[dialog].visible = true
    },
    openAndQueryDialog (dialog, ref) {
      this[dialog].visible = true
      this.$nextTick(() => {
        this.$refs[ref].getList()
      })
    },
    openDetailTab (item) {
      this.$emit('on-detail', item)
    },
    openMsgPreviewDialog (item) {
      this.msgPreviewDialog.app.id = item.app.id
      this.msgPreviewDialog.app.code = item.app.code
      this.msgPreviewDialog.subscribeGroup = item.subscribeGroup
      this.msgPreviewDialog.topic.id = item.topic.id
      this.msgPreviewDialog.topic.code = item.topic.code
      this.msgPreviewDialog.namespace.id = item.namespace.id
      this.msgPreviewDialog.namespace.code = item.namespace.code
      this.msgPreviewDialog.title = '消息预览 [App: ' + this.msgPreviewDialog.app.code +
          ', Topic: ' + this.msgPreviewDialog.topic.code + ']'
      this.openAndQueryDialog('msgPreviewDialog', 'msgPreview')
    },
    openMsgDetailDialog (item) {
      this.msgDetailDialog.app.id = item.app.id
      this.msgDetailDialog.app.code = item.app.code
      this.msgDetailDialog.subscribeGroup = item.subscribeGroup
      this.msgDetailDialog.topic.id = item.topic.id
      this.msgDetailDialog.topic.code = item.topic.code
      this.msgDetailDialog.namespace.id = item.namespace.id
      this.msgDetailDialog.namespace.code = item.namespace.code
      this.msgDetailDialog.title = '消息查询 [App: ' + this.msgDetailDialog.app.code +
          ', Topic: ' + this.msgDetailDialog.topic.code + ']'
      this.openDialog('msgDetailDialog')
    },
    openConfigDialog (item) {
      this.configConsumerData = Object.assign({}, item.config)
      this.configConsumerData['consumerId'] = item.id
      this.configDialog.visible = true
    },
    openRateLimitDialog (item) {
      this.configData = Object.assign({}, item.config)
      this.configData['consumerId'] = item.id
      this.rateLimitDialog.limitTps = item.config.limitTps
      this.rateLimitDialog.limitTraffic = item.config.limitTraffic
      this.rateLimitDialog.visible = true
    },
    rateLimitConfirm () {
      let configData = this.configData
      configData.limitTps = this.$refs.rateLimit.tps
      configData.limitTraffic = this.$refs.rateLimit.traffic
      this.config(configData, 'rateLimitDialog')
    },
    cancelSubscribe (item) {
      let _this = this
      this.$Dialog.confirm({
        title: '提示',
        content: '确定要取消订阅吗？'
      }).then(() => {
        apiRequest.delete(_this.urls.del + '/' + item.id).then((data) => {
          if (data.code !== this.$store.getters.successCode) {
            this.$Dialog.error({
              content: '取消订阅失败'
            })
          } else {
            this.$Message.success('取消订阅成功')
            _this.getList()
          }
        })
      })
    },
    handleSizeChange (val) {
      this.page.size = val
      this.getList()
    },
    handleCurrentChange (val) {
      this.page.page = val
      this.getList()
    },
    handleTabChange (data) {
      let name = data.name
      this.$refs[name].getList()
    },
    dialogConfirm (dialog) {
      this[dialog].visible = false
      this.getList()
    },
    dialogCancel (dialog) {
      this[dialog].visible = false
      this.getList()
    },
    goSummaryChart (item) {
      if (this.monitorUrls && this.monitorUrls.summary) {
        window.open(replaceChartUrl(this.monitorUrls.summary, item.topic.namespace.code,
          item.topic.code, getAppCode(item.app, item.subscribeGroup)))
      } else {
        apiRequest.get(apiUrl['monitor']['redirectUrl'] + '/' + this.monitorUIds.summary, {}, {}).then((data) => {
          if (data.data) {
            let url = data.data
            if (url.indexOf('?') < 0) {
              url += '?'
            } else if (!url.endsWith('?')) {
              url += '&'
            }
            url = url + 'var-topic=' + getTopicCode(item.topic, item.topic.namespace) + '&var-app=' +
                getAppCode(item.app, item.subscribeGroup)
            window.open(url)
          }
        })
      }
    },
    goDetailChart (item) {
      if (this.monitorUrls && this.monitorUrls.detail) {
        window.open(replaceChartUrl(this.monitorUrls.detail, item.topic.namespace.code,
          item.topic.code, getAppCode(item.app, item.subscribeGroup)))
      } else {
        apiRequest.get(apiUrl['monitor']['redirectUrl'] + '/' + this.monitorUIds.detail, {}, {}).then((data) => {
          if (data.data) {
            let url = data.data
            if (url.indexOf('?') < 0) {
              url += '?'
            } else if (!url.endsWith('?')) {
              url += '&'
            }
            url = url + 'var-topic=' + getTopicCode(item.topic, item.topic.namespace) + '&var-app=' +
              getAppCode(item.app, item.subscribeGroup)
            window.open(url)
          }
        })
      }
    },
    goPerformanceChart (item) {
      if (this.monitorUrls && this.monitorUrls.performance) {
        window.open(replaceChartUrl(this.monitorUrls.performance, item.topic.namespace.code,
          item.topic.code, getAppCode(item.app, item.subscribeGroup)))
      } else {
        apiRequest.get(apiUrl['monitor']['redirectUrl'] + this.monitorUIds.performance, {}, {}).then((data) => {
          if (data.data) {
            let url = data.data
            if (url.indexOf('?') < 0) {
              url += '?'
            } else if (!url.endsWith('?')) {
              url += '&'
            }
            url = url + 'var-topic=' + getTopicCode(item.topic, item.topic.namespace) + '&var-app=' +
                getAppCode(item.app, item.subscribeGroup)
            window.open(url)
          }
        })
      }
    },
    goCompareChart (item) {
      if (this.monitorUrls && this.monitorUrls.compare) {
        window.open(replaceChartUrl(this.monitorUrls.compare, item.topic.namespace.code,
          item.topic.code, getAppCode(item.app, item.subscribeGroup)))
      } else {
        apiRequest.get(apiUrl['monitor']['redirectUrl'] + '/' + this.monitorUIds.compare, {}, {}).then((data) => {
          if (data.data) {
            let url = data.data
            if (url.indexOf('?') < 0) {
              url += '?'
            } else if (!url.endsWith('?')) {
              url += '&'
            }
            url = url + 'var-topic=' + getTopicCode(item.topic, item.topic.namespace) + '&var-app=' +
              getAppCode(item.app, item.subscribeGroup)
            window.open(url)
          }
        })
      }
    },
    goOffsetChart (item) {
      if (this.monitorUrls && this.monitorUrls.offset) {
        window.open(replaceChartUrl(this.monitorUrls.offset, item.topic.namespace.code,
          item.topic.code, item.app.code))
      } else {
        apiRequest.get(apiUrl['monitor']['redirectUrl'] + '/' + this.monitorUIds.offset, {}, {}).then((data) => {
          let url = data.data || ''
          if (url.indexOf('?') < 0) {
            url += '?'
          } else if (!url.endsWith('?')) {
            url += '&'
          }
          url = url + 'var-topic=' + getTopicCode(item.topic, item.topic.namespace) + '&var-app=' +
            getAppCode(item.app, item.subscribeGroup)
          window.open(url)
        })
      }
    },
    getMonitor (row, index) {
      let data = {
        topic: {
          id: row.topic.id,
          code: row.topic.code
        },
        namespace: {
          id: row.namespace.id,
          code: row.namespace.code
        },
        app: {
          id: row.app.id,
          code: row.app.code
        },
        subscribeGroup: row.subscribeGroup || '',
        type: this.type
      }

      apiRequest.postBase(this.urls.getMonitor, {}, data, false).then((data) => {
        this.tableData.rowData[index] = Object.assign(row, data.data || [])
        this.$set(this.tableData.rowData, index, this.tableData.rowData[index])
      })
    },
    configConsumerConfirm () {
      this.$refs['configForm'].validate(() => {
        let configData = this.$refs.configForm.getFormData()
        this.config(configData, 'configDialog')
      })
    },
    config (configData, dialog) {
      apiRequest.post(this.configDialog.urls.addOrUpdate, {}, configData).then((data) => {
        if (data.code !== 200) {
          this.$Dialog.error({
            content: '配置失败'
          })
        } else {
          this[dialog].visible = false
          this.getList()
        }
      }).catch(() => {
      })
    },
    isAdmin (item) {
      return this.$store.getters.isAdmin
    },
    addOrUpdateConfig () {
      apiRequest.post(this.configDialog.urls.addOrUpdate, {}, this.configConsumerData).then(() => {
        this.configDialog.visible = false
        this.getList()
      })
    },
    // 查询
    // 原来的getList方法
    getList2 () {
      // 查询数据库里的数据
      this.showTablePin = true
      let query = {}
      if (this.keyword == null || this.keyword === '' || this.keyword === undefined) {
        query = {
          keyword: this.keyword
        }
      } else {
        query = {
          app: this.keyword
        }
      }
      let data = {
        pagination: {
          page: this.page.page,
          size: this.page.size
        },
        query: query
      }
      for (let i in this.search) {
        if (this.search.hasOwnProperty(i)) {
          data.query[i] = this.search[i]
        }
      }
      apiRequest.post(this.urls.search, {}, data).then((data) => {
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
          this.getMonitor(this.tableData.rowData[i], i)
        }
      })
    },
    // 实现懒加载的getList方法
    getList () {
      this.tableData.rowData = []
      // 查询数据库里的数据
      this.showTablePin = true
      let query = {}
      query.keyword = this.keyword
      let data = {
        pagination: {
          page: this.page.page,
          size: this.page.size
        },
        query: query
      }
      for (let i in this.search) {
        if (this.search.hasOwnProperty(i)) {
          data.query[i] = this.search[i]
        }
      }
      apiRequest.post(this.urls.search, {}, data).then((data) => {
        data.data = data.data || []
        data.pagination = data.pagination || {
          totalRecord: data.data.length
        }
        this.page.total = data.pagination.totalRecord
        this.page.page = data.pagination.page
        this.page.size = data.pagination.size
        data.data.sort(function (a, b) {
          return a.topic.code - b.topic.code
        })
        if (data.data.length > this.page.size) {
          this.tableData.rowData = data.data.slice(0, this.page.size)
          this.curIndex = this.page.size - 1
        } else {
          this.tableData.rowData = data.data
          this.curIndex = this.page.size
        }
        this.cacheList = data.data
        this.showTablePin = false
        for (let i = 0; i < this.tableData.rowData.length; i++) {
          this.getMonitor(this.tableData.rowData[i], i)
        }
      })
    },
    // 滚动事件触发下拉加载
    getRestList () {
      if (this.curIndex < this.cacheList.length - 1) {
        for (let i = 0; i < this.page.size; i++) {
          if (this.curIndex < this.cacheList.length - 1) {
            this.curIndex += 1
            if (!this.tableData.rowData.includes(this.cacheList[this.curIndex])) {
              this.tableData.rowData.push(this.cacheList[this.curIndex])
              this.getMonitor(this.tableData.rowData[this.curIndex], this.curIndex)
            }
          } else {
            break
          }
        }
      }
    }
  },
  mounted () {
    // this.getList();
    apiRequest.get(this.urls.messageTypes)
      .then(data => {
        this.messageTypes = data.data

        if (typeof this.messageTypes !== 'undefined' && this.messageTypes.length > 0) {
          if (typeof this.messageType === 'undefined') {
            this.messageType = this.messageTypes[0]
          }
        } else {
          console.error('Property message-types can not be empty!')
        }
      })
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  .label{text-align: right; line-height: 32px;}
  .val{}
  .load-btn { margin-right: 50px;margin-top: -100px;position: relative}
</style>
