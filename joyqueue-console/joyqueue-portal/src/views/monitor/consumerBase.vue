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

    <my-table :data="tableData" :showPin="showTablePin" :page="page"
              @on-detail-chart="goDetailChart" @on-current-change="handleCurrentChange" @on-detail="openDetailTab"
              @on-msg-preview="openMsgPreviewDialog" @on-msg-detail="openMsgDetailDialog" @on-config="openConfigDialog"
              @on-performance-chart="goPerformanceChart" @on-summary-chart="goSummaryChart"  @on-rateLimit="openRateLimitDialog"
              @on-size-change="handleSizeChange" @on-cancel-subscribe="cancelSubscribe"/>

    <!--Consumer subscribe dialog-->
    <my-dialog :dialog="subscribeDialog" @on-dialog-cancel="dialogCancel('subscribeDialog')">
      <subscribe ref="subscribe" :search="search" :type="type" :colData="subscribeDialog.colData"
                 :searchUrl="subscribeDialog.urls.search" :addUrl="subscribeDialog.urls.add"
                 :keywordName="keywordName" @on-refresh="getList"/>
    </my-dialog>

    <!--Msg preview dialog-->
    <my-dialog :dialog="msgPreviewDialog" @on-dialog-cancel="dialogCancel('msgPreviewDialog')">
      <msg-preview ref="msgPreview" :app="msgPreviewDialog.app" :topic="msgPreviewDialog.topic" :namespace="msgPreviewDialog.namespace"
                   :type="type" :subscribeGroup="msgPreviewDialog.subscribeGroup"/>
    </my-dialog>

    <!--Msg detail dialog-->
    <my-dialog :dialog="msgDetailDialog" @on-dialog-cancel="dialogCancel('msgDetailDialog')">
      <msg-detail ref="msgDetail" :app="msgDetailDialog.app" :topic="msgDetailDialog.topic" :namespace="msgDetailDialog.namespace"
                   :type="type" :subscribeGroup="msgDetailDialog.subscribeGroup"/>
    </my-dialog>

    <!--Config dialog-->
    <my-dialog :dialog="configDialog" @on-dialog-confirm="configConsumerConfirm" @on-dialog-cancel="dialogCancel('configDialog')">
      <consumer-config-form ref="configForm" :data="configConsumerData"/>
    </my-dialog>

    <!--Rate limit dialog-->
    <my-dialog :dialog="rateLimitDialog" @on-dialog-confirm="rateLimitConfirm" @on-dialog-cancel="dialogCancel('rateLimitDialog')">
      <rate-limit ref="rateLimit" :limitTraffic="rateLimitDialog.limitTraffic" :limitTps="rateLimitDialog.limitTps"/>
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

export default {
  name: 'consumer-base',
  components: {
    RateLimit,
    MsgDetail,
    myTable,
    myDialog,
    subscribe,
    msgPreview,
    consumerConfigForm
  },
  props: {
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
            method: 'on-rateLimit',
            isAdmin: true
          }
        ]
      }
    },
    colData: { // 消费者 列表表头
      type: Array
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
      urls: {
        search: `/consumer/search`,
        getMonitor: `/monitor/find`,
        previewMessage: '/monitor/preview/message',
        getUrl: `/grafana/getRedirectUrl`,
        del: `/consumer/delete`
      },
      showTablePin: false,
      tableData: {
        rowData: [],
        colData: this.colData,
        btns: this.btns,
        operates: this.operates
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
        width: '400',
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
          search: 'consumer/config/search'
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
       monitorUIds: {
         detail: this.$store.getters.uIds.consumer.detail,
         summary: this.$store.getters.uIds.consumer.summary
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
      this.configData = item.config || {}
      this.rateLimitDialog.limitTps = item.config.limitTps
      this.configConsumerData['consumerId'] = item.id
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
        apiRequest.get(this.urls.getUrl + '/' + this.monitorUIds.summary, {}, {}).then((data) => {
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
        apiRequest.get(this.urls.getUrl + '/' + this.monitorUIds.detail, {}, {}).then((data) => {
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
        apiRequest.get(this.urls.getUrl + '/cp', {}, {}).then((data) => {
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
    getList () {
      // 查询数据库里的数据
      this.showTablePin = true
      let data = {
        pagination: {
          page: this.page.page,
          size: this.page.size
        },
        query: {
          keyword: this.keyword
        }
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
    }
  },
  mounted () {
    // this.getList();
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  .label{text-align: right; line-height: 32px;}
  .val{}
</style>
