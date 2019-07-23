<template>
  <div>
    <div class="ml20 mt10">
      <d-input v-model="searchData.keyword" placeholder="请输入要查询的IP/名称" class="left"
               style="width: 300px" @on-enter="getList">
        <span slot="prepend">关键词</span>
        <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>
      </d-input>
    </div>
    <my-table :optional="true" :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange"
              @on-current-change="handleCurrentChange" @on-selection-change="handleSelectionChange" @on-add="add">
    </my-table>

  </div>
</template>

<script>
import apiRequest from '../../utils/apiRequest.js'
import myTable from '../../components/common/myTable.vue'
import crud from '../../mixins/crud.js'
export default {
  name: 'group-scale',
  components: {
    myTable
  },
  props: {
    data: {
      type: Object,
      default () {
        return {}
      }
    }
  },
  mixins: [ crud ],
  data () {
    return {
      urls: {
        search: '/partitionGroupReplica/searchBrokerToScale',
        addUrl: '/partitionGroupReplica/add'
      },
      searchData: {
        keyword: ''
      },
      tableData: {
        rowData: [{}],
        colData: [
          {
            title: 'Broker分组',
            key: 'group.code'
          },
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
        ],
        btns: [
          {
            txt: '添加',
            method: 'on-add'
          }
        ]
      },
      multipleSelection: []
    }
  },
  methods: {
    // 扩容
    add (item) {
      let parmas = {
        topic: this.data.topic,
        namespace: this.data.namespace,
        groupNo: this.data.groupNo,
        brokerId: item.id
      }
      console.log(99)
      console.log(this.data)
      let _this = this
      this.$Dialog.confirm({
        title: '提示',
        content: '确定要扩容吗？'
      }).then(() => {
        apiRequest.post(_this.urls.addUrl, null, parmas).then((data) => {
          _this.getList()
          _this.$emit('on-partition-group-change')
        })
      }).catch(() => {
      })
    },
    handleSelectionChange (val) {
      this.multipleSelection = val
      this.$emit('on-choosed-broker', val)
    },
    // 查询
    getList () {
      // 1. 查询数据库里的数据
      this.showTablePin = true
      let data = {
        pagination: {
          page: this.page.page,
          size: this.page.size
        },
        query: {
          topic: this.data.topic,
          namespace: this.data.namespace,
          groupNo: this.data.groupNo,
          keyword: this.searchData.keyword
        }
      }
      apiRequest.post(this.urlOrigin.search, {}, data).then((data) => {
        data.data = data.data || []
        data.pagination = data.pagination || {
          totalRecord: data.data.length
        }
        this.page.total = data.pagination.totalRecord
        this.page.page = data.pagination.page
        this.page.size = data.pagination.size
        this.tableData.rowData = data.data
        this.showTablePin = false
      })
    }
  },
  mounted () {
    this.getList()
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  .label{text-align: right; line-height: 32px;}
  .val{}
</style>
