<template>
  <div>
    <grid-row>
      <grid-col span="15" offset="8">
        <d-button type="primary" class="right" @click="getList" style="margin-left: 5px;">刷新
          <icon name="refresh-cw"></icon>
        </d-button>
        <d-button class="right" @click="openCreateFilterDialog">添加任务</d-button>
      </grid-col>
    </grid-row>
    <div>
      <my-table :data="tableData" :showPin="showTablePin" :showPagination="true" :page="page"
                @on-size-change="handleSizeChange" @on-current-change="handleCurrentChange"
                @on-selection-change="handleSelectionChange" @on-copy="copyMessageFilter"></my-table>
    </div>

    <my-dialog :dialog="createFilterDialog" @on-dialog-confirm="closeCreateFilterDialog"
               @on-dialog-cancel="closeCreateFilterDialog">
      <div style="margin-bottom: 20px;letter-spacing: 1px;">
        <p>提示:</p>
        <p>1. 同一查询只支持"<b>按位点时间范围</b>"查询或者"<b>位点值</b>+<b>查询条数查询</b>"</p>
        <p>2. 每个查询<b>最多</b>允许查询<b>100,000</b>条数据,超出查询条数限制,查询结果只显示前<b>100,000</b>条数据</p>
        <p v-for="tip in tips" :key="tip">
          {{tip}}
        </p>
      </div>

      <d-form ref="search" :model="search" label-width="80px" :rules="rules">
        <div>
          <grid-row>
            <grid-col span="7">
              <d-form-item label="主题" prop="topic">
                <d-tooltip content="如主题有namespace,请加上namespace">
                  <d-input v-model="search.topic" oninput="value=value.trim()" placeholder="请输入主题"
                           style="width: 200px">
                  </d-input>
                </d-tooltip>
              </d-form-item>
            </grid-col>
            <grid-col span="7">
              <d-form-item label="消息格式" prop="msgFormat">
                <d-select v-model="search.msgFormat" style="width:200px">
                  <d-option v-for="(supportedMessageType, index) in msgFormats" :value="supportedMessageType"
                            :key="index">{{ supportedMessageType }}
                  </d-option>
                </d-select>
              </d-form-item>
            </grid-col>
            <grid-col span="7">
              <d-form-item label="分区" prop="partition">
                <d-input v-model="search.partition" oninput="value=value.replace(/[^\d]/g, '')"
                         placeholder="分区为空默认查询所有分区"
                         style="width: 200px">
                </d-input>
              </d-form-item>
            </grid-col>
          </grid-row>
          <grid-row>
            <grid-col span="7">
              <d-form-item label="应用" prop="app">
                <d-input v-model="search.app" oninput="value=value.trim()" placeholder="请输入应用"
                         style="width: 200px">
                </d-input>
              </d-form-item>
            </grid-col>
            <grid-col span="7">
              <d-form-item label="令牌" prop="token">
                <d-input v-model="search.token" oninput="value=value.trim()" placeholder="请输入token"
                         style="width: 200px">
                </d-input>
              </d-form-item>
            </grid-col>
            <grid-col span="7" v-if="$store.getters.isAdmin">
              <d-form-item label="最大查询总数" prop="totalCount">
                <d-input v-model.number="search.totalCount" oninput="value=value.trim()" placeholder="请输入最大查询总数"
                         style="width: 200px">
                </d-input>
              </d-form-item>
            </grid-col>
          </grid-row>
          <grid-row>
            <grid-col span="7">
              <d-form-item>
                <d-radio-group v-model="search.method" name="radioGroup" @on-change="changeRadio">
                  <d-radio label="searchByOffsetTime">
                    <icon name="offsetTime" size="12"></icon>
                    按时间
                  </d-radio>
                  <d-radio label="searchByOffset">
                    <icon name="offset" size="12"></icon>
                    按位点值
                  </d-radio>
                </d-radio-group>
              </d-form-item>
            </grid-col>
            <div v-if="showOffset">
              <grid-col>
                <d-form-item label="位点" prop="offset">
                  <d-input v-model="search.offset" oninput="value=value.replace(/[^\d]/g, '')" placeholder="请输入位点值"
                           style="width: 200px;">
                  </d-input>
                </d-form-item>
              </grid-col>
            </div>
            <div v-if="showOffset">
              <grid-col>
                <d-form-item label="查询条数" prop="queryCount">
                  <d-input v-model="search.queryCount" oninput="value=value.replace(/[^\d]/g, '')"
                           placeholder="请输入查询条数"
                           style="width: 200px;">
                  </d-input>
                </d-form-item>
              </grid-col>
            </div>
            <div v-else>
              <grid-col span="42">
                <d-form-item label="时间范围" prop="times" class="duidui">
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
              <d-form-item prop="filter" label="查询内容">
                <d-input v-model="search.filter" oninput="value=value.trim()" placeholder="请输入消息关键字" class="left"
                         style="width: 200px;margin-top: 5px">
                </d-input>
              </d-form-item>
            </grid-col>
          </grid-row>
          <d-button type="primary" class="right" :disabled="btnDisabled" style="margin-right: 30px;margin-top: 40px" @click="validateAppToken('search')">创建
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
  props: {
    tips: {
      type: Array
    }
  },
  mixins: [crud],
  data () {
    let timesValidator = (rule, value, callback) => {
      if (!this.search.offset && !this.search.queryCount) {
        if (!this.search.times || this.search.times.length === 0) {
          return callback(new Error('查询时间和位点值必须输入一个'))
        }
      }
      return callback()
    }
    let offsetValidator = (rule, value, callback) => {
      if (!this.search.times || this.search.times.length === 0) {
        if (!this.search.offset && !this.search.queryCount) {
          return callback(new Error('查询时间和位点值必须输入一个'))
        }
        if (this.search.offset && !this.search.queryCount) {
          return callback(new Error('位点值和查询条数必须同时输入'))
        }
        if (!this.search.offset && this.search.queryCount) {
          return callback(new Error('位点值和查询条数必须同时输入'))
        }
        if (this.search.offset < 0) {
          return callback(new Error('位点不能小于0'))
        }
        if (this.search.queryCount > 100000 || this.search.queryCount < 1) {
          return callback(new Error('查询条数范围为1-100000'))
        }
        return callback()
      }
      return callback()
    }
    let queryCountValidator = (rule, value, callback) => {
      if (this.search.offset && !this.search.queryCount) {
        return callback(new Error('位点值和查询条数必须同时输入'))
      }
      if (!this.search.offset && this.search.queryCount) {
        return callback(new Error('位点值和查询条数必须同时输入'))
      }
      if (this.search.queryCount > 100000 || this.search.queryCount < 1) {
        return callback(new Error('查询条数范围为1-100000'))
      }
      return callback()
    }
    // let totalCountValidator = (rule, value, callback) => {
    //   if (this.search.totalCount < 1 || this.search.totalCount > 99999999) {
    //     return callback(new Error('最大查询总数范围在[1-99999999]'))
    //   }
    //   return callback()
    // }
    return {
      btnDisabled: false,
      search: {
        filter: '',
        topic: this.topic,
        method: 'searchByOffsetTime'
      },
      showOffset: false,
      msgFormats: [],
      apps: [],
      urls: {
        search: '/topic/findTopicMsgFilters',
        add: '/topic/addTopicMsgFilter',
        msgTypes: '/archive/message-types',
        validateAppToken: '/topic/validateAppToken'
      },
      showTablePin: true,
      tableData: {
        rowData: [],
        colData: [
          {
            title: '主题',
            key: 'topic',
            width: '10%'
          },
          {
            title: '应用',
            key: 'app',
            width: '10%'
          },
          {
            title: '查询条件',
            width: '30%',
            render: (h, params) => {
              let item = params.item
              let condition = ''
              if (item.offsetStartTime && item.offsetEndTime) {
                condition = '时间: ' + timeStampToString(item.offsetStartTime) + ' - ' + timeStampToString(item.offsetEndTime)
              } else {
                condition = '位点值: ' + item.offset + ', 查询条数: ' + item.queryCount
              }
              if (item.partition >= 0) {
                condition = condition + ', 分区: ' + item.partition
              } else {
                condition = condition + ', 分区: all'
              }
              condition = condition + ', 消息格式: ' + item.msgFormat
              condition = condition + ', 关键字: ' + item.filter
              let target = ''
              if (condition.length > 100) {
                target = condition.substring(0, 100) + ' ...'
              } else {
                target = condition
              }
              var html = []
              var p = h('d-tooltip', {
                attrs: {
                  content: condition
                }
              }, [h('span', {
              }, target)])
              html.push(p)
              return h('div', {}, html)
            }
          },
          {
            title: '任务开始时间',
            key: 'createTime',
            width: '12%',
            formatter (item) {
              return timeStampToString(item.createTime)
            }
          },
          {
            title: '任务结束时间',
            key: 'updateTime',
            width: '12%',
            formatter (item) {
              return timeStampToString(item.updateTime)
            }
          },
          {
            title: '状态',
            key: 'status',
            width: '8%',
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
            width: '15%',
            render: (h, params) => {
              if (params.item.status === -2) {
                return h('div', {}, params.item.description)
              }
              var html = []
              if (params.item.url) {
                var p = h('a', {
                  // attrs: {
                  //   href: params.item.url
                  // }
                  style: {
                    'text-decoration': 'underline',
                    color: 'dodgerblue'
                  },
                  on: {
                    click: () => {
                      window.open(params.item.url)
                    }
                  }
                }, '下载查询结果')
                html.push(p)
              }
              return h('div', {}, html)
            }
          }
        ],
        btns: [
          {
            txt: '复制',
            method: 'on-copy'
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
        title: '创建消息查询任务',
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
        app: [
          {
            required: true,
            message: '应用不能为空'
          }
        ],
        token: [
          {
            required: true,
            message: '令牌不能为空'
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
        // ],
        // totalCount: [
        //   {
        //     validator: totalCountValidator
        //   }
        ]
      }
    }
  },
  methods: {
    add () {
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
    },
    validateAppToken (formName) {
      this.$refs[formName].validate(async (valid) => {
        if (valid) {
          this.btnDisabled = true
          let app = this.search.app
          let token = this.search.token
          let topic = this.search.topic
          apiRequest.get(this.urls.validateAppToken + '/' + app + '/' + topic + '/' + token, {}, {}).then((data) => {
            if (data.code === 200) {
              this.add()
            } else {
              this.$Message.error(data.message)
            }
            this.btnDisabled = false
          })
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
      this.search = {}
      this.search.method = 'searchByOffsetTime'
      this.createFilterDialog.visible = true
    },
    closeCreateFilterDialog (item) {
      this.createFilterDialog.visible = false
    },
    copyMessageFilter (item) {
      this.openCreateFilterDialog(item)
      this.search = {}
      this.search.topic = item.topic
      this.search.msgFormat = item.msgFormat
      this.search.partition = item.partition
      this.search.filter = item.filter
      this.search.app = item.app
      this.search.token = item.token
      if (item.offsetStartTime) {
        this.search.times = []
        this.search.times.push(item.offsetStartTime)
        this.search.times.push(item.offsetEndTime)
        this.search.method = 'searchByOffsetTime'
        this.showOffset = false
      } else {
        this.search.offset = item.offset
        this.search.queryCount = item.queryCount
        this.search.method = 'searchByOffset'
        this.showOffset = true
      }
      if (item.partition < 0) {
        delete this.search.partition
      }
    },
    changeRadio (item) {
      this.showOffset = item === 'searchByOffset'
      delete this.search.times
      delete this.search.offset
      delete this.search.queryCount
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
  .duidui /deep/ .dui-date-editor--datetimerange.dui-input__inner { width: 480px; }
</style>
