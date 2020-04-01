<template>
  <div>
    <d-form ref="form" :model="search" label-width="80px">
      <div class="headLine">
        <d-input v-model="search.app" placeholder="请输入应用名" class="left" style="width: 200px">
          <span slot="prepend">应用</span>
        </d-input>
        <d-input v-model="search.filter" placeholder="请输入过滤内容" class="left" style="width: 300px">
          <span slot="prepend">过滤内容</span>
        </d-input>
        <d-button type="primary" class="left" style="margin-right: 30px" @click="getList">查询状态</d-button>
        <d-button type="primary" class="right" style="margin-right: 30px" @click="doFilter">搜索</d-button>
      </div>
    </d-form>

    <my-table :data="tableData" :showPin="showTablePin" :showPagination="true" :page="page" @on-size-change="handleSizeChange" @on-current-change="handleCurrentChange"
              @on-selection-change="handleSelectionChange" @on-detail="detail"></my-table>

    <my-dialog :dialog="detailDialog" @on-dialog-confirm="closeDetailDialog" @on-dialog-cancel="closeDetailDialog" >
      <my-table :data="filterQueryTableData" :showPin="showTablePin" :showPagination="true" :page="page" @on-size-change="handleSizeChange" @on-current-change="handleCurrentChange"
                @on-selection-change="handleSelectionChange" @on-detail="detail"></my-table>
    </my-dialog>
  </div>
</template>

<script>
import apiRequest from '../../utils/apiRequest.js'
import myDialog from '../../components/common/myDialog.vue'
import myTable from '../../components/common/myTable.vue'
import crud from '../../mixins/crud'

export default {
  name: 'messageFilter',
  components: {
    myDialog,
    myTable
  },
  mixins: [ crud ],
  props: {
    app: {
      type: String
    },
    topic: {
      type: String
    },
    namespace: {
      type: String
    },
    btns: {
      type: Array,
      default: function () {
        return [
          {
            txt: '报警详情',
            method: 'on-detail'
          }
        ]
      }
    },
    operates: {
      type: Array,
      default: function () {
        return [
          {
            txt: '报警详情',
            method: 'on-detail',
            isAdmin: true
          }
        ]
      }
    },
    search: {
      type: Object,
      default: function () {
        return {
          filter: '',
          topic: this.topic,
          app: this.app,
          namespace: this.namespace
        }
      }
    }
  },
  data () {
    return {
      apps: [],
      urls: {
        filter: '/topic/msgFilter',
        status: '/topic/msgFilterStatus'
      },
      showTablePin: false,
      filterQueryTableData: {
        rowData: [],
        colData: [
          {
            title: 'id',
            key: 'id',
            width: '20%'
          },
          {
            title: '过滤条件',
            key: 'filter',
            width: '10%'
          },
          {
            title: '查询时间',
            key: 'queryTime',
            width: '10%'
          },
          {
            title: '查询进度',
            key: 'filterProgress',
            width: '10%'
          }
        ],
        btns: this.btns,
        operates: this.operates
      },
      tableData: {
        rowData: [],
        colData: [
          {
            title: 'id',
            key: 'id',
            width: '10%'
          },
          {
            title: '所在分区',
            key: 'partition',
            width: '10%'
          },
          {
            title: '位点',
            key: 'offset',
            width: '10%'
          },
          {
            title: '生产时间',
            key: 'produceTime',
            width: '15%'
          },
          {
            title: '消息内容',
            key: 'content',
            width: '45%'
          }
        ],
        btns: this.btns,
        operates: this.operates
      },
      page: {
        page: 1,
        size: 10,
        total: 0
      },
      detailDialog: {
        visible: false,
        title: '消息详情',
        width: '600',
        showFooter: true
      }
    }
  },
  methods: {
    doFilter () {
      if (!this.validate()) {
        return
      }
      let data = this.search
      apiRequest.post(this.urls.filter, {}, data).then((data) => {
        data.data = data.data || []
        data.pagination = data.pagination || {
          totalRecord: data.data.length
        }
        this.page.total = data.pagination.totalRecord
        this.page.page = data.pagination.page
        this.page.size = data.pagination.size
        this.tableData.rowData = data.data
        if (this.tableData.rowData.length === 0) {
          this.$Message.info('没有搜到数据')
        }
      })
    },
    // 从mysql中获取过滤记录
    getList () {
      if (!this.validate()) {
        return
      }
      this.showTablePin = true
      let data = {
        pagination: {
          page: this.page.page,
          size: this.page.size
        },
        query: {}
      }
      for (let i in this.search) {
        if (this.search.hasOwnProperty(i)) {
          data.query[i] = this.search[i]
        }
      }
      apiRequest.post(this.urls.query, {}, data).then((data) => {
        data.data = data.data || []
        data.pagination = data.pagination || {
          totalRecord: data.data.length
        }
        this.page.total = data.pagination.totalRecord
        this.page.page = data.pagination.page
        this.page.size = data.pagination.size
        this.tableData.rowData = data.data
        if (this.tableData.rowData.length === 0) {
          this.$Message.info('没有搜到数据')
        }
      })
      this.showTablePin = false
    },
    detail (item) {
      this.openDetailDialog(item)
    },
    openDetailDialog (item) {
      this.detailDialog.visible = true
    },
    closeDetailDialog () {
      this.detailDialog.visible = false
    },
    innerValidate (field, name) {
      if (!this.search[field] || this.search[field].length === 0) {
        this.$Message.error(name + '项不可为空')
        return false
      }
      return true
    },
    validate () {
      if (!this.innerValidate('filter', '过滤内容')) {
        return false
      }
      if (!this.innerValidate('app', '应用')) {
        return false
      }
      return true
    }
  },
  mounted () {
    if (this.search.namespace) {
      this.search.topic = this.search.namespace + '.' + this.search.topic
      delete this.search.namespace
    }
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  .fixed-affix {
    display: inline-block;
    color: #fff;
    font-size: 12px;
    padding: 7px 16px;
    text-align: center;
    background: #4F8EEB;
  }
</style>
