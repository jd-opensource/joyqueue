<template>
  <div>
    <div class="ml20 mt30">
      <d-input type="textarea" v-model="searchData.keyword" oninput="value = value.trim()" placeholder="请输入ID/IP" class="left mr10"
               style="width:300px" @on-enter="getList">
        <span slot="prepend">&nbsp;ID/IP&nbsp;</span>
        <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>
      </d-input>
      <d-input v-model="searchData.group" oninput="value = value.trim()" placeholder="请输入Broker分组编码" class="left mr10"
               style="width:300px" @on-enter="getList">
        <span slot="prepend">分组编码</span>
        <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>
      </d-input>
      <d-button type="primary" @click="getList">搜索</d-button>
      <d-button type="primary" @click="openBatchBrokerGroupDialog">批量分组调整</d-button>

      <slot name="extendBtn"></slot>
    </div>
    <my-table :optional="true" :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange" @on-current-change="handleCurrentChange" @on-selection-change="handleSelectionChange"
              @on-edit="edit" @on-del="del" @on-detail="detail" @on-archiveMonitor="archiveMonitor">
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
          keyword: ''
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
          telnet: '/broker',
          startInfo: '/monitor/start',
          batchBrokerGroupSearch: '/brokerGroup/mvBatchBrokerGroup'
        }
      }
    },
    btns: {
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
          },
          {
            txt: '详情',
            method: 'on-detail'
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
            title: 'IP',
            key: 'ip',
            width: '15%'
          },
          {
            title: '端口',
            key: 'port',
            width: '10%'
          },
          {
            title: '启动时间',
            key: 'startupTime',
            width: '15%'
          },
          {
            title: '重试方式',
            key: 'retryType',
            width: '10%'
          },
          {
            title: '权限',
            key: 'permission',
            width: '10%'
          }
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
        btns: this.btns
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
          this.getBrokerStatus(this.tableData.rowData, i)
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
    detail (item) {
      this.brokerId = item.id
      this.$router.push({
        path: '/' + this.$i18n.locale + '/setting/brokerMonitor',
        query: {
          brokerId: item.id,
          brokerIp: item.ip,
          brokerPort: item.port
        }
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
