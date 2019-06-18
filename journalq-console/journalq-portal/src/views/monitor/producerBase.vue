<template>
  <div>
    <div class="headLine">
      <d-input v-model="keyword" :placeholder="keywordTip" class="input" @on-enter="getList">
        <span slot="prepend">{{keywordName}}</span>
        <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>
      </d-input>
      <d-button-group>
        <d-button type="primary" v-if="$store.getters.isAdmin" @click="openDialog('subscribeDialog')" class="button">
          订阅<icon name="plus-circle" style="margin-left: 3px;"/>
        </d-button>
        <d-button type="primary" @click="getList" class="button">刷新
          <icon name="refresh-cw" style="margin-left: 3px;"></icon>
        </d-button>
      </d-button-group>
    </div>
    <my-table :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange"
              @on-detail-chart="goDetailChart" @on-current-change="handleCurrentChange" @on-detail="openDetailTab"
              @on-config="openConfigDialog" @on-set-produce-weight="openWeightConfigDialog"
              @on-summary-chart="goSummaryChart" @on-performance-chart="goPerformanceChart" @on-rateLimit="openRateLimitDialog"/>

    <!--生产订阅弹出框-->
    <my-dialog :dialog="subscribeDialog" @on-dialog-cancel="dialogCancel('subscribeDialog')">
      <subscribe ref="subscribe" :search="search" :type="type" :colData="subscribeDialog.colData"
                 :keywordName="keywordName"
                 :searchUrl="subscribeDialog.urls.search" :addUrl="subscribeDialog.urls.add"
                 :doSearch="subscribeDialog.doSearch" @on-refresh="getList"/>
    </my-dialog>

    <!--Config dialog-->
    <my-dialog :dialog="configDialog" @on-dialog-confirm="configConfirm" @on-dialog-cancel="dialogCancel('configDialog')">
      <producer-config-form ref="configForm" :data="configData"/>
    </my-dialog>
    <my-dialog :dialog="produceWeightDialog" @on-dialog-confirm="weightConfigConfirm" @on-dialog-cancel="dialogCancel('produceWeightDialog')">
      <producer-weight ref="weightForm" :weights="produceWeightDialog.weights" :producer-id="configData.producerId"
                       :search="produceWeightDialog.urls.search" :update="produceWeightDialog.urls.update"/>
    </my-dialog>

    <my-dialog :dialog="rateLimitDialog" @on-dialog-confirm="rateLimitConfirm" @on-dialog-cancel="dialogCancel('rateLimitDialog')">
      <rate-limit ref="rateLimit" :limitTraffic="rateLimitDialog.limitTraffic" :limitTps="rateLimitDialog.limitTps"/>
    </my-dialog>

  </div>
</template>

<script>
import apiRequest from '../../utils/apiRequest.js'
import myTable from '../../components/common/myTable.vue'
import myDialog from '../../components/common/myDialog.vue'
import subscribe from './subscribe.vue'
import ProducerConfigForm from './producerConfigForm.vue'
import ProducerWeight from './produceWight.vue'
import {getTopicCode, replaceChartUrl} from '../../utils/common.js'
import RateLimit from './rateLimit'
import ButtonGroup from '../../components/button/button-group'

export default {
  name: 'producer-base',
  components: {
    ButtonGroup,
    RateLimit,
    myTable,
    myDialog,
    subscribe,
    ProducerConfigForm,
    ProducerWeight
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
            txt: '生产详情',
            method: 'on-detail'
          },
          {
            txt: '配置',
            method: 'on-config'
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
            txt: '设置生产权重',
            method: 'on-set-produce-weight'
          },
          {
            txt: '限流',
            method: 'on-rateLimit',
            isAdmin: true
          }
        ]
      }
    },
    colData: { // 生产者 列表表头
      type: Array
    },
    search: {// 查询条件，我的应用：app:{id:0,code:'',namespace:{id:0,code:''}}  ， 主题中心：topic:{id:0,code:'',namespace:{id:0,code:''}}
      type: Object
    },
    subscribeDialogColData: { // 订阅 列表表头
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
        search: `/producer/search`,
        getMonitor: `/monitor/find`,
        getUrl: `/grafana/getRedirectUrl`
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
        doSearch: false,
        limitTps: 0,
        limitTraffic: 0
      },
      type: this.$store.getters.producerType, // 1:生产， 2：消费
      subscribeDialog: {
        visible: false,
        title: '添加生产者',
        width: '850',
        showFooter: false,
        doSearch: false,
        colData: this.subscribeDialogColData.map((element) => Object.assign({}, element)), // 订阅框 列表表头,
        urls: {
          add: this.subscribeUrls.add,
          search: this.subscribeUrls.search
        }
      },
      produceWeightDialog: {
        visible: false,
        title: '设置生产权重',
        width: '700',
        showFooter: true,
        doSearch: false,
        weights: [], // 订阅框 列表表头,
        urls: {
          search: '/producer/weight',
          update: '/'
        }
      },
      configDialog: {
        visible: false,
        title: '生产者配置详情',
        width: '600',
        showFooter: true,
        urls: {
          addOrUpdae: `/producer/config/addOrUpdate`
        }
      },
      configData: {}
    }
  },
  methods: {
    openDialog (dialog) {
      this[dialog].doSearch = true
      this[dialog].visible = true
    },
    openDetailTab (item) {
      this.$emit('on-detail', item)
    },
    openConfigDialog (item) {
      this.configData = item.config || {}
      this.configData['producerId'] = item.id
      this.configDialog.visible = true
    },
    openWeightConfigDialog (item) {
      this.configData = item.config || {}
      this.configData['producerId'] = item.id
      this.produceWeightDialog.visible = true
    },
    openRateLimitDialog (item) {
      this.rateLimitDialog.limitTps = item.config.limitTps
      this.configData['producerId'] = item.id
      this.rateLimitDialog.limitTraffic = item.config.limitTraffic
      this.rateLimitDialog.visible = true
    },
    handleSizeChange (val) {
      this.page.size = val
      this.getList()
    },
    handleCurrentChange (val) {
      this.page.page = val
      this.getList()
    },
    dialogConfirm (dialog) {
      this[dialog].visible = false
      this.getList()
    },
    dialogCancel (dialog) {
      this[dialog].visible = false
      this.getList()
    },
    configConfirm () {
      this.configData = this.$refs.configForm.getFormData()
      this.config(this.configData, 'configDialog')
    },
    weightConfigConfirm () {
      let configData = {
        producerId: this.configData.producerId,
        weight: this.$refs.weightForm.getWeights()
      }
      this.config(configData, 'produceWeightDialog')
    },
    rateLimitConfirm () {
      let configData = {
        producerId: this.configData.producerId,
        limitTps: this.$refs.rateLimit.tps,
        limitTraffic: this.$refs.rateLimit.traffic
      }
      this.config(configData, 'rateLimitDialog')
    },
    goSummaryChart (item) {
      if (this.monitorUrls && this.monitorUrls.summary) {
        window.open(replaceChartUrl(this.monitorUrls.summary, item.topic.namespace.code,
          item.topic.code, item.app.code))
      } else {
        apiRequest.get(this.urls.getUrl + '/pt', {}, {}).then((data) => {
          let url = data.data || ''
          if (url.indexOf('?') < 0) {
            url += '?'
          } else if (!url.endsWith('?')) {
            url += '&'
          }
          url = url + 'var-topic=' + getTopicCode(item.topic, item.topic.namespace) + '&var-app=' + item.app.code
          window.open(url)
        })
      }
    },
    goDetailChart (item) {
      if (this.monitorUrls && this.monitorUrls.detail) {
        window.open(replaceChartUrl(this.monitorUrls.detail, item.topic.namespace.code,
          item.topic.code, item.app.code))
      } else {
        apiRequest.get(this.urls.getUrl + '/pd', {}, {}).then((data) => {
          let url = data.data || ''
          if (url.indexOf('?') < 0) {
            url += '?'
          } else if (!url.endsWith('?')) {
            url += '&'
          }
          url = url + 'var-topic=' + getTopicCode(item.topic, item.topic.namespace) + '&var-app=' + item.app.code
          window.open(url)
        })
      }
    },
    goPerformanceChart (item) {
      if (this.monitorUrls && this.monitorUrls.performance) {
        window.open(replaceChartUrl(this.monitorUrls.performance, item.topic.namespace.code,
          item.topic.code, item.app.code))
      } else {
        apiRequest.get(this.urls.getUrl + '/pp', {}, {}).then((data) => {
          if (data.data) {
            let url = data.data
            if (url.indexOf('?') < 0) {
              url += '?'
            } else if (!url.endsWith('?')) {
              url += '&'
            }
            url = url + 'var-topic=' + getTopicCode(item.topic, item.topic.namespace) + '&var-app=' + item.app.code
            window.open(url)
          }
        })
      }
    },
    isAdmin (item) {
      return this.$store.getters.isAdmin
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
        type: this.type
      }
      apiRequest.postBase(this.urls.getMonitor, {}, data, false).then((data) => {
        this.tableData.rowData[index] = Object.assign(row, data.data || [])
        this.$set(this.tableData.rowData, index, this.tableData.rowData[index])
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
      for (var i in this.search) {
        if (this.search.hasOwnProperty(i)) {
          data.query[i] = this.search[i]
        }
      }
      // this.tableData.rowData = [] // 先清空数据
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
    config (configData, dialog) {
      apiRequest.post(this.configDialog.urls.addOrUpdae, {}, configData).then((data) => {
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
