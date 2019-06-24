<template>
  <div>
    <my-table :data="tableData" :showPin="showTablePin" :showPagination=false
              :page="page" @on-size-change="handleSizeChange" @on-current-change="handleCurrentChange"/>
    <label >共：{{page.total}} 条记录</label>
  </div>
</template>

<script>
import MyTable from '../../../components/common/myTable'
import apiRequest from '../../../utils/apiRequest.js'
import crud from '../../../mixins/crud.js'

export default {
  name: 'detail-table',
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
          topic: {
            id: '',
            code: ''
          },
          namespace: {
            id: '',
            code: ''
          },
          app: {
            id: 0,
            code: ''
          },
          subscribeGroup: '',
          type: 1,
          clientType: -1
        }
      }
    },
    colData: {
      type: Array
    },
    urls: {
      type: Object,
      default: function () {
        return {
          search: ''
        }
      }
    },
    clientType: {
      type: Number,
      default: -1
    }
  },
  data () {
    return {
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
      apiRequest.postBase(this.urls.search, {}, this.search, false).then((data) => {
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
