<template>
  <div>
    <my-table :data="tableData" :showPin="showTablePin" style="height: 400px;overflow-y:auto" :showPagination=false
              :page="page" @on-size-change="handleSizeChange" @on-current-change="handleCurrentChange"/>
    <label >共 {{page.total}} 条记录</label>
  </div>
</template>

<script>
import MyTable from '../../components/common/myTable'
import apiRequest from '../../utils/apiRequest.js'
import crud from '../../mixins/crud.js'

export default {
  name: 'partition',
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
    btns: {
      type: Array
    },
    operates: {
      type: Array
    },
    colData: {
      type: Array
    }
  },
  data () {
    return {
      urls: {
        search: `/partitionGroup/findByTopic`,
        findConsumerPartitionGroups: `/monitor/find/partitionGroupsForTopicApp`,
        findProducerPartitionGroups: `/monitor/find/partitionGroups`
      },
      tableData: {
        rowData: [],
        colData: this.colData,
        btns: this.btns,
        operates: this.operates
      },
      page: {
        total: 0
      }
    }
  },
  methods: {
    getList () {
      this.showTablePin = true
      let payload = {
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
      // if(this.type == this.$store.getters.producerType) {
      // Find database
      apiRequest.post(this.urls.search, {}, payload).then((resp) => {
        resp.data = resp.data || []
        this.tableData.rowData = resp.data
        this.showTablePin = false
        // Find by monitor service
        this.page.total = resp.data.length
        let that = this
        // add subscribe column
        for (let i = 0; i < that.tableData.rowData.length; i++) {
          that.tableData.rowData[i].subscribe = payload
        }
        let requestUrl = this.type === this.$store.getters.producerType ? this.urls.findProducerPartitionGroups : this.urls.findConsumerPartitionGroups
        apiRequest.postBase(requestUrl, {}, payload, false).then((data) => {
          let monitorData = data.data || []
          for (let i = 0; i < that.tableData.rowData.length; i++) {
            for (let j = 0; j < monitorData.length; j++) {
              if (that.tableData.rowData[i].groupNo === monitorData[j].partitionGroup) {
                that.tableData.rowData[i].ip = monitorData[j].ip
                // that.tableData.rowData[i].connections = monitorData[j].connections;
                if (this.type === this.$store.getters.producerType) {
                  that.tableData.rowData[i].enQuence = monitorData[j].enQuence || {count: 0}
                  that.tableData.rowData[i].deQuence = monitorData[j].deQuence || {count: 0}
                } else if (this.type === this.$store.getters.consumerType) {
                  that.tableData.rowData[i].deQuence = monitorData[j].deQuence || {count: 0}
                  that.tableData.rowData[i].pending = monitorData[j].pending || {count: 0}
                }
                that.$set(this.tableData.rowData, i, that.tableData.rowData[i])
              }
            }
          }
        })
      })
      // }
      // if(this.type == this.$store.getters.consumerType){
      //    //Only find by monitor service
      //    apiRequest.postBase(this.urls.findConsumerPartitionGroups, {}, payload, false).then((data) => {
      //      if (data.code == 200) {
      //        this.tableData.rowData = data.data || [];
      //      }
      //    });
      //  }
    }
  }
}
</script>
<style scoped>

</style>
