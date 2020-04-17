<template>
  <div>
    <d-form ref="form" label-width="80px">
      <div class="headLine">
        <d-button type="primary" class="left" @click="getList">查询状态</d-button>
        <d-button type="primary" class="left" @click="openCreateFilterDialog">添加任务</d-button>
        <d-button type="primary" class="right" @click="doFilter">执行</d-button>
      </div>
    </d-form>

    <my-table :data="tableData" :showPin="showTablePin" :showPagination="true" :page="page" @on-size-change="handleSizeChange" @on-current-change="handleCurrentChange"
              @on-selection-change="handleSelectionChange"></my-table>

    <my-dialog :dialog="createFilterDialog" @on-dialog-confirm="closeCreateFilterDialog" @on-dialog-cancel="closeCreateFilterDialog" >
      <d-form ref="form" :model="search" label-width="80px">
        <div class="headLine">
<!--          <d-select v-model="search.topic" style="width:250px">-->
<!--            <d-option v-for="item in topics" :value="item.value" :key="item.value">{{ item.value }}</d-option>-->
<!--            <span slot="prepend">主题</span>-->
<!--          </d-select>-->
          <d-input v-model="search.topic" placeholder="请输入主题" class="left" style="width: 280px;margin-top: 5px">
            <span slot="prepend">主题</span>
          </d-input>
          <d-select v-model="search.msgFormat" style="width:280px;margin-top: 5px">
            <d-option v-for="(supportedMessageType, index) in msgFormats" :value="supportedMessageType" :key="index">{{ supportedMessageType }}</d-option>
            <span slot="prepend">消息格式</span>
          </d-select><br/>
          <d-input v-model="search.partition" placeholder="分区为空默认查询所有分区" class="left" style="width: 280px;margin-top: 5px">
            <span slot="prepend">分区</span>
          </d-input>
          <d-input v-model="search.offset" placeholder="请输入位点值" class="left" style="width: 280px;margin-top: 5px">
            <span slot="prepend">位点</span>
          </d-input>
          <d-input v-model="search.queryCount" placeholder="请输入查询条数" class="left" style="width: 280px;margin-top: 5px">
            <span slot="prepend">查询条数</span>
          </d-input>
          <d-input v-model="search.filter" placeholder="请输入消息关键字" class="left" style="width: 280px;margin-top: 5px">
            <span slot="prepend">消息关键字</span>
          </d-input>
          <d-date-picker v-model="search.times" type="daterange" range-separator="至" start-placeholder="开始日期" class="left"
                         style="margin-top: 5px" end-placeholder="结束日期" value-format="timestamp" :default-time="['00:00:00', '23:59:59']">
            <span slot="prepend">位点时间范围</span>
          </d-date-picker>
          <d-button type="primary" class="right" style="margin-right: 30px;margin-top: 40px" @click="add">创建</d-button>
        </div>
      </d-form>
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
    search: {
      type: Object,
      default: function () {
        return {
          filter: '',
          topic: this.topic
        }
      }
    }
  },
  data () {
    return {
      msgFormats: [
      ],
      apps: [],
      urls: {
        filter: '/topic/msgFilter',
        search: '/topic/findTopicMsgFilters',
        add: '/topic/addTopicMsgFilter',
        msgTypes: '/archive/message-types'
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
            title: '主题',
            key: 'topic',
            width: '10%'
          },
          {
            title: '查询方式',
            key: 'queryMethod',
            width: '9%',
            formatter(item) {
              if (item.offsetStartTime && item.offsetEndTime) {
                return '按位点时间'
              } else {
                return '按位点值'
              }
            }
          },
          {
            title: '分区',
            key: 'partition',
            width: '5%',
            formatter (item) {
              if (item.partition >= 0) {
                return item.partition
              } else {
                return '-'
              }
            }
          },
          {
            title: '消息格式',
            key: 'msgFormat',
            width: '5%'
          },
          {
            title: '查询条件',
            width: '15%',
            formatter(item) {
              if (item.offsetStartTime && item.offsetEndTime) {
                return timeStampToString(item.offsetStartTime)+' ' +timeStampToString(item.offsetEndTime)
              } else {
                return '位点值:' + item.offset +', 查询条数:'+item.queryCount
              }
            }
          },
          {
            title: '消息关键字',
            key: 'filter',
            width: '15%'
          },
          {
            title: '请求时间',
            key: 'createTime',
            width: '10%',
            formatter (item) {
              return timeStampToString(item.createTime)
            }
          },
          {
            title: '结束时间',
            key: 'updateTime',
            width: '10%',
            formatter (item) {
              return timeStampToString(item.updateTime)
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
                case -2: {
                  return '执行异常'
                }
              }
            }
          },
          {
            title: '结果',
            key: 'url',
            width: '25%'
          }
        ]
      },
      page: {
        page: 1,
        size: 10,
        total: 0
      },
      createFilterDialog: {
        visible: false,
        title: '创建消息过滤任务',
        width: '620',
        showFooter: false
      }
    }
  },
  methods: {
    add () {
      if (!this.validate()) {
        return
      }
      let data = this.search
      if (this.search.times) {
        data.offsetStartTime = this.search.times[0]
        data.offsetEndTime = this.search.times[1]
        delete this.search.times
      }
      apiRequest.post(this.urls.add, {}, data).then((data) => {
        if (data.code === 200) {
          this.$Message.info('添加成功')
          this.getList()
        }
      })
      this.createFilterDialog.visible = false
    },
    doFilter () {
      apiRequest.post(this.urls.filter, {}, {}).then((data) => {
        if (data.code === 200) {
          this.$Message.info(data.data)
          this.getList()
        } else {
          this.$Message.error(data.message)
        }
      })
    },
    getList () {
      this.showTablePin = true
      let data = {
        pagination: {
          page: this.page.page,
          size: this.page.size
        },
        query: {}
      }
      apiRequest.post(this.urls.search, {}, data, true).then((data) => {
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
    openCreateFilterDialog (item) {
      this.createFilterDialog.visible = true
    },
    closeCreateFilterDialog (item) {
      this.createFilterDialog.visible = false
    },
    innerValidate (field, name) {
      if (!this.search[field] || this.search[field].length === 0) {
        this.$Message.error(name + '项不可为空')
        return false
      }
      return true
    },
    validate () {
      if (this.search.times && (this.search.offset||this.search.queryCount)) {
        this.$Message.error('位点时间不能和位点值，查询条数同时输入')
        return false
      }
      if (this.search.offset && !this.search.queryCount) {
        this.$Message.error('位点值和查询条数必须同时输入')
        return false
      }
      if (!this.innerValidate('topic','主题')) {
        return false
      }
      if (!this.search.msgFormat) {
        this.$Message.error('消息格式不能为空')
        return false
      }
      if (!this.innerValidate('filter', '过滤内容')) {
        return false
      }
      if (!this.search.times && !this.search.offset) {
        this.$Message.error('位点时间范围和位点值至少输入一个')
        return false
      }
      return true
    }
  },
  mounted () {
    apiRequest.get(this.urls.msgTypes)
      .then(data => {
        this.msgFormats = data.data
      })
    this.getList()
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
