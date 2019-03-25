<template>
  <div>
    <div class="ml20 mt30">
      <d-select v-model="searchData.type" style="width:8%" >
        <d-option :value="-1" >全部</d-option>
        <d-option :value="0" >任务调度</d-option>
        <!--<d-option :value="1" >报警</d-option>-->
        <!--<d-option :value="2" >归档</d-option>-->
        <d-option :value="4" >MQTT代理</d-option>
      </d-select>
      <d-input v-model="searchData.keyword" placeholder="请输入IP" class="left mr10" style="width: 20%">
        <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>
      </d-input>
      <d-button type="primary" @click="openDialog('addDialog')">新建服务执行器<icon name="plus-circle" style="margin-left: 5px;"></icon></d-button>
      <d-button type="primary" @click="openBatchAddExecutorDialog()">批量导入服务执行器<icon name="plus-circle" style="margin-left: 5px;"></icon></d-button>
      <d-button type="primary" @click="batchEnableExecutor">启用<icon name="check-circle" style="margin-left: 5px;"></icon></d-button>
      <d-button type="primary" @click="batchDisableExecutor">禁用<icon name="check-circle" style="margin-left: 5px;"></icon></d-button>
    </div>
    <my-table :optional="true" :data="tableData" :showPin="showTablePin" :page="page"  @on-size-change="handleSizeChange"
              @on-current-change="handleCurrentChange" @on-selection-change="handleSelectionChange"
              @on-select-all="handleSelectionAll" @on-del="del" @on-enable="enable" @on-disable="disable" @on-edit="edit" @on-executor-monitor="openExecutorMonitorDialog" >
    </my-table>
    <!--新建任务-->
    <my-dialog :dialog="addDialog" @on-dialog-confirm="addConfirm()" @on-dialog-cancel="addCancel()">
      <grid-row class="mb10">
        <grid-col :span="5" class="label">IP:</grid-col>
        <grid-col :span="16" class="val">
          <d-input style="width: 249px" v-model="addData.ip" placeholder="192.168.123.123"></d-input>
        </grid-col>
      </grid-row>
      <grid-row class="mb10">
        <grid-col :span="5" class="label">端口号:</grid-col>
        <grid-col :span="16" class="val">
          <d-input style="width: 249px" v-model="addData.jmxPort" placeholder="7563"></d-input>
        </grid-col>
      </grid-row>
      <grid-row class="mb10">
        <grid-col :span="5" class="label"><span class="star">*</span>类型:</grid-col>
        <grid-col :span="16" class="val">
          <d-select v-model="addData.type" style="width: 35%">
            <d-option :value="0" >任务调度</d-option>
            <!--<d-option :value="1" >报警</d-option>-->
            <d-option :value="2" >归档</d-option>
            <d-option :value="4" >MQTT代理</d-option>
          </d-select>
        </grid-col>
      </grid-row>
    </my-dialog>

    <my-dialog :dialog="batchAddDialog" @on-dialog-confirm="submitUpload()" @on-dialog-cancel="addCancel()">
      <grid-row class="mb10">
        <grid-col :span="5" class="label">文件:</grid-col>
        <grid-col :span="16" class="val">
          <d-upload
            class="upload-demo"
            ref="upload"
            action="/v1/executor/batchAdd"
            :on-preview="handlePreview"
            :on-remove="handleRemove"
            :on-success="handleSuccess"
            :on-error="handleError"
            :file-list="fileList"
            :data="batchAddData"
            :auto-upload="false">
            <d-button slot="trigger" size="small" type="primary">选取文件</d-button>
            <!--<d-button style="margin-left: 10px;" size="small" type="success" @click="submitUpload">上传到服务器</d-button>-->
            <div slot="tip" class="dui-upload__tip">只能上传xls文件</div>
          </d-upload>
        </grid-col>
      </grid-row>
      <grid-row class="mb10">
        <grid-col :span="5" class="label">端口号:</grid-col>
        <grid-col :span="16" class="val">
          <d-input  style="width: 249px" v-model="batchAddData.jmxPort" placeholder="7563"></d-input>
        </grid-col>
      </grid-row>
      <grid-row class="mb10">
        <grid-col :span="5" class="label"><span class="star">*</span>类型:</grid-col>
        <grid-col :span="16" class="val">
          <d-select v-model="batchAddData.type" style="width: 35%">
            <d-option :value="0" >任务调度</d-option>
            <!--<d-option :value="1" >报警</d-option>-->
            <!--<d-option :value="2" >归档</d-option>-->
            <!--<d-option :value="3" >代理</d-option>-->
            <d-option :value="4" >MQTT代理</d-option>
          </d-select>
        </grid-col>
      </grid-row>
    </my-dialog>
    <my-dialog :dialog="editDialog" @on-dialog-confirm="editConfirm()" @on-dialog-cancel="editCancel()">
      <grid-row class="mb10">
        <grid-col :span="5" class="label">IP:</grid-col>
        <grid-col :span="16" class="val">
          <d-input style="width: 249px" v-model="editData.ip" placeholder="192.168.123.123"></d-input>
        </grid-col>
      </grid-row>
      <grid-row class="mb10">
        <grid-col :span="5" class="label">端口号:</grid-col>
        <grid-col :span="16" class="val">
          <d-input style="width: 249px" v-model="editData.jmxPort" placeholder="7563"></d-input>
        </grid-col>
      </grid-row>
      <grid-row class="mb10">
        <grid-col :span="5" class="label"><span class="star">*</span>类型:</grid-col>
        <grid-col :span="16" class="val">
          <d-select v-model="editData.type" style="width: 35%">
            <d-option :value="0" >任务调度</d-option>
            <!--<d-option :value="1" >报警</d-option>-->
            <!--<d-option :value="2" >归档</d-option>-->
            <d-option :value="4" >MQTT代理</d-option>
          </d-select>
        </grid-col>
      </grid-row>
    </my-dialog>
    <my-dialog :dialog="taskExecutorMonitorDialog" >
      <task-executor-detail ref="executorDetail" :executorId="taskExecutorMonitorDialog.executorId" />
    </my-dialog>
    <my-dialog :dialog="mqttProxyExecutorMonitorDialog" >
      <d-tabs @on-change="handleTabChange">
        <d-tab-pane label="概览" name="mqttProxyOverview" icon="pocket">
          <mqtt-proxy-overview ref="mqttProxyOverview" :executorId="mqttProxyExecutorMonitorDialog.executorId" />
        </d-tab-pane>

        <d-tab-pane label="连接" name="mqttProxyClientConnections" icon="user">
          <mqtt-base-monitor ref="mqttProxyClientConnections" :col-data="connectionsProperties.colData" :btns="connectionsProperties.btns"
                             :executorId="mqttProxyExecutorMonitorDialog.executorId" :search="connectionsProperties.search"
                             :close="connectionsProperties.close"  @on-close-connection="closeConnection"/>
        </d-tab-pane>
        <d-tab-pane label="会话" name="mqttProxyClientSessions" icon="github">
          <mqtt-base-monitor ref="mqttProxyClientSessions" :col-data="sessionsProperties.colData"
                             :executorId="mqttProxyExecutorMonitorDialog.executorId" :search="sessionsProperties.search"/>
        </d-tab-pane>

        <d-tab-pane label="消费线程" name="mqttProxyConsumeClients" icon="github">
          <mqtt-base-monitor ref="mqttProxyConsumeClients" :col-data="clientsProperties.colData" :btns="clientsProperties.btns"
                             :executorId="mqttProxyExecutorMonitorDialog.executorId" :search="clientsProperties.search"
                             :threads="clientsProperties.thread.url" :threads-select="true"  :thread-type="clientsProperties.type.consume"
                              :client-debug="clientsProperties.client.debug" :thread-debug="clientsProperties.thread.debug"/>
        </d-tab-pane>
        <d-tab-pane label="分发线程" name="mqttProxyDeliveryClients" icon="github">
          <mqtt-base-monitor ref="mqttProxyDeliveryClients" :col-data="clientsProperties.colData"
                             :executorId="mqttProxyExecutorMonitorDialog.executorId" :search="clientsProperties.search"
                             :threads="clientsProperties.thread.url" :threads-select="true" :thread-type="clientsProperties.type.delivery"/>
        </d-tab-pane>
      </d-tabs>
    </my-dialog>

  </div>
</template>

<script>
import apiRequest from '../../utils/apiRequest.js'
import myTable from '../../components/common/myTable.vue'
import myDialog from '../../components/common/myDialog.vue'
import taskExecutorDetail from './taskExecutorDetail'
import mqttProxyOverview from './mqttProxyOverview'
import mqttBaseMonitor from './mqttBaseMonitor'
import columnExpand from './columnExpand'
import crud from '../../mixins/crud.js'
import {timeStampToString} from '../../utils/dateTimeUtils'

export default {
  name: 'executor',
  components: {
    myTable,
    myDialog,
    taskExecutorDetail,
    mqttProxyOverview,
    mqttBaseMonitor,
    columnExpand
  },
  mixins: [ crud ],
  data () {
    return {
      searchData: {
        keyword: '',
        type: -1
      },
      searchRules: {
      },
      tableData: {
        rowData: [],
        colData: [
          {
            title: '服务名',
            key: 'name'
          },
          {
            title: 'IP',
            key: 'ip'
          }, {
            title: '端口号',
            key: 'jmxPort'
          }, {
            title: '类型',
            key: 'type',
            formatter (row) {
              if (row.type === 0) {
                return '任务调度'
              }
              if (row.type === 1) {
                return '报警'
              }
              if (row.type === 2) {
                return '归档'
              }
              if (row.type === 3) {
                return '代理'
              }
              if (row.type === 4) {
                return 'MQTT代理'
              }
            }
          }, {
            title: '状态',
            key: 'status',
            formatter (row) {
              if (row.status === 1) {
                return '启用'
              }
              if (row.status === 0) {
                return ' 停用'
              }
              if (row.status === -1) {
                return '删除'
              }
            }
          }
        ],
        // 表格操作，如果需要根据特定值隐藏显示， 设置bindKey对应的属性名和bindVal对应的属性值
        btns: [
          {
            txt: '禁用',
            method: 'on-disable',
            bindKey: 'status',
            bindVal: 1
          },
          {
            txt: '启用',
            method: 'on-enable',
            bindKey: 'status',
            bindVal: 0
          },
          {
            txt: '编辑',
            method: 'on-edit'
          },
          {
            txt: '执行器监控',
            method: 'on-executor-monitor'
          },
          {
            txt: '删除',
            method: 'on-del'
          }
        ]
      },
      multipleSelection: [],
      addDialog: {
        visible: false,
        title: '新建服务执行器',
        showFooter: true
      },
      batchAddDialog: {
        visible: false,
        title: '导入服务执行器',
        showFooter: true
      },
      fileList: [
      ],
      batchAddUrl: '',
      batchAddData: {
        name: '',
        file: '',
        jmxPort: '',
        type: 0
      },

      addData: {
        name: '',
        ip: '',
        jmxPort: '',
        type: 0
      },
      editDialog: {
        visible: false,
        title: '编辑',
        showFooter: true
      },
      editData: {},
      taskExecutorMonitorDialog: {
        visible: false,
        title: '任务执行器监控详情',
        width: '1000',
        showFooter: false,
        doSearch: true,
        executorId: -1
      },
      mqttProxyExecutorMonitorDialog: {
        visible: false,
        title: 'MQTT Proxy 监控详情',
        width: '1000',
        showFooter: false,
        doSearch: true,
        executorId: -1
      },
      connectionsProperties: {
        colData: [
          {
            title: '客户端ID',
            key: 'clientId',
            width: 800
          },
          {
            title: '应用',
            key: 'application',
            width: 300
          }, {
            title: 'IP',
            key: 'ipAddress',
            width: 1200
          }, {
            title: '非持久化会话',
            key: 'cleanSession'
            // },{
            //   title:'保留消息',
            //   key:'isWillRetain'
          },
          {
            type: 'expand',
            title: '分组名称',
            // key:'clientGroupName',
            render: (h, params) => {
              return h(columnExpand, {
                props: {
                  col: params.row.clientGroupName
                }
              })
            }
          }, {
            //   title:'服务等级',
            //   key:'willQos'
            // },{
            title: '版本',
            key: 'mqttVersion'
          }, {
            title: '遗嘱标识',
            key: 'willFlag'
          }, {
            title: '存活时间(s)',
            key: 'keepAliveTimeSeconds'
          }, {
            title: '创建时间',
            key: 'createdTime',
            width: 400,
            formatter (item) {
              return timeStampToString(item.createdTime)
            }
          }, {
            title: '操作时间',
            key: 'lastOperateTime',
            width: 400,
            formatter (item) {
              return timeStampToString(item.lastOperateTime)
            }
          }
        ],
        btns: [
          {
            txt: '断开',
            method: 'on-close-connection'
          }
        ],
        search: '/monitor/mqtt/proxy/connections/',
        close: '/monitor/mqtt/proxy/closeConnection',
        executorId: -1
      },
      sessionsProperties: {
        colData: [
          {
            title: '客户端ID',
            key: 'clientID'
          },
          {
            title: '会话持久化',
            key: 'cleanSession'
          },
          {
            title: '消息确认积压数',
            key: 'messageAcknowledgedZoneSize'
          }, {
            title: '消费消息数',
            key: 'consumed'
          }, {
            type: 'expand',
            title: '订阅信息',
            width: 50,
            render: (h, params) => {
              return h(columnExpand, {
                props: {
                  col: params.row.subscriptions
                }
              })
            }
          }
        ],
        search: '/monitor/mqtt/proxy/sessions/',
        executorId: -1
      },
      clientsProperties: {
        colData: [
          {
            title: '客户端ID',
            formatter (item) {
              console.log(item)
              return item.clientId
            }
          },
          {
            title: '调试状态',
            formatter (item) {
              console.log(item)
              return item.status;
            }
          }
        ],
        btns: [
          {
            txt: '开启调试',
            method: 'on-client-debug',
            bindKey: 'status',
            bindVal: 0
          },
          {
            txt: '关闭调试',
            method: 'on-client-debug',
            bindKey: 'status',
            bindVal: 1
          }
        ],
        search: '/monitor/mqtt/proxy/thread/clients',
        client:{
          'debug':'/monitor/mqtt/proxy/client/debug/'
        },
        thread: {
          url: '/monitor/mqtt/proxy/threads/executor/',
          debug:'/monitor/mqtt/proxy/thread/debug/',
          select: false
        },
        type: {
          consume: 'consume',
          delivery: 'delivery'
        }
      }

    }
  },
  methods: {
    openDialog (dialog) {
      this[dialog].visible = true
      this.addData.ip = ''
      this.addData.jmxPort = ''
      this.addData.type = 0
    },
    openExecutorMonitorDialog (item) {
      console.log(item)
      let target = ''
      switch (item.type) {
        case 0:
          target = 'taskExecutorMonitorDialog'
          this.taskExecutorMonitorDialog.executorId = item.id
          break
        case 4:
          target = 'mqttProxyExecutorMonitorDialog'
          this.mqttProxyExecutorMonitorDialog.executorId = item.id
          break
      }
      if (target != null && target.length > 0) {
        this.openDialog(target)
      }
    },
    openBatchAddExecutorDialog () {
      this.batchAddUrl = this.urlOrigin.batchAdd
      this.openDialog('batchAddDialog')
    },
    batchStateExecutor (status) {
      // todo 启用逻辑
      let editData = this.multipleSelection
      if (!editData) {
        this.$Dialog.warning({
          content: '请先选择数据！'
        })
        return
      }
      var ids = []
      for (var i = 0; i < editData.length; i++) {
        if (editData[i].status === status) {
          let hint = editData[i].status === 1 ? '已启用' : '已禁用'
          this.$Dialog.warning({
            content: 'IP:' + editData[i].ip + ' ' + hint + '，请重新选择！'
          })
          return
        }
        ids.push(editData[i].id)
      }
      apiRequest.put(this.urlOrigin.batchState + '/' + status, {}, ids).then((data) => {
        this.editDialog.visible = false
        this.$Dialog.success({
          content: '成功'
        })
        this.getList()
      })
    },
    batchEnableExecutor () {
      this.batchStateExecutor(1)
    },
    batchDisableExecutor () {
      this.batchStateExecutor(0)
    },
    validateExecutor (executor) {
      if (executor.ip === '') {
        return '请填写ip'
      }
      if (executor.jmxPort === 0) {
        return '请填写端口号'
      }
      // let pattern=/^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/ ;
      let pattern = /^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$/
      if (!pattern.test(executor.ip)) {
        return '请输入正确的ip'
      }
      return ''
    },
    beforeAdd () {
      return new Promise((resolve, reject) => {
        let msg = this.validateExecutor(this.addData)
        if (msg === '') {
          this.addData.name = this.addData.ip.replace(/\./g, '_') + '_' + this.addData.jmxPort
          resolve(this.addData)
        } else {
          reject(msg)
        }
      })
    },

    beforeEdit () {
      return new Promise((resolve, reject) => {
        let msg = this.validateExecutor(this.addData)
        if (msg) {
          this.editData.name = this.editData.ip.replace(/\./g, '_') + '_' + this.editData.jmxPort
          resolve({
            id: this.editData.id,
            ip: this.editData.ip,
            jmxPort: this.editData.jmxPort,
            type: this.editData.type,
            name: this.editData.name
          })
        } else {
          reject(msg)
        }
      })
    },
    // 启用/禁用
    state (item, val) {
      let editData = item
      editData.status = val// 启用1，禁用0
      apiRequest.put(this.urlOrigin.state + '/' + editData.id, {}, editData).then((data) => {
        this.$Dialog.success({
          content: '启用成功'
        })
        this.getList()
      })
    },
    // 启用
    enable (item) {
      this.state(item, 1)
    },
    // 禁用
    disable (item) {
      this.state(item, 0)
    },
    submitUpload () {
      this.$refs.upload.submit()
    },
    handleRemove (file, fileList) {
      console.log(file, fileList)
    },
    handlePreview (file) {
      console.log(file)
    },
    handleError () {
      this.$Message.error('上传失败')
    },
    handleSuccess () {
      this.fileList = []
      this.batchAddData.jmxPort = ''
      this.batchAddDialog.visible = false
      this.$Message.success('上传成功')
      this.getList()
    },
    handleTabChange (data) {
      let name = data.name
      this.$refs[name].getList()
    },
    closeConnection (item) {
      console.log('on close connection')
      let closeConnectionPath = this.connectionsProperties.close
      apiRequest.put(closeConnectionPath)
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
