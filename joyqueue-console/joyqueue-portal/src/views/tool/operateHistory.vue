<template>
  <div>
    <div class="ml20 mt30">
      <d-date-picker v-model="times" type="daterange" range-separator="至" start-placeholder="开始日期" class="left mr5"
                     end-placeholder="结束日期" value-format="timestamp" :default-time="['00:00:00', '23:59:59']"
                     style="width: 370px" @on-change="getListWithDate">
        <span slot="prepend">日期范围</span>
      </d-date-picker>
      <d-select v-model="searchData.type" class="left mr5"  style="width:213px">
        <span slot="prepend">操作类型</span>
        <d-option v-for="item in typeList" :value="item.key" :key="item.key">{{ item.value }}</d-option>
      </d-select>
      <d-input v-model="searchData.identity" class="left mr5" placeholder="关联Id" style="width:213px">
        <span slot="prepend">关联Id</span>
      </d-input>
      <d-input v-model="searchData.erp" placeholder="用户名" class="left mr5" style="width:213px">
        <span slot="prepend">用户名</span>
      </d-input>
      <d-button type="primary" color="success" @click="getListWithDate">查询</d-button>
    </div>
    <my-table :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange"
              @on-current-change="handleCurrentChange" @on-selection-change="handleSelectionChange"
              @on-del="del">
    </my-table>
  </div>
</template>

<script>
import myTable from '../../components/common/myTable.vue'
import crud from '../../mixins/crud.js'
import {timeStampToString} from '../../utils/dateTimeUtils'

export default {
  name: 'operate-history',
  components: {
    myTable
  },
  mixins: [ crud ],
  data () {
    return {
      searchData: {
        type: -1,
        beginTime: '',
        endTime: '',
        erp: ''
      },
      typeList: [
        {key: -1, value: '请选择类型'},
        {key: 1, value: 'topic'},
        {key: 2, value: 'consumer'},
        {key: 3, value: 'producer'},
        {key: 4, value: 'config'},
        {key: 5, value: 'appToken'},
        {key: 6, value: 'group'},
        {key: 7, value: 'broker'},
        {key: 8, value: 'datacenter'},
        {key: 9, value: 'namespace'},
        {key: 10, value: 'partitiongroup'},
        {key: 11, value: 'replica'}
      ],
      searchRules: {
      },
      tableData: {
        rowData: [],
        colData: [
          {
            title: '用户名',
            key: 'createBy.code'
          }, {
            title: '操作时间',
            key: 'createTime',
            formatter (row) {
              return timeStampToString(row.createTime)
            }
          }, {
            title: '类型',
            key: 'type',
            formatter (row) {
              switch (row.type) {
                case 1:
                  return 'topic'
                case 2:
                  return 'consumer'
                case 3:
                  return 'producer'
                case 4:
                  return 'config'
                case 5:
                  return 'appToken'
                case 6:
                  return 'group'
                case 7:
                  return 'broker'
                case 8:
                  return 'datacenter'
                case 9:
                  return 'namespace'
                case 10:
                  return 'partitiongroup'
                case 11:
                  return 'replica'
              }
            }
          }, {
            title: '关联id',
            key: 'identity'
          }, {
            title: '操作描述',
            key: 'target'
            // render(h, params) {
            //   return  h('div',params.item.target)
            // }
          }
        ],
        // 表格操作，如果需要根据特定值隐藏显示， 设置bindKey对应的属性名和bindVal对应的属性值
        btns: [
          // {
          //   txt: '编辑',
          //   method: 'on-edit'
          // },
          // {
          //   txt: '删除',
          //   method: 'on-del'
          // }
        ]
      },
      multipleSelection: [],
      addDialog: {
        visible: false,
        title: '新建任务',
        showFooter: true
      },
      addData: {
        type: '',
        referId: 0,
        priority: 0,
        daemons: 1,
        url: '',
        cron: '',
        dispatchType: 0,
        retry: 1
      },
      editDialog: {
        visible: false,
        title: '编辑任务',
        showFooter: true
      },
      editData: {},
      times: []
    }
  },
  methods: {
    getListWithDate () {
      this.searchData.beginTime = this.times[0]
      this.searchData.endTime = this.times[1]
      this.getList()
    }
  },
  mounted () {
    this.searchData.beginTime = this.times[0]
    this.searchData.endTime = this.times[1]

    this.getList()
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  .label{text-align: right; line-height: 32px;}
  .val{}
</style>
