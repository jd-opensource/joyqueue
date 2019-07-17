<template>
  <div>
    <div class="ml20 mt30">
      <d-input v-model="searchData.keyword" placeholder="请输入ID/Broker分组编码/IP" class="left mr10"
               style="width:300px" @on-enter="getList">
        <span slot="prepend">关键词</span>
        <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>
      </d-input>
      <slot name="extendBtn"></slot>
    </div>
    <my-table :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange" @on-current-change="handleCurrentChange" @on-selection-change="handleSelectionChange"
              @on-edit="edit" @on-del="del" @on-detail="detail" @on-archiveMonitor="archiveMonitor">
    </my-table>

    <!--编辑Broker-->
    <my-dialog :dialog="editDialog" @on-dialog-confirm="editConfirm()" @on-dialog-cancel="editCancel()">
      <grid-row class="mb10">
        <grid-col :span="8" class="label">IP:</grid-col>
        <grid-col :span="16" class="val">
          <d-input v-model="editData.ip"></d-input>
        </grid-col>
      </grid-row>
      <grid-row class="mb10">
        <grid-col :span="8" class="label">端口:</grid-col>
        <grid-col :span="16" class="val">
          <d-input v-model="editData.port"></d-input>
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
          startInfo: '/monitor/start'
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
            key: 'id'
          },
          {
            title: 'Broker分组编码',
            key: 'group.code'
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
            title: '启动时间',
            key: 'startupTime'
          },
          {
            title: '重试方式',
            key: 'retryType'
          },
          {
            title: '权限',
            key: 'permission'
          }
        ]
      }
    }
  },
  data () {
    return {
      tableData: {
        rowData: [],
        colData: this.colData,
        btns: this.btns
      },
      brokerId: '',
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
      this.monitorDetailDialog.visible = true
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
