<template>
  <div>
    <div class="ml20 mt10">
      <d-input v-model="searchData.keyword" placeholder="请输入要查询的IP/名称" class="left"
               oninput="value = value.trim()"
               style="width: 300px" @on-enter="getList">
        <span slot="prepend">关键词</span>
        <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>
      </d-input>
    </div>
    <my-table :optional="true" :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange"
              @on-current-change="handleCurrentChange" @on-selection-change="handleSelectionChange" @on-del="del">
    </my-table>

  </div>
</template>

<script>
import apiRequest from '../../utils/apiRequest.js'
import myTable from '../../components/common/myTable.vue'
import crud from '../../mixins/crud.js'
export default {
  name: 'group-new',
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
      partitionGroup: {},
      urls: {
        search: '/partitionGroupReplica/search',
        del: '/partitionGroupReplica/delete'
      },
      searchData: {
        keyword: ''
      },
      tableData: {
        rowData: [{}],
        colData: [
          {
            title: 'brokerId',
            key: 'brokerId'
          },
          {
            title: 'IP',
            key: 'broker.ip'
          },
          {
            title: '端口',
            key: 'broker.port'
          },
          {
            title: '角色',
            key: 'role',
            render: (h, params) => {
              let label
              switch (params.item.role) {
                case 0:
                  label = 'dynamics'
                  break
                case 1:
                  label = 'master'
                  break
                case 2:
                  label = 'slave'
                  break
                case 3:
                  label = 'leaner'
                  break
                case 4:
                  label = 'outsync'
                  break
              }
              return h('label', {}, label)
            }
          },
          {
            title: '状态',
            key: 'broker.status',
            render: (h, params) => {
              let label
              switch (params.item.status) {
                case 1:
                  label = '正常'
                  break
                case 2:
                  label = '异常'
                  break
              }
              return h('label', {}, label)
            }
          }
        ],
        btns: [
          {
            txt: '移除',
            method: 'on-del'
          }
        ]
      },
      multipleSelection: []
    }
  },
  methods: {
    handleSelectionChange (val) {
      this.multipleSelection = val
      this.$emit('on-choosed-broker', val)
    },
    del (item) {
      let _this = this
      this.$Dialog.confirm({
        title: '提示',
        content: '确定要删除吗？'
      }).then(() => {
        let data = item
        apiRequest.post(this.urls.del, {}, data).then((data) => {
          _this.$Message.success('删除成功')
          _this.getList()
          _this.$emit('on-partition-group-change')
        })
      })
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
          topic: this.partitionGroup.topic,
          namespace: this.partitionGroup.namespace,
          groupNo: this.partitionGroup.groupNo,
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
    this.partitionGroup = this.data
    this.getList()
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  .label{text-align: right; line-height: 32px;}
  .val{}
</style>
