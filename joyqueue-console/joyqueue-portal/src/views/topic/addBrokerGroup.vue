<template>
  <div>
    <div class="ml20 mt10">
      <d-input v-model="searchData.keyword" oninput="value = value.trim()" placeholder="请输入要查询的IP" class="left"
               style="width: 300px" @on-enter="getList">
        <span slot="prepend">关键词</span>
        <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>
      </d-input>
    </div>
    <my-table :optional="true" :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange"
              @on-current-change="handleCurrentChange" @on-selection-change="handleSelectionChange">
    </my-table>

  </div>
</template>

<script>
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
    }
  },
  mixins: [ crud ],
  data () {
    return {
      urls: {
        search: '/broker/search'
      },
      searchData: {
        keyword: '',
        brokerGroupCodes: this.data.brokerGroupCodes
      },
      tableData: {
        rowData: [],
        colData: [
          {
            title: 'Broker分组',
            key: 'brokerGroup.code'
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
      },
      multipleSelection: []
    }
  },
  computed: {
  },
  methods: {
    handleSelectionChange (val) {
      this.multipleSelection = val
      this.$emit('on-add-brokerGroup', val)
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
