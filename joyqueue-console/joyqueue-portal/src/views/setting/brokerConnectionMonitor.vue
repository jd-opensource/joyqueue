<template>
  <div>
      <my-table :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange"
                @on-current-change="handleCurrentChange" @on-selection-change="handleSelectionChange"
                @on-edit="edit">
      </my-table>
  </div>
</template>

<script>
import myTable from '../../components/common/myTable.vue'
import myDialog from '../../components/common/myDialog.vue'
import crud from '../../mixins/crud.js'
import ClientConnection from '../monitor/detail/clientConnection'

export default {
  name: 'brokerConnectionMonitor',
  components: {
    ClientConnection,
    myTable,
    myDialog
  },
  props: {
  },
  mixins: [ crud ],
  data () {
    return {
      theme1: 'light',
      brokerId: this.$route.params.brokerId || this.$route.query.brokerId,
      urls: {
        search: '/monitor/broker/connection/search'
      },
      searchData: {
        brokerId: this.$route.params.brokerId
      },
      searchRules: {
      },
      tableData: {
        rowData: [],
        colData: [
          {
            title: '连接ID',
            key: 'connectionId'
          },
          {
            title: '应用',
            key: 'app'
          },
          {
            title: '版本号',
            key: 'version'
          },
          {
            title: '语言',
            key: 'language'
          },
          {
            title: '来源',
            key: 'source'
          },
          {
            title: '创建时间',
            key: 'createTime'
          },
          {
            title: '应用ip',
            key: 'ip'
          },
          {
            title: '端口',
            key: 'port'
          },
          {
            title: '作用域',
            key: 'namespace'
          },
          {
            title: '角色',
            key: 'producerRole',
            formatter (item) {
              if (item.producerRole) { return '生产者' }
              if (item.consumerRole) { return '消费者' }
            }

          }
        ]
        // 表格操作，如果需要根据特定值隐藏显示， 设置bindKey对应的属性名和bindVal对应的属性值
        // btns: [
        //   {
        //     txt: '编辑',
        //     method: 'on-edit'
        //   }
        // ]
      },
      multipleSelection: []
    }
  },
  methods: {
  },
  mounted () {
    this.getList()
  }
}
</script>
