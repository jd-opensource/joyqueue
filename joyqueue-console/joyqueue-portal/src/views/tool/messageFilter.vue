<template>
  <div>
    <div>
      <d-button type="primary" @click="getList">刷新
        <icon name="refresh-cw"></icon>
      </d-button>
      <d-button type="primary" @click="openCreateFilterDialog">添加任务</d-button>
    </div>
    <div>
      <my-table :data="tableData" :showPin="showTablePin" :showPagination="true" :page="page"
                @on-size-change="handleSizeChange" @on-current-change="handleCurrentChange"
                @on-selection-change="handleSelectionChange"></my-table>
    </div>

    <my-dialog :dialog="createFilterDialog" @on-dialog-confirm="closeCreateFilterDialog"
               @on-dialog-cancel="closeCreateFilterDialog">
      <div style="margin-bottom: 20px">
        <p>提示:</p>
        <p>1. 同一查询只支持"<b>按位点时间范围</b>"查询或者"<b>位点值</b>+<b>查询条数查询</b>"</p>
        <p>2. 每个查询<b>最多</b>允许查询<b>100,000</b>条数据,超出查询条数限制,查询条数只显示前<b>100,000</b>条数据</p>
      </div>

      <d-form ref="search" :model="search" label-width="100px" :rules="rules">
        <div>
          <grid>
            <grid-row>
              <grid-col span="8">
                <d-form-item label="主题" prop="topic">
                  <d-tooltip content="如主题有namespace,请加上namespace">
                    <d-input v-model="search.topic" placeholder="请输入主题"
                             oninput="value = value.trim()"
                             style="width: 200px">
                    </d-input>
                  </d-tooltip>
                </d-form-item>
              </grid-col>
              <grid-col span="8">
                <d-form-item label="消息格式" prop="msgFormat">
                  <d-select v-model="search.msgFormat" style="width:200px">
                    <d-option v-for="(supportedMessageType, index) in msgFormats" :value="supportedMessageType"
                              :key="index">{{ supportedMessageType }}
                    </d-option>
                  </d-select>
                </d-form-item>
              </grid-col>
              <grid-col span="8">
                <d-form-item label="分区" style="margin-left: -25px" prop="partition">
                  <d-input v-model="search.partition" oninput="value=value.replace(/[^\d]/g, '').trim()"
                           placeholder="分区为空默认查询所有分区"
                           style="width: 200px">
                  </d-input>
                </d-form-item>
              </grid-col>
            </grid-row>
            <grid-row>
              <grid-col span="8">
                <d-form-item>
                  <d-radio-group v-model="search.method" name="radioGroup">
                    <d-radio label="searchByOffsetTime">
                      <icon name="offsetTime" size="12"></icon>
                      按位点时间
                    </d-radio>
                    <d-radio label="searchByOffset">
                      <icon name="offset" size="12"></icon>
                      按位点值
                    </d-radio>
                  </d-radio-group>
                </d-form-item>
              </grid-col>
              <div v-if="search.method === 'searchByOffset'">
                <grid-col>
                  <d-form-item label="位点" prop="offset">
                    <d-input v-model="search.offset" oninput="value=value.replace(/[^\d]/g, '').trim()" placeholder="请输入位点值"
                             style="width: 200px;">
                    </d-input>
                  </d-form-item>
                </grid-col>
              </div>
              <div v-if="search.method === 'searchByOffset'">
                <grid-col>
                  <d-form-item label="查询条数" prop="queryCount">
                    <d-input v-model="search.queryCount" oninput="value=value.replace(/[^\d]/g, '').trim()"
                             placeholder="请输入查询条数"
                             style="width: 200px;">
                    </d-input>
                  </d-form-item>
                </grid-col>
              </div>
              <div v-else>
                <grid-col span="22">
                  <d-form-item label="位点时间范围" prop="times">
                    <d-date-picker v-model="search.times" type="datetimerange" range-separator="至"
                                   start-placeholder="开始日期" class="left"
                                   end-placeholder="结束日期" value-format="timestamp"
                                   :default-time="['00:00:00', '23:59:59']">
                    </d-date-picker>
                  </d-form-item>
                </grid-col>
              </div>
            </grid-row>
            <grid-row>
              <grid-col>
                <d-form-item prop="filter" label="过滤内容">
                  <d-input v-model="search.filter" placeholder="请输入消息关键字" class="left"
                           oninput="value = value.trim()"
                           style="width: 280px;margin-top: 5px">
                  </d-input>
                </d-form-item>
              </grid-col>
            </grid-row>
          </grid>
          <d-button type="primary" class="right" style="margin-right: 30px;margin-top: 40px" @click="add('search')">创建
          </d-button>
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
import {timeStampToString} from '../../utils/dateTimeUtils'

export default {
  name: 'messageFilter',
  components: {
    myDialog,
    myTable
  },
  mixins: [crud],
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
          topic: this.topic,
          method: 'searchByOffsetTime'
        }
      }
    }
  },
  data () {
    let timesValidator = (rule, value, callback) => {
      if (this.search.times && (this.search.offset || this.search.queryCount)) {
        return callback(new Error('位点时间不能和位点值，查询条数同时输入'))
      }
      if (!this.search.times && !this.search.offset) {
        return callback(new Error('位点时间范围和位点值至少输入一个'))
      }
      return callback()
    }
    let offsetValidator = (rule, value, callback) => {
      if (this.search.times && (this.search.offset || this.search.queryCount)) {
        return callback(new Error('位点时间不能和位点值，查询条数同时输入'))
      }
      if (!this.search.times && !this.search.offset) {
        return callback(new Error('位点时间范围和位点值至少输入一个'))
      }
      return callback()
    }
    let queryCountValidator = (rule, value, callback) => {
      if (this.search.offset && !this.search.queryCount) {
        return callback(new Error('位点值和查询条数必须同时输入'))
      }
      if (this.search.queryCount > 100000 || this.search.queryCount < 1) {
        return callback(new Error('查询条数范围为1-100000'))
      }
      return callback()
    }
    return {
      msgFormats: [],
      apps: [],
      urls: {
        search: '/topic/findTopicMsgFilters',
        add: '/topic/addTopicMsgFilter',
        msgTypes: '/archive/message-types'
      },
      showTablePin: true,
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
            formatter (item) {
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
            formatter (item) {
              if (item.offsetStartTime && item.offsetEndTime) {
                return timeStampToString(item.offsetStartTime) + ' ' + timeStampToString(item.offsetEndTime)
              } else {
                return '位点值:' + item.offset + ', 查询条数:' + item.queryCount
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
              switch (item.status) {
                case 2: {
                  return '上传中'
                }
                case 1 : {
                  return '查询中'
                }
                case 0 : {
                  return '等待'
                }
                case -1: {
                  return '查询完成'
                }
                case -2: {
                  return '查询异常'
                }
              }
            }
          },
          {
            title: '结果',
            key: 'url',
            width: '25%',
            render: (h, params) => {
              if (params.item.status === -2) {
                return h('div', {}, params.item.description)
              }
              var html = []
              if (params.item.url) {
                var p = h('a', {
                  attrs: {
                    href: params.item.url
                  }
                }, '下载查询结果')
                html.push(p)
              }
              return h('div', {}, html)
            }
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
        width: '1000',
        showFooter: false
      },
      rules: {
        topic: [
          {
            required: true,
            message: '主题不可为空'
          }
        ],
        msgFormat: [
          {
            required: true,
            message: '消息格式不可为空'
          }
        ],
        filter: [
          {
            required: true,
            message: '查询内容不可为空'
          }
        ],
        offset: [
          {
            validator: offsetValidator
          }
        ],
        times: [
          {
            validator: timesValidator
          }
        ],
        queryCount: [
          {
            validator: queryCountValidator
          }
        ]
      }
    }
  },
  methods: {
    add (formName) {
      this.$refs[formName].validate(async (valid) => {
        if (valid) {
          let data = this.search
          if (this.search.times) {
            data.offsetStartTime = this.search.times[0]
            data.offsetEndTime = this.search.times[1]
          }
          apiRequest.post(this.urls.add, {}, data).then((data) => {
            if (data.code === 200) {
              this.$Message.info('添加成功')
              this.getList()
              delete this.search.offsetStartTime
              delete this.search.offsetEndTime
            }
          })
          this.createFilterDialog.visible = false
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
    }
  },
  mounted () {
    apiRequest.get(this.urls.msgTypes)
      .then(data => {
        this.msgFormats = data.data
        if (this.msgFormats.length > 0) {
          this.search.msgFormat = this.msgFormats[0]
        }
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
