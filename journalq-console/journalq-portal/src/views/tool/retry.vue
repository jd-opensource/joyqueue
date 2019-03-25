<template>
  <div>
    <div class="ml20 mt30">
      <d-input v-model="searchData.topic" placeholder="请输入队列名" class="left mr10" style="width: 15%">
        <span slot="prepend">队列名</span>
      </d-input>
      <d-input v-model="searchData.app" placeholder="请输入消费者" class="left mr10" style="width: 15%">
        <span slot="prepend">消费者</span>
      </d-input>
      <d-select v-model="searchData.status" class="left mr10" style="width:10%">
        <span slot="prepend">状态</span>
        <d-option v-for="item in statusList" :value="item.key" :key="item.key">{{ item.value }}</d-option>
      </d-select>
      <d-date-picker class="left mr10"
                     v-model="times"
                     type="daterange"
                     range-separator="至"
                     start-placeholder="开始日期"
                     end-placeholder="结束日期"
                     value-format="timestamp"
                     :default-time="['00:00:00', '23:59:59']">
        <span slot="prepend">发送时间</span>
      </d-date-picker>
      <d-input v-model="searchData.businessId" placeholder="请输入业务ID" class="left mr10" style="width: 15%">
        <span slot="prepend">业务ID</span>
      </d-input>
      <d-button type="primary" @click="getListWithDate">查询<icon name="search" style="margin-left: 5px;"></icon></d-button>
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
  data () {
    return {
      searchData: {
        topic: '',
        app: '',
        status: 1,
        beginTime: '',
        endTime: ''
      },
      searchRules: {
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
      // if (this.searchData.app == '' || this.searchData.topic == '') {
      //   return ;
      // }
      console.log(this.times)
      if (this.times != null && this.times.length == 2) {
        this.searchData.beginTime = this.times[0]
        this.searchData.endTime = this.times[1]
      } else {
        this.searchData.beginTime='';
        this.searchData.endTime = '';
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
    this.searchData.topic = this.$route.query.topic
    this.searchData.app = this.$route.query.app
    // if (this.searchData.app == '' || this.searchData.topic == '') {
    //   return ;
    // }
    // this.searchData.beginTime=this.times[0];
    // this.searchData.endTime=this.times[1];
    this.getList()
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  .label{text-align: right; line-height: 32px;}
  .val{}
</style>
