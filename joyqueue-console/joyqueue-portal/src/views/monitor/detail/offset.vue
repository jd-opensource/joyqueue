<template>
  <div>
    <grid-row>
      <grid-col span="24">
      <d-button-group class="right" style="padding-right:50px">
        <d-button @click="showResetOffset(undefined)" >按时间重置</d-button>
        <d-button @click="setBound('MIN')" >全量最小</d-button>
        <d-button @click="setBound('MAX')" >全量最大</d-button>
      </d-button-group>
      </grid-col>
    </grid-row>
    <my-table :data="tableData" :showPin="showTablePin" :showPagination=false :page="page"
              @on-size-change="handleSizeChange" @on-current-change="handleCurrentChange" @on-set-offset="showResetOffset"/>
    <label >共：{{page.total}} 条记录</label>

    <my-dialog  :dialog="resetDialog" @on-dialog-confirm="onConfirm"  @on-dialog-cancel="onCancel">
      <d-form ref="partitionOffset" :model="partitionInfo"  label-width="100px" style="height: 350px; overflow-y:auto; width: 100%; padding-right: 20px">
        <d-form-item label="主题:">
          <d-input v-model="partitionInfo.topic" oninput="value = value.trim()" style="width: 60%" disabled ></d-input>
        </d-form-item>
        <d-form-item label="应用:">
          <d-input v-model="partitionInfo.app" oninput="value = value.trim()" style="width: 60%" disabled ></d-input>
        </d-form-item>
        <d-form-item label="重置时间:" v-if="!partitionInfo.isPartition">
          <d-date-picker
                         v-model="partitionInfo.time"
                         align="right"
                         value-format="timestamp"
                         type="datetime"
                         placeholder="选择时间">
          </d-date-picker>
        </d-form-item>
        <d-form-item label="最小值："  v-if="partitionInfo.isPartition">
          <d-input v-model.number="partitionInfo.partition.leftIndex" oninput="value = value.trim()" style="width: 60%" disabled/>
        </d-form-item>
        <d-form-item label="最大值："  v-if="partitionInfo.isPartition">
          <d-input v-model.number="partitionInfo.partition.rightIndex" oninput="value = value.trim()" style="width: 60%" disabled/>
        </d-form-item>
        <d-form-item label="重置位置："  v-if="partitionInfo.isPartition" >
          <d-input v-model.number="partitionInfo.offset" oninput="value = value.trim()" style="width: 60%"/>
        </d-form-item>
      </d-form>
    </my-dialog>
  </div>
</template>

<script>
import MyTable from '../../../components/common/myTable'
import apiRequest from '../../../utils/apiRequest.js'
import crud from '../../../mixins/crud.js'
import myDialog from '../../../components/common/myDialog.vue'
import {getTopicCode, getAppCode, bytesToSize} from '../../../utils/common.js'
import {timeStampToString} from '../../../utils/dateTimeUtils'

export default {
  name: 'offset',
  components: {myDialog, MyTable},
  mixins: [crud],
  props: {
    doSearch: {
      type: Boolean,
      default: false
    },
    colData: {
      type: Array,
      default: function () {
        return [
          {
            title: '分区',
            key: 'partition'
          },
          {
            title: '主',
            key: 'leader'
          },
          {
            title: '最小值',
            key: 'leftIndex'
          },
          {
            title: '最大值',
            key: 'rightIndex'
          },
          {
            title: '最后拉取时间',
            key: 'lastPullTime',
            formatter (item) {
              return timeStampToString(item.lastPullTime)
            }
          },
          {
            title: '最后应答时间',
            key: 'lastAckTime',
            formatter (item) {
              return timeStampToString(item.lastAckTime)
            }
          },
          {
            title: '应答',
            key: 'offset'
          },
          {
            title: 'TPS',
            key: 'tps'
          },
          {
            title: '流量',
            key: 'traffic',
            formatter (item) {
              return bytesToSize(item.traffic)
            }
          }
        ]
      }
    },
    search: {
      type: Object,
      default: function () {
        return {
          topic: {
            id: '',
            code: ''
          },
          namespace: {
            id: '',
            code: ''
          },
          app: {
            id: 0,
            code: ''
          },
          subscribeGroup: '',
          type: 1,
          clientType: -1
        }
      }
    }
  },
  data () {
    return {
      urls: {
        search: '/consumer/offsets',
        boundReset: '/consumer/offset/reset/location/', // MAX,MIN
        offsetResetPartition: '/consumer/offset/reset/partition/', // partition/:partition/offset/:offset
        offsetResetByTime: '/consumer/offset/reset/timestamp/'
      },
      tableData: {
        rowData: [],
        colData: this.colData,
        btns: [
          {
            txt: '重置消费位置',
            method: 'on-set-offset'
          }
        ]
      },
      resetDialog: {
        visible: false,
        title: '重置消费位置',
        width: '500',
        showFooter: true,
        scrollable: true,
        doSearch: true
      },
      partitionInfo: {
        partition: {},
        subscribe: {},
        time: '',
        offset: undefined,
        isPartition: true
      },
      page: {
        total: 0
      }
    }
  },
  methods: {
    openDialog (dialog) {
      this[dialog].doSearch = true
      this[dialog].visible = true
    },
    closeDialog (dialog) {
      this[dialog].doSearch = false
      this[dialog].visible = false
    },
    setBound (location) {
      let _this = this
      this.$Dialog.confirm({
        title: '提示',
        content: '确定将所有分区消费位置设成最' + (location === 'MIN' ? '小' : '大') + '值吗？'
      }).then(() => {
        apiRequest.postBase(_this.urls.boundReset + location, {}, _this.search, false).then((data) => {
          data.data = data.data
          if (data.data === 'success') {
            _this.$Message.success('重置成功')
            _this.getList()
          } else {
            _this.$Message.error('重置失败')
          }
        })
      })
    },
    showResetOffset (item) {
      this.partitionInfo.subscribe = this.search
      this.partitionInfo.topic = getTopicCode(this.partitionInfo.subscribe.topic, this.partitionInfo.subscribe.namespace)
      this.partitionInfo.app = getAppCode(this.partitionInfo.subscribe.app, this.partitionInfo.subscribe.subscribeGroup)
      if (!item) {
        this.partitionInfo.isPartition = false
      } else {
        this.partitionInfo.partition = item
        this.partitionInfo.isPartition = true
      }
      this.openDialog('resetDialog')
    },
    resetPartitionOffset () {
      if (this.partitionInfo.offset > this.partitionInfo.partition.rightIndex ||
          this.partitionInfo.offset < this.partitionInfo.partition.leftIndex) {
        this.$Message.error('重置位置必须在最大最小值之间')
        return
      }
      let path = this.partitionInfo.partition.partition + '/offset/' + this.partitionInfo.offset
      apiRequest.postBase(this.urls.offsetResetPartition + path, {}, this.search, false).then((data) => {
        data.data = data.data
        if (data.data === 'success') {
          this.$Message.success('重置成功')
          this.closeDialog('resetDialog')
          this.getList()
        } else {
          this.$Message.error('重置失败')
        }
      })
    },
    onConfirm () {
      if (this.partitionInfo.isPartition) {
        this.resetPartitionOffset()
      } else {
        this.resetAppOffset()
      }
    },
    onCancel () {

    },
    resetAppOffset () {
      apiRequest.postBase(this.urls.offsetResetByTime + this.partitionInfo.time, {}, this.search, false).then((data) => {
        data.data = data.data
        if (data.data === 'success') {
          this.$Message.success('重置成功')
          this.closeDialog('resetDialog')
          this.getList()
        } else {
          this.$Message.error('重置失败')
        }
      })
    },
    getList () {
      this.showTablePin = true
      apiRequest.postBase(this.urls.search, {}, this.search, false).then((data) => {
        data.data = data.data || []
        for (let i = 0; i < data.data.length; i++) {
          data.data[i].offset = data.data[i].index
        }
        this.tableData.rowData = data.data
        // this.onListResult(data);
        this.page.total = data.data.length
        this.showTablePin = false
      })
    }
  }

}
</script>

<style scoped>

</style>
