<template>
  <div>
    <d-table :columns="tableData.colData" :data="tableData.rowData"></d-table>
  </div>

</template>

<script>
import MyTable from '../../../components/common/myTable'
import apiRequest from '../../../utils/apiRequest.js'
import crud from '../../../mixins/crud.js'
export default {
  name: 'partition-expand',
  components: {MyTable},
  mixins: [crud],
  props: {
    row: Object,
    subscribe: Object,
    partitionGroup: {
      type: Number
    },
    colData: {
      type: Array
    }
  },
  data () {
    return {
      urls: {
        search: `/monitor/find/partitionGroupDetailForTopicApp`
      },
      searchData: {
        subscribe: this.subscribe,
        partitionGroup: this.partitionGroup
      },
      tableData: {
        rowData: [],
        colData: this.colData
      }

    }
  },
  methods: {
    getList () {
      this.showTablePin = true
      apiRequest.postBase(this.urls.search, {}, this.searchData, false).then((data) => {
        data.data = data.data || []
        this.tableData.rowData = data.data
        this.showTablePin = false
      })
    }
  },
  watch: {
    row () {
      this.getList()
    }
  },
  mounted () {
    this.getList()
  }
}
</script>

<style scoped>
  .expand-row{
    margin-bottom: 16px;
  }
</style>
