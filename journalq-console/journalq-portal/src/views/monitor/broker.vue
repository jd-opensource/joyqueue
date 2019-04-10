<template>
  <div>
    <my-table :data="tableData" :showPin="showTablePin" style="height: 400px;overflow-y:auto" :showPagination=false
              :page="page" @on-size-change="handleSizeChange" @on-current-change="handleCurrentChange"/>
    <label >共 {{page.total}} 条记录</label>
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
    type: { // 0-producer, 1-consumer
      type: Number,
      default: 0
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
            title: '连接数',
            key: 'connection',
            render: (h, params) => {
              if (this.type === this.$store.getters.producerType) {
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
        getMonitor: `/monitor/find/connection`
      },
      tableData: {
        rowData: [],
        colData: this.colData
      },
      page: {
        total: 0
      }
      // producerType: this.$store.getters.producerType
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
        this.tableData.rowData = data.data
        this.page.total = data.data.length
        this.showTablePin = false
      })
    }
  }
}
</script>

<style scoped>

</style>
