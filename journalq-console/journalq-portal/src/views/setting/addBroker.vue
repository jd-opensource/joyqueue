<template>
  <div>
    <div class="ml20 mt10">
      <d-input v-model="searchData.keyword" placeholder="请输入要查询的IP/名称" class="left" style="width: 60%">
        <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>
      </d-input>
    </div>
    <my-table :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange"
              @on-current-change="handleCurrentChange" @on-selection-change="handleSelectionChange" @on-add="addBroker">
    </my-table>
  </div>
</template>

<script>
import apiRequest from '../../utils/apiRequest.js'
import myTable from '../../components/common/myTable.vue'
import crud from '../../mixins/crud.js'

export default {
  name: '',
  components: {
    myTable
  },
  props: {
    data: {
      type: Object,
      default () {
        return {}
      }
    },
    group: {
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
        search: '/broker/search',
        addBroker: '/broker/update'
      },
      searchData: {
        brokerGroupId: -1
      },
      tableData: {
        rowData: [{}],
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
            title: '分组',
            key: 'group.code'
          }
        ],
        btns: [
          {
            txt: '添加',
            method: 'on-add',
            bindKey: 'status',
            bindVal: 1
          }
        ]
      },
      multipleSelection: []
    }
  },
  computed: {
    addBrokerUrl () {
      return this.urlOrigin.addBroker
    }
  },
  methods: {
    addBroker (val) {
      let parmas = {
        id: val.id,
        group: this.group
      }
      let _this = this
      this.$Dialog.confirm({
        title: '提示',
        content: '确定要添加吗？'
      }).then(() => {
        apiRequest.put(_this.addBrokerUrl + '/' + val.id, null, parmas).then((data) => {
          _this.getList()
        })
      }).catch(() => {
      })
    }
  },
  mounted () {
    // this.getList()
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
.label{text-align: right; line-height: 32px;}
.val{}
</style>
