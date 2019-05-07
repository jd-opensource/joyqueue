<template>
  <div>
    <div class="ml20">
      <d-input v-model="search.topic" placeholder="请输入队列名" class="left mr5 mt10"
               style="width: 213px" @on-enter="getListWithDate">
        <span slot="prepend">队列名</span>
      </d-input>
      <d-input v-model="search.app" placeholder="请输入消费者" class="left mr5 mt10"
               style="width: 213px"  @on-enter="getListWithDate">
        <span slot="prepend">消费者</span>
      </d-input>
      <d-select v-model="search.status" class="left mr5 mt10" style="width: 143px" @on-change="getListWithDate">
        <span slot="prepend">状态</span>
        <d-option v-for="item in statusList" :value="item.key" :key="item.key">{{ item.value }}</d-option>
      </d-select>
      <d-date-picker class="left mr5 mt10"
                     v-model="times"
                     type="daterange"
                     range-separator="至"
                     start-placeholder="开始日期"
                     end-placeholder="结束日期"
                     value-format="timestamp"
                     :default-time="['00:00:00', '23:59:59']"
                     style="width:370px">
        <span slot="prepend">发送时间</span>
      </d-date-picker>
      <d-input v-model="search.businessId" placeholder="请输入业务ID" class="left mr5 mt10"
               style="width: 213px" @on-enter="getListWithDate">
        <span slot="prepend">业务ID</span>
      </d-input>
      <d-button class="left mr5 mt10" type="primary" color="success" @click="getListWithDate">
        查询
        <icon name="search" style="margin-left: 5px;">
        </icon>
      </d-button>
    </div>
    <my-table :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange"
              @on-current-change="handleCurrentChange" @on-selection-change="handleSelectionChange"
              @on-del="del" @on-download="download">
    </my-table>
  </div>
</template>

<script>
import myTable from '../../components/common/myTable.vue'
import crud from '../../mixins/crud.js'
import apiRequest from '../../utils/apiRequest.js'
import {timeStampToString} from '../../utils/dateTimeUtils'

export default {
  name: 'retry',
  components: {
    myTable
  },
  mixins: [ crud ],
  props: {
    search: {
      type: Object,
      default: function () {
        return {
          topic: '',
          app: '',
          status: 1,
          beginTime: '',
          endTime: ''
        }
      }
    }
  },
  data () {
    return {
      urls: {
        search: '/retry/search',
        del: '/retry/delete',
        download: '/retry/download',
        recovery: '/retry/recovery'
      },
      businessId: '',
      statusList: [
        {key: 1, value: '重试中'},
        {key: -2, value: '过期'},
        {key: -1, value: '已删除'},
        {key: 0, value: '成功'}
      ],
      tableData: {
        rowData: [],
        colData: [
          {
            title: '主键',
            key: 'id'
          },
          {
            title: '队列',
            key: 'topic'
          }, {
            title: '应用',
            key: 'app'
          }, {
            title: '业务ID',
            key: 'businessId'
          }, {
            title: '次数',
            key: 'retryCount'
          }, {
            title: '发送时间',
            key: 'sendTime',
            formatter (item) {
              return timeStampToString(item.sendTime)
            }
          }, {
            title: '下次重试时间',
            key: 'retryTime',
            formatter (item) {
              return timeStampToString(item.retryTime)
            }
          }, {
            title: '过期时间',
            key: 'expireTime',
            formatter (item) {
              return timeStampToString(item.expireTime)
            }
          }
        ],
        // 表格操作，如果需要根据特定值隐藏显示， 设置bindKey对应的属性名和bindVal对应的属性值
        btns: [
          // {
          //   txt: '恢复',
          //   method: 'on-recovery'
          // },
          {
            txt: '下载',
            method: 'on-download'
          },
          {
            txt: '删除',
            method: 'on-del'
          }
        ]
      },
      editData: {},
      times: []
    }
  },
  methods: {
    getListWithDate () {
      if (this.times != null && this.times.length === 2) {
        this.search.beginTime = this.times[0]
        this.search.endTime = this.times[1]
      } else {
        this.search.beginTime = ''
        this.search.endTime = ''
      }
      this.getList()
    },
    download (item) {
      apiRequest.get(this.urlOrigin.download + '/' + item.id).then()
    },
    recovery (item) {
      apiRequest.put(this.urlOrigin.recovery + '/' + item.id).then(data => {
        this.$Dialog.success({
          content: '恢复成功'
        })
        this.getListWithDate()
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
