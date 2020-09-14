<template>
  <div>
    <grid-row>
      <grid-col span="3">消息格式: </grid-col>
      <grid-col span="21">
        <d-radio v-for="(supportedMessageType, index) in messageTypes" :key="index" :value="messageType" name="messageTypeRadio" :label="supportedMessageType"  @on-change="$emit('update:messageType', $event)">{{supportedMessageType}}</d-radio>
      </grid-col>
    </grid-row>
    <my-table :data="tableData" :showPin="showTablePin" style="height: 400px;overflow-y:auto" :showPagination=false
              :page="page" @on-size-change="handleSizeChange"  @on-current-change="handleCurrentChange"/>
    <label >共 {{page.total}} 条记录</label>
  </div>
</template>

<script>
import MyTable from '../../components/common/myTable'
import apiRequest from '../../utils/apiRequest.js'
import crud from '../../mixins/crud.js'

import {timeStampToString} from '../../utils/dateTimeUtils'

export default {
  name: 'msg-preview',
  components: {MyTable},
  mixins: [crud],
  props: {
    // doSearch: {
    //   type: Boolean,
    //   default: false
    // },
    messageTypes: {
      type: Array,
      default: [
        'UTF8 TEXT'
      ]
    }, // 支持的消息格式
    messageType: String,
    app: {
      id: 0,
      code: ''
    },
    subscribeGroup: '',
    topic: {
      id: '',
      code: ''
    },
    namespace: {
      id: '',
      code: ''
    },
    type: {
      type: Number,
      default: 0
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
            title: '发送时间',
            key: 'sendTime',
            formatter (item) {
              return timeStampToString(item.sendTime)
            }
          },

          {
            title: '服务端收到时间',
            key: 'storeTime',
            formatter (item) {
              return timeStampToString(item.sendTime + item.storeTime)
            }
          },
          {
            title: '已消费',
            key: 'flag'
          },
          {
            title: 'BusinessID',
            key: 'businessId'
          },
          {
            title: '内容',
            key: 'body',
            render: (h, params) => {
              return h('d-input', {
                props: {
                  disabled: true,
                  type: 'textarea',
                  value: params.item.body
                }
              })
            }
          }
        ]
      }
    }
  },
  data () {
    return {
      urls: {
        previewMessage: '/monitor/preview/message',
      },
      tableData: {
        rowData: [],
        colData: this.colData
      },
      page: {
        total: 0
      }
    }
  },
  watch: {
    messageType: function () {
      this.getList()
    }
  },
  methods: {
    getList () {
      this.showTablePin = true
      let data = {
        topic: {
          id: this.topic.id,
          code: this.topic.code
        },
        namespace: {
          id: this.namespace.id,
          code: this.namespace.code
        },
        app: {
          id: this.app.id,
          code: this.app.code
        },
        subscribeGroup: this.subscribeGroup || '',
        type: this.type
      }
      let params = '?messageDecodeType=' + this.messageType
      apiRequest.postBase(this.urls.previewMessage+params, {}, data, false).then((data) => {
        data.data = data.data || []
        this.tableData.rowData = data.data
        this.page.total = this.tableData.rowData.length
        this.showTablePin = false
      })
    }
  },
  mounted () {
    // this.getList()
  }

}
</script>

<style scoped>

</style>
