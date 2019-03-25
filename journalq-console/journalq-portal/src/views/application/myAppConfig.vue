<template>
  <div>
    <div class="ml20 mt30">
      <d-button type="primary" @click="getList">刷新<icon name="rotate-cw" style="margin-left: 5px;"></icon></d-button>
    </div>
    <my-table :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange" @on-current-change="handleCurrentChange">
    </my-table>
  </div>
</template>

<script>
import myTable from '../../components/common/myTable.vue'
import myDialog from '../../components/common/myDialog.vue'
import crud from '../../mixins/crud.js'
export default {
  name: 'application',
  components: {
    myTable,
    myDialog
  },
  mixins: [ crud ],
  data () {
    return {
      urls: {
        search: `/application/${this.$route.query.id}/config_privileges/search`
      },
      searchRules: {
      },
      tableData: {
        rowData: [],
        colData: [
          {
            title: '配置',
            key: 'configuration.code'
          },
          {
            title: '权限',
            key: 'authority'
          },
          {
            title: '生效时间',
            key: 'effectiveTime'
          },
          {
            title: '失效时间',
            key: 'expirationTime'
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
    // this.getList();
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
.label{text-align: right; line-height: 32px;}
.val{}
</style>
