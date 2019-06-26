<template>
  <div>
    <my-table :data="tableData" :showPin="showTablePin" style="height: 400px;overflow-y:auto" :showPagination="false"/>
  </div>
</template>

<script>
import MyTable from '../../components/common/myTable'
import crud from '../../mixins/crud.js'
import apiRequest from '../../utils/apiRequest.js'
import {timeStampToString} from '../../utils/dateTimeUtils'

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
        search: `/broker/findByTopic`,
        startInfo: '/monitor/start/'
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
          },
          {
            title: '启动时间',
            key: 'startupTime'
          },
          {
            title: 'revision',
            key: 'revision'
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
        for (let i = 0; i < this.tableData.rowData.length; i++) {
          this.getBrokerStatus(this.tableData.rowData, i)
        }
        this.showTablePin = false
      })
    },
    getBrokerStatus (rowData, i) {
      apiRequest.get(this.urlOrigin.startInfo + '/' + rowData[i].id).then((data) => {
        if (data.code === 200) {
          this.tableData.rowData[i].startupTime = timeStampToString(data.data.startupTime)
          this.tableData.rowData[i].revision = data.data.revision
        } else {
          this.tableData.rowData[i].startupTime = '不存活'
        }
        this.$set(this.tableData.rowData, i, this.tableData.rowData[i])
      })
    }
  }
}
</script>

<style scoped>

</style>
