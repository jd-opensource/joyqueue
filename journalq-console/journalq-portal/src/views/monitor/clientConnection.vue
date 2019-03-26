<template>
  <div>
    <my-table :data="tableData" :showPin="showTablePin" style="height: 400px;overflow-y:auto" :showPagination=false
              :page="page" @on-size-change="handleSizeChange" @on-current-change="handleCurrentChange"/>
    <!--<d-pagination  class="right mr20"-->
                   <!--show-total show-sizer show-quickjump-->
                   <!--:current="page.page"-->
                   <!--:page-size="page.size"-->
                   <!--:total="page.total">-->
    <!--</d-pagination>-->
    <label >共 {{page.total}} 条记录</label>
  </div>
</template>

<script>
import MyTable from '../../components/common/myTable'
import apiRequest from '../../utils/apiRequest.js'
import crud from '../../mixins/crud.js'

export default {
  name: 'client-connection',
  components: {MyTable},
  mixins: [crud],
  props: {
    doSearch: {
      type: Boolean,
      default: false
    },
    app: {
      id: 0,
      code: ''
    },
    subscribeGroup: '',
    topic: {
      id: '0',
      code: ''
    },
    namespace: {
      id: '0',
      code: ''
    },
    type: {
      type: Number,
      default: 0
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
      let data = {
        topic: {
          id: this.topic.id,
          code: this.topic.code
        },
        namespace: {
          id: this.namespace.id,
          code: this.namespace.code
        },
        app: {
          id: this.app.id,
          code: this.app.code
        },
        subscribeGroup: this.subscribeGroup || '',
        type: this.type
      }

      apiRequest.postBase(this.urls.getMonitor, {}, data, false).then((data) => {
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
}
</script>

<style scoped>

</style>
