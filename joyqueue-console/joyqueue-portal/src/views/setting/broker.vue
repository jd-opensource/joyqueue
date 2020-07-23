<template>
  <div>
    <div>
      <d-input type="textarea" v-model="searchData.keyword" rows="1" placeholder="请输入ID/IP" class="left"
               style="width:300px" @on-enter="getList">
        <span slot="prepend">&nbsp;ID/IP&nbsp;</span>
        <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>
      </d-input>
      <d-input v-if="searchData.groupVisible" v-model="searchData.group" oninput="value = value.trim()" placeholder="请输入Broker分组编码" class="left mr10"
               style="width:300px" @on-enter="getList">
        <span slot="prepend">分组编码</span>
        <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>
      </d-input>
      <d-button type="primary" @click="getList">搜索</d-button>
      <d-button type="primary" @click="openBatchBrokerGroupDialog">批量分组调整</d-button>

      <slot name="extendBtn"></slot>
    </div>
    <my-table :optional="true" :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange" @on-current-change="handleCurrentChange" @on-selection-change="handleSelectionChange"
              @on-edit="edit" @on-del="del" @on-archiveMonitor="archiveMonitor">
    </my-table>

    <!--编辑Broker-->
    <my-dialog :dialog="editDialog" @on-dialog-confirm="editConfirm()" @on-dialog-cancel="editCancel()">
      <grid-row class="mb10">
        <grid-col :span="8" class="label">IP:</grid-col>
        <grid-col :span="16" class="val">
          <d-input v-model="editData.ip" oninput="value = value.trim()"></d-input>
        </grid-col>
      </grid-row>
      <grid-row class="mb10">
        <grid-col :span="8" class="label">端口:</grid-col>
        <grid-col :span="16" class="val">
          <d-input v-model="editData.port" oninput="value = value.trim()"></d-input>
        </grid-col>
      </grid-row>
      <grid-row class="mb10">
        <grid-col :span="8" class="label">重试方式:</grid-col>
        <grid-col :span="16" class="val">
          <d-select v-model="editData.retryType" style="width:40%" >
            <d-option v-for="item in retryTypeList" :key="item.key" :value="item.key">{{item.value}}</d-option>
            <!--<d-option :value="DB">DB</d-option>-->
            <!--<d-option :value="RemoteRetry">RemoteRetry</d-option>-->
          </d-select>
        </grid-col>
      </grid-row>
      <grid-row class="mb10">
        <grid-col :span="8" class="label">权限:</grid-col>
        <grid-col :span="16" class="val">
          <d-select v-model="editData.permission" style="width:40%" >
            <d-option v-for="item in permissionList" :key="item.key" :value="item.key">{{item.value}}</d-option>
            <!--<d-option :value="FULL">FULL</d-option>-->
            <!--<d-option :value="1">READ</d-option>-->
            <!--<d-option :value="2">WRITE</d-option>-->
            <!--<d-option :value="3">NONE</d-option>-->
          </d-select>
        </grid-col>
      </grid-row>
      <!--<grid-row class="mb10">-->
      <!--<grid-col :span="8" class="label">描述:</grid-col>-->
      <!--<grid-col :span="16" class="val">-->
      <!--<d-input v-model="editData.description"></d-input>-->
      <!--</grid-col>-->
      <!--</grid-row>-->
    </my-dialog>
    <my-dialog :dialog="archiveMonitorDialog">
      <grid-row class="mb10" justify="end">
        <grid-col :span="6" class="label">待归档消费记录数:</grid-col>
        <grid-col :span="2" class="label" >
          <span >{{archiveMonitorData.consumeBacklog}}</span>
        </grid-col>
        <grid-col :span="6" class="label">待归生产记录数:</grid-col>
        <grid-col :span="2" class="label">
          <span >{{archiveMonitorData.produceBacklog}}</span>
        </grid-col>
      </grid-row>
    </my-dialog>
    <my-dialog :dialog="monitorDetailDialog">
      <broker-monitor :brokerId="brokerId"> </broker-monitor>
    </my-dialog>
    <my-dialog :dialog="batchBrokerGroupDialog" @on-dialog-confirm="batchBrokerGroupHandle('batchBrokerGroupSearch')" @on-dialog-cancel="closeBatchBrokerGroupDialog">
      <d-form ref="batchBrokerGroupSearch" :model="batchBrokerGroupSearch" label-width="100px" :rules="batchBrokerGroupDialog.rules">
        <d-form-item label="broker分组" prop="batchBrokerGroup">
          <d-input placeholder="请输入broker分组" oninput="value = value.trim()" v-model="batchBrokerGroupSearch.batchBrokerGroup"></d-input>
        </d-form-item>
      </d-form>
    </my-dialog>
  </div>
</template>

<script>
import apiRequest from '../../utils/apiRequest.js'
import myTable from '../../components/common/myTable.vue'
import myDialog from '../../components/common/myDialog.vue'
import crud from '../../mixins/crud.js'
import BrokerMonitor from './brokerMonitor'
import {timeStampToString} from '../../utils/dateTimeUtils'
import {brokerRetryTypeRender, brokerPermissionTypeRender} from '../../utils/common.js'

export default {
  name: 'application',
  components: {
    BrokerMonitor,
    myTable,
    myDialog
  },
  mixins: [crud],
  props: {
    searchData: {
      type: Object,
      default: function () {
        return {
          keyword: '',
          groupVisible: true
        }
      }
    },
    urls: {
      type: Object,
      default: function () {
        return {
          search: '/broker/search',
          del: '/broker/delete',
          edit: '/broker/update',
          archiveMonitor: '/monitor/archive',
          findDetail: '/monitor/broker/findBrokerDetail/',
          telnet: '/broker',
          startInfo: '/monitor/start',
          batchBrokerGroupSearch: '/brokerGroup/mvBatchBrokerGroup'
        }
      }
    },
    operates: {
      type: Array,
      default: function () {
        return [
          {
            txt: '编辑',
            method: 'on-edit'
          },
          {
            txt: '删除',
            method: 'on-del'
          },
          {
            txt: '归档监控',
            method: 'on-archiveMonitor'
          }
        ]
      }
    },
    colData: {
      type: Array,
      default: function () {
        return [
          {
            title: 'ID',
            key: 'id',
            width: '10%'
          },
          {
            title: 'Broker分组编码',
            key: 'group.code',
            width: '15%'
          },
          {
            title: 'IP:端口',
            key: 'ip',
            width: '15%',
            render: (h, params) => {
              const ip = params.item.ip
              const port = params.item.port
              return h('label', {
                style: {
                  color: '#3366FF'
                },
                on: {
                  click: () => {
                    let route = this.$router.resolve({
                      path: '/' + this.$i18n.locale + '/setting/brokerMonitor',
                      query: {
                        brokerId: params.item.id,
                        brokerIp: ip,
                        brokerPort: port
                      }
                    })
                    window.open(route.href, '_blank')
                  },
                  mousemove: (event) => {
                    event.target.style.cursor = 'pointer'
                  }
                }
              }, `${ip}:${port}`)
            }
          },
          {
            title: '机房 (编码/名称)',
            key: 'dataCenter.code',
            width: '9%',
            formatter (item) {
              if (item.dataCenter) {
                return item.dataCenter.code + '/' + item.dataCenter.name
              }
            }
          },
          {
            title: '内存百分比/存储百分比', // bufferPoolMonitorInfo.used%bufferPoolMonitorInfo.maxMemorySize  store.freeSpace%store.totalSpace
            key: 'bufferPoolMonitorInfo.maxMemorySize',
            width: '9%',
            formatter: function (item) {
              if (item.bufferPoolMonitorInfo && item.store) {
                let res1 = 0
                let res2 = 0
                let a = parseFloat(item.bufferPoolMonitorInfo.maxMemorySize)
                let b = parseFloat(item.bufferPoolMonitorInfo.used)
                res1 = Number(b / a * 100).toFixed(1)
                a = parseFloat(item.store.freeSpace)
                b = parseFloat(item.store.totalSpace)
                res2 = Number((b - a) / b * 100).toFixed(1)
                return res1 + '% / ' + res2 + '%'
              }
            }
          },
          {
            title: '出队/入队',
            key: 'enQueue.count',
            width: '9%',
            formatter (item) {
              if (item.enQueue && item.deQueue) {
                return item.deQueue.count + '/' + item.enQueue.count
              }
            }
          },
          /*          {
            title: '启动时间',
            key: 'startupTime',
            width: '15%'
          }, */
          {
            title: '启动时间/版本',
            key: 'startupInfo.version',
            width: '20%', // 15
            render: (h, params) => {
              let html = []
              html.push(params.item.startupTime + '/')
              let spin = h('d-spin', {
                attrs: {
                  size: 'small'
                },
                style: {
                  display: (params.item.startupInfo.version !== undefined) ? 'none' : 'inline-block'
                }
              })
              html.push(spin)
              let text = params.item.startupInfo.version
              if (text === 'UNKNOWN') {
                let error = h('icon', {
                  style: {
                    color: 'red'
                  },
                  props: {
                    name: 'x-circle'
                  }
                })
                html.push(error)
              } else {
                let textSpan = h('span', {
                  style: {
                    position: 'relative',
                    display: (params.item.startupInfo.version === undefined) ? 'none' : 'inline-block'
                  }
                }, text)
                html.push(textSpan)
              }
              return h('div', {}, html)
            }
          },
          {
            title: '重试方式/权限',
            key: 'retryType',
            width: '18%', // 10
            render: (h, params) => {
              return h('div', [brokerRetryTypeRender(h, params.item.retryType), brokerPermissionTypeRender(h, params.item.permission)])
            }
          }
          /*          {
            title: '权限',
            key: 'permission',
            width: '8%',
            render: (h, params) => {
              return brokerPermissionTypeRender(h, params.item.permission)
            }
          } */
        ]
      }
    }
  },
  data () {
    return {
      batchBrokerGroupSearch: {
        selectedBrokers: [],
        batchBrokerGroup: ''
      },
      tableData: {
        rowData: [],
        colData: this.colData,
        operates: this.operates
      },
      brokerId: -1,
      multipleSelection: [],
      editDialog: {
        visible: false,
        title: '编辑Broker',
        showFooter: true
      },
      archiveMonitorDialog: {
        visible: false,
        title: '归档监控信息',
        showFooter: true
      },
      archiveMonitorData: {},
      monitorDetailDialog: {
        visible: false,
        title: 'broker详情',
        showFooter: false,
        width: '1200px'
      },
      batchBrokerGroupDialog: {
        visible: false,
        title: '批量分组迁移',
        showFooter: true,
        width: '600px',
        rules: {
          batchBrokerGroup: [
            {
              required: true,
              message: 'broker分组不可以为空'
            }
          ]
        }
      },
      retryTypeList: [
        {key: 'DB', value: 'DB'},
        {key: 'RemoteRetry', value: 'RemoteRetry'}
      ],
      permissionList: [
        {key: 'FULL', value: 'FULL'},
        {key: 'READ', value: 'READ'},
        {key: 'WRITE', value: 'WRITE'},
        {key: 'NONE', value: 'NONE'}
      ],
      monitorDetailData: {},
      editData: {}
    }
  },
  methods: {
    getList () {
      if (this.searchData.keyword && this.searchData.group) {
        this.$Message.error('验证不通过，ID/IP搜索和Broker分组编号不能同时搜索')
        return
      }
      this.showTablePin = true
      let data = this.getSearchVal()
      apiRequest.post(this.urlOrigin.search, {}, data).then((data) => {
        data.data = data.data || []
        data.pagination = data.pagination || {
          totalRecord: data.data.length
        }
        this.page.total = data.pagination.totalRecord
        this.page.page = data.pagination.page
        this.page.size = data.pagination.size
        this.tableData.rowData = data.data
        for (let i = 0; i < this.tableData.rowData.length; i++) {
          if (!this.tableData.rowData[i].startupInfo) {
            this.tableData.rowData[i].startupInfo = {}
          }
          this.getBrokerStatus(this.tableData.rowData, i)
          this.getDetail(this.tableData.rowData[i], i)
        }
        this.showTablePin = false
      })
    },
    getBrokerStatus (rowData, i) {
      apiRequest.get(this.urls.startInfo + '/' + rowData[i].id).then((data) => {
        if (data.code === 200) {
          this.tableData.rowData[i].startupTime = timeStampToString(data.data.startupTime)
        } else {
          this.tableData.rowData[i].startupTime = '不存活'
        }
        this.$set(this.tableData.rowData, i, this.tableData.rowData[i])
      })
    },
    getDetail (row, index) {
      apiRequest.getBase(this.urls.findDetail + row.id, {}, false)
        .then((data) => {
          if (data.code === 200) {
            this.tableData.rowData[index] = Object.assign(row, data.data || [])
          } else {
            this.tableData.rowData[index].startupInfo = {}
            this.tableData.rowData[index].startupInfo.startupTime = 'UNKNOWN'
            this.tableData.rowData[index].startupInfo.version = 'UNKNOWN'
          }
          this.$set(this.tableData.rowData, index, this.tableData.rowData[index])
        })
    },
    archiveMonitor (item) {
      let broker = {
        ip: item.ip,
        port: item.port
      }
      apiRequest.postBase(this.urlOrigin.archiveMonitor, {}, broker, false).then((data) => {
        data.data = data.data || {}
        this.archiveMonitorData = data.data
        this.openDialog('archiveMonitorDialog')
      })
    },
    beforeEdit () {
      return new Promise((resolve, reject) => {
        resolve({
          id: this.editData.id,
          brokerId: this.editData.brokerId,
          ip: this.editData.ip,
          port: this.editData.port,
          // dataCenter: this.editData['dataCenter'].id,
          retryType: this.editData.retryType,
          permission: this.editData.permission
          // description: this.editData.description
        })
      })
    },
    handleSelectionChange (val) {
      this.batchBrokerGroupSearch.selectedBrokers = val
    },
    openBatchBrokerGroupDialog () {
      this.batchBrokerGroupDialog.visible = true
    },
    closeBatchBrokerGroupDialog () {
      this.batchBrokerGroupDialog.visible = false
    },
    batchBrokerGroupHandle (formName) {
      this.$refs[formName].validate(async (valid) => {
        if (valid) {
          apiRequest.post(this.urls.batchBrokerGroupSearch + '?group=' + this.batchBrokerGroupSearch.batchBrokerGroup, {}, this.batchBrokerGroupSearch.selectedBrokers).then((data) => {
            if (data.code === 200) {
              this.$Message.info('update success')
              this.closeBatchBrokerGroupDialog()
              this.getList()
            } else {
              this.$Message.error(data.message)
            }
          })
        }
      })
    }
  },
  mounted () {
    this.getList()
  },
  computed: {
    curLang () {
      return this.$i18n.locale
    }
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  .label{text-align: right; line-height: 32px;}
  .val{}
</style>
