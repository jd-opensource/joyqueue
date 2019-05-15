<template>
  <div>
    <my-table :data="tableData" :showPin="showTablePin" style="height: 400px;overflow-y:auto" :showPagination=false
              :page="page" @on-size-change="handleSizeChange" @on-current-change="handleCurrentChange"/>
    <label >共：{{page.total}} 条记录</label>
  </div>
</template>

<script>
import MyTable from '../../components/common/myTable'
import apiRequest from '../../utils/apiRequest.js'
import crud from '../../mixins/crud.js'

export default {
  name: 'tab-table',
  components: {MyTable},
  mixins: [crud],
  props: {
    doSearch: {
      type: Boolean,
      default: false
    },
    colData: {
      type: Array
    },
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
    },
    type: {
      type: Number,
      default: 0
    },
    searchData: {},
    search: {
      type: String,
      default: ''
    },
    clientType: {
      type: Number,
      default: -1
    }
  },
  data () {
    return {
      urls: {
        search: this.search
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
        type: this.type,
        clientType: this.clientType
      }
      apiRequest.postBase(this.urls.search, {}, data, false).then((data) => {
        data.data = data.data || []
        this.tableData.rowData = data.data
        // this.onListResult(data);
        this.page.total = data.data.length
        this.showTablePin = false
      })
    }
  }
}
</script>

<style scoped>

</style>
