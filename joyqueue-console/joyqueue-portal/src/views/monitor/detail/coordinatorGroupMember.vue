<template>
  <div>
    <p> 扩展字段：{{extension}}</p>
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
  name: 'coordinator-group-member',
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
    urls: {
      type: Object,
      default: function () {
        return {
          search: ''
        }
      }
    },
  },
  data () {
    return {
      tableData: {
        rowData: [],
        colData: this.colData
      },
      page: {
        total: 0
      },
      extension: ''
    }
  },
  methods: {
    getList () {
      this.showTablePin = true
      apiRequest.postBase(this.urls.search, {}, this.search, false).then((data) => {
        let result = (data.data || {})
        this.extension = result.extension
        this.tableData.rowData = (result.members ||[])
        // this.onListResult(data)
        this.page.total = (result.members || []).length
        this.showTablePin = false
      })
    }
  }
}
</script>

<style scoped>

</style>
