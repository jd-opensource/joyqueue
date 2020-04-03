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
        <d-button type="primary" class="left" @click="getList">查询状态</d-button>
        <d-button type="primary" class="left" @click="add">添加任务</d-button>
      </div>
    </d-form>

    <my-table :data="tableData" :showPin="showTablePin" :showPagination="true" :page="page" @on-size-change="handleSizeChange" @on-current-change="handleCurrentChange"
              @on-selection-change="handleSelectionChange" @on-detail="detail"></my-table>

    <my-dialog :dialog="detailDialog" @on-dialog-confirm="closeDetailDialog" @on-dialog-cancel="closeDetailDialog" >

    </my-dialog>
  </div>
</template>

<script>
import apiRequest from '../../utils/apiRequest.js'
import myDialog from '../../components/common/myDialog.vue'
import myTable from '../../components/common/myTable.vue'
import crud from '../../mixins/crud'
import {timeStampToString} from "../../utils/dateTimeUtils";

export default {
  name: 'messageFilter',
  components: {
    myDialog,
    myTable
  },
  mixins: [ crud ],
  props: {
    detailItem: {
      type: Object
    },
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
            txt: '执行',
            method: 'on-do-filter'
          }
        ]
      }
    },
    operates: {
      type: Array,
      default: function () {
        return []
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
      filterStatus: [
        {
          label: '结束',
          value: -1
        },
        {
          label: '等待',
          value: 0
        },
        {
          label: '正在执行',
          value: 1
        }
      ],
      apps: [],
      urls: {
        filter: '/topic/msgFilter',
        search: '/topic/findTopicMsgFilters',
        add: '/topic/addTopicMsgFilter'
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
            title: '应用',
            key: 'app',
            width: '10%'
          },
          {
            title: '主题',
            key: 'topic',
            width: '10%'
          },
          {
            title: '请求时间',
            key: 'createTime',
            width: '15%',
            formatter (item) {
              return timeStampToString(item.createTime)
            }
          },
          {
            title: '状态',
            key: 'status',
            width: '10%',
            formatter (item) {
              switch(item.status) {
                case 1 :{
                  return '正在执行'
                }
                case 0 : {
                  return '等待'
                }
                case -1: {
                  return '结束'
                }
              }
            }
          },
          {
            title: '过滤条件',
            key: 'filter',
            width: '15%'
          },
          {
            title: '位点',
            key: 'offset',
            width: '10%'
          },
          {
            title: '描述',
            key: 'description',
            width: '25%'
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
    add () {
      if (!this.validate()) {
        return
      }
      let data = this.search
      apiRequest.post(this.urls.add, {}, data).then((data) => {
        if (data.code === 200) {
          this.$Message.info('添加成功')
        }
      })
    },
    doFilter () {
      let data = this.search
      apiRequest.post(this.urls.filter, {}, data).then((data) => {
        if (this.tableData.rowData.length === 0) {
          this.$Message.info('没有搜到数据')
        }
      })
    },
    // 从mysql中获取过滤记录
    getList () {
      this.showTablePin = true
      let data = {
        pagination: {
          page: this.page.page,
          size: this.page.size
        },
        query: {}
      }
      apiRequest.post(this.urls.search, {}, data).then((data) => {
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
