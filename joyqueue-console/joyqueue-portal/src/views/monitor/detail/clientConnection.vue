<template>
  <div>
    <my-table :data="tableData" :showPin="showTablePin" :showPagination=false
              :page="page" @on-size-change="handleSizeChange" @on-current-change="handleCurrentChange"/>
    <label >共 {{page.total}} 条记录</label>
  </div>
</template>

<script>
import MyTable from '../../../components/common/myTable'
import apiRequest from '../../../utils/apiRequest.js'
import crud from '../../../mixins/crud.js'

export default {
  name: 'client-connection',
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
            title: '连接ID',
            key: 'client.connectionId'
          },
          {
            title: '地址',
            key: 'client.ip'
          },
          {
            title: '端口',
            key: 'client.port'
          },
          {
            title: '版本号',
            key: 'client.version'
          },
          {
            title: '语言',
            key: 'client.language'
          }, {
            title: '来源',
            key: 'client.source'
          },
          {
            title: '消息服务器',
            key: 'ip'
          },
          {
            title: '授权',
            key: 'client.auth',
            formatter (item) {
              return (item.client && item.client.auth === true) ? '是' : '否'
            }
          }
        ]
      }
    }
  },
  data () {
    return {
      urls: {
        getMonitor: `/monitor/find/client`
      },
      tableData: {
        rowData: [],
        colData: this.colData
      },
      page: {
        total: 0
      }
    }
  },
  methods: {
    getList () {
      this.showTablePin = true
      apiRequest.postBase(this.urls.getMonitor, {}, this.search, false).then((data) => {
        data.data = data.data || []
        // this.onListResult(data);
        this.tableData.rowData = data.data
        let page = {
          total: data.data.length
        }
        this.page = page
        this.showTablePin = false
        // this.page.total = data.pagination.;
        // this.page.page = data.pagination.page ;
        // this.page.size = data.pagination.size;
      })
    }
  }
  // watch: {
  //   '$route' (to, from) {
  //     console.log('clientconn')
  //     if (to.query.subTab === 'clientConnection') {
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
