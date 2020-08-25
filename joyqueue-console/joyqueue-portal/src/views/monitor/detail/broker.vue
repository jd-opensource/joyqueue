<template>
  <div>
    <my-table :data="tableData" :showPin="showTablePin" :showPagination=false
              :page="page" @on-size-change="handleSizeChange" @on-current-change="handleCurrentChange"
              @on-disconnect="disconnectBroker"/>
    <label >共 {{page.total}} 条记录</label>
  </div>
</template>

<script>
import MyTable from '../../../components/common/myTable'
import crud from '../../../mixins/crud.js'
import apiRequest from '../../../utils/apiRequest.js'

export default {
  name: 'broker',
  components: {MyTable},
  mixins: [crud],
  props: {
    doSearch: {
      type: Boolean,
      default: false
    },
    search: {
      type: Object,
      default: function () {
        return {
          subscribeGroup: '',
          topic: {
            code: ''
          },
          namespace: {
            code: ''
          },
          app: {
            code: ''
          },
          type: 0
        }
      }
    },
    colData: {
      type: Array,
      default: function () {
        return [
          {
            title: 'IP:端口',
            key: 'ip'
          },
          {
            title: '机房 [编码/名称]',
            key: 'dataCenter',
            formatter (item) {
              if (item.dataCenter) {
                return item.dataCenter.code + '/' + item.dataCenter.name
              }
            }
          },
          {
            title: '连接数',
            key: 'connection',
            render: (h, params) => {
              if (this.search.type === this.$store.getters.producerType) {
                return h('span', params.item.producer)
              } else {
                return h('span', params.item.consumer)
              }
            }
          }
        ]
      }
    }
  },
  data () {
    return {
      urls: {
        disconnect: `/monitor/remove/connections`,
        getMonitor: `/monitor/find/connection`
      },
      tableData: {
        rowData: [],
        colData: this.colData,
        btns: [
          {
            txt: '关闭连接',
            method: 'on-disconnect',
            isAdmin: 1
          }
        ]
      },
      page: {
        total: 0
      }
      // producerType: this.$store.getters.producerType
    }
  },
  methods: {
    isAdmin (item) {
      return this.$store.getters.isAdmin
    },
    disconnectBroker (item, index) {
      let params = '?id=' + item.id
      apiRequest.postBase(this.urls.disconnect + params, {}, this.search, true).then((data) => {
        data.data = data.data
        if (data.data === 'success') {
          this.$Message.success('断开成功')
          this.getList()
        } else {
          this.$Message.error('断开失败')
        }
      })
    },
    getList () {
      this.showTablePin = true
      apiRequest.postBase(this.urls.getMonitor, {}, this.search, false).then((data) => {
        data.data = data.data || []
        this.tableData.rowData = data.data
        this.page.total = data.data.length
        this.showTablePin = false
      })
    }
  }
  // watch: {
  //   '$route' (to, from) {
  //     console.log('broker')
  //     if (to.query.subTab === 'broker') {
  //       this.search.app.code = to.query.app
  //       this.search.topic.code = to.query.topic
  //       this.search.namespace.code = to.query.namespace
  //       this.getList()
  //     }
  //   }
  // },
  // mounted () {
  //   this.getList()
  // }
}

</script>

<style scoped>

</style>
