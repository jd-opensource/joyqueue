<template>
  <div>
    <div class="ml20 mt30">
      <d-input v-model="searchData.name" placeholder="请输入分组/IP" class="left mr10" style="width: 10%">
        <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>
      </d-input>
    </div>
    <my-table :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange"
              @on-current-change="handleCurrentChange">
    </my-table>
  </div>
</template>

<script>
import myTable from '../../components/common/myTable.vue'
import myDialog from '../../components/common/myDialog.vue'
import crud from '../../mixins/crud.js'
export default {
  name: '',
  components: {
    myTable,
    myDialog
  },
  mixins: [ crud ],
  data () {
    return {
      urls: {
        search: `/broker/search`
      },
      searchData: {
        keyword: ''
      },
      searchRules: {
      },
      tableData: {
        rowData: [],
        colData: [
          {
            title: '分组',
            key: 'brokerGroup.code'
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
            title: '数据中心',
            key: 'dataCenter.name'
          },
          {
            title: '角色',
            key: 'role',
            render: (h, params) => {
              let txt = params.item.role === 'SLAVE' ? '从' : '主'
              return h('label', {}, txt)
            }
          },
          {
            title: '复制方式',
            key: 'syncMode',
            render: (h, params) => {
              let txt = params.item.syncMode === 'SYNCHRONOUS' ? '同步' : '异步'
              return h('label', {}, txt)
            }
          },
          {
            title: '重试类型',
            key: 'retryType',
            render: (h, params) => {
              let label
              switch (params.item.retryType) {
                case 'DB':
                  label = '直连数据库'
                  break
                case 'REMOTE':
                  label = '访问远程服务'
                  break
              }
              return h('label', {}, label)
            }
          },
          {
            title: '权限',
            key: 'permission',
            render: (h, params) => {
              let label
              switch (params.item.permission) {
                case 'NONE':
                  label = '无权限'
                  break
                case 'FULL':
                  label = '读写'
                  break
                case 'READ':
                  label = '只读'
                  break
                case 'WRITE':
                  label = '只写'
                  break
              }
              return h('label', {}, label)
            }
          },
          {
            title: '备注',
            key: 'description'
          },
          {
            title: '状态',
            key: 'status',
            render: (h, params) => {
              let txt = params.item.status === 1 ? '已启用' : '不可用'
              return h('label', {}, txt)
            }
          }
        ]
      }
    }
  },
  computed: {
  },
  methods: {
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
