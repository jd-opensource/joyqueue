<template>
  <div>
    <my-table :data="tableData" :showPin="showTablePin" style="height: 400px;overflow-y:auto" :showPagination="false"/>
  </div>
</template>

<script>
import MyTable from '../../components/common/myTable'
import crud from '../../mixins/crud.js'
import apiRequest from '../../utils/apiRequest.js'

export default {
  name: 'broker',
  components: {MyTable},
  mixins: [crud],
  props: {
    topicId: ''
  },
  data () {
    return {
      urls: {
        search: `/broker/findByTopic`
      },
      tableData: {
        rowData: [],
        colData: [
          {
            title: 'ID',
            key: 'id'
          },
          {
            title: 'IP',
            key: 'ip'
          },
          {
            title: '端口',
            key: 'port'
          }
        ]
      }
    }
  },
  methods: {
    getList () {
      this.showTablePin = true
      apiRequest.postBase(this.urls.search, {}, this.topicId, false).then((data) => {
        data.data = data.data || []
        this.tableData.rowData = data.data
        this.showTablePin = false
      })
    }
  }
}
</script>

<style scoped>

</style>
