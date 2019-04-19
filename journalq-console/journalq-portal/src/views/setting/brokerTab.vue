<template>
  <div>
    <div class="ml20 mt30">
      <d-input v-model="searchData.keyword" placeholder="请输入ID/分组编码/IP" class="left mr10" style="width: 10%">
        <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>
      </d-input>
      <!--<d-button type="primary" @click="remove">移除<icon name="minus-circle" style="margin-left: 5px;"></icon></d-button>-->
    </div>
    <my-table :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange"
              @on-current-change="handleCurrentChange" @on-del="del">
    </my-table>
  </div>
</template>

<script>
import apiRequest from '../../utils/apiRequest.js'
import apiUrl from '../../utils/apiUrl.js'
import myTable from '../../components/common/myTable.vue'
import crud from '../../mixins/crud.js'

export default {
  name: 'brokerTab',
  components: {
    myTable
  },
  mixins: [ crud ],
  data () {
    return {
      urls: {
        search: `/broker/search`,
        removeBroker: `/brokerGroup/updateBroker`
      },
      searchData: {
        brokerGroupId: this.$route.query.id,
        keyword: ''
      },
      searchRules: {
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
            title: '数据中心',
            key: 'dataCenter.id'
          },
          {
            title: '重试方式',
            key: 'retryType'
          },
          {
            title: '描述',
            key: 'description'
          }
        ],
        btns: [
          {
            txt: '移除',
            method: 'on-del'
          }
        ]
      }
    }
  },
  computed: {
  },
  methods: {
    // 查询
    getList () {
      this.showTablePin = true
      let data = {
        pagination: {
          page: this.page.page,
          size: this.page.size
        },
        query: {
          brokerGroupId: this.$route.query.id,
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
    },
    // 从分组移除Broker
    del (item) {
      let _this = this
      this.$Dialog.confirm({
        title: '提示',
        content: '确定要删除吗？'
      }).then(() => {
        let editData = {
          id: item.id,
          group: {
            id: -1
          }
        }
        apiRequest.put(_this.urls.removeBroker + '/' + item.id, {}, editData).then((data) => {
          _this.getList()
        })
      }).catch(() => {
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
