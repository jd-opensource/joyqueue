<template>
  <div>
    <div class="ml20 mt30">
      <d-input v-model="keyword" :placeholder="keywordTip" class="left mr10" style="width: 10%">
        <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>
      </d-input>
      <d-button type="primary" v-if="$store.getters.isAdmin" @click="openDialog('subscribeDialog')" class="left mr10">订阅<icon name="plus-circle" style="margin-left: 5px;"></icon></d-button>
      <!--<d-button type="primary" @click="syncProducer" class="left mr10">同步生产者<icon name="download" style="margin-left: 5px;"></icon></d-button>-->
      <d-button type="primary" v-if="summaryChartShow" @click="goSummaryChart" class="left mr10">汇总图表<icon name="bar-chart" style="margin-left: 5px;"></icon></d-button>
    </div>
    <my-table :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange"
              @on-detail-chart="goDetailChart" @on-current-change="handleCurrentChange" @on-detail="openDetailDialog"
              @on-config="openConfigDialog" @on-set-produce-weight="openWeightConfigDialog"/>

    <!--生产订阅弹出框-->
    <my-dialog :dialog="subscribeDialog" @on-dialog-cancel="dialogCancel('subscribeDialog')">
      <subscribe ref="subscribe" :search="search" :type="type" :colData="subscribeDialog.colData"
                 :searchUrl="subscribeDialog.urls.search" :addUrl="subscribeDialog.urls.add"
                 :doSearch="subscribeDialog.doSearch" @on-refresh="getList"/>
    </my-dialog>

    <!--详情弹出框-->
    <my-dialog :dialog="detailDialog" @on-dialog-cancel="dialogCancel('detailDialog')">
      <d-tabs @on-change="handleTabChange">
        <d-tab-pane label="分组" name="partition" icon="pocket">
          <partition ref="partition" :app="detailDialog.app" :topic="detailDialog.topic"
                     :colData="detailDialog.partition.colData" :namespace="detailDialog.namespace"
                     :type="type" :doSearch="detailDialog.doSearch"/>
        </d-tab-pane>
        <d-tab-pane label="客户端连接" name="clientConnection" icon="github">
          <client-connection ref="clientConnection" :app="detailDialog.app" :topic="detailDialog.topic" :namespace="detailDialog.namespace"
                             :type="type" :doSearch="detailDialog.doSearch"/>
        </d-tab-pane>
        <d-tab-pane label="Broker" name="broker" icon="file-text">
          <broker ref="broker" :app="detailDialog.app" :topic="detailDialog.topic" :namespace="detailDialog.namespace"
                  :type="type" :doSearch="detailDialog.doSearch"/>
        </d-tab-pane>
      </d-tabs>
    </my-dialog>

    <!--Config dialog-->
    <my-dialog :dialog="configDialog" @on-dialog-confirm="configConfirm" @on-dialog-cancel="dialogCancel('configDialog')">
      <producer-config-form ref="configForm" :data="configData"/>
    </my-dialog>
    <my-dialog :dialog="produceWeightDialog" @on-dialog-confirm="weightConfigConfirm" @on-dialog-cancel="dialogCancel('produceWeightDialog')">
      <producer-weight ref="weightForm" :weights="produceWeightDialog.weights" :producer-id="configData.producerId"
                       :search="produceWeightDialog.urls.search" :update="produceWeightDialog.urls.update"/>
    </my-dialog>

  </div>
</template>

<script>
import apiRequest from '../../utils/apiRequest.js'
import myTable from '../../components/common/myTable.vue'
import myDialog from '../../components/common/myDialog.vue'
import subscribe from './subscribe.vue'
import broker from './broker.vue'
import partition from './partition.vue'
import clientConnection from './clientConnection.vue'
import ProducerConfigForm from './producerConfigForm.vue'
import ProducerWeight from './produceWight.vue'
import cookie from '../../utils/cookie.js'
import partitionExpand from './partitionExpand'

export default {
  name: 'producer-base',
  components: {
    myTable,
    myDialog,
    subscribe,
    broker,
    partition,
    clientConnection,
    ProducerConfigForm,
    ProducerWeight,
    partitionExpand
  },
  props: {
    keywordTip: {
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
            txt: '详情图表',
            method: 'on-detail-chart'
          },
          {
            txt: '配置',
            method: 'on-config'
          }
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
    partitionColData: { // 分片 列表表头
      type: Array,
      default: function () {
        return [
          {
            type: 'expand',
            width: 50,
            render: (h, params) => {
              // console.log(h);
              console.log('expand:' + params)
              return h(partitionExpand, {
                props: {
                  row: params.row,
                  colData: [
                    {
                      title: 'ID',
                      key: 'partitionGroup'
                    },
                    // {
                    //   title: '主分片',
                    //   key: 'ip'
                    // },
                    {
                      title: '分区',
                      key: 'partition'
                    },
                    {
                      title: '入队数',
                      key: 'enQuence.count'
                    }
                  ],
                  subscribe: params.row.subscribe,
                  partitionGroup: params.row.groupNo
                }
              })
            }
          },
          {
            title: 'ID',
            key: 'groupNo'
          },
          {
            title: '主分片',
            key: 'ip'
          },
          {
            title: '分区',
            key: 'partitions'
          },
          {
            title: '入队数',
            key: 'enQuence.count'
          }
        ]
      }
    },
    summaryChartShow: {
      type: Boolean,
      default: false
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
      type: this.$store.getters.producerType, // 1:生产， 2：消费
      subscribeDialog: {
        visible: false,
        title: '添加生产者',
        width: '700',
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
      detailDialog: {
        visible: false,
        title: '生产者详情',
        width: '900',
        showFooter: false,
        doSearch: true,
        app: {
          id: 0,
          code: ''
        },
        topic: {
          id: '0',
          code: ''
        },
        namespace: {
          id: '0',
          code: ''
        },
        partition: {
          colData: this.partitionColData

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
    openDetailDialog (item) {
      this.detailDialog.app.id = item.app.id
      this.detailDialog.app.code = item.app.code
      this.detailDialog.topic.id = item.topic.id
      this.detailDialog.topic.code = item.topic.code
      this.detailDialog.namespace.id = item.namespace.id
      this.detailDialog.namespace.code = item.namespace.code
      this.openDialog('detailDialog')
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
    // syncProducer() {
    //   //todo 同步生产者
    // },
    goSummaryChart () {
      // 1. get open url and token
      apiRequest.get(this.urls.getUrl + '/pt', {}, {}).then((data) => {
        let url = data.data || ''
        if (url.indexOf('?') < 0) {
          url += '?'
        } else if (!url.endsWith('?')) {
          url += '&'
        }
        if (this.search.app === undefined || this.search.app.code === undefined) {
          this.$Message.error('app获取失败！')
          return
        }
        url = url + 'var-app=' + this.search.app.code
        // 2. open
        let cookieValue = cookie.get(this.$store.getters.cookieName)
        if (cookieValue == null) {
          this.$Message.error('cookie获取失败！')
          return
        }
        url = url + '&var-cookie=' + this.$store.getters.cookieName + '=' + cookieValue
        window.open(url)
      })
    },
    goDetailChart (item) {
      // 1. get open url and token
      apiRequest.get(this.urls.getUrl + '/pd', {}, {}).then((data) => {
        let url = data.data || ''
        if (url.indexOf('?') < 0) {
          url += '?'
        } else if (!url.endsWith('?')) {
          url += '&'
        }
        url = url + 'var-topic=' + item.topic.code + '&var-app=' + item.app.code
        // 2. open
        let cookieValue = cookie.get(this.$store.getters.cookieName)
        if (cookieValue == null) {
          this.$Message.error('cookie获取失败！')
          return
        }
        url = url + '&var-cookie=' + this.$store.getters.cookieName + '=' + cookieValue
        window.open(url)
      })
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
